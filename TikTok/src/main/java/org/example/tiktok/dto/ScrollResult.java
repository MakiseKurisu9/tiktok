package org.example.tiktok.dto;

import lombok.Data;

import java.util.List;

@Data
public class ScrollResult {
    private List<?> list;//数据集合
    private Long minTime;//本次查询的推送的最小时间戳
    private Integer offset;//偏移量
}
