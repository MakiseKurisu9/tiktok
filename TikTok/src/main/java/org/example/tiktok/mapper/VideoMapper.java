package org.example.tiktok.mapper;

import org.apache.ibatis.annotations.*;
import org.example.tiktok.dto.CommentersDTO;
import org.example.tiktok.entity.User.Follow;
import org.example.tiktok.entity.Video.Comment;
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
    List<Video> getVideosByVideoIds(@Param("ids") List<Long> ids);

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

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into comment(video_id, from_user_id, to_user_id, content, parent_id, root_id, likes_count, child_count, create_time, update_time) " +
            "VALUES (#{videoId},#{fromUserId},#{toUserId},#{content},#{parentId},#{rootId},#{likesCount},#{childCount},NOW(),NOW())")
    void addComment(Comment comment);

    @Select("select * from comment where id = #{id}")
    Comment getCommentById(Long id);

    @Update("update comment set child_count = child_count + 1 where id = #{id}")
    void addChildCount(Long id);

    @Update("update comment set root_id = #{rootId} where id = #{id}")
    void updateRootId(@Param("id") Long id,@Param("rootId") Long rootId);

    @Select("select * from comment where video_id = #{videoId} and parent_id = 0 order by create_time desc")
    List<Comment> getRootCommentsByVideoId(Long videoId);

    @Select("select id,nickname,avatar_source from user where id = #{fromUserId}")
    CommentersDTO getUserById(Long fromUserId);

    @Update("update comment set likes_count = likes_count + 1 where id = #{commentId}")
    Boolean likeComment(Long commentId);

    @Update("update comment set likes_count = likes_count - 1 where id = #{commentId}")
    Boolean unlikeComment(Long commentId);

    @Delete("delete from comment where id = #{commentId}")
    void deleteComment(Long commentId);

    @Delete("delete from comment where root_id = #{rootId}")
    void deleteCommentByRootId(Long rootId);

    @Delete("delete from comment where parent_id = #{parentId}")
    void deleteCommentByParentId(Long parentId);

    @Select("select * from comment where root_id = #{rootId} and id != #{rootId} order by create_time")
    List<Comment> getRootCommentsExcludeParentByVideoId(Long rootId);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into video( title, description, type, source, img_source, video_type_id, publisher_id, likes, views, favourites, shares, create_time, update_time, comments,publisher_name)" +
            " VALUES ( #{title}, #{description}, #{type}, #{source}, #{imgSource}, #{videoTypeId}, #{publisherId}, #{likes}, #{views}, #{favourites}, #{shares}, #{createTime}, #{updateTime}, #{comments},#{publisherName})")
    void addVideo(Video video);

    @Select("select * from video")
    List<Video> getAllVideos();

    @Update("update video set title = #{title},description = #{description}, type = #{type}, source = #{source}, img_source = #{imgSource},video_type_id = #{videoTypeId}, update_time = NOW() where id = #{id}")
    void updateVideo(Video video);

    @Select("select * from follow where follow_id = #{userId}")
    List<Follow> getFollowers(Long userId);
}
