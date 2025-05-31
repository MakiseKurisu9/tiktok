package org.example.tiktok.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import org.example.tiktok.entity.Result;

public interface IndexService {
    Result getVideosByTypeId(Long typeId);

    Result getAllVideoTypes();

    Result getPushedVideos() throws JsonProcessingException;

    Result getVideoById(Long videoId) throws JsonProcessingException;

    Result searchVideo(String searchName, Integer page, Integer limit);

    Result shareVideo(Long videoId, HttpServletRequest request);

    Result searchVideoHistory();

    Result deleteSearchHistory(String searchName);

    Result getUserPublishVideos(int page, int limit, Long userId) throws JsonProcessingException;

    Result getHotVideo() throws JsonProcessingException;

    Result getHotRank();

    Result getSimilarVideoByType(Long videoId, String typeNames);
}
