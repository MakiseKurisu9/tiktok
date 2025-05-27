package org.example.tiktok.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindPasswordDTO {
    private String email;
    private String code;
    private String newPassword;
}
