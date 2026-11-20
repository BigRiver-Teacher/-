package com.example.elderly.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "老人-家属关系表")
@Data
@TableName("elder_family")
public class ElderFamily {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long elderId;
    private String elderName;
    private String familyName;
    private String relationship;
    private String phone;
    private Long familyUserId;
    private Boolean isEmergency;
    private Boolean isPush;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
