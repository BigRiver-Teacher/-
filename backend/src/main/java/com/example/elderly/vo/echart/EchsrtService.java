package com.example.elderly.vo.echart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "EchsrtService")
    public class EchsrtService {
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "数据")
    private List<Integer> data;
}
