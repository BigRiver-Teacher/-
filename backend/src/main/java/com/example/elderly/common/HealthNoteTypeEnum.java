package com.example.elderly.common;

import lombok.Getter;

@Getter
public enum HealthNoteTypeEnum {
    HEALTH_OBSERVE("health_observe","健康观察"),
    DOCTOR_ADVICE("doctor_advice","医嘱"),
    DAILY_NURSE("daily_nurse","日常护理"),
    MEDICINE_REMIND("medicine_remind","用药提醒"),
    PSYCHOLOGY_CARE("psychology_care","心理关怀");
    private final String code;
    private final String desc;
    HealthNoteTypeEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }
}
