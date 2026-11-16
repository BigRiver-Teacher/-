package com.example.elderly.entity;

import com.example.elderly.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "推送偏好配置表")
@Data
public class PreferenceConfig extends BaseEntity {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "推送渠道配置")
    private String channels;
    @Schema(description = "级别过滤配置")
    private String levelFiters;
    @Schema(description = "场景配置")
    private String scenes;
    @Schema(description = "静默时段配置")
    private String silentPeriods;
    @Schema(description = "模板配置")
    private String templates;
    @Schema(description = "接收人配置")
    private String receivers;
}
