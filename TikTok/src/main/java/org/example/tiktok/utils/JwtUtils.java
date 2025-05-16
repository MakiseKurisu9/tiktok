package org.example.tiktok.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.example.tiktok.entity.User.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@Getter
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private long expire;

    private Key key;

    @PostConstruct
    public void initKey() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())
                .claim("nickname", user.getNickname())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token已过期");
        } catch (JwtException e) {
            throw new RuntimeException("Token无效");
        }
    }

    public boolean isTokenExpired(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }

    public String getEmailFromToken(String token) {
        return parseToken(token).getSubject();
    }
}
