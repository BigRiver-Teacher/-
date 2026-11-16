package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import org.apache.ibatis.type.JdbcType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "model_param_version", autoResultMap = true)
public class ModelParamVersion {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "版本号")
    private String version;
    @TableField(typeHandler = JacksonTypeHandler.class, jdbcType = JdbcType.VARCHAR)
    @Schema(description = "检测参数")
    private ModelParam.DetectionParam detection;
    @TableField(typeHandler = JacksonTypeHandler.class, jdbcType = JdbcType.VARCHAR)
    @Schema(description = "评估参数")
    private ModelParam.EvaluationParam evaluation;
    @TableField(typeHandler = JacksonTypeHandler.class, jdbcType = JdbcType.VARCHAR)
    @Schema(description = "告警参数")
    private ModelParam.AlertParam alert;
    @TableField(typeHandler = JacksonTypeHandler.class, jdbcType = JdbcType.VARCHAR)
    @Schema(description = "设备参数")
    private ModelParam.DeviceParam device;
    @Schema(description = "操作人")
    private String operator;
    @Schema(description = "版本描述")
    private String description;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
