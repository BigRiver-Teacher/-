package com.example.elderly.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum EvaluateResultEnum {
    NORMAL("normal","正常"),
    ABNORMAL("abnormal","异常"),
    HIGH_RISK("high_risk","高风险");
    private final String code;
    private final String desc;
    EvaluateResultEnum(String code,String desc){
        this.code = code;
        this.desc = desc;
    }
    public static Optional<EvaluateResultEnum> getByCode(String code){
        return Arrays.stream(values()).filter(e->e.getCode().equals(code)).findFirst();
    }
}
