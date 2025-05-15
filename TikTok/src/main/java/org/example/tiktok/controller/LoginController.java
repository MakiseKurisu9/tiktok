package org.example.tiktok.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.example.tiktok.dto.EmailCodeDTO;
import org.example.tiktok.dto.RegistryDTO;
import org.example.tiktok.entity.Result;
import org.example.tiktok.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Resource
    UserService userService;



    @GetMapping("/captcha.jpg/{uuid}")
    public void getCaptcha(@PathVariable("uuid") String uuid,
                             HttpServletResponse response) throws IOException {

    }


    @PostMapping("/sendMail")
    public Result sendMail(@RequestBody EmailCodeDTO emailCodeDTO) {
        return userService.sendMail(emailCodeDTO);
    }

    @PostMapping("/registry")
    public Result registry(RegistryDTO registryDTO) {
        return userService.registry(registryDTO);
    }


}
