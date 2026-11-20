package com.example.elderly.dto.Expert;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExpertScheduleReviewDTO {
    @NotBlank(message = "审批结果不能为空")
    private String status;
    private String comment;
}
