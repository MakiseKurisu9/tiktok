package org.example.tiktok.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.example.tiktok.dto.PageBean;
import org.example.tiktok.entity.Result;
import org.example.tiktok.entity.Video.Video;
import org.example.tiktok.entity.Video.VideoType;
import org.example.tiktok.mapper.IndexMapper;
import org.example.tiktok.service.IndexService;
import org.example.tiktok.utils.PublicVideoServiceUtil;
import org.example.tiktok.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.example.tiktok.utils.SystemConstant.CACHE_VIDEO_SAVE;
import static org.example.tiktok.utils.SystemConstant.NULL_VALUE_SAVE;

@Service
public class IndexServiceImpl implements IndexService {

    @Resource
    IndexMapper indexMapper;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    PublicVideoServiceUtil publicVideoServiceUtil;

    private static final ObjectMapper objectMapper = new ObjectMapper();

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
    public Result getPushedVideos() {
        /*todo*/
        return null;
    }

    @Override
    public Result getVideoById(Long videoId) throws JsonProcessingException {
        String key = "index:video:" + videoId;
        String videoJson = stringRedisTemplate.opsForValue().get(key);
        //缓存命中不是空值
        if(!StringUtils.isBlank(videoJson)) {
            Video video = objectMapper.readValue(videoJson, Video.class);
            publicVideoServiceUtil.isStared(video);
            return Result.ok("success get video",video);
        }
        //判断空值
        if(videoJson.isEmpty()) {
            return Result.fail("not find video");
        }
        //get data from db
        Video video = indexMapper.getVideoById(videoId);

        if(video != null) {
            String video2Json = objectMapper.writeValueAsString(video);
            stringRedisTemplate.opsForValue().set(key,video2Json,CACHE_VIDEO_SAVE,TimeUnit.HOURS);
        } else {
            stringRedisTemplate.opsForValue().set(key,"",NULL_VALUE_SAVE, TimeUnit.MINUTES);
            return Result.fail("not find video");
        }
        publicVideoServiceUtil.isStared(video);
        return Result.ok("success get video",video);
    }

    @Override
    public Result searchVideo(String searchName, Integer page, Integer limit) {
        PageBean<Video> pageResult = new PageBean<>();

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
