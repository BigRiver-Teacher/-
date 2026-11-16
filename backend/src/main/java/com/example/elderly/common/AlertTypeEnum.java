package com.example.elderly.common;

import lombok.Getter;

@Getter
public enum AlertTypeEnum {
    FALL_DETECT("fall_delect","跌倒检测"),
    HEART_RATE_ABNORMAL("heart_rate_abnormal","心率异常"),
    DEVICE_OFFLINE("device_offline","设备离线"),
    BP_ABNORMAL("bp_abnormal","血压异常"),
    ACTIVITY_ANORMAL("activity_anormal","活动量异常"),
    SOS_CALL("sos_clall","SOS紧急呼叫"),
    POOR_SLEEP("poor_sleep","睡眠质量差");
    private final String code;
    private final String desc;

    AlertTypeEnum(String code,String desc){
        this.code=code;
        this.desc=desc;
    }
}
