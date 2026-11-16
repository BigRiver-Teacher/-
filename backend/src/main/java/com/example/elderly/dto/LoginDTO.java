package com.example.elderly.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录请求 DTO
 * username 和 phone 二选一即可（兼容不同前端传参习惯）
 * 校验在 Service 层做，避免 @Valid 阶段就因字段名不匹配而拦截
 */
@Data
@Schema(description = "登录请求")
    public class LoginDTO {

    /** 用户名 / 手机号（兼容旧版前端） */
    @Schema(description = "用户名")
    private String username;

    /** 手机号（新版前端直接传 phone） */
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "密码(MD5)")
    private String password;

    /**
     * 获取有效的手机号：优先取 phone，为空则取 username
     */
    public String getEffectiveUsername() {
        if (phone != null && !phone.isBlank()) {
            return phone;
        }
        return username;
    }
}
