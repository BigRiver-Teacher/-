package com.example.elderly.common;

import lombok.Getter;

@Getter
public enum ReviewStatusEnum {
    PENDING("pending","待处理"),
    APPROVED("approved","已通过"),
    REJECTED("rejected","未通过");
    private final String code;
    private final String desc;
    ReviewStatusEnum(String code,String desc){
        this.code=code;
        this.desc=desc;
    }
}
