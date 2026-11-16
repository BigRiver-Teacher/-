package com.example.elderly.common;

import lombok.Getter;

@Getter
public enum ExperTitleEnum {
    CHIEF_PHYSICIAN("chief_physician","主任医师"),
    DEPUTY_CHIEF("deputy_chief","副主任医师"),
    ATTENDEE("attendee","主治医师"),
    REHAB_THERAPIST("rehab_therapist","康复师");
    private final String code;
    private final String desc;
    ExperTitleEnum(String code,String desc){
        this.code = code;
        this.desc = desc;
    }
}
