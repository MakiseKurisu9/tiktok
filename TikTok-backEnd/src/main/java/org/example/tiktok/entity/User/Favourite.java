package org.example.tiktok.entity.User;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.tiktok.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = false)
//收藏夹表
public class Favourite extends BaseEntity {
    private Long id;

    private String name;

    private String description;

    //视频数量
    private Integer videoCount;

    private Long createUserId;
}
