package org.example.tiktok.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.tiktok.entity.Result;

public interface IndexService {
    Result getVideosByTypeId(Long typeId);

    Result getAllVideoTypes();

    Result getPushedVideos();

    Result getVideoById(Long videoId) throws JsonProcessingException;

    Result searchVideo(String searchName, Integer page, Integer limit);

    Result shareVideo(Long videoId, HttpServletRequest request);
}
