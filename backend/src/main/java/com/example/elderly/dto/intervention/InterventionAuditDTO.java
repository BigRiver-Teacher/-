package com.example.elderly.dto.intervention;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "审核干预方案参数")
public class InterventionAuditDTO {
    @NotBlank(message = "审核状态不能为空")
    @Schema(description = "审核状态 approved/rejected")
    private String status;
    @Schema(description = "驳回备注，驳回时必填")
    private String reviewNote;
}
