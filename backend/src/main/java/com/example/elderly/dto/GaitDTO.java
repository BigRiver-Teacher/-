package com.example.elderly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "步态记录请求")
    public class GaitDTO {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "老人ID")
    private Long elderId;
    @Schema(description = "记录日期")
    private LocalDateTime date;
    @Schema(description = "步速(m/s)")
    private BigDecimal speed;
    @Schema(description = "步频(步/分钟)")
    private Integer frequency;
    @Schema(description = "步长(cm)")
    private Integer stepLength;
    @Schema(description = "步态评分")
    private Integer score;
    @Schema(description = "评估结果")
    private String result;
}
