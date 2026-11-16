package com.example.elderly.dto.echart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设备统计 — 字段命名与前端 src/api/stats.ts 的 EchartDeviceStatDTO 保持一致
 */
@Data
@Schema(description = "设备统计")
    public class EchartDeviceStatDTO {
    private Integer totalDevices;
    private Integer onlineDevices;
    private Integer offlineDevices;
    private Integer faultDevices;
    private Integer bindRate;
    private Integer todayAlertCount;
}
