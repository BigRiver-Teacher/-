package com.example.elderly.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum TaskStatusEnum {
    PENOING("penging","待执行"),
    PROCESSING("perocessing","执行中"),
    COMPLETED("completed","已完成"),
    CANCELLED("cancelled","已取消");
    private final String code;
    private  final String desc;
    TaskStatusEnum(String code,String desc){
        this.code=code;
        this.desc=desc;
    }

    public static Optional<TaskStatusEnum> getByCode(String code){
        return Arrays.stream(values()).filter(e->e.getCode().equals(code)).findFirst();
    }
}
