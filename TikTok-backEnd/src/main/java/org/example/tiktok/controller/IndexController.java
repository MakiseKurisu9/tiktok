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

    /** 根据分类 ID 获取该分类下的视频列表（分页） */
    @GetMapping("/video/type/{typeId}")
    public Result getVideosByTypeId(
            @PathVariable Long typeId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        return indexService.getVideosByTypeId(typeId,page,limit);
    }
    /** 获取所有视频分类列表 */
    @GetMapping("/types")
    public Result getAllTypes() {
        return indexService.getAllVideoTypes();
    }

    /** 根据视频 ID 获取视频详情信息 */
    @GetMapping("/video/{videoId}")
    public Result getVideo(@PathVariable Long videoId) throws JsonProcessingException {
        return indexService.getVideoById(videoId);
    }
    /** 搜索视频（分页） */
    @GetMapping("/search")
    public Result searchVideo(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            String searchName
    ){
        return indexService.searchVideo(searchName,page,limit);
    }
    /** 获取当前用户的搜索历史记录 */
    @GetMapping("/search/history")
    public Result searchVideoHistory() {
        return indexService.searchVideoHistory();
    }
    /** 删除指定关键词的搜索历史记录 */
    @PostMapping("/search/history/delete")
    public Result deleteSearchHistory(@RequestParam String searchName) {
        return indexService.deleteSearchHistory(searchName);
    }

    /**
     * 分享视频（参考 B 站实现：复制链接 + 分享数 + 热度排序使用）
     * 分享时会增加分享次数，用于热榜计算
     */
    @PostMapping("/share/{videoId}")
    public Result shareVideo(@PathVariable Long videoId, HttpServletRequest request) {
        /*todo*/
        return indexService.shareVideo(videoId,request);
    }

    /** 获取指定用户发布的视频列表（分页） */
    @GetMapping("/video/user")
    public Result getUserPublishVideos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam Long userId
    ) throws JsonProcessingException {
        return indexService.getUserPublishVideos(page,limit,userId);
    }
    /** 获取热度排行视频（综合热度） */
    @GetMapping("/video/hot/rank")
    public Result getHotRank() {
        return indexService.getHotRank();
    }
    /** 获取热门视频列表（分页） */
    @GetMapping("/video/hot")
    public Result getHotVideo(@RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "5") Integer limit) throws JsonProcessingException {
        return indexService.getHotVideo(page,limit);
    }
    /** 根据视频类型推荐相似视频 */
    @GetMapping("/video/similar")
    public Result getSimilarVideoByType(Long videoId,
                                        @RequestParam(required = false) String typeNames) {
        return indexService.getSimilarVideoByType(videoId,typeNames);
    }

    /** 获取系统为当前用户推送的个性化推荐视频（兴趣推荐） */
    @GetMapping("/pushVideos")
    public Result getPushVideos() throws JsonProcessingException {
        return indexService.getPushedVideos();
    }



}
