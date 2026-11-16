package com.example.elderly.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum SpaceOccupyEnum {
    OCCUPIED("occupied","已入住"),
    VACANT("vacant","空置");
    private final String code;
    private final String desc;
    SpaceOccupyEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
    public static Optional<SpaceOccupyEnum> getByCode(String code){
        return Arrays.stream(values()).filter(e->e.getCode().equals(code)).findFirst();
    }
}
