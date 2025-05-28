package org.example.tiktok.entity.Video;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.tiktok.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = false)
public class Video extends BaseEntity {
    private Long id;

    private String title;

    private String description;
    //冗余
    private String type;

    //通过fileProtect实现，故存储Long类型 保护资源路径
    private Long source;

    private Long imgSource;

    private VideoType videoType;

    private Long publisherId;

    //第三方api字段
    private String auditStatus;

    private String auditMessage;

    //例如 公开/私密
    private String status;

    //冗余字段
    //点赞次数
    private Long likes;

    //浏览次数
    private Long views;

    //收藏次数
    private Long favourites;

    //分享次数
    private Long shares;

    //no need

    private Boolean isLiked;

}
