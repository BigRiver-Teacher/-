package com.example.elderly.dto.alert;

import jakarta.validation.constraints.NotEmpty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "告警批量操作请求")
    public class AlertBatchDTO {
    @NotEmpty(message = "警告ID数组不能为空")
    private List<Long> ids;
    private String handle;
    @Schema(description = "处理备注")
    private String handleNote;
}
