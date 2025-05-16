package org.example.tiktok.service;

import jakarta.servlet.http.HttpServletResponse;
import org.example.tiktok.dto.EmailCodeDTO;
import org.example.tiktok.dto.FindPasswordDTO;
import org.example.tiktok.dto.LoginDTO;
import org.example.tiktok.dto.RegistryDTO;
import org.example.tiktok.entity.Result;

import java.io.IOException;

public interface LoginService {
    Result sendMail(EmailCodeDTO emailCodeDTO);

    Result registry(RegistryDTO registryDTO);

    void getCaptcha(String uuid, HttpServletResponse response) throws IOException;

    Result login(LoginDTO loginDTO);

    Result findPassword(FindPasswordDTO findPasswordDTO);
}
