package com.example.elderly.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum InspectionResultEnum {
    QUALIFIED("qualified","合格"),
    UNQUALIFIED("unqualified","不合格"),
    NEED_RECTIFY("need_rectify","需要整改");
    private final String code;
    private final String desc;
    InspectionResultEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
}
