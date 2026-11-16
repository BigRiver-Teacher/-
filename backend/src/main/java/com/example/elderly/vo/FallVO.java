package com.example.elderly.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 跌倒记录 VO — 字段命名与前端 src/api/fall.ts 的 FallVO 保持一致
 */
@Data
@Schema(description = "跌倒记录信息")
    public class FallVO {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "老人姓名")
    private String elderName;
    /** 跌倒时间 */
    @Schema(description = "记录日期")
    private LocalDateTime date;
    @Schema(description = "发生位置")
    private String location;
    @Schema(description = "原因分析")
    private String cause;
    @Schema(description = "严重程度")
    private String severity;
    /** 恢复状态 */
    @Schema(description = "执行状态")
    private String status;
    @Schema(description = "老人ID")
    private Long elderId;
    @Schema(description = "处理人姓名")
    private String handler;
    @Schema(description = "处理时间")
    private LocalDateTime handleTime;
    /** 处理措施 */
    @Schema(description = "备注")
    private String remark;
}
