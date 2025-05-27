package org.example.tiktok.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailCodeDTO {
    private String uuid;
    private String code;
    private String email;
}
