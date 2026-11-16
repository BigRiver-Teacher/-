package com.example.elderly.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum PushStatusEnum {
    SUCCESS("success","成功"),
    FAIL("fail","失败"),
    SENDING("sending","发送中");
    private final String code;
    private final String desc;
    PushStatusEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
    public static Optional<PushStatusEnum> getByCode(String code){
        return Arrays.stream(values()).filter(e->e.getCode().equals(code)).findFirst();
    }
}
