package com.example.elderly.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
//动态菜单
@Schema(description = "MenuDTO")
    public class MenuDTO {
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "路由路径")
    private String path;
    @Schema(description = "图标")
    private String icon;
    @Schema(description = "父菜单ID(0=顶级)")
    private Long parentId;
    @Schema(description = "排序号")
    private Integer sort;
    @Schema(description = "是否可见")
    private Boolean visible;
    @Schema(description = "子菜单")
    private List<MenuDTO> children;
}
