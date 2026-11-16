package com.example.elderly.dto.Expert;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "编辑专家请求")
    public class ExpertUpdateDTO {
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "职称")
    private String title;
    @Schema(description = "专长领域")
    private String specialty;
    @Schema(description = "所属机构")
    private String institution;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "执行状态")
    private String status;
    @Schema(description = "评分(1-5)")
    private BigDecimal rating;
}
