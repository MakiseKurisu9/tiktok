package org.example.tiktok.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.tiktok.entity.Result;

public interface IndexService {
    Result getVideosByTypeId(Long typeId);

    Result getAllVideoTypes();

    Result getPushedVideos();

    Result getVideoById(Long videoId) throws JsonProcessingException;
}
