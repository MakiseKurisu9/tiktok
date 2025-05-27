package org.example.tiktok.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String email;
    private String password;
    private String code;
    private String uuid;
}
