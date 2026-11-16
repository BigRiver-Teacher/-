package com.example.elderly.dto.community;

import com.example.elderly.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "社区联动查询条件")
    public class CommunityQueryDTO extends PageQueryDTO {
    @Schema(description = "老人姓名")
    private String elderName;
    @Schema(description = "审核状态")
    private String reviewStatus;
    @Schema(description = "告警类型")
    private String alertType;

}
