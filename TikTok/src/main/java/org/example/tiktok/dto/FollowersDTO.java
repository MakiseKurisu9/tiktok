package org.example.tiktok.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.tiktok.entity.BaseEntity;

@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class FollowersDTO extends BaseEntity {
    private Long id;
    private String nickname;
    private String avatarSource;
}
