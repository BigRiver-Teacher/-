package com.example.elderly.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "重置密码请求")
    public class ResetPasswordDTO {
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "验证码")
    private String verifyCode;
    @Schema(description = "新密码")
    private String newPassword;
}

