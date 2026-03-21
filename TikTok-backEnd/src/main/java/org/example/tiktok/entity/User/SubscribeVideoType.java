package org.example.tiktok.entity.User;

import lombok.Data;

@Data
//订阅
public class SubscribeVideoType {
    private Long id;

    private Long userId;

    //感兴趣的视频分类
    private Long videoTypeId;

}
