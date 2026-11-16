package com.example.elderly.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户查询条件")
    public class UserQueryDTO {
    private Integer page=1;
    private Integer size=10;
    @Schema(description = "账号")
    private String account;
    @Schema(description = "角色")
    private String role;
    @Schema(description = "所属社区")
    private String community;
    @Schema(description = "执行状态")
    private String status;
}
