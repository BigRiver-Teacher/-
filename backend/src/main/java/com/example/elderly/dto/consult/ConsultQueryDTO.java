package com.example.elderly.dto.consult;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "咨询查询条件")
    public class ConsultQueryDTO extends PageDTO {
    @Schema(description = "老人姓名")
    private String elderName;
    @Schema(description = "专家姓名")
    private String expertName;
    @Schema(description = "执行状态")
    private String status;
    @Schema(description = "紧急程度")
    private String urgency;
}
