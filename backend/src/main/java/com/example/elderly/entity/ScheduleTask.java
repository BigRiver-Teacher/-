package com.example.elderly.entity;

import com.example.elderly.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleTask extends BaseEntity {
    @Schema(description = "任务编号")
    private String taskCode;
    @Schema(description = "任务类型")
    private String taskType;
    @Schema(description = "老人ID")
    private Long elderId;
    @Schema(description = "老人姓名")
    private String elderName;
    @Schema(description = "位置")
    private String location;
    @Schema(description = "优先级")
    private String priority;
    @Schema(description = "任务状态")
    private String taskStatus;
    @Schema(description = "触发原因")
    private String triggerReson;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "关联告警编号")
    private String relateAlertCode;
    @Schema(description = "关联设备序列号")
    private String relateDeviceSn;
    @Schema(description = "处理结果")
    private String handleResult;
    @Schema(description = "处理人")
    private String handler;
    @Schema(description = "处理人账号ID(user.id)")
    private Long handlerId;
    @Schema(description = "完成时间")
    private LocalDateTime completeTime;
    @Schema(description = "备注")
    private String note;
    @Schema(description = "任务日期")
    private LocalDateTime taskDate;
}
