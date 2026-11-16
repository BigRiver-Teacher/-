package com.example.elderly.vo.model;

import com.example.elderly.entity.ModelParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "模型参数")
    public class ModelParamVO {
    @Schema(description = "检测参数 {sensitivity,acceleration,angle,timeWindow,confidence}")
    private ModelParam.DetectionParam detection;
    @Schema(description = "评估参数 {gaitThreshold,activityThreshold,riskCycle}")
    private ModelParam.EvaluationParam evaluation;
    @Schema(description = "告警参数 {delay,cooldown,threshold}")
    private ModelParam.AlertParam alert;
    @Schema(description = "设备参数 {sensorFrequency,cameraFps,offlineTimeout}")
    private ModelParam.DeviceParam device;
    @Schema(description = "版本号")
    private VersionInfo version;

    @Data
    public static  class VersionInfo{
        private String current;
    @Schema(description = "执行状态")
    private String status;
        private LocalDateTime lastUpdate;
        private Integer totalVersions;
    }
}
