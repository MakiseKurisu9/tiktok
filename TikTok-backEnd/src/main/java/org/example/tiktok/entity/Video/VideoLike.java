package org.example.tiktok.entity.Video;

import lombok.Data;

@Data
public class VideoLike {
    private Long id;

    private Long videoId;
    //点赞人的id
    private Long likeId;
}
