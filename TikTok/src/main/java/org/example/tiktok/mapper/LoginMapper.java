package org.example.tiktok.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.tiktok.entity.User.User;

@Mapper
public interface LoginMapper {

    @Insert("insert into user(nickname,  password, user_description, avatar_source,email, create_time, update_time) " +
            "VALUES (#{nickname},#{password},#{userDescription},#{avatarSource},#{email},NOW(),NOW())")
    void registry(User user);

    @Select("select * from user where email = #{email}")
    User getUserByEmail(String email);

    @Update("update user set password = #{newPassword} where email = #{email}")
    void changePassword(String newPassword, String email);
}
