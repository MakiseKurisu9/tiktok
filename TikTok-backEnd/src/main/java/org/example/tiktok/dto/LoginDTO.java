package org.example.tiktok.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String username;
    private String password;
    private String code;
    private String uuid;
}
