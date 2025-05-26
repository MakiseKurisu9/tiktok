package org.example.tiktok.service;


import org.example.tiktok.entity.Result;


public interface VideoService {
    Result getVideoInFavouriteTable(Long favouriteTableId);

    Result addVideoIntoFavouriteTable(Long favouriteTableId, Long videoId);

    Result addVideoIntoHistory(Long videoId);

    Result getVideoHistory();

    Result starVideo(Long videoId);

    Result deleteVideo(Long videoId);

    Result listVideos(Integer page,Integer limit);
}
