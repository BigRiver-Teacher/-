package com.example.elderly.dto.custom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "移动端配置")
    public class MobileConfigDTO {
    @Schema(description = "标签页列表")
    private List<MobileTab> tabList;
    @Schema(description = "菜单列表")
    private List<MobileMenu> menuList;
    private List<MobileFeature>featureList;
    @Schema(description = "主题色")
    private MobileTheme themeColors;
    @Schema(description = "推送设置")
    private MobilePush pushSetting;
    @Data
    public static class MobileTab{
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "图标")
    private String icon;
    @Schema(description = "路由路径")
    private String path;
    @Schema(description = "是否可见")
    private Boolean visible;
    }
    @Data
    public static class MobileMenu{
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "图标")
    private String icon;
    @Schema(description = "是否可见")
    private Boolean visible;
    }
    @Data
    public static class MobileFeature{
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "图标")
    private String icon;
    @Schema(description = "是否可见")
    private Boolean visible;
        private String badge;
    }
    @Data
    public static class MobileTheme{
        private String primary;
        private String secondary;
        private String background;
    }
    @Data
    public static class MobilePush{
        private Boolean enabled;
        private Boolean sound;
        private Boolean banner;
    @Schema(description = "咨询内容")
    private String content;
    }
}
