package com.example.elderly.common;

import lombok.Getter;

@Getter
public enum ConsultStatusEnum {
    PENDING("pending","待分配"),
    ONGOING("ongoing","进行中"),
    COMPLETED("completed","已完成"),
    CANCELLED("concelled","已取消");
    private final String code;
    private final String desc;
    ConsultStatusEnum(String code,String  desc){
        this.code=code;
        this.desc=desc;
    }
}
