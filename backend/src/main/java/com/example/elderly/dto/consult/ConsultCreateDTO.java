package com.example.elderly.dto.consult;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "新建咨询请求")
    public class ConsultCreateDTO {
    @Schema(description = "老人姓名")
    private String elderName;
    @Schema(description = "咨询类型")
    private String type;
    @Schema(description = "紧急程度")
    private String urgency;
    @Schema(description = "咨询内容")
    private String content;
}
