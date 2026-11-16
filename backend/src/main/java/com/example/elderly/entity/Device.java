package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "设备信息表")
@Data
public class Device {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "设备序列号")
    private String deviceSn;
    @Schema(description = "设备名称")
    private String deviceName;
    @Schema(description = "设备类型: WEARABLE/CAMERA/SENSOR/GATEWAY")
    private String deviceType;
    private String status;
    @Schema(description = "电量百分比")
    private Integer battery;
    //固件版本
    @Schema(description = "固件版本")
    private String firmware;
    @Schema(description = "绑定老人姓名")
    private String bindElderName;
    @Schema(description = "绑定老人ID")
    private Long bindElderId;
    @Schema(description = "绑定时间")
    private LocalDateTime bindTime;
    @Schema(description = "最后在线时间")
    private LocalDateTime lastOnlineTime;
    @Schema(description = "安装位置")
    private String location;
    @Schema(description = "安装日期")
    private LocalDateTime installDate;
    @Schema(description = "告警规则配置")
    private String alertRule;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
