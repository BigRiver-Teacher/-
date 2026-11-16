package com.example.elderly.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum AlertLevelEnum {
    URGENT("urgent", "紧急","红色"),
    IMPORTANT("important", "重要","橙色"),
    NORMAL("normal", "普通","蓝色");
    private final String code;
    private final String desc;
    private final String color;
    AlertLevelEnum(String code,String desc,String color){
        this.code = code;
        this.desc = desc;
        this.color = color;
    }
    public static Optional<AlertLevelEnum>getByCode(String code){
        return Arrays.stream(values()).filter(e->e.getCode().equals(code)).findFirst();
    }
}
