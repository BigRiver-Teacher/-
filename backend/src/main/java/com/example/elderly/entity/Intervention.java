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
@TableName(value = "intervention", autoResultMap = true)
@Schema(description = "干预方案")
public class Intervention {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "方案编号")
    private String code;
    @Schema(description = "方案名称")
    private String name;
    @Schema(description = "干预类型：运动康复/营养干预/心理疏导/用药管理/日常护理/综合干预")
    private String type;
    @Schema(description = "优先级 urgent/important/normal")
    private String priority;
    @Schema(description = "方案状态 draft/pending/approved/ongoing/completed/terminated")
    private String status;
    @Schema(description = "负责人")
    private String leader;
    @Schema(description = "适用对象")
    private String targetGroup;
    @Schema(description = "执行周期(周)")
    private Integer totalWeeks;
    @Schema(description = "当前周数")
    private Integer currentWeek;
    @Schema(description = "执行频率")
    private String frequency;
    @Schema(description = "开始日期")
    private LocalDateTime startDate;
    @Schema(description = "预计完成日期")
    private LocalDateTime endDate;
    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "关联老人ID数组")
    private List<Long> associatedElders;
    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "干预目标数组")
    private List<String> goals;
    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "具体措施数组")
    private List<String> measures;
    @Schema(description = "注意事项")
    private String notes;
    @Schema(description = "总体评分 1-5")
    private Integer rating;
    @Schema(description = "平均执行评分")
    private Double avgExecRating;
    @Schema(description = "执行进度0-100")
    private Integer progress;
    @Schema(description = "已执行次数")
    private Integer executedCount;
    @Schema(description = "总执行次数")
    private Integer totalCount;
    @Schema(description = "异常次数")
    private Integer abnormalCount;
    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "执行记录数组")
    private List<InterventionRecord> records;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
