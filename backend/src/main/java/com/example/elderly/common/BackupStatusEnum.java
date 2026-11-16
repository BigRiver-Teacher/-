package com.example.elderly.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum BackupStatusEnum {
    SUCCESS("success","成功"),
    FAILED("failed","失败");
    private final String code;
    private final String desc;
    BackupStatusEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
    public static Optional<BackupStatusEnum> getByCode(String code){
        return Arrays.stream(values()).filter(e->e.getCode().equals(code)).findFirst();
    }
}
