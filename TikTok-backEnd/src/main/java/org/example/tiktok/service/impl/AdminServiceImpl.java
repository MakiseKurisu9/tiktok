package org.example.tiktok.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.example.tiktok.entity.Result;
import org.example.tiktok.entity.User.User;
import org.example.tiktok.entity.Video.Video;
import org.example.tiktok.mapper.AdminMapper;
import org.example.tiktok.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    AdminMapper adminMapper;


    @Override
    public Result getAllUser(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> users = adminMapper.getAllUser();
        PageInfo<User> pageInfo = new PageInfo<>(users);

        // Hide passwords before returning
        pageInfo.getList().forEach(u -> u.setPassword(null));
        Map<String, Object> data = new HashMap<>();
        data.put("items", pageInfo.getList());
        data.put("total", pageInfo.getTotal());
        data.put("pages", pageInfo.getPages());
        data.put("pageNum", pageInfo.getPageNum());
        data.put("pageSize", pageInfo.getPageSize());

        return Result.ok("success get all users",data);
    }

    @Override
    public Result deleteUser(Long userId) {
        adminMapper.deleteUser(userId);

        return Result.ok("success delete user");
    }

    @Override
    public Result getAllVideos(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Video> videos = adminMapper.getAllVideos();
        PageInfo<Video> pageInfo = new PageInfo<>(videos);

        Map<String, Object> data = new HashMap<>();
        data.put("items", pageInfo.getList());
        data.put("total", pageInfo.getTotal());
        data.put("pages", pageInfo.getPages());
        data.put("pageNum", pageInfo.getPageNum());
        data.put("pageSize", pageInfo.getPageSize());


        return Result.ok("success get all videos",data);
    }

    @Override
    @Transactional
    public Result deleteVideo(Long videoId) {
        adminMapper.deleteVideoLikes(videoId);
        adminMapper.deleteVideoShares(videoId);
        adminMapper.deleteVideoTypeRelations(videoId);
        adminMapper.deleteVideoFavouriteRelations(videoId);
        adminMapper.deleteVideo(videoId);
        return Result.ok("successfully deleted video");
    }
}
