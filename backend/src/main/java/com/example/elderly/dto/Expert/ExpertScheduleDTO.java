package com.example.elderly.dto.Expert;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "专家排班请求")
    public class ExpertScheduleDTO {
    @Schema(description = "排班信息")
    private String schedule;
}
