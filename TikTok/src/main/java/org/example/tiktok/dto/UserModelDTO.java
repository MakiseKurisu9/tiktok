package org.example.tiktok.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserModelDTO {
    private Long videoId;//此后不推荐
    private Long typeId;//推荐相关type
    private double score;//权重
}
