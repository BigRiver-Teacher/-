package com.example.elderly.dto.alert;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "告警处理请求")
    public class AlertProcessDTO {
    @Schema(description = "处理人姓名")
    private String handler;
    @Schema(description = "处理备注")
    private String handleNote;
}
