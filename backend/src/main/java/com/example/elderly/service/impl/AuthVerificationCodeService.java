package com.example.elderly.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 阶段1：内存验证码服务。
 *
 * <p>特性：TTL 过期、发送限流、错误次数上限、成功后立即失效（一次性）。
 * 演示环境通过日志输出验证码（不随接口返回），生产应替换为 Redis + 真实短信/邮件网关。</p>
 */
@Slf4j
@Service
public class AuthVerificationCodeService {

    /** 验证码有效期（秒） */
    @Value("${app.auth.code-ttl-seconds:300}")
    private long codeTtlSeconds;

    /** 同一目标两次发送的最小间隔（秒） */
    @Value("${app.auth.code-resend-seconds:60}")
    private long resendSeconds;

    /** 单目标每小时最大发送次数 */
    @Value("${app.auth.code-max-send-per-hour:5}")
    private int maxSendPerHour;

    /** 单个验证码最大错误尝试次数 */
    @Value("${app.auth.code-max-attempts:5}")
    private int maxAttempts;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, CodeEntry> store = new ConcurrentHashMap<>();

    private static class CodeEntry {
        String code;
        long expireAt;
        long lastSentAt;
        int sendCountHour;
        int hourWindowStart;
        int attempts;
    }

    /**
     * 生成并保存验证码。返回 true 表示成功，false 表示触发限流。
     * scope 建议取值 reset:邮箱 / register:手机号，避免不同用途互相覆盖。
     */
    public synchronized boolean send(String scope, String target) {
        String key = scope + ":" + target;
        long now = System.currentTimeMillis();
        CodeEntry entry = store.computeIfAbsent(key, k -> new CodeEntry());

        // 小时窗口限流
        int hourStart = (int) (now / 3_600_000L);
        if (entry.hourWindowStart != hourStart) {
            entry.hourWindowStart = hourStart;
            entry.sendCountHour = 0;
        }
        if (entry.sendCountHour >= maxSendPerHour) {
            log.warn("[验证码] 发送过于频繁(小时上限), scope={}, target={}", scope, target);
            return false;
        }
        if (entry.lastSentAt > 0 && now - entry.lastSentAt < resendSeconds * 1000L) {
            log.warn("[验证码] 发送过于频繁(重发间隔), scope={}, target={}", scope, target);
            return false;
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        entry.code = code;
        entry.expireAt = now + codeTtlSeconds * 1000L;
        entry.lastSentAt = now;
        entry.sendCountHour++;
        entry.attempts = 0;

        // 演示环境：日志输出，便于本地验收；接口响应不返回验证码
        log.info("[演示验证码] scope={}, target={}, code={}, 有效期={}秒", scope, target, code, codeTtlSeconds);
        return true;
    }

    /** 校验验证码，成功即销毁（一次性）；失败返回 false */
    public synchronized boolean verify(String scope, String target, String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        String key = scope + ":" + target;
        CodeEntry entry = store.get(key);
        if (entry == null || entry.code == null) {
            return false;
        }
        if (System.currentTimeMillis() > entry.expireAt) {
            store.remove(key);
            return false;
        }
        if (entry.attempts >= maxAttempts) {
            store.remove(key);
            return false;
        }
        if (!entry.code.equals(code.trim())) {
            entry.attempts++;
            return false;
        }
        store.remove(key); // 一次性使用
        return true;
    }

    /** 清理过期条目（轻量，可定时调用） */
    public void purgeExpired() {
        long now = System.currentTimeMillis();
        store.entrySet().removeIf(e -> e.getValue().expireAt < now);
    }
}
