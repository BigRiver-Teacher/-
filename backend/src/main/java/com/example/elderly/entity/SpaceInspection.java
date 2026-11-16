package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "空间安全检查记录")
public class SpaceInspection {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "空间ID")
    private Long spaceId;
    @Schema(description = "检查日期")
    private LocalDate date;
    @Schema(description = "检查结果 合格/不合格/需整改")
    private String result;
    @Schema(description = "检查人")
    private String inspector;
    @Schema(description = "检查类型 月度/季度/专项/应急")
    private String type;
    @Schema(description = "问题描述")
    private String issue;
    @Schema(description = "整改状态")
    private String repairStatus;
    @Schema(description = "整改期限")
    private LocalDate deadline;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
