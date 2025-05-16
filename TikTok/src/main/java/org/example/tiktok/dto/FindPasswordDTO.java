package org.example.tiktok.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindPasswordDTO {
    String email;
    String code;
    String newPassword;
}
