package com.example.elderly.dto.schedule;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "日程处理请求")
    public class ScheduleTaskProcessDTO {
    @Schema(description = "任务状态")
    private String taskStatus;
    @Schema(description = "处理结果")
    private String handleResult;
    @Schema(description = "处理人姓名")
    private String handler;
    @Schema(description = "备注2")
    private String note;
}
