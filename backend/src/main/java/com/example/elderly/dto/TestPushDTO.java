package com.example.elderly.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "测试推送请求")
    public class TestPushDTO {
    @Schema(description = "推送渠道")
    private String channel;
    @NotBlank(message="接收人不能为空")
    private String recerver;
}
