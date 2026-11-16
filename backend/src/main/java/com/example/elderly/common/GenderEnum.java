package com.example.elderly.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum GenderEnum {
    MALE("male", "男"),
    FEMALE("female", "女");
    private final String code;
    private final String desc;
    GenderEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
    public static Optional<GenderEnum> getByCode(String code){
        return Arrays.stream(values()).filter(e->e.getCode().equals(code)).findFirst();
    }
}
