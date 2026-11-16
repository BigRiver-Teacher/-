package com.example.elderly.dto.echart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.PipedReader;
import java.util.List;

@Data
@Schema(description = "趋势数据")
    public class EchartTrendDTO {
    private List<String> categories;
    private List<EchartSeries>series;
    @Data
    public static class EchartSeries{
    @Schema(description = "姓名")
    private String name;
        private List<Integer>data;
    }
}
