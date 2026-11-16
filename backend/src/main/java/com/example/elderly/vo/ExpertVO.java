package com.example.elderly.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "专家信息")
    public class ExpertVO {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "职称")
    private String title;
    @Schema(description = "职称文本")
    private String titleText;
    @Schema(description = "专长领域")
    private String specialty;
    @Schema(description = "所属机构")
    private String institution;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "执行状态")
    private String status;
    @Schema(description = "今日咨询数")
    private Integer todayCount;
    @Schema(description = "累计咨询数")
    private Integer totalCount;
    @Schema(description = "评分(1-5)")
    private BigDecimal rating;
    @Schema(description = "排班信息")
    private String schedule;
}
