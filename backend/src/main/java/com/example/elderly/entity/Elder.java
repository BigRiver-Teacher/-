package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Schema(description = "老人信息表")
@Data
public class Elder {
    @TableId(type= IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "性别: MALE/FEMALE")
    private String gender;
    @Schema(description = "年龄")
    private Integer age;
    @Schema(description = "身份证号")
    private String idCard;
    @Schema(description = "出生日期")
    private LocalDateTime birthDate;
    @Schema(description = "民族")
    private String ethnicity;
    @Schema(description = "联系电话")
    private String phone;
    @Schema(description = "地址")
    private String address;
    @Schema(description = "居住类型")
    private String residenceType;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "是否绑定设备")
    private Boolean deviceBound;
    @Schema(description = "逻辑删除")
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
