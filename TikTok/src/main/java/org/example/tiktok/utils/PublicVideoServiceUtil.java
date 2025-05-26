package org.example.tiktok.utils;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.example.tiktok.entity.Video.Video;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class PublicVideoServiceUtil {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    public void isStared(Video video) {
        Long userId = UserHolder.getUser().getId();
        String key = "video:liked:" + video.getId();
        //判断用户是否已经点赞
        Boolean member = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        video.setIsLiked(BooleanUtils.isTrue(member));
    }
}
