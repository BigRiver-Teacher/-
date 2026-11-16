package com.example.elderly.util;

import com.example.elderly.common.exception.UnauthorizedException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * 登录用户工具类
 * 从请求上下文中获取当前登录用户ID（由 JwtAuthenticationFilter 注入）
 */
public class LoginUserUtil {

    public static Long getUserId() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new UnauthorizedException("请求上下文丢失，用户未登录");
        }
        Object uid = attrs.getAttribute("uid", RequestAttributes.SCOPE_REQUEST);
        if (uid == null) {
            throw new UnauthorizedException("用户未登录");
        }
        return Long.valueOf(uid.toString());
    }
}
