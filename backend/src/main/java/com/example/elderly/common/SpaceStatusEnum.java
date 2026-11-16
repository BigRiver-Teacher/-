package com.example.elderly.common;

import lombok.Getter;

@Getter
public enum SpaceStatusEnum {
    NORMAL("normal","正常"),
    RENOVATING("renovating","正在装修"),
    REPAIR("repair","正在维修"),
    INACTIVE("inactive","停用");
    private final String code;
    private final String desc;
    SpaceStatusEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
}
