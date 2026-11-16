package com.example.elderly.dto.echart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 雷达图 — 字段命名与前端 src/api/stats.ts 的 EchartRadarDTO 保持一致
 */
@Data
@Schema(description = "雷达图数据")
    public class EchartRadarDTO {
    private List<Indicator> indicator;
    private List<RadarSeries> series;

    @Data
    public static class Indicator {
    @Schema(description = "姓名")
    private String name;
        private Integer max;
    }

    @Data
    public static class RadarSeries {
    @Schema(description = "姓名")
    private String name;
        private List<Integer> value;
    }
}
