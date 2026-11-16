package com.example.elderly.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum InterventionStatusEnum {
    DRAFT("draft", "草稿"),
    PENDING("pending", "待处理"),
    APPROVED("approved", "已通过"),
    ONGOING("ongoing", "进行中"),
    COMPLETED("completed", "已完成"),
    TERMINATED("terminated", "已终止");
    private final String code;
    private final String desc;

    InterventionStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static Optional<InterventionStatusEnum> getByCode(String code) {
        return Arrays.stream(values()).filter(e -> e.getCode().equals(code)).findFirst();
    }
}
