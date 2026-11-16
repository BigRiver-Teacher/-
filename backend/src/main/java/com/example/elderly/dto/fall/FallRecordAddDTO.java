package com.example.elderly.dto.fall;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "新增跌倒记录请求")
    public class FallRecordAddDTO {
    @Schema(description = "老人ID")
    private Long elderId;
    @Schema(description = "老人姓名")
    private String elderName;
    @Schema(description = "发生位置")
    private String location;
    @Schema(description = "跌倒时间")
    private LocalDateTime fallTime;
    @Schema(description = "原因分析")
    private String cause;
    @Schema(description = "受伤部位")
    private String injuryPart;
    @Schema(description = "严重程度")
    private String severity;
    @Schema(description = "处理措施")
    private String treatment;
    @Schema(description = "恢复状态")
    private String recoverStatus;
    @Schema(description = "处理人姓名")
    private String handler;
}
