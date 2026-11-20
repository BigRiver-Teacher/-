package com.example.elderly.entity;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "护工信息表")
@Data
public class Caregiver {
    @Schema(description = "主键ID")
    private Long id;
    @NotBlank(message = "姓名不能为空")
    @Schema(description = "姓名")
    private String name;
    @NotBlank(message = "年龄不能为空")
    @Schema(description = "年龄")
    private Integer age;
    @NotBlank(message = "职位不能为空")
    @Schema(description = "职位")
    private String position;
    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号")
    private String phone;
    @NotBlank(message = "地址不能为空")
    @Schema(description = "地址")
    private String address;
    @Schema(description = "关联账号ID(user.id)")
    private Long userId;
    @Schema(description = "所属社区")
    private String community;
    @NotBlank(message = "状态不能为空")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
