package com.example.elderly.dto.push;

import com.example.elderly.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "推送日志查询条件")
    public class PushLogQueryDTO extends PageQueryDTO {
    @Schema(description = "推送渠道")
    private String channel;
    @Schema(description = "执行状态")
    private String status;
    @Schema(description = "老人姓名")
    private String elderName;

}
