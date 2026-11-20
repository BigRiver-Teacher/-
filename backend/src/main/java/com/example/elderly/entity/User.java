package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户表")
@Data
public class User {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "密码(MD5)")
    private String password;
    @Schema(description = "真实姓名")
    private String realName;
    @Schema(description = "角色: admin/expert/caregiver/supervisor/elder/staff")
    private String role;
    private String status;
    @Schema(description = "所属社区")
    private String community;
    @Schema(description = "账号")
    private String account;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLogin;
    @TableLogic
    @Schema(description = "逻辑删除: 0=未删除, 1=已删除")
    private Integer deleted;
    @TableField(fill= FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @TableField(fill= FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
