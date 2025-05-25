package org.example.tiktok.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import net.sf.jsqlparser.statement.create.table.Index;
import org.apache.commons.lang3.StringUtils;
import org.example.tiktok.entity.Result;
import org.example.tiktok.entity.Video.Video;
import org.example.tiktok.entity.Video.VideoType;
import org.example.tiktok.mapper.IndexMapper;
import org.example.tiktok.service.IndexService;
import org.example.tiktok.utils.PublicVideoServiceUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
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
}
