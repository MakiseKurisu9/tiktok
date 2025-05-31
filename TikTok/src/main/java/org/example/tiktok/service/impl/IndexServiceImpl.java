package org.example.tiktok.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.example.tiktok.dto.PageBean;
import org.example.tiktok.dto.VideoHot;
import org.example.tiktok.entity.Result;
import org.example.tiktok.entity.User.UserModel;
import org.example.tiktok.entity.Video.Video;
import org.example.tiktok.entity.Video.VideoType;
import org.example.tiktok.mapper.IndexMapper;
import org.example.tiktok.service.IndexService;
import org.example.tiktok.utils.CacheClient;
import org.example.tiktok.utils.HotRank;
import org.example.tiktok.utils.PublicVideoServiceUtil;
import org.example.tiktok.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class IndexServiceImpl implements IndexService {

    @Resource
    IndexMapper indexMapper;

    @Resource
    CacheClient cacheClient;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    PublicVideoServiceUtil publicVideoServiceUtil;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    HotRank hotRank;

    @Override
    public Result getVideosByTypeId(Long typeId) {
        List<Video> videos = indexMapper.getVideosByTypeId(typeId);
        if(videos.isEmpty()) {
            return Result.ok("this type has no video", Collections.emptyList());
        }
        return Result.ok("success get videosByTypeId",videos);
    }

    @Override
    public Result getAllVideoTypes() {
        //肯定有video 不需要判断非空
        List<VideoType> videoTypes = indexMapper.getVideoTypes();
        return Result.ok("success get videoTypes",videoTypes);
    }



    @Override
    public Result getVideoById(Long videoId) throws JsonProcessingException {
        String keyPrefix = "index:video:";
        Video video = cacheClient.queryWithPassThrough(
                keyPrefix,
                videoId,
                Video.class,
                videoId1 -> indexMapper.getVideoById(videoId1),
                2L,
                TimeUnit.HOURS
        );
        if(video == null) {
            return Result.fail("cannot find this video");
        }
        //增加浏览次数
        stringRedisTemplate.opsForValue().increment("video:views:"+ videoId);
        publicVideoServiceUtil.isStared(video);
        return Result.ok("success get video",video);
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    private void syncViewsToDb() {
        //get all key
        Set<String> keys = stringRedisTemplate.keys("video:views:*");
        if(keys == null || keys.isEmpty()) {
            return ;
        }
        for(String key : keys) {
            //get videoId
            String idString = key.substring("video:views:".length());
            Long videoId = Long.parseLong(idString);

            String count = stringRedisTemplate.opsForValue().get("video:views" + videoId);
            if( count == null ) continue;
            Long views = Long.parseLong(count);

            indexMapper.addViews(videoId,views);

            stringRedisTemplate.delete(key);
        }

    }



    @Override
    public Result searchVideo(String searchName, Integer page, Integer limit) {
        PageBean<Video> pageResult = new PageBean<>();
        //搜索框输入非空判断 前端进行
        Long userId = UserHolder.getUser().getId();
        String key = "search:history:" + userId;
        long now = System.currentTimeMillis();

        stringRedisTemplate.opsForZSet().add(key,searchName,now);
        //过期时间七天
        stringRedisTemplate.expire(key,7,TimeUnit.DAYS);
        //get number of members in zSet
        Long size = stringRedisTemplate.opsForZSet().zCard(key);
        if(size != null && size > 10) {
            //从小到大排，只保留最近的十个
            stringRedisTemplate.opsForZSet().removeRange(key,0,size - 11);
        }

        PageHelper.startPage(page,limit);
        List<Video> videos = indexMapper.searchVideo(searchName);

        PageInfo<Video> pageInfo= new PageInfo<>(videos);

        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setItems(pageInfo.getList());

        if(videos.isEmpty()) {
            return Result.ok("cannot search any information",Collections.emptyList());
        }
        return Result.ok("successfully search data",pageResult);

    }

    @Override
    public Result searchVideoHistory() {
        Long userId = UserHolder.getUser().getId();
        String key = "search:history:" + userId;
        Set<String> result = stringRedisTemplate.opsForZSet().reverseRange(key, 0, 9);
        if( result== null ||result.isEmpty()) {
            return Result.ok("there is no searchName",Collections.emptySet());
        }
        return Result.ok("successfully get history data",result);
    }

    @Override
    public Result deleteSearchHistory(String searchName) {
        Long userId = UserHolder.getUser().getId();
        String key = "search:history:" + userId;
        Long isSuccess = stringRedisTemplate.opsForZSet().remove(key, searchName);
        if(isSuccess <= 0 ) {
            return Result.fail("cannot delete searchName");
        }
        return Result.ok("successfully delete searchName");
    }

    @Override
    public Result getUserPublishVideos(int page, int limit, Long userId) throws JsonProcessingException {
        //保持顺序
        Map<Long,Video> videoMap = new HashMap<>();

        PageHelper.startPage(page,limit);
        //查id而不是 * 性能更好
        List<Long> ids = indexMapper.getUserPublishVideoIds(userId);
        if(ids == null || ids.isEmpty()) {
            return Result.ok("this user do not publish any videos",Collections.emptyList());
        }
        //构造redis key
        List<String> keys = ids.stream()
                .map(id -> "index:video:"+ id)
                .toList();
        //mget   like video1 video2 null video3 "" video4
        List<String> jsonList = stringRedisTemplate.opsForValue().multiGet(keys);

        List<Long> missingIds = new ArrayList<>();

        for (int i = 0; i < jsonList.size(); i++) {
            String json = jsonList.get(i);
            Long id = ids.get(i);
            if(json != null && !json.isBlank()) {
                Video video = objectMapper.readValue(json, Video.class);
                videoMap.put(id,video);
            } else {
                missingIds.add(id);
            }
        }
        //查询数据库
        if(!missingIds.isEmpty()) {
            List<Video> videoDB = indexMapper.getVideosByIds(missingIds);
            for(Video video : videoDB) {
                if (video != null) {
                    videoMap.put(video.getId(),video);
                    cacheClient.set("index:video:" + video.getId(),video,2L,TimeUnit.HOURS);
                }
            }
        }
        //正常情况下 所有id都会在map中
        for(Long id : ids) {
            if(!videoMap.containsKey(id)) {
                cacheClient.set("index:video:"+id,"",2L,TimeUnit.HOURS);
            }
        }
        //保持顺序
        List<Video> videos = ids.stream()
                .map(videoMap :: get)
                .filter(Objects::nonNull)
                .toList();

        PageBean<Video> pageBean = new PageBean<>();
        PageInfo<Long> pageInfo = new PageInfo<>(ids);
        pageBean.setItems(videos);
        pageBean.setTotal(pageInfo.getTotal());
        return Result.ok("successfully get videos by this user",pageBean);
    }

    @Override
    public Result getHotVideo() throws JsonProcessingException {
        List<Video> videos = hotRank.getRecentHotVideo();
        if(videos == null || videos.isEmpty()) {
            return Result.ok("there is no recent hot video",Collections.emptyList());
        }
        return Result.ok("successfully get hot videos",videos);
    }

    @Override
    public Result getHotRank() {
        List<VideoHot> hotVideo = hotRank.getHotRankInfo();
        if(hotVideo == null || hotVideo.isEmpty()) {
            return Result.ok("there is no hot video now",Collections.emptyList());
        }
        return Result.ok("successfully get hot rank",hotVideo);
    }

    //dto太多了 不加了
    @Override
    public Result getSimilarVideoByType(Long videoId, String typeNames) {
        Video currentVideo = indexMapper.getVideoById(videoId);
        if(currentVideo == null ) {
            return Result.fail("video not exist");
        }
        if(typeNames == null || typeNames.isEmpty()) {
            typeNames = currentVideo.getType();
        }

        String[] types = typeNames.split(",");
        List<Video> similarVideos = indexMapper.getSimilarVideos(types,videoId);

        return Result.ok("successful get similar videos",similarVideos);
    }

    @Override
    public Result getPushedVideos() throws JsonProcessingException {
        Long userId = UserHolder.getUser().getId();

        String key = "user:model:" + userId;
        String json = stringRedisTemplate.opsForValue().get(key);

        List<Video> videos = new ArrayList<>();

        if(json == null || json.isEmpty()) {
            videos = indexMapper.getRandomVideos();
        } else {
            UserModel model = objectMapper.readValue(json, UserModel.class);
            Map<Long,Double> interestMap = model.getModel();

            List<Long> tagIds = getRandomTagIds(interestMap,3);

            videos = indexMapper.getVideosByTagIds(tagIds,10);

        }

        return Result.ok("successfully get videos you may like",videos);
    }
//加点随机 带权随机抽样从用户兴趣分布中选出若干标签，用于生成个性化视频推荐结果
    private List<Long> getRandomTagIds(Map<Long, Double> interestMap, int count) {
        List<Long> tagIds = new ArrayList<>();
        List<Map.Entry<Long, Double>> entries = new ArrayList<>(interestMap.entrySet());

        // 归一化
        double total = entries.stream().mapToDouble(Map.Entry::getValue).sum();
        List<Double> cumulative = new ArrayList<>();
        double acc = 0;
        for (Map.Entry<Long, Double> entry : entries) {
            acc += entry.getValue() / total;
            cumulative.add(acc);
        }

        Random random = new Random();
        while (tagIds.size() < count && tagIds.size() < entries.size()) {
            double r = random.nextDouble();
            for (int i = 0; i < cumulative.size(); i++) {
                if (r <= cumulative.get(i)) {
                    Long tagId = entries.get(i).getKey();
                    if (!tagIds.contains(tagId)) {
                        tagIds.add(tagId);
                    }
                    break;
                }
            }
        }
        return tagIds;
    }

    @Override
    @Transactional
    public Result shareVideo(Long videoId, HttpServletRequest request) {
        String ip = getClientRealIp(request);
        Long userId = UserHolder.getUser().getId();
        indexMapper.shareVideo(userId,ip,videoId);
        CompletableFuture.runAsync(() -> {
            indexMapper.updateVideoShare(videoId);
        });
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String shareUrl = "http://" + serverName + ":" +serverPort + "/video/" + videoId;
        return Result.ok("share successfully",shareUrl);
    }



    private String getClientRealIp(HttpServletRequest request) {
        //记录代理ip
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
