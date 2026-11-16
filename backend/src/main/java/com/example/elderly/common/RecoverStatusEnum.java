package com.example.elderly.common;

import lombok.Getter;

@Getter
public enum RecoverStatusEnum {
    RECOVERED("recovered","已恢复"),
    RECOVERING("recovering","恢复中"),
    SEQUELA("sequela","后遗症");
    private final String code;
    private final String desc;
    RecoverStatusEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
}
