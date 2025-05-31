package org.example.tiktok.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.tiktok.entity.Result;
import org.example.tiktok.entity.Video.Video;


public interface VideoService {
    Result getVideoInFavouriteTable(Long favouriteTableId);

    Result addVideoIntoFavouriteTable(Long favouriteTableId, Long videoId);

    Result addVideoIntoHistory(Long videoId);

    Result getVideoHistory();

    Result starVideo(Long videoId);

    Result deleteVideo(Long videoId);

    Result listVideos(Integer page,Integer limit);

    Result feedPush();

    Result commentOrAnswerComment(Long videoId, Long parentId, String comment);

    Result getCommentByVideoId(int page, int limit, Long videoId);

    Result likeComment(Long commentId);

    Result deleteComment(Long commentId);

    Result getSubCommentsByRootId(int page, int limit, Long rootId);

    Result addOrUpdateVideo(Video video) throws JsonProcessingException;
}
