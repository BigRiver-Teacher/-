package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "老人-陪护关系表")
@Data
@TableName("elder_caregiver")
public class ElderCaregiver {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long elderId;
    private String elderName;
    private Long caregiverId;
    private String caregiverName;
    private String serviceType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
