package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.example.elderly.common.OperateStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "操作日志表")
@Data
public class OperationLog {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "操作时间")

    private LocalDateTime operateTime;
    @Schema(description = "操作人")

    private String operator;
    @Schema(description = "操作类型: INSERT/UPDATE/DELETE/EXPORT")

    private String operateType;
    @Schema(description = "操作模块")

    private String module;
    @Schema(description = "操作内容")

    private String content;
    @Schema(description = "IP地址")

    private String ip;
    @Schema(description = "操作状态: SUCCESS/FAILED")

    private OperateStatusEnum status;
    @Schema(description = "错误信息")

    private String errorMsg;
}
