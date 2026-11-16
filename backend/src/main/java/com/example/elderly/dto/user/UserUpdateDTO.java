package com.example.elderly.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "UserUpdateDTO")
    public class UserUpdateDTO {
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "角色")
    private String role;
    @Schema(description = "所属社区")
    private String community;
    @Schema(description = "执行状态")
    private String status;
}
