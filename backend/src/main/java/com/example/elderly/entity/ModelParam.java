package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import org.apache.ibatis.type.JdbcType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "model_param", autoResultMap = true)
public class ModelParam {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @TableField(typeHandler = JacksonTypeHandler.class, jdbcType = JdbcType.VARCHAR)
    @Schema(description = "检测参数 {sensitivity,acceleration,angle,timeWindow,confidence}")
    private DetectionParam detection;
    @TableField(typeHandler = JacksonTypeHandler.class, jdbcType = JdbcType.VARCHAR)
    @Schema(description = "评估参数 {gaitThreshold,activityThreshold,riskCycle}")
    private EvaluationParam evaluation;
    @TableField(typeHandler = JacksonTypeHandler.class, jdbcType = JdbcType.VARCHAR)
    @Schema(description = "告警参数 {delay,cooldown,threshold}")
    private AlertParam alert;
    @TableField(typeHandler = JacksonTypeHandler.class, jdbcType = JdbcType.VARCHAR)
    @Schema(description = "设备参数 {sensorFrequency,cameraFps,offlineTimeout}")
    private DeviceParam device;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;


    @Data
    public static class DetectionParam{
        private Integer sensitivity;
        private Double acceleration;
        private Double angle;
        private Integer timeWindow;
        private Integer confidence;
    }

    @Data
    public static class EvaluationParam{
        private Integer gaitThreshold;
        private Integer activityThreshold;
        private Integer riskCycle;
    }

    @Data
    public static class AlertParam{
        private Integer delay;
        private Integer cooldown;
        private Integer threshold;
    }
    @Data
    public static class DeviceParam{
        private Integer sensorFrequency;
        private Integer cameraFps;
        private Integer offlineTimeout;
    }
}
