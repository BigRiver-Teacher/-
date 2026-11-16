package com.example.elderly.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
//通知列表
@Schema(description = "NotificationDTO")
    public class NotificationDTO {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "职称")
    private String title;
    @Schema(description = "咨询内容")
    private String content;
    @Schema(description = "咨询类型")
    private String type;
    @Schema(description = "时间")
    private String time;
    @Schema(description = "是否已读")
    private Boolean read;
    @Schema(description = "跳转链接")
    private String link;
}
