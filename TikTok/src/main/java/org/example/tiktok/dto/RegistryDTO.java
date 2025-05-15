package org.example.tiktok.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistryDTO {
    private String email;
    private String password;
    private String code;
    private String uuid;
    private String emailCode;
}
