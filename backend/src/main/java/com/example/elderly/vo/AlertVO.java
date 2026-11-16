package com.example.elderly.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 告警列表 VO — 字段命名与前端 src/api/alert.ts 的 AlertVO 保持一致
 */
@Data
@Schema(description = "告警信息")
    public class AlertVO {
    @Schema(description = "主键ID")
    private Long id;
    /** 老人姓名 */
    private String elderlyName;
    /** 老人ID */
    private Long elderlyId;
    /** 告警类型 */
    private String warningType;
    /** 优先级 */
    @Schema(description = "优先级")
    private String priority;
    /** 告警描述 */
    @Schema(description = "告警描述")
    private String description;
    /** 告警时间 */
    @Schema(description = "时间")
    private LocalDateTime time;
    /** 发生位置 */
    @Schema(description = "发生位置")
    private String location;
    /** 处理状态 */
    @Schema(description = "执行状态")
    private String status;
    /** 处理人 */
    @Schema(description = "处理人姓名")
    private String handler;
    /** 处理时间 */
    @Schema(description = "处理时间")
    private LocalDateTime handleTime;
    /** 联系电话 */
    @Schema(description = "联系电话")
    private String contactPhone;
    /** 紧急联系人 */
    @Schema(description = "紧急联系人")
    private String emergencyContact;
    /** 建议措施 */
    @Schema(description = "建议措施")
    private String suggestedAction;
}
