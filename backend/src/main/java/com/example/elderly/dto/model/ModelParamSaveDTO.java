package com.example.elderly.dto.model;

import com.example.elderly.entity.ModelParam;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "模型参数保存请求")
    public class ModelParamSaveDTO {
    @Schema(description = "检测参数 {sensitivity,acceleration,angle,timeWindow,confidence}")
    private ModelParam.DetectionParam detection;
    @Schema(description = "告警参数 {delay,cooldown,threshold}")
    private ModelParam.AlertParam alert;
    @Schema(description = "设备参数 {sensorFrequency,cameraFps,offlineTimeout}")
    private ModelParam.DeviceParam device;
    @Schema(description = "评估参数 {gaitThreshold,activityThreshold,riskCycle}")
    private ModelParam.EvaluationParam evaluation;
    @Schema(description = "告警描述")
    private String description;
}
