package com.example.elderly.dto.preference;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "推送偏好配置")
    public class PreferenceConfigDTO {
    @Schema(description = "推送渠道配置")
    private List<ChannelItem> channels;
    private List<LevelFilterItem> levelFilters;
    @Schema(description = "接收人配置")
    private List<ReceiverItem> receivers;
    @Schema(description = "场景配置")
    private List<SceneItem> scenes;
    @Schema(description = "模板配置")
    private List<TemplateItem> templates;
    @Schema(description = "静默时段配置")
    private List<SilentPeriodItem> silentPeriods;

    @Data
    public static class ChannelItem {
    @Schema(description = "方案编号")
    private String code;
    @Schema(description = "姓名")
    private String name;
        private Boolean enable;
    }

    @Data
    public static class LevelFilterItem {
    @Schema(description = "方案编号")
    private String code;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "推送渠道配置")
    private List<String> channels;
    }


    @Data
    public static class ReceiverItem {
        private String enderName;
        private String managerName;
        private String managerPhone;
    @Schema(description = "家属电话")
    private String familyPhone;
        private String familyName;
        private String backupName;
        private String backupPhone;
    }

    @Data
    public static class SceneItem {
    @Schema(description = "姓名")
    private String name;
        private Boolean enable;
    @Schema(description = "推送渠道配置")
    private List<String> channels;
        private Integer delayMin;
        private Boolean silentMode;
    }

    @Data
    public static class SilentPeriodItem {
        private String start;
        private String end;
    @Schema(description = "姓名")
    private String name;
    }

    @Data
    public static class TemplateItem {
    @Schema(description = "场景")
    private String scene;
    @Schema(description = "咨询内容")
    private String content;
    }

}


