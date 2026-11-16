package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import com.example.elderly.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

@Schema(description = "告警记录表")
@Data
public class Alert extends BaseEntity {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "老人姓名")

    private String elderName;
    @Schema(description = "老人ID")

    private Long elderId;
    @Schema(description = "告警类型: FALL/ABNORMAL_GAIT/DEVICE_OFFLINE/EMERGENCY")

    private String alertType;
    /** 告警级别 AlertLevelEnum */
    @Schema(description = "告警级别: CRITICAL/HIGH/MEDIUM/LOW")
    private String level;
    /** 告警描述 */
    @Schema(description = "告警描述")
    private String description;
    @Schema(description = "告警时间")

    private LocalDateTime alertTime;
    @Schema(description = "发生位置")

    private String location;
    /** 处理状态 AlertHandleStatusEnum */
    private String status;
    @Schema(description = "处理人ID")

    private Long handlerId;
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
    @Schema(description = "建议措施")

    private String suggestedAction;
    @Schema(description = "是否已读: 0=未读, 1=已读")

    @TableField("`read`")
    private Integer read;
    /** 处理备注 */
    @Schema(description = "处理备注")
    private String handleNote;
}