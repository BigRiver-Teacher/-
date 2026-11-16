package com.example.elderly.dto.community;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "社区联动审核请求")
    public class CommunityReviewDTO {
    @Schema(description = "审核状态")
    private String reviewStatus;
    @Schema(description = "驳回原因")
    private String rejectReason;
    private String reviewer;
    /** 审核意见（兼容前端 review 字段） */
    @Schema(description = "审核意见")
    private String review;
}
