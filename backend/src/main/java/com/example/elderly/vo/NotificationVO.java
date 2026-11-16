package com.example.elderly.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Schema(description = "通知信息")
    public class NotificationVO {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "职称")
    private String title;
    @Schema(description = "咨询内容")
    private String content;
    @Schema(description = "咨询类型")
    private String type;
    @Schema(description = "时间")
    private LocalDateTime time;
    @Schema(description = "是否已读")
    private Boolean read;
    @Schema(description = "跳转链接")
    private String link;
}
