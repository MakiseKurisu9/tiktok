package org.example.tiktok.service;


import org.example.tiktok.entity.Result;
import org.example.tiktok.entity.Video.Video;

import java.util.List;

public interface VideoService {
    Result getVideoInFavouriteTable(String favouriteTableId);

    Result addVideoIntoFavouriteTable(String favouriteTableId, String videoId);

    Result addVideoIntoHistory(String videoId);

    Result getVideoHistory();
}
