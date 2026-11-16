package com.example.elderly.common;

import lombok.Getter;

@Getter
public enum ServerityEnum {
    LIGHT("light","轻微"),
    MID("mid","中度"),
    HEAVY("heavy","重度");
    private final String code;
    private final String desc;
    ServerityEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
}
