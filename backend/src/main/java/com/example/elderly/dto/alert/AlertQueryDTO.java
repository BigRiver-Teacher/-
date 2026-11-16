package com.example.elderly.dto.alert;

import com.example.elderly.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Schema(description = "告警查询条件")
    public class AlertQueryDTO extends PageQueryDTO {
    @Schema(description = "告警类型")
    private String alertType;
    @Schema(description = "告警级别")
    private String level;
    @Schema(description = "执行状态")
    private String status;
    @Schema(description = "老人姓名")
    private String elderName;
}
