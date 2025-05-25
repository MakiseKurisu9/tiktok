package org.example.tiktok.config;

import jakarta.annotation.Resource;
import org.example.tiktok.interceptor.PublicInterceptor;
import org.example.tiktok.interceptor.TokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    TokenInterceptor tokenInterceptor;

    @Resource
    PublicInterceptor publicInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        //and some other pages about watching videos
                        "/login/captcha.jpg/**",  // 排除验证码请求
                        "/login/sendMail",        // 排除发送邮件请求
                        "/login/registry",        // 排除注册请求
                        "/login/logIn",           // 排除登录请求
                        "/error",
                        "/static/**",
                        "/webjars/**"
                        )
                .order(0);                // 排除错误页面,
        registry.addInterceptor(publicInterceptor)
                .addPathPatterns("/**")//占位符，之后修改为所有和个人信息有关的路径
                .order(1);
    }
}
