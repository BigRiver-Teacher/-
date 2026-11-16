package com.example.elderly.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum DeviceStatusEnum {
    ONLINE("online","在线"),
    OFFLINE("offline","离线"),
    FAULT("fault","故障"),
    LOW_BATTERY("low_battery","低电量"),
    PENDING("pending","待激活");
    private final String code;
    private final String desc;
    DeviceStatusEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
    public static Optional<DeviceStatusEnum> getByCode(String code){
        return Arrays.stream(values()).filter(e->e.getCode().equals(code)).findFirst();
    }
}
