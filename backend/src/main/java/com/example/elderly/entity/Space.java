package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "space", autoResultMap = true)
public class Space {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "空间编号")
    private String code;
    @Schema(description = "空间名称")
    private String name;
    @Schema(description = "楼栋")
    private String building;
    @Schema(description = "楼层")
    private String floor;
    @Schema(description = "户型")
    private String layout;
    @Schema(description = "面积㎡")
    private Double area;
    @Schema(description = "入住状态 occupied/vacant")
    private String occupancyStatus;
    @Schema(description = "入住老人姓名")
    private String elderName;
    @Schema(description = "入住老人ID")
    private Long elderId;
    @Schema(description = "入住时间")
    private LocalDateTime moveInDate;
    @Schema(description = "上次入住时间(空置时)")
    private LocalDateTime lastOccupied;
    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "适老化配置数组")
    private List<String> facilities;
    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "风险因素数组")
    private List<String> risks;
    @Schema(description = "空间状态 normal/renovating/repair/inactive")
    private String status;
    @Schema(description = "安全评级 A/B/C/D")
    private String safetyRating;
    @Schema(description = "最近检查日期")
    private LocalDateTime lastInspection;
    @Schema(description = "最近检查结果 合格/不合格/需整改")
    private String lastInspectionResult;
    @Schema(description = "下次检查日期")
    private LocalDateTime nextInspection;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
