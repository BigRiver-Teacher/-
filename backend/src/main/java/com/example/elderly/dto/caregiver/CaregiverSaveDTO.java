package com.example.elderly.dto.caregiver;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "护工保存请求")
    public class CaregiverSaveDTO {
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "职位")
    private String position;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "年龄")
    private Integer age;
    @Schema(description = "地址")
    private String address;
    @Schema(description = "执行状态")
    private String status;
}
