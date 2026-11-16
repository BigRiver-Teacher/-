package com.example.elderly.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录结果")
    public class LoginVO {
    @Schema(description = "登录令牌")
    private String token;
    @Schema(description = "用户信息")
    private UserInfoVO userInfo;
}