package com.example.elderly.util;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final long EXPIRE = 1000 * 60 * 60 * 24;

    /** 签名密钥由配置项 jwt.secret 提供（可用环境变量 JWT_SECRET 覆盖）。 */
    private final SecretKey KEY;

    public JwtUtil(@Value("${jwt.secret:elderly2026SecretKey1234567890abcdef}") String secret) {
        this.KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String userId) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + EXPIRE);
        return Jwts.builder()

                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .signWith(KEY)
                .compact();
    }

    /**
     * 从token获取用户id
     */
    public String getUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}