package com.example.elderly.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "老人档案请求")
    public class ElderDTO {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "性别")
    private String gender;
    @Schema(description = "年龄")
    private Integer age;
    @Schema(description = "身份证号")
    private String idCard;
    @Schema(description = "出生日期")
    private LocalDateTime birthDate;
    @Schema(description = "民族")
    private String ethnicity;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "地址")
    private String address;
    @Schema(description = "居住类型")
    private String residenceType;
    @Schema(description = "备注")
    private String remark;
}
