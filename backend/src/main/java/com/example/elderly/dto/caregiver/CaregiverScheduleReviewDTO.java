package com.example.elderly.dto.caregiver;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "陪护请假/换班审批")
@Data
public class CaregiverScheduleReviewDTO {
    @NotBlank(message = "审批状态不能为空")
    @Schema(description = "审批状态: APPROVED/REJECTED")
    private String status;

    @Schema(description = "审批意见")
    private String comment;
}
