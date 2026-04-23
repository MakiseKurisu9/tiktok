package org.example.tiktok.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.tiktok.entity.User.User;
import org.example.tiktok.entity.Video.Video;

import java.util.List;

@Mapper
public interface AdminMapper {

    @Select("select * from user")
    List<User> getAllUser();

    @Delete("delete from user where id = #{userId}")
    void deleteUser(Long userId);

    @Select("select * from video")
    List<Video> getAllVideos();

    @Delete("delete from video where id = #{videoId}")
    void deleteVideo(Long videoId);

    @Delete("DELETE FROM video_like WHERE video_id = #{videoId}")
    void deleteVideoLikes(Long videoId);

    @Delete("DELETE FROM video_share WHERE video_id = #{videoId}")
    void deleteVideoShares(Long videoId);

    @Delete("DELETE FROM favourite_video_relation WHERE vid = #{videoId}")
    void deleteVideoFavouriteRelations(Long videoId);

    @Delete("DELETE FROM video_type_relation WHERE video_id = #{videoId}")
    void deleteVideoTypeRelations(Long videoId);
}
