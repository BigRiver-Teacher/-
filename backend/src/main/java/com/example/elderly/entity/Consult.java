package com.example.elderly.entity;

import jakarta.validation.constraints.NotBlank;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "咨询记录表")
@Data
public class Consult {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @NotBlank(message ="老人姓名不能为空")
    @Schema(description = "老人姓名")
    private String elderName;
    @Schema(description = "老人ID")
    private Long elderId;
    @NotBlank(message ="咨询类型不能为空")
    @Schema(description = "咨询类型")
    private String type;
    @Schema(description = "专家ID")
    private Long expertId;
    @Schema(description = "专家姓名")
    private String expertName;
    @Schema(description = "关联会话ID(conversation.id)")
    private Long conversationId;
    private String status;
    @Schema(description = "紧急程度")
    private String urgency;
    @Schema(description = "咨询时间")
    private LocalDateTime consultTime;
    @NotBlank(message ="咨询内容不能为空")
    @Schema(description = "咨询内容")
    private String content;
    @Schema(description = "回复内容")
    private String reply;
    @Schema(description = "评分(1-5)")
    private Integer rating;
    @Schema(description = "反馈")
    private String feedback;
    @Schema(description = "专家备注(仅专家/管理员可见)")
    private String expertNote;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
