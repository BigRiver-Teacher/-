package com.example.elderly.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum AlertHandleStatusEnum {
    PENDING("pending","待处理"),
    PROCESSING("processing","处理中"),
    RESOLVED("resolved","已解决"),
    IGNORED("ignored","已忽略");
    private final String code;
    private final String desc;
    AlertHandleStatusEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
    public static Optional<AlertHandleStatusEnum> getByCode(String code){
        return Arrays.stream(values()).filter(e->e.getCode().equals(code)).findFirst();
    }
}
