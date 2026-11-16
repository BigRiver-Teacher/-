package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.elderly.common.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "推送日志表")
@Data
public class PushLog extends BaseEntity {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "老人姓名")
    private String elderName;
    @Schema(description = "家属电话")
    private String familyPhone;
    @Schema(description = "推送渠道: SMS/WECHAT/APP/PUSH")
    private String channel;
    @Schema(description = "推送场景")
    private String scence;
    @Schema(description = "推送内容(简要)")
    private String content;
    @Schema(description = "推送内容(完整)")
    private String fullContent;
    private String status;
    @Schema(description = "推送时间")
    private LocalDateTime time;
    @Schema(description = "耗时(ms)")
    private Long duration;
    @Schema(description = "重试次数")
    private Integer retryCount;
    @Schema(description = "响应结果")
    private String response;
    @Schema(description = "重试历史")
    private String retryHistory;
}
