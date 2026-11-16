package com.example.elderly.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "搜索结果")
    public class SearchVO {
    //模块标识
    @Schema(description = "操作模块")
    private String module;
    //标题展示文本
    @Schema(description = "职称")
    private String title;
    //描述
    @Schema(description = "描述")
    private String desc;
    //路由
    private String route;
    //业务ID
    private Long businessId;
}
