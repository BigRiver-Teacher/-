package com.example.elderly.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum UserStatusEnum {
    ACTIVE("active","启用"),
    INACTIVE("inactive","禁用");
    private final String code;
    private final String desc;
    UserStatusEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
    public static Optional<UserStatusEnum> getByCode(String code){
        return Arrays.stream(values()).filter(e->e.getCode().equals(code)).findFirst();
    }
}
