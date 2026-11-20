package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "老人健康指标表")
@Data
@TableName("health_metric")
public class HealthMetric {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long elderId;
    private String elderName;
    /** BLOOD_PRESSURE/HEART_RATE/BLOOD_SUGAR/BLOOD_OXYGEN/TEMPERATURE */
    private String metricType;
    /** 收缩压(mmHg, 血压专用) */
    private Integer systolic;
    /** 舒张压(mmHg, 血压专用) */
    private Integer diastolic;
    /** 指标数值(心率/血糖/血氧/体温) */
    private BigDecimal value;
    private String unit;
    /** DEVICE/SIMULATED/MANUAL */
    private String source;
    private LocalDateTime measureTime;
    private LocalDateTime createTime;
}
