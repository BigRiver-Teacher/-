package com.example.elderly.dto.schedule;

import com.example.elderly.dto.PageQueryDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "康养日程查询条件")
    public class ScheduleTaskQueryDTO extends PageQueryDTO {
    @Schema(description = "记录日期")
    private String date;
    @Schema(description = "任务类型")
    private String taskType;
    @Schema(description = "优先级")
    private String priority;
    @Schema(description = "任务状态")
    private String taskStatus;
    @Schema(description = "老人姓名")
    private String elderName;
}
