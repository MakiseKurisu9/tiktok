package org.example.tiktok.service.impl;

import jakarta.annotation.Resource;
import org.example.tiktok.entity.Result;
import org.example.tiktok.entity.Video.Video;
import org.example.tiktok.mapper.VideoMapper;
import org.example.tiktok.service.VideoService;
import org.example.tiktok.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.example.tiktok.utils.SystemConstant.History_PREFIX;

@Service
public class VideoServiceImpl implements VideoService {
    @Resource
    VideoMapper videoMapper;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public Result getVideoInFavouriteTable(String favouriteTableId) {
        List<Video> videoList = videoMapper.getVideoInFavouriteTable(favouriteTableId);
        if(videoList.isEmpty()) {
            return Result.ok("no data in favourite", Collections.emptyList());
        }
        return Result.ok("success get videoList in favourite table",videoList);
    }

    @Override
    public Result addVideoIntoFavouriteTable(String favouriteTableId, String videoId) {
        Integer isVideoInFavourite = videoMapper.isVideoInFavouriteTable(favouriteTableId, videoId);
        if(isVideoInFavourite > 0) {
            return Result.fail("this video already in this favourite");
        }
        videoMapper.addVideoIntoFavouriteTable(favouriteTableId, videoId);
        return Result.ok("add a video into favourite table success");
    }

    @Override
    public Result addVideoIntoHistory(String videoId) {
        String key = History_PREFIX+ UserHolder.getUser().getId();

        //if video exist, first remove then add
        stringRedisTemplate.opsForList().remove(key,0,videoId);
        //make sure video in first place
        stringRedisTemplate.opsForList().leftPush(key,videoId);

        //keep a user can only save 100 data
        stringRedisTemplate.opsForList().trim(key,0,99);
        //save 7 days
        stringRedisTemplate.expire(key,7, TimeUnit.DAYS);
        return Result.ok("add a video into history list");
    }

    @Override
    public Result getVideoHistory() {
        String key = History_PREFIX+ UserHolder.getUser().getId();
        //get all videoId in list, like videoId:1 videoId:2
        List<String> list = stringRedisTemplate.opsForList().range(key, 0, -1);
        if(list==null || list.isEmpty()) {
            return Result.fail("no video history");
        }
        //解析id
        List<Long> ids = list.stream()
                .map(Long::valueOf)
                .toList();
        //get data from db equal SELECT * FROM video WHERE id IN (1, 2, 3, 4);
        List<Video> videos = videoMapper.getVideoByVideoId(ids);
        //先将数据存入map中，用于保存历史记录
        Map<Long,Video> videoMap = videos.stream()
                .collect(Collectors.toMap(Video::getId,video -> video));
        //再通过顺序遍历ids，通过map获取当前id对应的video，并过滤null数据，最后转为list并返回
        //学名顺序还原order preserving
        List<Video> orderedVideos = ids.stream()
                .map(videoMap::get)
                .filter(Objects::isNull)
                .toList();
        return Result.ok("success get history list",orderedVideos);
    }
}
