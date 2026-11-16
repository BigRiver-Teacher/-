package com.example.elderly.dto.device;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "设备查询条件")
    public class DeviceQueryDTO extends PageDTO {
    @Schema(description = "设备序列号")
    private String deviceSn;
    @Schema(description = "设备名称")
    private String deviceName;
    @Schema(description = "执行状态")
    private String status;
    @Schema(description = "绑定老人ID")
    private Long bindElderId;
}
