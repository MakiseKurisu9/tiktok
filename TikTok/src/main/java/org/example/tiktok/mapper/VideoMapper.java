package org.example.tiktok.mapper;

import org.apache.ibatis.annotations.*;
import org.example.tiktok.entity.Video.Video;

import java.util.List;

@Mapper
public interface VideoMapper {

    @Select("select v.* from favourite_video_relation f join video v on f.vid = v.id where f.fid = #{favouriteTableId}")
    List<Video> getVideoInFavouriteTable(Long favouriteTableId);

    @Insert("insert into favourite_video_relation(fid, vid) values (#{favouriteTableId},#{videoId})")
    void addVideoIntoFavouriteTable(@Param("favouriteTableId")Long favouriteTableId,@Param("videoId")Long videoId);

    @Select("select * from favourite_video_relation where vid = #{videoId} and fid = #{favouriteTableId}")
    Integer isVideoInFavouriteTable(@Param("favouriteTableId")Long favouriteTableId,@Param("videoId") Long videoId);
//in xml
    List<Video> getVideosByVideoId(@Param("ids") List<Long> ids);

    @Update("update video set likes = likes + 1 where video.id = #{videoId}")
    Boolean starVideo(Long videoId);

    @Insert("insert into video_like(video_id, like_id) VALUES (#{videoId},#{userId})")
    Boolean videoLike(@Param("videoId")Long videoId,@Param("userId") Long userId);

    @Update("update video set likes = likes - 1 where video.id = #{videoId}")
    Boolean decreaseStarVideo(Long videoId);

    @Delete("delete from video_like where video_id = #{videoId} and like_id = #{userId}")
    Boolean videoNotLike(@Param("videoId")Long videoId,@Param("userId") Long userId);

    @Delete("delete video from video where id = #{videoId}")
    Boolean deleteVideo(Long videoId);

    @Delete("delete from video_like where video_id = #{videoId}")
    void deleteVideoLikes(Long videoId);

    @Delete("delete from video_share where video_id = #{videoId}")
    void deleteVideoShares(Long videoId);

    @Delete("delete from video_type_relation where video_id = #{videoId}")
    void deleteVideoTypeRelations(Long videoId);

    @Select("select * from video where publisher_id = #{userId}")
    List<Video> getVideosByUserId(@Param("userId") Long userId);

}
