package org.example.tiktok.mapper;

import org.apache.ibatis.annotations.*;
import org.example.tiktok.entity.User.User;

@Mapper
public interface LoginMapper {

    @Insert("insert into user(user.username,nickname,  password, user_description, avatar_source,email, create_time, update_time,follow,followers) " +
            "VALUES (#{username},#{nickname},#{password},#{userDescription},#{avatarSource},#{email},NOW(),NOW(),0,0)")
    void registry(User user);

    @Select("select * from user where username = #{username}")
    User getUserByUsername(String username);

    @Update("update user set password = #{newPassword} where email = #{email}")
    void changePassword(@Param("newPassword")String newPassword,@Param("email") String email);


}
