package com.example.elderly.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum ResidenceTypeEnum {
    ALONE("alone", "独居"),
    WITH_CHILD("with_child","与子女同住"),
    NURSING_HOME("nursing_home","养老院");
    private final String code;
    private final String desc;
    ResidenceTypeEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
    public static Optional<ResidenceTypeEnum> getByCode(String code){
        return Arrays.stream(values()).filter(e->e.getCode().equals(code)).findFirst();
    }
}
