package org.example.tiktok.entity.Video;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.tiktok.dto.CommentersDTO;
import org.example.tiktok.entity.BaseEntity;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment extends BaseEntity {
    private Long id;
    private Long videoId;

    private Long fromUserId;
    private Long toUserId;

    private String content;

    private Long parentId;
    private Long rootId;
    //喜欢数量 子评论数量
    private Integer likesCount;
    private Integer childCount;
    //exist = false
    private CommentersDTO commentersDTO;
    private List<Comment> comments;
    private Boolean isLiked;
}
