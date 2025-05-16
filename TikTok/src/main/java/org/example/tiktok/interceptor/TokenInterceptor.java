package org.example.tiktok.interceptor;

import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.example.tiktok.dto.UserDTO;
import org.example.tiktok.utils.JwtUtils;
import org.example.tiktok.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;

import java.util.Map;

import static org.example.tiktok.utils.SystemConstant.TOKEN_PREFIX;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if(StringUtils.isEmpty(token)) {
            throw new RuntimeException("not take token");
        }

        try{
            Claims claims = jwtUtils.parseToken(token);

            Long userId = claims.get("id", Long.class);
            if (userId == null) {
                throw new RuntimeException("invalid token");
            }
            String key = TOKEN_PREFIX + userId;
            //is token expire or wrong
            String tokenRedis = stringRedisTemplate.opsForValue().get(key);
            if (tokenRedis == null || !token.equals(tokenRedis)) {
                throw new RuntimeException("token wrong or expire");
            }

            String nickname = claims.get("nickname", String.class);
            String email = claims.getSubject();
            UserDTO userDTO = new UserDTO(userId, nickname, email);
            UserHolder.saveUser(userDTO);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("cannot parse token" + e.getMessage());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
