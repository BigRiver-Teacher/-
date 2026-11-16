package com.example.elderly.dto.user;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户保存请求")
    public class UserSaveDTO {
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
    @Schema(description = "密码(MD5)")
    private String password;
    @Schema(description = "执行状态")
    private String status;
}
