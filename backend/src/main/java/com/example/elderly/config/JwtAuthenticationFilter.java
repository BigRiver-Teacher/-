package com.example.elderly.config;

import com.example.elderly.common.Result;
import com.example.elderly.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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
 * JWT 认证过滤器（默认拒绝）。
 *
 * <p>策略：仅白名单路径（登录注册、接口文档、上传的静态附件）直接放行；
 * 其余所有请求必须携带有效的 Bearer Token，否则在过滤器内直接返回 401，
 * 不再把请求透传给 Controller，避免"部分接口无 Token 也能 200"的越权问题。</p>
 *
 * <p>Token 解析成功后把 userId 注入 RequestContextHolder，供 LoginUserUtil 读取。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /** 不需要认证的路径前缀（保持最小集） */
    private static final String[] WHITE_LIST = {
            "/api/auth/",      // 登录 / 注册 / 找回密码（注册与找回由 Controller 开关二次封禁）
            "/swagger",        // Knife4j / Swagger 静态资源与 swagger-resources
            "/v3/api-docs",    // OpenAPI JSON
            "/doc.html",       // Knife4j 入口
            "/webjars",        // 文档前端依赖
            "/uploads/",       // 聊天附件静态资源：<img src> 无法携带 Authorization 头
            "/favicon.ico",
            "/error"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // CORS 预检请求不带凭证，直接放行交给 CorsFilter 处理，否则跨域会被 401 打断
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isWhiteListed(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 默认拒绝：缺少 / 格式错误的 Authorization 头一律 401
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ") || authHeader.length() <= 7) {
            log.warn("[JWT Filter] 未认证请求被拒绝: {} {}", request.getMethod(), requestURI);
            writeUnauthorized(response, "用户未登录");
            return;
        }

        String token = authHeader.substring(7);
        try {
            String userId = jwtUtil.getUserId(token);
            Long uid = Long.valueOf(userId);
            RequestContextHolder.currentRequestAttributes()
                    .setAttribute("uid", uid, RequestAttributes.SCOPE_REQUEST);
            log.debug("[JWT Filter] 认证通过, userId={}, path={}", uid, requestURI);
        } catch (ExpiredJwtException e) {
            log.warn("[JWT Filter] token 已过期: {}", requestURI);
            writeUnauthorized(response, "登录已过期，请重新登录");
            return;
        } catch (JwtException | NumberFormatException e) {
            log.warn("[JWT Filter] token 无效: {}", requestURI);
            writeUnauthorized(response, "无效的登录凭证");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /** 直接写出与业务响应一致的 401 JSON，避免异常处理器无法覆盖过滤器层的问题 */
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(401, message)));
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
