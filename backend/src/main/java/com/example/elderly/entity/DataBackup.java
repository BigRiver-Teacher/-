package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "数据备份记录表")
@Data
public class DataBackup {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "文件名")
    private String fileName;
    @Schema(description = "备份时间")
    private LocalDateTime backupTime;
    /** 备份类型: AUTO/MANUAL */
    @Schema(description = "备份类型: AUTO/MANUAL")
    private String type;
    @Schema(description = "文件大小")
    private String fileSize;
    /** 状态: SUCCESS/FAILED/IN_PROGRESS */
    @Schema(description = "状态: SUCCESS/FAILED/IN_PROGRESS")
    private String status;
    /** 文件路径：仅服务端内部使用，不对外暴露 */
    @JsonIgnore
    @Schema(description = "文件路径（内部字段，不对外暴露）")
    private String filePath;
}
