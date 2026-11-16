package com.example.elderly.dto.echart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 设备故障统计 — 字段命名与前端 src/api/stats.ts 的 EchartDeviceFaultDTO 保持一致
 */
@Data
@Schema(description = "设备故障统计")
    public class EchartDeviceFaultDTO {
    private List<String> categories;
    private List<EchartTrendDTO.EchartSeries> series;
}
