package com.example.elderly.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "设备信息")
    public class DeviceVO {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "设备序列号")
    private String deviceSn;
    @Schema(description = "设备名称")
    private String deviceName;
    @Schema(description = "设备类型")
    private String deviceType;
    @Schema(description = "执行状态")
    private String status;
    @Schema(description = "电量百分比")
    private Integer battery;
    @Schema(description = "固件版本")
    private String firmware;
    @Schema(description = "绑定老人")
    private String bindElder;
    @Schema(description = "绑定老人ID")
    private Long bindElderId;
    @Schema(description = "绑定时间")
    private LocalDateTime bindTime;
    @Schema(description = "最后在线时间")
    private LocalDateTime lastOnline;
    @Schema(description = "发生位置")
    private String location;
    @Schema(description = "安装日期")
    private LocalDateTime installDate;
    @Schema(description = "告警规则配置")
    private String alertRule;
}
