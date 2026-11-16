package com.example.elderly.common;

import com.example.elderly.common.exception.BusinessException;
import com.example.elderly.common.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.BindingException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 使用 ResponseEntity 动态设置 HTTP 状态码，确保与响应体中 code 一致
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 自定义业务异常 ====================

    /**
     * 未授权/未登录异常 — HTTP 401
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Result<String>> handleUnauthorized(UnauthorizedException e) {
        log.warn("未授权访问: {}", e.getMessage());
        Result<String> result = Result.fail(ResultCodeEnum.UNAUTHORIZED.getCode(),
                e.getMessage() != null ? e.getMessage() : "用户未登录");
        return ResponseEntity.status(401).body(result);
    }

    /**
     * 通用业务异常 — 使用异常自带的业务状态码
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<String>> handleBusiness(BusinessException e) {
        log.warn("业务异常 [code={}]: {}", e.getCode(), e.getMessage());
        Result<String> result = Result.fail(e.getCode(), e.getMessage());
        return ResponseEntity.status(mapCodeToHttpStatus(e.getCode())).body(result);
    }

    // ==================== 参数校验异常 ====================

    /**
     * 处理 @Valid 校验失败（JSON 请求体）— HTTP 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<String>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", errorMsg);
        Result<String> result = Result.fail(ResultCodeEnum.FAIL.getCode(), errorMsg);
        return ResponseEntity.status(400).body(result);
    }

    /**
     * 处理表单绑定校验失败 — HTTP 400
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<String>> handleBindException(BindException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("表单绑定失败: {}", errorMsg);
        Result<String> result = Result.fail(ResultCodeEnum.FAIL.getCode(), errorMsg);
        return ResponseEntity.status(400).body(result);
    }

    // ==================== 数据库异常 ====================

    /**
     * SQL 语法 / 数据访问异常 — HTTP 500
     */
    @ExceptionHandler({SQLException.class, SQLSyntaxErrorException.class, DataAccessException.class})
    public ResponseEntity<Result<String>> handleSqlException(Exception e) {
        log.error("数据库异常: {}", e.getMessage(), e);
        return ResponseEntity.status(500)
                .body(Result.fail(ResultCodeEnum.INTERNAL_ERROR.getCode(), "数据库查询异常"));
    }

    /**
     * MyBatis 绑定异常（Mapper 方法找不到 SQL）— HTTP 500
     */
    @ExceptionHandler(BindingException.class)
    public ResponseEntity<Result<String>> handleBindingException(BindingException e) {
        log.error("MyBatis绑定异常: {}", e.getMessage(), e);
        return ResponseEntity.status(500)
                .body(Result.fail(ResultCodeEnum.INTERNAL_ERROR.getCode(), "SQL映射配置异常"));
    }

    // ==================== 404 资源不存在 ====================

    /**
     * 请求路径不存在 — HTTP 404
     * Spring 6.1 对未映射路径抛 NoResourceFoundException，若不处理会被兜底成 500
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<String>> handleNoResource(NoResourceFoundException e) {
        log.warn("请求的资源不存在: {}", e.getMessage());
        Result<String> result = Result.fail(404, "请求的资源不存在");
        return ResponseEntity.status(404).body(result);
    }

    // ==================== 运行时异常 ====================

    /**
     * 运行时异常 — HTTP 500（兜底）
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<String>> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        return ResponseEntity.status(500)
                .body(Result.fail(ResultCodeEnum.INTERNAL_ERROR.getCode(),
                        "服务器内部错误: " + e.getMessage()));
    }

    // ==================== 兜底异常 ====================

    /**
     * 捕获所有未处理的异常 — HTTP 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<String>> handleException(Exception e) {
        log.error("未捕获异常: {}", e.getMessage(), e);
        return ResponseEntity.status(500)
                .body(Result.fail(ResultCodeEnum.INTERNAL_ERROR.getCode(), "服务器内部错误"));
    }

    // ==================== 工具方法 ====================

    /**
     * 将业务状态码映射为 HTTP 状态码
     */
    private int mapCodeToHttpStatus(int businessCode) {
        return switch (businessCode) {
            case 400 -> 400;
            case 401 -> 401;
            case 403 -> 403;
            case 404 -> 404;
            default  -> 500;
        };
    }
}
