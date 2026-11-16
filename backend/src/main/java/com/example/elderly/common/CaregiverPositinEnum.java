package com.example.elderly.common;

import lombok.Getter;

@Getter
public enum CaregiverPositinEnum {
    CARE_SUPERVISOR("care_supervisor","陪护主管"),
    SENIOR_CAREGIVER("senior_caregiver","高级陪护员"),
    CAREGIVER("caregiver","陪护员"),
    REHAB_NURSE("rehab_nurse","康复护理师"),
    PSYCHO_COUNSELOR("psycho_counselor","心理辅导师");
    private final String code;
    private  final String desc;
    CaregiverPositinEnum(String code,String desc){
        this.code=code;
        this.desc=desc;
    }
}
