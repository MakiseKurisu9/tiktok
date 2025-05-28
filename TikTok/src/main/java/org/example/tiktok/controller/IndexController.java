package org.example.tiktok.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
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

    @GetMapping("/search")
    public Result searchVideo(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            String searchName
    ){
        return indexService.searchVideo(searchName,page,limit);
    }

    @GetMapping("/search/history")
    public Result searchVideoHistory() {
        return indexService.searchVideoHistory();
    }

    @PostMapping("/search/history/delete")
    public Result deleteSearchHistory(@RequestParam String searchName) {
        return indexService.deleteSearchHistory(searchName);
    }

    //实现思路，参考b站
    //，前端弹出toast提供一个剪贴板，提示点击获取连接，同时share+1，方便hotRank进行排序
    @PostMapping("/share/{videoId}")
    public Result shareVideo(@PathVariable Long videoId, HttpServletRequest request) {
        /*todo*/
        return indexService.shareVideo(videoId,request);
    }

    //查看用户 自己或别人发布的视频, 也可以理解为看别人主页
    @GetMapping("/video/user")
    public Result getUserPublishVideos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam Long userId
    ) {
        return indexService.getUserPublishVideos(page,limit,userId);
    }

}
