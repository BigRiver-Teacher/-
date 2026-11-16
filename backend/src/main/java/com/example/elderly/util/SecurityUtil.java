package com.example.elderly.util;

/**
 * 安全工具类
 * 获取当前登录用户ID（委托给 LoginUserUtil）
 */
public class SecurityUtil {

    /**
     * 获取当前登录用户的ID
     * @return 用户ID，如果未登录则抛 UnauthorizedException
     */
    public static Long getLoginUserId() {
        return LoginUserUtil.getUserId();
    }
}
