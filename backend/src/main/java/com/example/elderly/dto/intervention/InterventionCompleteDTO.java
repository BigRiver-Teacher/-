package com.example.elderly.dto.intervention;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "完成干预方案参数")
public class InterventionCompleteDTO {
    @Min(1)
    @Max(5)
    @Schema(description = "总体评分1-5")
    private Integer rating;
    @NotBlank(message = "方案总结不能为空")
    @Schema(description = "方案总结")
    private String summary;
}
