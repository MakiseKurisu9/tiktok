package org.example.tiktok.controller;

import jakarta.annotation.Resource;
import org.example.tiktok.entity.Result;
import org.example.tiktok.entity.Video.Video;
import org.example.tiktok.service.VideoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/video")
public class VideoController {

    @Resource
    VideoService videoService;

    //favourite table
    //get all videos in a favourite table
    @GetMapping("/favourite/{favouriteTableId}")
    public Result getVideoInFavouriteTable(@PathVariable Long favouriteTableId) {
        return videoService.getVideoInFavouriteTable(favouriteTableId);
    }

    @PostMapping("/favourite/{favouriteTableId}/{videoId}")
    public Result addVideoIntoFavouriteTable(@PathVariable Long favouriteTableId,@PathVariable Long videoId) {
        return videoService.addVideoIntoFavouriteTable(favouriteTableId,videoId);
    }
    //在用户刷视频的时候调用，在前端实现，视频播放几秒 后调用
    @PostMapping("/history/{videoId}")
    public Result addVideoIntoHistory(@PathVariable Long videoId) {
        return videoService.addVideoIntoHistory(videoId);
    }

    @GetMapping("/history")
    public Result getVideoHistory() {
        return videoService.getVideoHistory();
    }

    @PostMapping("/star/{videoId}")
    public Result starVideo(@PathVariable Long videoId) {
        return videoService.starVideo(videoId);
    }






}
