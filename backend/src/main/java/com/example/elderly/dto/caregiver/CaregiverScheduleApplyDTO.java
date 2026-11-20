package com.example.elderly.dto.caregiver;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "陪护请假/换班申请")
@Data
public class CaregiverScheduleApplyDTO {
    @NotBlank(message = "申请类型不能为空")
    @Schema(description = "申请类型: LEAVE请假/SHIFT换班")
    private String applyType;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "申请原因")
    private String reason;
}
