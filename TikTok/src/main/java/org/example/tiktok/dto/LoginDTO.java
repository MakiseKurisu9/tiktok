package org.example.tiktok.dto;

import lombok.Data;

@Data
public class LoginDTO {
    String email;
    String password;
    String code;
    String uuid;
}
