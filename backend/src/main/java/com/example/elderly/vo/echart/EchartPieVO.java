package com.example.elderly.vo.echart;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "饼图数据")
    public class EchartPieVO {
    @Schema(description = "姓名")
    private String name;
    private Integer value;
}

