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
                .order(0);                // 排除错误页面,
        registry.addInterceptor(publicInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        //and some other pages about watching videos
                        "/login/captcha.jpg/**",  // 排除验证码请求
                        "/login/sendMail",        // 排除发送邮件请求
                        "/login/registry",        // 排除注册请求
                        "/login/logIn",           // 排除登录请求

                        "/video/index/comment/by/**",//获取视频评论

                        "/index/video/type/**",//获取分类下的所有视频
                        "/index/types",//获取视频分类
                        "/index/video/**",//看某个视频
                        "/index/pushVideos",//推荐
                        "/index/search",//搜索
                        "/index/share/**",//分享
                        "/index/video/user",//查看用户发布的视频
                        "/index/video/hot/**",//热榜
                        "/index/video/similar",

                        "/customer/getInfo/**",
                        "/customer/follow",
                        "/customer/followers",

                        "/upload/**",



                        "/static/**",
                        "/webjars/**"
                )//占位符，之后修改为所有和个人信息有关的路径
                .order(1);
    }
}
