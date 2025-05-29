package org.example.tiktok.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//for hot rank
public class VideoHot {
    private Long videoId;
    private String title;
    private double hotScore;
}
