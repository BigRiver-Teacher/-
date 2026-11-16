package com.example.elderly.dto.space;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "新增/编辑空间参数")
public class SpaceSaveDTO {
    @Schema(description = "ID，编辑传，新增不传")
    private Long id;
    @NotBlank(message = "空间名称不能为空")
    @Schema(description = "空间名称")
    private String name;
    @NotBlank(message = "楼栋不能为空")
    @Schema(description = "楼栋")
    private String building;
    @NotBlank(message = "楼层不能为空")
    @Schema(description = "楼层")
    private String floor;
    @Schema(description = "户型")
    private String layout;
    @Schema(description = "面积㎡")
    private Double area;
    @Schema(description = "入住状态 occupied/vacant")
    private String occupancyStatus;
    @Schema(description = "入住老人ID")
    private Long elderId;
    @Schema(description = "入住时间")
    private LocalDateTime moveInDate;
    @Schema(description = "适老化配置数组")
    private List<String> facilities;
    @Schema(description = "风险因素数组")
    private List<String> risks;
    @Schema(description = "空间状态 normal/renovating/repair/inactive")
    private String status;
    @Schema(description = "安全评级 ")
    private String safetyRating;
    @Schema(description = "下次检查日期")
    private LocalDateTime nextInspection;
    @Schema(description = "备注")
    private String remark;
}
