package org.example.tiktok.config;

import jakarta.annotation.Resource;
import org.example.tiktok.interceptor.TokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login/captcha.jpg/**",  // 排除验证码请求
                        "/login/sendMail",        // 排除发送邮件请求
                        "/login/registry",        // 排除注册请求
                        "/login/logIn",           // 排除登录请求
                        "/error",
                        "/static/**",
                        "/webjars/**"
                        );                // 排除错误页面,
    }
}
