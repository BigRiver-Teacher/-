package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "步态分析记录表")
@Data
public class GaitRecord {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "老人姓名")
    private String name;
    @Schema(description = "老人ID")
    private Long elderId;
    @Schema(description = "记录日期")
    private LocalDateTime date;
    @Schema(description = "步速(m/s)")
    private BigDecimal speed;
    @Schema(description = "步频(步/分钟)")
    private Integer frequency;
    @Schema(description = "步长(cm)")
    private Integer stepLength;
    @Schema(description = "步态评分")
    private Integer score;
    @Schema(description = "评估结果: NORMAL/ABNORMAL/BORDERLINE")
    private String result;
}
