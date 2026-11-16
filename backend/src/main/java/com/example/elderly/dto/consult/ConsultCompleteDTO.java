package com.example.elderly.dto.consult;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "完成咨询请求")
    public class ConsultCompleteDTO {
    @Schema(description = "回复内容")
    private String reply;
    @Schema(description = "评分(1-5)")
    private Integer rating;
    @Schema(description = "反馈")
    private String feedback;
}
