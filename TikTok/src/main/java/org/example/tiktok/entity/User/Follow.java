package org.example.tiktok.entity.User;

import lombok.Data;

@Data
public class Follow {
    private Long id;
    //关注的id
    private Long followId;

    //粉丝id
    private Long followerId;

    private Long userId;
}
