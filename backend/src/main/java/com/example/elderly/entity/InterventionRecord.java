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
@TableName(value = "intervention_record", autoResultMap = true)
public class InterventionRecord {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "关联干预方案ID")
    private Long interventionId;

    @Schema(description = "执行日期")
    private LocalDateTime executeDate;

    @Schema(description = "老人姓名")
    private String elderName;

    @Schema(description = "老人ID")
    private Long elderId;

    @Schema(description = "执行人")
    private String executor;
    @Schema(description = "执行人账号ID(user.id)")
    private Long executorId;

    @Schema(description = "执行状态：正常/部分执行/未执行/异常")
    private String status;

    @Schema(description = "评分1-5")
    private Integer rating;

    @Schema(description = "备注")
    private String remark;
    @Schema(description = "备注")
    private String note;
    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "附件数组")
    private List<String> attachments;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
