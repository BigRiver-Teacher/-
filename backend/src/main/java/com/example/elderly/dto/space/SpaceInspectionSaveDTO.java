package com.example.elderly.dto.space;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "新增空间检查记录参数")
public class SpaceInspectionSaveDTO {
    @NotNull(message = "空间ID不能为空")
    @Schema(description = "空间ID")
    private Long spaceId;
    @NotNull(message = "检查日期不能为空")
    @Schema(description = "检查日期")
    private LocalDate date;
    @NotBlank(message = "检查结果不能为空")
    @Schema(description = "检查结果 合格/不合格/需整改")
    private String result;
    @NotBlank(message = "检查人不能为空")
    @Schema(description = "检查人")
    private String inspector;
    @NotBlank(message = "检查类型不能为空")
    @Schema(description = "检查类型 ")
    private String type;
    @Schema(description = "问题描述")
    private String issue;
    @Schema(description = "整改状态")
    private String repairStatus;
    @Schema(description = "整改期限")
    private LocalDate deadline;
}
