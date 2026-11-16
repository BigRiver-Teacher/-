package com.example.elderly.dto.healthNote;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class HealthNoteAddDTO {
    @Schema(description = "老人ID")
    private Long elderId;
    @Schema(description = "老人姓名")
    private String elderName;
    @NotNull(message = "记录时间不能为空")
    @Schema(description = "记录时间")
    private LocalDateTime recordTime;
    @Schema(description = "记录人")
    private String recorder;
    @NotBlank(message = "备注类型不能为空")
    @Schema(description = "备注类型")
    private String noteType;
    @NotBlank(message = "备注内容不能为空")
    @Schema(description = "备注内容")
    private String content;
    @NotBlank(message = "重要程度不能为空")
    @Schema(description = "重要程度:普通/重要/紧急")
    private String importance;
    @NotBlank(message = "是否已处理不能为空")
    @Schema(description = "是否已处理:待跟进/跟进中/处理完成")
    private String fallowStatus;
}
