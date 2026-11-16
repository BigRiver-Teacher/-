package com.example.elderly.dto.space;

import com.example.elderly.dto.PageQueryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "空间列表查询条件")
public class SpaceQueryDTO extends PageQueryDTO {
    @Schema(description = "空间名称/编号模糊搜索")
    private String keyword;
    @Schema(description = "入住状态 occupied/vacant")
    private String occupancyStatus;
    @Schema(description = "安全评级 A/B/C/D")
    private String safetyRating;
}
