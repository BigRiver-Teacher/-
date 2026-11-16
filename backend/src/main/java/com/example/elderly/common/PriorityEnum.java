package com.example.elderly.common;

import lombok.Getter;

@Getter
public enum PriorityEnum {
    URGENT("紧急","紧急"),
    HIGH("重要","重要"),
    MEDIUM("普通","普通");
    private final String code;
    private final String desc;
    PriorityEnum(String code,String desc){
        this.code = code;
        this.desc = desc;
    }
}
