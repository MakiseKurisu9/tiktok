package org.example.tiktok.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentersDTO {
    private Long id;
    private String nickname;
    private String avatarSource;
}
