package com.example.elderly.config;

import com.example.elderly.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * 从 Authorization header 提取 Bearer token，解析后将 userId 注入到当前请求上下文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /** 不需要认证的白名单路径前缀 */
    private static final String[] WHITE_LIST = {
            "/api/auth/",
            "/swagger",
            "/v3/api-docs",
            "/doc.html",
            "/webjars",
            "/error"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.debug("[JWT Filter] 请求路径: {}", requestURI);

        // 白名单直接放行
        if (isWhiteListed(requestURI)) {
            log.debug("[JWT Filter] 白名单路径, 直接放行: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 提取 Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[JWT Filter] 缺少 Authorization header 或格式不正确: {}", requestURI);
            // 不放行，但不在此处写响应 — 由后续 LoginUserUtil 抛出 UnauthorizedException
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        log.debug("[JWT Filter] 提取token前20位: {}...", token.substring(0, Math.min(20, token.length())));

        try {
            // 使用已有的 JwtUtil 解析 token
            String userId = jwtUtil.getUserId(token);
            log.debug("[JWT Filter] token解析成功, userId={}", userId);

            // 将 userId 设置到 RequestContextHolder，供 LoginUserUtil 读取
            RequestContextHolder.currentRequestAttributes()
                    .setAttribute("uid", Long.valueOf(userId), RequestAttributes.SCOPE_REQUEST);
            log.debug("[JWT Filter] userId={} 已注入请求上下文", userId);

        } catch (ExpiredJwtException e) {
            log.warn("[JWT Filter] token已过期: {}", e.getMessage());
            // 设置过期标记，供 GlobalExceptionHandler 识别
            request.setAttribute("jwt.error", "TOKEN_EXPIRED");
        } catch (JwtException e) {
            log.warn("[JWT Filter] token解析失败: {}", e.getMessage());
            request.setAttribute("jwt.error", "TOKEN_INVALID");
        }

        filterChain.doFilter(request, response);
    }

    /** 判断是否为白名单路径 */
    private boolean isWhiteListed(String uri) {
        for (String prefix : WHITE_LIST) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
