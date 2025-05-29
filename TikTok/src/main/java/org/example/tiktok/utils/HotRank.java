package org.example.tiktok.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.tiktok.dto.VideoHot;
import org.example.tiktok.entity.Video.Video;
import org.example.tiktok.mapper.VideoMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class HotRank {
    private static final double HALF_LIFE_SECONDS = 12 * 3600;
    private static final String REDIS_HOT_RANK_KEY = "video:hot:top10";

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private CacheClient cacheClient;

    private static final int HOT_LIMIT = 400;

    @Scheduled(cron = "0 0 0 * * ?") // 每天 0 点执行
    public void calculateDailyHotRank() {
        List<VideoHot> scored = calculateScore();
        // 写入Redis
        stringRedisTemplate.delete(REDIS_HOT_RANK_KEY);

        for (VideoHot v : scored) {
            try {
                String json = objectMapper.writeValueAsString(v);
                stringRedisTemplate.opsForZSet().add(REDIS_HOT_RANK_KEY, json, v.getHotScore());
            } catch (JsonProcessingException e) {
                log.error("序列化Video对象失败: {}", v.getVideoId(), e);
            }
        }
        stringRedisTemplate.expire(REDIS_HOT_RANK_KEY, Duration.ofDays(1));
    }

    private List<VideoHot> calculateScore(){
        List<Video> allVideos = videoMapper.getAllVideos();

        List<VideoHot> scored = allVideos.stream()
                .map(v -> {
                    double hotScore = computeHotScore(v);
                    return new VideoHot(v.getId(),v.getTitle(),hotScore);
                })
                .sorted((a, b) -> Double.compare(b.getHotScore(), a.getHotScore()))
                .limit(10)
                .toList();
        return scored;
    }

    @Scheduled(cron = "0 0 0 * * ?") // 每天 0 点执行一次
    public void calculateEvery3Days() throws JsonProcessingException {
        //每三天更新一次
        if (LocalDate.now().getDayOfYear() % 3 != 0) return;
        List<Video> recentHot = getRecentHotVideo();
        log.info("三日热点视频更新成功，共 {} 条", recentHot.size());
    }

    //热门视频 和排行榜无关 但是放这了
    public List<Video> getRecentHotVideo() throws JsonProcessingException {
        List<Video> allVideos = videoMapper.getAllVideos();
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

         List<Video> result =
                 allVideos.stream()
                .filter(video -> video.getCreateTime().isAfter(threeDaysAgo))
                .filter(v -> {
                    double score = computeHotScore(v);
                    return score > HOT_LIMIT;
                })
                .sorted((v1,v2) -> {
                    double score1 = computeHotScore(v1);
                    double score2 = computeHotScore(v2);
                    return Double.compare(score2,score1);
                })
                .collect(Collectors.toList());
        for(Video v : result) {
            cacheClient.set("index:video:"+v.getId(),v,3L, TimeUnit.DAYS);
        }
        return result;
    }

    private double computeHotScore(Video v) {
        long ageSeconds = ChronoUnit.SECONDS.between(v.getCreateTime(), LocalDateTime.now());
        double weight = v.getLikes() * 1.0
                + v.getShares() * 1.5
                + v.getViews() * 0.1
                + v.getFavourites() * 2.0;
        double decayFactor = Math.pow(0.5, ageSeconds / HALF_LIFE_SECONDS);
        return weight * decayFactor;
    }


    public List<VideoHot> getHotRankInfo() {
        Set<String> jsonSet = stringRedisTemplate.opsForZSet().reverseRange(REDIS_HOT_RANK_KEY, 0, 9);

        if (jsonSet == null || jsonSet.isEmpty()) {
            return Collections.emptyList();
        }

        return jsonSet.stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, VideoHot.class);
                    } catch (JsonProcessingException e) {
                        log.warn("反序列化失败: {}", json, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}

