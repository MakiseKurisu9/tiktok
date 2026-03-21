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

    /**
     * 获取图形验证码图片
     * 用于前端验证码显示，例如：<img :src="`/captcha.jpg/${uuid}`" />
     * @param uuid 验证码唯一标识
     * @param response 响应对象，用于输出图片流
     */
    @GetMapping("/captcha.jpg/{uuid}")
    public void getCaptcha(@PathVariable("uuid") String uuid,
                             HttpServletResponse response) throws IOException {
         userService.getCaptcha(uuid,response);
    }
    /**
     * 发送邮箱验证码（注册、找回密码时使用）
     * @param emailCodeDTO 包含邮箱地址及用途
     * @return 操作结果
     */
    @PostMapping("/sendMail")
    public Result sendMail(@RequestBody EmailCodeDTO emailCodeDTO) {
        return userService.sendMail(emailCodeDTO);
    }
    /**
     * 用户注册
     * @param registryDTO 注册信息（包含邮箱、验证码、密码等）
     * @return 注册结果
     */
    @PostMapping("/registry")
    public Result registry(@RequestBody RegistryDTO registryDTO) {
        return userService.registry(registryDTO);
    }
    /**
     * 用户登录
     * @param loginDTO 登录信息（邮箱、密码、验证码等）
     * @return 登录结果，包含 token 等信息
     */
    @PostMapping("/logIn")
    public Result login(@RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }
    /**
     * 找回密码
     * @param findPasswordDTO 包含邮箱、验证码、新密码等信息
     * @return 重置密码结果
     */
    @PostMapping("/findPassword")
    public Result findPassword(@RequestBody FindPasswordDTO findPasswordDTO){
        return userService.findPassword(findPasswordDTO);
    }

}
