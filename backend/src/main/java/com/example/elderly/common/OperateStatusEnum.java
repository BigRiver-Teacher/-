package com.example.elderly.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum OperateStatusEnum {
    SUCCESS("success","成功"),
    FAIL("fail","失败");
    private final String code;
    private final String desc;
    OperateStatusEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
    public static Optional<OperateStatusEnum> getByCode(String code){
        return Arrays.stream(values()).filter(e->e.getCode().equals(code)).findFirst();
    }
}
