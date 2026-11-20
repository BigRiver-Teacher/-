package com.example.elderly.entity;

import com.example.elderly.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HealthNote extends BaseEntity {
    @Schema(description = "老人ID")
    private Long elderId;
    @Schema(description = "老人姓名")
    private String elderName;
    @Schema(description = "记录时间")
    private LocalDateTime recordTime;
    @Schema(description = "记录人")
    private String recorder;
    @Schema(description = "记录人账号ID(user.id)")
    private Long recorderId;
    @Schema(description = "备注类型")
    private String noteType;
    @Schema(description = "备注内容")
    private String content;
    @Schema(description = "重要程度:普通/重要/紧急")
    private String importance;
    @Schema(description = "是否已处理:待跟进/跟进中/处理完成")
    private String fallowStatus;
}
