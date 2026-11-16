package com.example.elderly.dto.intervention;

import com.example.elderly.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "干预方案列表查询条件")
public class InterventionQueryDTO extends PageQueryDTO {
    @Schema(description = "方案名称/编号模糊搜索")
    private String keyword;
    @Schema(description = "干预类型")
    private String type;
    @Schema(description = "方案状态")
    private String status;
    @Schema(description = "优先级 urgent/important/normal")
    private String priority;
}
