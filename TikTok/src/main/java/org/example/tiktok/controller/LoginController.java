package org.example.tiktok.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.example.tiktok.dto.EmailCodeDTO;
import org.example.tiktok.dto.FindPasswordDTO;
import org.example.tiktok.dto.LoginDTO;
import org.example.tiktok.dto.RegistryDTO;
import org.example.tiktok.entity.Result;
import org.example.tiktok.service.LoginService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Resource
    LoginService userService;

    @GetMapping("/captcha.jpg/{uuid}")
    public void getCaptcha(@PathVariable("uuid") String uuid,
                             HttpServletResponse response) throws IOException {
        userService.getCaptcha(uuid,response);
    }

    @PostMapping("/sendMail")
    public Result sendMail(@RequestBody EmailCodeDTO emailCodeDTO) {
        return userService.sendMail(emailCodeDTO);
    }

    @PostMapping("/registry")
    public Result registry(@RequestBody RegistryDTO registryDTO) {
        return userService.registry(registryDTO);
    }

    @PostMapping("/logIn")
    public Result login(@RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    @PostMapping("/findPassword")
    public Result findPassword(@RequestBody FindPasswordDTO findPasswordDTO){
        return userService.findPassword(findPasswordDTO);
    }


}
