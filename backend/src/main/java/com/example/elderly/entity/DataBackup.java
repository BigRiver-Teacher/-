package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
    @Schema(description = "文件路径")
    private String filePath;
}
