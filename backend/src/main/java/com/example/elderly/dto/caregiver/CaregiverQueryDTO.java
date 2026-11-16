package com.example.elderly.dto.caregiver;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "护工查询条件")
    public class CaregiverQueryDTO extends PageDTO {
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "职位")
    private String position;
    @Schema(description = "执行状态")
    private String status;
}
