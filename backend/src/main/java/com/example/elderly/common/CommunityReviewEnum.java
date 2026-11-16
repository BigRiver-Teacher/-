package com.example.elderly.common;

import lombok.Getter;

@Getter
public enum CommunityReviewEnum {
    PENDING("pending", "待审核"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "驳回");
    private final String code;
    private final String desc;
    CommunityReviewEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
