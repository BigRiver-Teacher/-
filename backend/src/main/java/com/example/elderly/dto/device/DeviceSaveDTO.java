package com.example.elderly.dto.device;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "新增设备请求")
    public class DeviceSaveDTO {
    @Schema(description = "设备序列号")
    private String deviceSn;
    @Schema(description = "设备名称")
    private String deviceName;
    @Schema(description = "设备类型")
    private String deviceType;
    @Schema(description = "发生位置")
    private String location;
    @Schema(description = "安装日期")
    private LocalDateTime installDate;
    @Schema(description = "告警规则配置")
    private String alertRule;
}
