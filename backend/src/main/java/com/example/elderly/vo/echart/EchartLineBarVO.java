package com.example.elderly.vo.echart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 线柱图 — 字段命名与前端 src/api/stats.ts 的 EchartLineBarVO 保持一致
 */
@Data
@Schema(description = "线柱图数据")
    public class EchartLineBarVO {
    private List<String> categories;
    private List<SeriesItem> lineSeries;
    private List<SeriesItem> barSeries;

    @Data
    public static class SeriesItem {
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "数据")
    private List<Integer> data;
    }
}
