package org.example.tiktok.service;

import jakarta.servlet.http.HttpServletResponse;
import org.example.tiktok.dto.EmailCodeDTO;
import org.example.tiktok.dto.RegistryDTO;
import org.example.tiktok.entity.Result;

import java.io.IOException;

public interface UserService {
    Result sendMail(EmailCodeDTO emailCodeDTO);

    Result registry(RegistryDTO registryDTO);

    void getCaptcha(String uuid, HttpServletResponse response) throws IOException;
}
