package com.example.elderly.dto.community;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "社区联动保存请求")
    public class CommunitySaveDTO {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "老人姓名")
    private String elderName;
    @Schema(description = "告警类型")
    private String alertType;
    @Schema(description = "联动动作")
    private String action;
    @Schema(description = "处理人姓名")
    private String handler;
    @Schema(description = "处理人ID")
    private String handlerId;
    @Schema(description = "评估结果")
    private String result;
    @Schema(description = "告警描述")
    private String description;
    @Schema(description = "附件数组")
    private List<String> attachments;
    @Schema(description = "所属社区")
    private String community;
    @Schema(description = "关联告警编号")
    private String alertCode;
}

