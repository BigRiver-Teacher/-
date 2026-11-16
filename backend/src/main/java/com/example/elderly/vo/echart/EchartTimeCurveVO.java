package com.example.elderly.vo.echart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 时间曲线 — 字段命名与前端 src/api/stats.ts 的 EchartTimeCurveVO 保持一致
 */
@Data
@Schema(description = "时间曲线数据")
    public class EchartTimeCurveVO {
    private List<String> categories;
    private List<SeriesItem> series;

    @Data
    public static class SeriesItem {
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "数据")
    private List<Integer> data;
    }
}
