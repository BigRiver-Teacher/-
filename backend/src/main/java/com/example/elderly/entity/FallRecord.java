package com.example.elderly.entity;

import com.example.elderly.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.PrimitiveIterator;

@Schema(description = "跌倒记录表")
@Data
public class FallRecord extends BaseEntity {
    @Schema(description = "老人ID")
    private Long elderId;
    @Schema(description = "老人姓名")
    private String elderName;
    @Schema(description = "跌倒位置")
    private String location;
    @Schema(description = "跌倒时间")
    private LocalDateTime fallTime;
    @Schema(description = "原因分析")
    private String cause;
    @Schema(description = "受伤部位")
    private String injuryPart;
    @Schema(description = "严重程度: MILD/MODERATE/SEVERE")
    private String severity;
    @Schema(description = "处理措施")
    private String treatment;
    @Schema(description = "恢复状态")
    private String recoverStatus;
    @Schema(description = "处理人")
    private String handler;
}
