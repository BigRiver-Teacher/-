package com.example.elderly.vo.echart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 专家统计 — 字段命名与前端 src/api/stats.ts 的 EchartStatVO 保持一致
 */
@Data
@Schema(description = "统计结果")
    public class EchartStatVO {
    private Integer totalExperts;
    private Integer totalConsultations;
    private Double avgSatisfaction;
    private Integer pendingConsultations;
}
