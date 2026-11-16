package com.example.elderly.common;

import lombok.Getter;

@Getter
public enum BackupTypeEnum {
    FULL("full","全量备份"),
    INCREMENTAL("incremental","增量备份");
    private final String code;
    private final String desc;
    BackupTypeEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
}

