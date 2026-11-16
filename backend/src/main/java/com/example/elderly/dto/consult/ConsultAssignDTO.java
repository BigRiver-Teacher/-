package com.example.elderly.dto.consult;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分配专家请求")
    public class ConsultAssignDTO {
    @Schema(description = "专家ID")
    private Long expertId;
}
