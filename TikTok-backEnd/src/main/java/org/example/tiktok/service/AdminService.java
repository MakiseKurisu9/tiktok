package org.example.tiktok.service;

import org.example.tiktok.entity.Result;

public interface AdminService {
    Result getAllUser(int pageNum, int pageSize);

    Result deleteUser(Long userId);

    Result getAllVideos(int pageNum, int pageSize);

    Result deleteVideo(Long videoId);
}
