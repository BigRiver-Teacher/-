package com.example.elderly.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
//用户信息DTO
@Schema(description = "用户信息")
    public class UserInfoVO {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "真实姓名")
    private String realName;
    @Schema(description = "角色")
    private String role;
}
