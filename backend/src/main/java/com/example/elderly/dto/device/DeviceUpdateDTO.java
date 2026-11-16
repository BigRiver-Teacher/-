package com.example.elderly.dto.device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "编辑设备请求")
    public class DeviceUpdateDTO {
    @Schema(description = "设备名称")
    private String deviceName;
    @Schema(description = "设备类型")
    private String deviceType;
    @Schema(description = "告警规则配置")
    private String alertRule;
    @Schema(description = "发生位置")
    private String location;
}
