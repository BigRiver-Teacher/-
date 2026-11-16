package com.example.elderly.dto;

import com.example.elderly.common.BackupTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "备份操作请求")
    public class BackupOperateDTO {
    @Schema(description = "咨询类型")
    private BackupTypeEnum type;
}
