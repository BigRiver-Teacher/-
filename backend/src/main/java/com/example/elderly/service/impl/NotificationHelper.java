package com.example.elderly.service.impl;

import com.example.elderly.entity.Notification;
import com.example.elderly.entity.PushLog;
import com.example.elderly.mapper.NotificationMapper;
import com.example.elderly.mapper.PushLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 通知助手：统一站内通知（notification 表）+ 短信模拟（push_log 表，不接真实网关）。
 */
@Component
public class NotificationHelper {

    @Resource
    private NotificationMapper notificationMapper;
    @Resource
    private PushLogMapper pushLogMapper;

    /** 站内通知：按 userId 送达，前端铃铛红点 + 点击跳转 link */
    public void notify(Long userId, String title, String content, String type, String link) {
        if (userId == null) {
            return;
        }
        try {
            Notification n = new Notification();
            n.setUserId(userId);
            n.setTitle(title);
            n.setContent(content);
            n.setType(type);
            n.setLink(link);
            n.setRead(false);
            n.setCreateTime(LocalDateTime.now());
            notificationMapper.insert(n);
        } catch (Exception e) {
            System.err.println("[NotificationHelper] 通知失败: " + e.getMessage());
        }
    }

    /** 短信模拟：写 push_log，状态 SIMULATED，不接真实短信网关 */
    public void sms(String elderName, String familyPhone, String scence, String content) {
        try {
            PushLog log = new PushLog();
            log.setElderName(elderName);
            log.setFamilyPhone(familyPhone);
            log.setChannel("SMS");
            log.setScence(scence);
            log.setContent(content);
            log.setFullContent(content);
            log.setStatus("SIMULATED");
            log.setTime(LocalDateTime.now());
            log.setDuration(0L);
            log.setRetryCount(0);
            log.setResponse("模拟发送，未接入真实短信网关");
            pushLogMapper.insert(log);
        } catch (Exception e) {
            System.err.println("[NotificationHelper] 短信模拟失败: " + e.getMessage());
        }
    }
}
