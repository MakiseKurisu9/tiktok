package org.example.tiktok.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistryDTO {
    private String username;
    private String email;
    private String password;
    private String code;
    private String emailCode;
    private String uuid;
}
