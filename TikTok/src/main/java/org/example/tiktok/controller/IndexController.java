package org.example.tiktok.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import org.example.tiktok.entity.Result;
import org.example.tiktok.service.IndexService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/index")
public class IndexController {

    @Resource
    IndexService indexService;

    //获取某个分类下的所有视频
    @GetMapping("/video/type/{typeId}")
    public Result getVideosByTypeId(@PathVariable Long typeId) {
        return indexService.getVideosByTypeId(typeId);
    }
    //获取app中所有的视频分类
    @GetMapping("/types")
    public Result getAllTypes() {
        return indexService.getAllVideoTypes();
    }

    //先返回十个视频，用户刷到第七个的时候再返回十个，前端调
    @GetMapping("/pushVideos")
    public Result getPushedVideos() {
        return indexService.getPushedVideos();
    }

    @GetMapping("/video/{videoId}")
    public Result getVideo(@PathVariable Long videoId) throws JsonProcessingException {
        return indexService.getVideoById(videoId);
    }



}
