package com.example.elderly.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "发送重置密码验证码请求")
    public class SendResetCodeDTO {
    @Schema(description = "邮箱")
    private String email;
}
