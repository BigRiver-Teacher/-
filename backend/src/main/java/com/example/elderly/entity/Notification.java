package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "通知表")
@Data
public class Notification {
    @TableId(type= IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "通知标题")
    private String title;
    @Schema(description = "通知内容")
    private String content;
    @Schema(description = "通知类型: ALERT/SYSTEM/REMINDER")
    private String type;
    @Schema(description = "跳转链接")
    private String link;
    @Schema(description = "是否已读")
    @TableField("`read`")
    private Boolean read;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
