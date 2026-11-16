package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "菜单权限表")
@Data
public class Menu {
    @TableId(type= IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "菜单名称")
    private String name;
    @Schema(description = "路由路径")
    private String path;
    @Schema(description = "图标")
    private String icon;
    @Schema(description = "父菜单ID(0=顶级)")
    private Long parentId;
    @Schema(description = "是否可见: 0=隐藏, 1=显示")
    private Boolean visible;
    @Schema(description = "排序号")
    private Integer sort;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    @TableField(exist = false)
    private List<Menu> children;
}
