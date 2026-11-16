package com.example.elderly.dto.intervention;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "干预方案查询参数")
public class InterventionSaveDTO {
    @Schema(description = "ID，编辑传，新增不传")
    private Long id;
    @NotBlank(message = "方案名称不能为空")
    @Schema(description = "方案名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @NotBlank(message = "干预类型不能为空")
    @Schema(description = "干预类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;
    @NotBlank(message = "优先级不能为空")
    @Schema(description = "优先级 urgent/important/normal", requiredMode = Schema.RequiredMode.REQUIRED)
    private String priority;
    @NotBlank(message = "负责人不能为空")
    @Schema(description = "负责人")
    private String leader;
    @NotNull(message = "执行总周期不能为空")
    @Schema(description = "执行总周期(周)")
    private Integer totalWeeks;
    @NotBlank(message = "执行频率不能为空")
    @Schema(description = "执行频率")
    private String frequency;
    @NotNull(message = "开始日期不能为空")
    @Schema(description = "开始日期")
    private LocalDateTime startDate;
    @NotNull(message = "预计完成日期不能为空")
    @Schema(description = "预计完成日期")
    private LocalDateTime endDate;
    @NotNull(message = "关联老人不能为空")
    @Schema(description = "关联老人ID数组")
    private List<Long> associatedElders;
    @Schema(description = "干预目标数组")
    private List<String> goals;
    @Schema(description = "具体措施数组")
    private List<String> measures;
    @Schema(description = "注意事项")
    private String notes;
}
