package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "一键呼救记录表")
@Data
@TableName("sos_record")
public class SosRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long elderId;
    private String elderName;
    private Long familyUserId;
    private LocalDateTime sosTime;
    private String location;
    /** PENDING/RESPONDING/RESOLVED */
    private String status;
    private Long handlerId;
    private String handlerName;
    private LocalDateTime handleTime;
    private String note;
    private String relateAlertCode;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
