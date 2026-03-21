package org.example.tiktok.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.tiktok.utils.UserHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
//此拦截器判断用户是否能访问个人信息页面
public class PublicInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(UserHolder.getUser() == null ) {
            response.setStatus(401);
            System.out.println("interceptor affect for test");
            return false;
        }
        return true;
    }
}
