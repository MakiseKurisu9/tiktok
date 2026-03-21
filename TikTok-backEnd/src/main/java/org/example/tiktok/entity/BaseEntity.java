package org.example.tiktok.entity;

import lombok.Data;
import net.bytebuddy.asm.Advice;

import java.time.LocalDateTime;

@Data
public class BaseEntity {

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
