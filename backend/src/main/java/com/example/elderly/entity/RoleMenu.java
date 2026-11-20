package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "角色-菜单权限映射表")
@Data
@TableName("role_menu")
public class RoleMenu {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "角色: admin/expert/caregiver/supervisor/elder")
    private String role;
    @Schema(description = "菜单ID(关联menu.id)")
    private Long menuId;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
