package com.example.elderly.dto;

import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分页查询条件")
    public class PageQueryDTO {
    private Long page=1L;
    private Long size=10L;
    @Schema(description = "咨询类型")
    private String type;
    @Schema(description = "操作模块")
    private String module;
    @Schema(description = "开始日期")
    private String startDate;
    @Schema(description = "预计完成日期")
    private String endDate;
}
