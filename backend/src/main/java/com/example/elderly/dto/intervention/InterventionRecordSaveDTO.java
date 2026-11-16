package com.example.elderly.dto.intervention;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "新增干预执行记录参数")
public class InterventionRecordSaveDTO {
    @NotNull(message = "干预方案ID不能为空")
    @Schema(description = "干预方案ID")
    private Long interventionId;
    @NotBlank(message = "老人姓名不能为空")
    @Schema(description = "老人姓名")
    private String elderName;
    @NotBlank(message = "执行人不能为空")
    @Schema(description = "执行人")
    private String executor;
    @NotBlank(message = "执行状态不能为空")
    @Schema(description = "执行状态 正常/部分执行/未执行/异常")
    private String status;
    @Min(1)
    @Max(5)
    @Schema(description = "评分1-5")
    private Integer rating;
    @Schema(description = "备注")
    private String note;
    @Schema(description = "附件文件名数组")
    private List<String> attachments;
    @NotNull(message = "执行日期不能为空")
    @Schema(description = "执行日期")
    private LocalDateTime executeDate;
}
