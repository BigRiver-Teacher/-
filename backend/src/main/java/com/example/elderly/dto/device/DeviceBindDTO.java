package com.example.elderly.dto.device;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "设备绑定请求")
    public class DeviceBindDTO {
    @Schema(description = "设备ID")
    private String deviceId;
    @Schema(description = "老人ID")
    private String elderId;
}
