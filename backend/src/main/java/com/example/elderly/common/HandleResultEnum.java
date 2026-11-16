package com.example.elderly.common;

import lombok.Getter;

@Getter
public enum HandleResultEnum {
    SOLVED("已解决"),
    FOLLOW_UP("待处理"),
    TO_REVIEW("待审核"),
    UNSOLVABLE("无法解决");
    private final String msg;
    HandleResultEnum(String msg){
        this.msg=msg;
    }
}
