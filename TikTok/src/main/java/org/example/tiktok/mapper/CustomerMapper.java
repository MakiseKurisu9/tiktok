package org.example.tiktok.mapper;

import org.apache.ibatis.annotations.*;
import org.example.tiktok.dto.FollowersDTO;
import org.example.tiktok.entity.User.Favourite;
import org.example.tiktok.entity.User.User;
import org.example.tiktok.entity.Video.VideoType;

import java.util.List;

@Mapper
public interface CustomerMapper {

    @Select("select * from favourite where create_user_id =#{userId}")
    List<Favourite> getCustomerFavourite(Long userId);

    @Select("select * from favourite where favourite.id = #{favouriteId}")
    Favourite getFavouriteById(Long favouriteId);

    @Update("update favourite set name = #{name}, description = #{description}, update_time = NOW() where id = #{id}")
    void updateFavourite(Favourite favourite);

    @Insert("insert into favourite(name, description, video_count, create_user_id, create_time, update_time) " +
            "values(#{name}, #{description}, 0, #{createUserId}, NOW(), NOW())")
    void addFavourite(Favourite favourite);

    @Delete("delete from favourite where id = #{favouriteId}")
    void delFavouriteById(Long favouriteId);

    @Delete("delete from favourite_video_relation where favourite_video_relation.fid = #{favouriteId}")
    void delFavouriteVideoById(Long favouriteId);

    @Delete("delete from user_favourite_relation where favourite_id = #{favouriteId}")
    void delFavouriteUserById(Long favouriteId);

    void subscribeVideoTypes(@Param("userId") Long userId,@Param("typeIds") List<Long> typeIds);

    //返回的是订阅的视频分类id
    @Select("select video_type_id from subscribe_video_type where user_id = #{userId}")
    List<Long> getSubscribeByUserId(Long userId);

    List<VideoType> getVideoTypesByIds(@Param("ids") List<Long> videoTypeId);

    @Select("select * from user where id = #{userId}")
    User getUserByUserId(Long userId);

    @Update("update user set nickname=#{nickName},avatar_source=#{avatarSource}," +
            "sex=#{sex},user_description=#{userDescription},create_time=NOW()" +
            " where id = #{Id}")
    void updateUserInfo(User user);

    @Select("select follow.follow_id from follow where follower_id = #{userId}")
    List<Long> getFollowIds(Long userId);

    List<FollowersDTO> getFollowInfo(@Param("ids") List<Long> followId);

    @Select("select follower_id from follow where follow_id = #{followId}")
    List<Long> getFollowers(Long followId);

    List<FollowersDTO> getFollowersInfo(@Param("ids") List<Long> followersId);

    @Select("select count(*) from follow where follow_id =#{followUserId} and follower_id = #{followerId}")
    int isFollow(@Param("followUserId") Long followUserId,@Param("followerId") Long followerId);

    @Delete("delete from follow where follow_id =#{followUserId} and follower_id = #{followerId}")
    int unFollow(@Param("followUserId")Long followUserId,@Param("followerId") Long followerId);

    @Insert("INSERT INTO follow (follow_id, follower_id) VALUES (#{followUserId}, #{followerId})")
    int follow(@Param("followUserId")Long followUserId,@Param("followerId") Long followerId);
}
