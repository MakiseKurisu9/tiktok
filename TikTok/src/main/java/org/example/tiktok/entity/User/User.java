package org.example.tiktok.entity.User;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.tiktok.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = false)
//未来可能引入权限表authorization,角色表role,以及权限user表，权限角色表
public class User extends BaseEntity {
    private Long id;

    private String nickname;

    private String username;

    private String password;

    private String userDescription;

    private String avatarSource;
}

