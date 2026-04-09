package org.example.tiktok.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.tiktok.dto.PageBean;
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
import java.util.*;
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

    @PostConstruct
    // 应用启动时计算一次热点排行榜，确保有数据可用
    public void init() {
        calculateDailyHotRank();
        log.info("hot rank initialized on startup");
    }

    @Scheduled(cron = "0 0 0 * * ?") // 每天 0 点执行
    public void calculateDailyHotRank() {
        List<VideoHot> scored = calculateScore();
        //if scored is empty, expire will throw error, use tempKey to avoid it
        String tempKey = REDIS_HOT_RANK_KEY + ":temp";
        // 写入Redis
        stringRedisTemplate.delete(tempKey);

        for (VideoHot v : scored) {
            try {
                String json = objectMapper.writeValueAsString(v);
                stringRedisTemplate.opsForZSet().add(tempKey, json, v.getHotScore());
            } catch (JsonProcessingException e) {
                log.error("序列化Video对象失败: {}", v.getVideoId(), e);
            }
        }

        Boolean tempExists = stringRedisTemplate.hasKey(tempKey);
        if(Boolean.TRUE.equals(tempExists)) {
            stringRedisTemplate.rename(tempKey,REDIS_HOT_RANK_KEY);
            stringRedisTemplate.expire(REDIS_HOT_RANK_KEY, Duration.ofDays(1));
        } else {
            stringRedisTemplate.delete(REDIS_HOT_RANK_KEY);
        }


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
        //如果投入实际使用 开发环境下没有持续上传的视频，三日热点可能没有数据，暂时注释掉这个限制
        //if (LocalDate.now().getDayOfYear() % 3 != 0) return;
        PageBean<Video> recentHot = getRecentHotVideo(1,10);
        log.info("三日热点视频更新成功，共 {} 条", recentHot.getTotal());
    }

    //热门视频
    public PageBean<Video> getRecentHotVideo(int pageNum,int pageSize) throws JsonProcessingException {
        List<Video> allVideos = videoMapper.getAllVideos();
        //for test
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(0);

        List<Video> recentVideos = allVideos.stream()
                .filter(v -> v.getCreateTime().isAfter(threeDaysAgo))
                .toList();

        List<Video> result;
        //control return random videos but someway by weight
        Random random = new Random();

        if (!recentVideos.isEmpty()) {
            // Compute avg and noise ONCE before sorting
            double avg = recentVideos.stream()
                    .mapToDouble(this::computeHotScore)
                    .average().orElse(1);
            double noise = avg * 0.2;

            // Pre-compute each video's final score with random noise ONCE
            Map<Long, Double> scoreMap = recentVideos.stream()
                    .collect(Collectors.toMap(
                            Video::getId,
                            v -> computeHotScore(v) + random.nextDouble() * noise
                    ));

            result = recentVideos.stream()
                    .filter(v -> computeHotScore(v) > HOT_LIMIT)
                    .sorted((v1, v2) -> Double.compare(scoreMap.get(v2.getId()), scoreMap.get(v1.getId())))
                    .collect(Collectors.toList());

        } else {
            // Pre-compute scores for fallback too
            Map<Long, Double> scoreMap = allVideos.stream()
                    .collect(Collectors.toMap(
                            Video::getId,
                            v -> computeHotScore(v) + random.nextDouble() * 50
                    ));

            result = allVideos.stream()
                    .sorted((v1, v2) -> Double.compare(scoreMap.get(v2.getId()), scoreMap.get(v1.getId())))
                    .collect(Collectors.toList());
        }


        int fromIndex = Math.min((pageNum - 1) * pageSize, result.size());
        int toIndex = Math.min(fromIndex + pageSize, result.size());
        List<Video> pagedVideos = result.subList(fromIndex, toIndex);

        PageBean<Video> pageBean = new PageBean<>((long) result.size(),pagedVideos);

        for(Video v : pagedVideos) {
            cacheClient.set("index:video:"+v.getId(),v,3L, TimeUnit.DAYS);
        }
        return pageBean;
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
                .map( v-> {
                    double rounded = Math.round(v.getHotScore() * 100 * 10) / 10.0;
                    v.setHotScore(rounded);
                    return v;
                        })
                .collect(Collectors.toList());
    }
}

