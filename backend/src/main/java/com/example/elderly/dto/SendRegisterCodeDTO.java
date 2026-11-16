package com.example.elderly.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "发送注册验证码请求")
    public class SendRegisterCodeDTO {
    @Schema(description = "手机号")
    private String phone;
}
