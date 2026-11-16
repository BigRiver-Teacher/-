package com.example.elderly.common;

import lombok.Getter;

@Getter
public enum PushChannelEnum {
    APP("app","app推送"),
    WECHAT("wechat","微信推送"),
    EMAIL("email","邮件推送"),
    SMS("sms","短信推送"),
    VOICE("voice","电话推送");
    private final String code;
    private final String desc;
    PushChannelEnum(String code,String desc){
        this.desc = desc;
        this.code = code;
    }
}
