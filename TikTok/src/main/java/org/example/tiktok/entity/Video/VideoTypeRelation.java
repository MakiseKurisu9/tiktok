package org.example.tiktok.entity.Video;

import lombok.Data;

@Data
public class VideoTypeRelation {
    private Long id;

    private Long videoId;

    private Long videoTypeId;

}
