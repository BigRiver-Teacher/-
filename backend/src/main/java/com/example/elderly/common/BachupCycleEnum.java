package com.example.elderly.common;

import lombok.Getter;

@Getter
public enum BachupCycleEnum {
    DAILY("daily","每天"),
    WEEKLY("weekly","每周"),
    MONTHLY("monthly","每月"),
    DISABLED("disabled","定时关闭");
    private final String code;
    private final String desc;
    BachupCycleEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
}
