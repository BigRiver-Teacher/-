package com.example.elderly.dto.healthNote;

import com.example.elderly.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "健康档案查询条件")
    public class HealthNoteQueryDTO extends PageQueryDTO {
    @Schema(description = "老人姓名")
    private String elderName;
    @Schema(description = "备注类型")
    private String noteType;
    private String importance;
    private String followStatus;
    private LocalDateTime startRecordTime;
    private LocalDateTime endRecordTime;
}
