package com.example.elderly.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "注册请求")
    public class RegisterDTO {
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "验证码")
    private String verifyCode;
    @Schema(description = "密码(MD5)")
    private String password;
}
