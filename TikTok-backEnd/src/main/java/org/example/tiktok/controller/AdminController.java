package org.example.tiktok.controller;

import jakarta.annotation.Resource;
import org.example.tiktok.entity.Result;
import org.example.tiktok.service.AdminService;
import org.springframework.web.bind.annotation.*;

// just for graduate project X(
@RestController
@RequestMapping("/admin")
public class AdminController {
    @Resource
    AdminService adminService;

    @GetMapping("/users")
    //list all users
    public Result getAllUsers(@RequestParam(defaultValue = "1") int pageNum,
                              @RequestParam(defaultValue = "10") int pageSize) {

        return adminService.getAllUser(pageNum,pageSize);
    }

    @DeleteMapping("/users/{userId}")
    //delete any user
    public Result deleteUser(@PathVariable Long userId) {
        return adminService.deleteUser(userId);
    }

    //list all videos
    @GetMapping("/videos")
    public Result getAllVideos(@RequestParam(defaultValue = "1") int pageNum,
                               @RequestParam(defaultValue = "10") int pageSize) {
        return adminService.getAllVideos(pageNum,pageSize);
    }

    //delete any video
    @DeleteMapping("/videos/{videoId}")
    public Result deleteVideo(@PathVariable Long videoId) {
        return adminService.deleteVideo(videoId);
    }

}
