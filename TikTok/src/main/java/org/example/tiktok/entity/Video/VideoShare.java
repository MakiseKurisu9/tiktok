package org.example.tiktok.entity.Video;

import lombok.Data;

@Data
public class VideoShare {
    private Long id;
    //分享的用户id
    private Long shareUserId;

    //未登录的用户也能进行分享，使用Ip判断唯一性
    private String ip;

    private Long videoId;
}
