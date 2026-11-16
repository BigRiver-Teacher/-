package com.example.elderly.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum ExecuteStatusEnum {
    NORMAL("normal","正常"),
    PART("part","部分执行"),
    NOT_EXEC("not_exec","未执行"),
    ABNORMAL("abnormal","异常");
    private final String code;
    private final String desc;
    ExecuteStatusEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
    public static Optional<ExecuteStatusEnum> getByCode(String code){
        return Arrays.stream(values()).filter(e->e.getCode().equals(code)).findFirst();
    }
}
