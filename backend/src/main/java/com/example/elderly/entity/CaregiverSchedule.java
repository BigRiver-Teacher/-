package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "陪护排班表（课程表式）")
@Data
@TableName("caregiver_schedule")
public class CaregiverSchedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long caregiverId;
    private String caregiverName;
    private String community;
    private LocalDate workDate;
    /** 班次: MORNING/AFTERNOON/NIGHT/FULL/REST/LEAVE */
    private String shift;
    private String startTime;
    private String endTime;
    private String note;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
