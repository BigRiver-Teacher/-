package com.example.elderly.dto.alert;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "创建告警请求（供视频检测等外部能力写入业务告警）")
public class AlertCreateDTO {

    @Schema(description = "老人姓名")
    @NotBlank(message = "老人姓名不能为空")
    private String elderName;

    @Schema(description = "老人ID")
    private Long elderId;

    @Schema(description = "告警类型，默认 FALL（跌倒）")
    private String alertType;

    @Schema(description = "级别：urgent / important / normal，默认 urgent")
    private String level;

    @Schema(description = "告警描述")
    private String description;

    @Schema(description = "发生时间，缺省取当前时间")
    private LocalDateTime alertTime;

    @Schema(description = "发生地点")
    private String location;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "紧急联系人")
    private String emergencyContact;

    @Schema(description = "建议措施")
    private String suggestedAction;
}
