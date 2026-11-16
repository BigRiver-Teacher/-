package com.example.elderly.dto.custom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "侧边栏配置")
    public class SidebarConfigDTO {
    @Schema(description = "分组")
    private List<SidebarGroup> groups;
    @Schema(description = "配置项")
    private List<SidebarItem> items;
    @Data
    public static class SidebarGroup {
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "主键ID")
    private String id;
    }
    @Data
    public static class SidebarItem{
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "图标")
    private String icon;
    @Schema(description = "分组")
    private String group;
    @Schema(description = "是否可见")
    private Boolean visible;
    }
}
