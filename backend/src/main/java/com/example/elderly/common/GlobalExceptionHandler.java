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
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
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
                .map(f -> f.getField() + ": " + friendlyMessage(f))
                .distinct()
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
                .map(f -> f.getField() + ": " + friendlyMessage(f))
                .distinct()
                .collect(Collectors.joining("; "));
        log.warn("表单绑定失败: {}", errorMsg);
        Result<String> result = Result.fail(ResultCodeEnum.FAIL.getCode(), errorMsg);
        return ResponseEntity.status(400).body(result);
    }

    /**
     * 请求参数类型错误（如 page=abc）— HTTP 400，统一友好文案，不回显内部转换细节
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<String>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String name = e.getName() == null ? "参数" : e.getName();
        log.warn("参数类型不匹配: {}={}", name, e.getValue());
        Result<String> result = Result.fail(400, "参数 " + name + " 格式不正确");
        return ResponseEntity.status(400).body(result);
    }

    /**
     * 缺少必填请求参数（query / form）— HTTP 400
     * 阶段4 回归发现：原先未处理，直接落到兜底 500。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<String>> handleMissingParam(MissingServletRequestParameterException e) {
        String name = e.getParameterName();
        log.warn("缺少必填参数: {}", name);
        Result<String> result = Result.fail(400, "缺少必要参数 " + name);
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
        // 阶段0：完整异常仅写入服务端日志，响应体不回显任何内部实现细节
        log.error("运行时异常: {}", e.getMessage(), e);
        return ResponseEntity.status(500)
                .body(Result.fail(ResultCodeEnum.INTERNAL_ERROR.getCode(), "服务器内部错误，请联系管理员"));
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

    /** 避免把 Bean Validation 的默认英文转换细节回显给客户端 */
    private String friendlyMessage(FieldError fieldError) {
        String msg = fieldError.getDefaultMessage();
        if (msg == null || msg.contains("Failed to convert") || msg.contains("For input string")) {
            return "格式不正确";
        }
        return msg;
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
            case 429 -> 429;   // 验证码发送限流
            case 500 -> 500;
            default  -> 500;
        };
    }
}
