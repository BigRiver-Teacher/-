package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "老人-专家关系表")
@Data
@TableName("elder_expert")
public class ElderExpert {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long elderId;
    private String elderName;
    private Long expertId;
    private String expertName;
    private String relationType;
    private LocalDateTime createTime;
    private Integer isDeleted;
}
