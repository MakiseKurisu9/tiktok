package org.example.tiktok.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import org.example.tiktok.entity.Result;
import org.example.tiktok.entity.Video.Video;
import org.example.tiktok.service.VideoService;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/delete/{videoId}")
    public Result deleteVideo(@PathVariable Long videoId) { return videoService.deleteVideo(videoId);}

    @PostMapping
    public Result addOrUpdateVideo(@RequestBody Video video) throws JsonProcessingException {
        return videoService.addOrUpdateVideo(video);
    }

    //查询用户所管理的视频
    @GetMapping
    public Result listVideos(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        return videoService.listVideos(page,limit);
    }

    //max第一次前端给 把当前时间转为雪花id
    //max 当前时间戳 || 上一次查询的最小时间戳 视频排列顺序 从大到小排
    //数据动态变化，记录上一次看的id来进行分页
    @GetMapping("/follow/feed")
    public Result feedPush(
            @RequestParam("lastId") Long max
    ) {
        return videoService.pushFeedVideos(max);
    }

    @PostMapping("/comment")
    public Result commentOrAnswerComment(
            @RequestParam Long videoId,
            @RequestParam(required = false) Long parentId,
            @RequestParam String content
    ) {
        return videoService.commentOrAnswerComment(videoId,parentId, content);
    }

    @GetMapping("/index/comment/by/video")
    public Result getCommentByVideoId(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam Long videoId
    ) {
        return videoService.getCommentByVideoId(page,limit,videoId);
    }

    @PostMapping("/comment/like/{commentId}")
    public Result likeComment(
            @PathVariable Long commentId
    ) {
        return videoService.likeComment(commentId);
    }

    @DeleteMapping("/comment/{commentId}")
    public Result deleteComment(
            @PathVariable Long commentId
    ) {
        return videoService.deleteComment(commentId);
    }

    @GetMapping("/index/comment/by/rootComment")
    public Result getSubCommentsByRootId(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) Long rootId
    ) {
        return videoService.getSubCommentsByRootId(page,limit,rootId);
    }




}
