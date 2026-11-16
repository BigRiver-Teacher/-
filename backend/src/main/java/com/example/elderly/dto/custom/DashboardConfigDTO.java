package com.example.elderly.dto.custom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "看板配置")
    public class DashboardConfigDTO {
    @Schema(description = "卡片")
    private List<DashboardCard> cards;
    @Data
    public static class DashboardCard{
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "图标")
    private String icon;
    @Schema(description = "咨询类型")
    private String type;
    @Schema(description = "是否可见")
    private Boolean visible;
        private Integer colspan;
        private String color;
        private String previewValue;
    }
}
