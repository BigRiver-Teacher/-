package com.example.elderly.vo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "模型参数版本")
    public class ModelParamVersionVO {
    @Schema(description = "版本号")
    private String version;
    @Schema(description = "时间")
    private LocalDateTime time;
    private String user;
    @Schema(description = "告警描述")
    private String description;
}
