package com.example.elderly.common;

import lombok.Getter;

@Getter
public enum NoteFollowStatusEnum {
    PENDING("pending","待跟进"),
    FOLLOWING("following","跟进中"),
    FINISH("finish","已完成");
    private final String code;
    private final String desc;
    NoteFollowStatusEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
}
