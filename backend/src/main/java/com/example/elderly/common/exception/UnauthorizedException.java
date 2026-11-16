package com.example.elderly.common.exception;

import com.example.elderly.common.ResultCodeEnum;

/**
 * 未授权/未登录异常
 * 对应 HTTP 401
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException() {
        super(ResultCodeEnum.UNAUTHORIZED.getCode(), "用户未登录");
    }

    public UnauthorizedException(String message) {
        super(ResultCodeEnum.UNAUTHORIZED.getCode(), message);
    }
}
