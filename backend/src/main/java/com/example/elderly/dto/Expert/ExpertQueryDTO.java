package com.example.elderly.dto.Expert;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "专家查询条件")
    public class ExpertQueryDTO extends PageDTO {
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "职称")
    private String title;
    @Schema(description = "专长领域")
    private String specialty;
    @Schema(description = "执行状态")
    private String status;
}
