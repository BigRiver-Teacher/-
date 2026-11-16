package com.example.elderly.dto.push;

import jakarta.validation.constraints.NotEmpty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量重推请求")
    public class PushBatchRetryDTO {
    @NotEmpty(message = "推送ID不能为空")
    private List<Long> ids;
}
