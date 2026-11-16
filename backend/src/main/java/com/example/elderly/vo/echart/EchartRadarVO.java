package com.example.elderly.vo.echart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "雷达图数据")
    public class EchartRadarVO {
    private List<String> dimensions;
    private List<Integer>before;
    private List<Integer>after;
}
