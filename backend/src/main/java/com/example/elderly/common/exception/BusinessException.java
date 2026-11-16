package com.example.elderly.common.exception;

import lombok.Getter;

/**
 * 业务异常基类，携带业务状态码
 * 用于替代 RuntimeException 字符串匹配，让 GlobalExceptionHandler 能精确区分异常类型
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
