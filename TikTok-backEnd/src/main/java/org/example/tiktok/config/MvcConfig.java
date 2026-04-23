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
                        // ── Infrastructure ──────────────────────────────────
                        "/error",                // Spring error page
                        "/favicon.ico",          // browser auto-request

                        // ── Static assets ───────────────────────────────────
                        "/static/**",
                        "/webjars/**",

                        // ── Auth / registration ─────────────────────────────
                        "/login/captcha.jpg/**", // CAPTCHA image
                        "/login/sendMail",       // send verification e-mail
                        "/login/registry",       // register new account
                        "/login/logIn",          // sign in
                        "/login/findPassword",   // password recovery

                        // ── Public video browsing (no login required) ────────
                        "/index/pushVideos",          // recommended feed
                        "/index/search",              // search
                        "/index/types",               // list all categories
                        "/index/video/type/**",       // videos by category
                        "/index/video/**",            // watch a video
                        "/index/video/hot/**",        // hot / trending
                        "/index/video/similar",       // similar videos
                        "/index/share/**",            // share link

                        // ── Public comment viewing ───────────────────────────
                        "/video/index/comment/by/**", // comments on a video

                        // ── Public profile viewing ───────────────────────────
                        "/customer/getInfo/**",       // view any user's profile
                        "/customer/follow",           // follower count (read-only)
                        "/customer/followers",         // follower list  (read-only

                        "/admin/**"                    // admin operations (read-only)


                        // NOTE: /upload/** is intentionally NOT excluded here.
                        // Uploading content requires the user to be signed in.
                        // If you need a public CDN-style read path (e.g. serving
                        // uploaded files), add a separate read-only pattern such
                        // as "/upload/public/**" and exclude only that.
                )//占位符，之后修改为所有和个人信息有关的路径
                .order(1);
    }
}
