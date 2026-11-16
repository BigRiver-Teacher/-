package com.example.elderly.entity;

import com.example.elderly.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Schema(description = "社区联动记录表")
@Data
public class CommunityRecord extends BaseEntity {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "记录编号")
    private String code;
    @Schema(description = "老人姓名")
    private String elderName;
    @Schema(description = "告警类型")
    private String alertType;
    @Schema(description = "联动动作")
    private String action;
    @Schema(description = "处理人")
    private String handler;
    @Schema(description = "处理人ID")
    private String handlerId;
    @Schema(description = "处理结果")
    private String result;
    @Schema(description = "审核状态")
    private String reviewStatus;
    @Schema(description = "时间")
    private LocalDateTime time;
    @Schema(description = "完整时间")
    private LocalDateTime fullTime;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "附件")
    private String attachments;
    @Schema(description = "所属社区")
    private String community;
    @Schema(description = "关联告警编号")
    private String alertCode;
    @Schema(description = "审核意见")
    private String review;
    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;
    @Schema(description = "驳回原因")
    private String rejectReason;
}
