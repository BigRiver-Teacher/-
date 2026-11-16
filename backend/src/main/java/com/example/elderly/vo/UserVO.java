package com.example.elderly.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户信息")
    public class UserVO {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "账号")
    private String account;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "角色")
    private String role;
    @Schema(description = "所属社区")
    private String community;
    @Schema(description = "执行状态")
    private String status;
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLogin;
}
