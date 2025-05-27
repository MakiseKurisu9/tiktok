package org.example.tiktok.interceptor;

import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.tiktok.dto.UserDTO;
import org.example.tiktok.utils.JwtUtils;
import org.example.tiktok.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

import static org.example.tiktok.utils.SystemConstant.TOKEN_EXPIRE;
import static org.example.tiktok.utils.SystemConstant.TOKEN_PREFIX;

@Component
@Slf4j
//此拦截器判断用户是否携带token
public class TokenInterceptor implements HandlerInterceptor {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if(StringUtils.isEmpty(token)) {
            log.debug("token is empty");
            return true;
        }

        try {
            Claims claims = jwtUtils.parseToken(token);

            Long userId = claims.get("id", Long.class);
            if (userId == null) {
                log.debug("cannot find userId");
                return true;
            }
            String key = TOKEN_PREFIX + userId;
            //is token expire or wrong
            String tokenRedis = stringRedisTemplate.opsForValue().get(key);
            if (tokenRedis == null || !token.equals(tokenRedis)) {
                log.debug("tokenRedis is null or wrong");
                return true;
            }
            //用户停留在公共页面时一直刷新token时长
            stringRedisTemplate.expire(key, Long.parseLong(TOKEN_EXPIRE), TimeUnit.DAYS);

            String nickname = claims.get("nickname", String.class);
            String email = claims.getSubject();

            if(nickname != null && email != null) {
                UserDTO userDTO = new UserDTO(userId,nickname,email);
                UserHolder.saveUser(userDTO);
            }
            return true;
        } catch (Exception e) {
            //出现异常不影响访问公共api
            log.debug("exception!!!");
        }
        return true;

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
