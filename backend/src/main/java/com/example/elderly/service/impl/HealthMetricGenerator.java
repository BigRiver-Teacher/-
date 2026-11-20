package com.example.elderly.service.impl;

import com.example.elderly.entity.Elder;
import com.example.elderly.entity.ElderFamily;
import com.example.elderly.entity.HealthMetric;
import com.example.elderly.mapper.ElderFamilyMapper;
import com.example.elderly.mapper.ElderMapper;
import com.example.elderly.mapper.HealthMetricMapper;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 健康数据模拟生成器：定时为有家属关联的老人追加血压/心率/血糖/血氧/体温数据点。
 * 当前无真实硬件设备，全部为 SIMULATED 模拟数据。
 */
@Component
public class HealthMetricGenerator {

    @Resource
    private ElderMapper elderMapper;
    @Resource
    private ElderFamilyMapper elderFamilyMapper;
    @Resource
    private HealthMetricMapper healthMetricMapper;

    private final Random random = new Random();

    /** 每 30 分钟为所有有家属关联的老人追加一条最新健康数据 */
    @Scheduled(fixedDelay = 30 * 60 * 1000L)
    public void generateScheduled() {
        try {
            List<ElderFamily> families = elderFamilyMapper.selectList(null);
            Set<Long> elderIds = families.stream()
                    .map(ElderFamily::getElderId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            for (Long id : elderIds) {
                Elder elder = elderMapper.selectById(id);
                if (elder != null) {
                    generateForElder(elder);
                }
            }
        } catch (Exception e) {
            // 定时任务异常不阻断主流程
            System.err.println("[HealthMetricGenerator] 生成失败: " + e.getMessage());
        }
    }

    /** 为指定老人生成一组（5 项）模拟健康数据 */
    public void generateForElder(Elder elder) {
        LocalDateTime now = LocalDateTime.now();
        insert(elder, "BLOOD_PRESSURE", 115 + random.nextInt(25), 70 + random.nextInt(20), null, "mmHg", now);
        insert(elder, "HEART_RATE", null, null, round(60 + random.nextDouble() * 40), "bpm", now);
        insert(elder, "BLOOD_SUGAR", null, null, round(4.5 + random.nextDouble() * 3.5), "mmol/L", now);
        insert(elder, "BLOOD_OXYGEN", null, null, round(95 + random.nextDouble() * 5), "%", now);
        insert(elder, "TEMPERATURE", null, null, round(36.0 + random.nextDouble() * 1.5), "℃", now);
    }

    private void insert(Elder elder, String type, Integer sys, Integer dia, BigDecimal value, String unit, LocalDateTime time) {
        HealthMetric m = new HealthMetric();
        m.setElderId(elder.getId());
        m.setElderName(elder.getName());
        m.setMetricType(type);
        m.setSystolic(sys);
        m.setDiastolic(dia);
        m.setValue(value);
        m.setUnit(unit);
        m.setSource("SIMULATED");
        m.setMeasureTime(time);
        m.setCreateTime(LocalDateTime.now());
        healthMetricMapper.insert(m);
    }

    private BigDecimal round(double v) {
        return BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP);
    }
}
