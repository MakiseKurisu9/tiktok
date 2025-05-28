package org.example.tiktok.mapper;

import org.apache.ibatis.annotations.*;
import org.example.tiktok.entity.Video.Video;
import org.example.tiktok.entity.Video.VideoType;

import java.util.List;

@Mapper
public interface IndexMapper {

    @Select("select * from video where video_type_id = #{typeId}")
    List<Video> getVideosByTypeId(Long typeId);

    @Select("select * from video_type")
    List<VideoType> getVideoTypes();

    @Select("select * from video where video.id = #{videoId}")
    Video getVideoById(Long videoId);

    List<Video> searchVideo(@Param("searchName") String searchName);

    @Insert("insert into video_share(share_user_id, ip, video_id) values (#{shareUserId},#{ip},#{videoId})")
    void shareVideo(@Param("shareUserId") Long shareUserId,@Param("ip") String ip,@Param("videoId") Long videoId);

    @Update("update video set shares = shares + 1 where id = #{videoId}")
    void updateVideoShare(Long videoId);

    @Update("update video set views = video.views + #{views} where id = #{videoId}")
    void addViews(@Param("videoId") Long videoId,@Param("views") Long views);
}
