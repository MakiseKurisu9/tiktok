package org.example.tiktok.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.tiktok.entity.User.Favourite;
import org.example.tiktok.entity.Video.Video;

import java.util.List;

@Mapper
public interface VideoMapper {

    @Select("select v.* from favourite_video_relation f join video v on f.vid = v.id where f.fid = #{favouriteTableId}")
    List<Video> getVideoInFavouriteTable(String favouriteTableId);

    @Insert("insert into favourite_video_relation(fid, vid) values (#{favouriteTableId},#{videoId})")
    void addVideoIntoFavouriteTable(String favouriteTableId,String videoId);

    @Select("select * from favourite_video_relation where vid = #{videoId} and fid = #{favouriteTableId}")
    Integer isVideoInFavouriteTable(String favouriteTableId, String videoId);

    List<Video> getVideoByVideoId(List<Long> ids);
}
