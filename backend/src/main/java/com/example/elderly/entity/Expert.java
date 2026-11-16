package com.example.elderly.entity;

import com.baomidou.mybatisplus.extension.plugins.inner.ReplacePlaceholderInnerInterceptor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "专家信息表")
@Data
public class Expert {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "职称")
    private String title;
    @Schema(description = "专长领域")
    private String specialty;
    @Schema(description = "所属机构")
    private String institution;
    @Schema(description = "联系电话")
    private String phone;
    private String status;
    @Schema(description = "今日咨询数")
    private Integer todayCount;
    @Schema(description = "累计咨询数")
    private Integer totalCount;
    @Schema(description = "评分(1-5)")
    private BigDecimal rating;
    @Schema(description = "排班信息")
    private String schedule;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

