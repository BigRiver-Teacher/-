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

    public JwtUtil(@Value("${jwt.secret:}") String secret) {
        // 未配置 jwt.secret / JWT_SECRET 时直接失败，避免使用不安全的默认密钥
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                "jwt.secret 未配置或长度不足 32 字节，请通过环境变量 JWT_SECRET 或配置项 jwt.secret 提供");
        }
        this.KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String userId, String role) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + EXPIRE);
        return Jwts.builder()

                .setSubject(userId)
                .claim("role", role)
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

    /**
     * 从token获取用户角色
     */
    public String getRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }
}