package com.example.elderly.controller;

import com.example.elderly.common.Result;
import com.example.elderly.common.exception.BusinessException;
import com.example.elderly.dto.*;
import com.example.elderly.service.AuthService;
import com.example.elderly.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "登录注册")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录接口
     * 业务失败时 Service 层返回 Result.fail(code, msg)，
     * Controller 检查 code != 200 时抛 BusinessException，
     * 由 GlobalExceptionHandler 统一转为正确的 HTTP 状态码。
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        log.info("========== [AuthController] 登录请求开始 ==========");
        log.info("[AuthController] 请求参数: username={}, phone={}", dto.getUsername(), dto.getPhone());

        // 校验至少提供了 username 或 phone
        String effectiveUsername = dto.getEffectiveUsername();
        if (effectiveUsername == null || effectiveUsername.isBlank()) {
            throw new BusinessException(400, "手机号不能为空");
        }
        log.info("[AuthController] 有效手机号: {}", effectiveUsername);

        Result<LoginVO> result = authService.login(dto);
        log.info("[AuthController] 登录结果: code={}, message={}, dataNull={}",
                result.getCode(), result.getMessage(), result.getData() == null);

        // 业务失败 → 抛异常让全局异常处理器设置正确的 HTTP 状态码
        if (result.getCode() != 200) {
            log.warn("[AuthController] 登录业务失败, 转发到异常处理器: code={}, msg={}",
                    result.getCode(), result.getMessage());
            throw new BusinessException(result.getCode(), result.getMessage());
        }

        log.info("[AuthController] 登录成功, 返回 token 和用户信息");
        return result;
    }

    @PostMapping("/send-reset-code")
    public Result<String> sendResetCode(@Valid @RequestBody SendResetCodeDTO dto) {
        log.info("[AuthController] 发送重置验证码请求, 邮箱: {}", dto.getEmail());
        Result<String> result = authService.sendResetCode(dto);
        if (result.getCode() != 200) {
            throw new BusinessException(result.getCode(), result.getMessage());
        }
        return result;
    }

    @PostMapping("/reset-password")
    public Result<String> reSendPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        log.info("[AuthController] 重置密码请求, 邮箱: {}", dto.getEmail());
        Result<String> result = authService.resetPassword(dto);
        if (result.getCode() != 200) {
            throw new BusinessException(result.getCode(), result.getMessage());
        }
        return result;
    }

    @PostMapping("/send-register-code")
    public Result<String> sendRegisterCode(@Valid @RequestBody SendRegisterCodeDTO dto) {
        log.info("[AuthController] 发送注册验证码请求, 手机号: {}", dto.getPhone());
        Result<String> result = authService.sendRegisterCode(dto);
        if (result.getCode() != 200) {
            throw new BusinessException(result.getCode(), result.getMessage());
        }
        return result;
    }

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterDTO dto) {
        log.info("[AuthController] 注册请求, 手机号: {}", dto.getPhone());
        Result<String> result = authService.register(dto);
        if (result.getCode() != 200) {
            throw new BusinessException(result.getCode(), result.getMessage());
        }
        return result;
    }
}
