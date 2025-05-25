package org.example.tiktok.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
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
}
