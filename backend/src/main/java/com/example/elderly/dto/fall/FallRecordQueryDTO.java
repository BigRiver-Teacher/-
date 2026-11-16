package com.example.elderly.dto.fall;

import com.example.elderly.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "跌倒记录查询条件")
    public class FallRecordQueryDTO extends PageQueryDTO {
    @Schema(description = "老人姓名")
    private String elderName;
    @Schema(description = "严重程度")
    private String severity;
    @Schema(description = "恢复状态")
    private String recoverStatus;
    private LocalDateTime startFallTime;
    private LocalDateTime endFallTime;
}
