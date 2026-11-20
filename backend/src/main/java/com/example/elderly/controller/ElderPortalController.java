package com.example.elderly.controller;

import com.example.elderly.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderly.common.Result;
import com.example.elderly.entity.Consult;
import com.example.elderly.entity.Elder;
import com.example.elderly.entity.ElderCaregiver;
import com.example.elderly.entity.ElderFamily;
import com.example.elderly.entity.Expert;
import com.example.elderly.entity.HealthMetric;
import com.example.elderly.entity.ScheduleTask;
import com.example.elderly.mapper.ConsultMapper;
import com.example.elderly.mapper.ElderCaregiverMapper;
import com.example.elderly.mapper.ElderFamilyMapper;
import com.example.elderly.mapper.ElderMapper;
import com.example.elderly.mapper.ExpertMapper;
import com.example.elderly.mapper.HealthMetricMapper;
import com.example.elderly.mapper.ScheduleTaskMapper;
import com.example.elderly.util.SecurityUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 老人端门户（家属代办）：当前关联老人的健康概览与个人档案。
 */
@Tag(name = "老人门户")
@RestController
@RequestMapping("/api/elder/portal")
public class ElderPortalController {

    @Resource
    private ElderFamilyMapper elderFamilyMapper;
    @Resource
    private ElderMapper elderMapper;
    @Resource
    private HealthMetricMapper healthMetricMapper;
    @Resource
    private ElderCaregiverMapper elderCaregiverMapper;
    @Resource
    private ScheduleTaskMapper scheduleTaskMapper;
    @Resource
    private ConsultMapper consultMapper;
    @Resource
    private ExpertMapper expertMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    /** 当前家属关联的老人档案 */
    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        Elder elder = currentElder();
        if (elder == null) {
            throw new BusinessException(404, "未关联老人档案");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", elder.getId());
        data.put("name", elder.getName());
        data.put("gender", elder.getGender());
        data.put("age", elder.getAge());
        data.put("idCard", elder.getIdCard());
        data.put("birthDate", elder.getBirthDate() != null ? elder.getBirthDate().toLocalDate().toString() : null);
        data.put("ethnicity", elder.getEthnicity());
        data.put("phone", elder.getPhone());
        data.put("address", elder.getAddress());
        data.put("residenceType", elder.getResidenceType());
        data.put("remark", elder.getRemark());
        // 家属关系
        List<ElderFamily> families = elderFamilyMapper.selectList(new LambdaQueryWrapper<ElderFamily>()
                .eq(ElderFamily::getElderId, elder.getId())
                .eq(ElderFamily::getIsDeleted, 0));
        data.put("familyCount", families.size());
        data.put("families", families.stream().map(f -> {
            Map<String, Object> m = new HashMap<>();
            m.put("name", f.getFamilyName());
            m.put("relationship", f.getRelationship());
            m.put("phone", f.getPhone());
            m.put("isEmergency", f.getIsEmergency());
            return m;
        }).toList());
        return Result.success(data);
    }

    /** 健康概览：最新值 + 近 7 天趋势（血压/心率/血糖/血氧/体温） */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Elder elder = currentElder();
        if (elder == null) {
            throw new BusinessException(404, "未关联老人档案");
        }
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        List<HealthMetric> list = healthMetricMapper.selectList(new LambdaQueryWrapper<HealthMetric>()
                .eq(HealthMetric::getElderId, elder.getId())
                .ge(HealthMetric::getMeasureTime, from)
                .orderByAsc(HealthMetric::getMeasureTime));

        // 按类型分组
        Map<String, List<HealthMetric>> grouped = new LinkedHashMap<>();
        for (HealthMetric h : list) {
            grouped.computeIfAbsent(h.getMetricType(), k -> new ArrayList<>()).add(h);
        }

        Map<String, Object> latest = new LinkedHashMap<>();
        Map<String, Object> trend = new LinkedHashMap<>();

        // 血压：最新收缩压/舒张压
        if (grouped.containsKey("BLOOD_PRESSURE")) {
            List<HealthMetric> bp = grouped.get("BLOOD_PRESSURE");
            HealthMetric last = bp.get(bp.size() - 1);
            Map<String, Object> l = new HashMap<>();
            l.put("systolic", last.getSystolic());
            l.put("diastolic", last.getDiastolic());
            l.put("unit", "mmHg");
            l.put("time", fmt(last.getMeasureTime()));
            latest.put("BLOOD_PRESSURE", l);
            List<Map<String, Object>> t = new ArrayList<>();
            for (HealthMetric h : bp) {
                Map<String, Object> p = new HashMap<>();
                p.put("time", fmt(h.getMeasureTime()));
                p.put("systolic", h.getSystolic());
                p.put("diastolic", h.getDiastolic());
                t.add(p);
            }
            trend.put("BLOOD_PRESSURE", t);
        }

        // 其余单值指标
        String[] singleTypes = {"HEART_RATE", "BLOOD_SUGAR", "BLOOD_OXYGEN", "TEMPERATURE"};
        for (String type : singleTypes) {
            if (grouped.containsKey(type)) {
                List<HealthMetric> items = grouped.get(type);
                HealthMetric last = items.get(items.size() - 1);
                Map<String, Object> l = new HashMap<>();
                l.put("value", last.getValue());
                l.put("unit", last.getUnit());
                l.put("time", fmt(last.getMeasureTime()));
                latest.put(type, l);
                List<Map<String, Object>> t = new ArrayList<>();
                for (HealthMetric h : items) {
                    Map<String, Object> p = new HashMap<>();
                    p.put("time", fmt(h.getMeasureTime()));
                    p.put("value", h.getValue());
                    t.add(p);
                }
                trend.put(type, t);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("elderId", elder.getId());
        data.put("elderName", elder.getName());
        data.put("latest", latest);
        data.put("trend", trend);
        return Result.success(data);
    }

    /** 我的护工与家人 */
    @GetMapping("/relation")
    public Result<Map<String, Object>> relation() {
        Elder elder = currentElder();
        if (elder == null) {
            throw new BusinessException(404, "未关联老人档案");
        }
        List<ElderCaregiver> caregivers = elderCaregiverMapper.selectList(new LambdaQueryWrapper<ElderCaregiver>()
                .eq(ElderCaregiver::getElderId, elder.getId())
                .eq(ElderCaregiver::getIsDeleted, 0));
        List<ElderFamily> families = elderFamilyMapper.selectList(new LambdaQueryWrapper<ElderFamily>()
                .eq(ElderFamily::getElderId, elder.getId())
                .eq(ElderFamily::getIsDeleted, 0));
        Map<String, Object> data = new HashMap<>();
        data.put("caregivers", caregivers.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("name", c.getCaregiverName());
            m.put("serviceType", c.getServiceType());
            m.put("status", c.getStatus());
            return m;
        }).toList());
        data.put("families", families.stream().map(f -> {
            Map<String, Object> m = new HashMap<>();
            m.put("name", f.getFamilyName());
            m.put("relationship", f.getRelationship());
            m.put("phone", f.getPhone());
            m.put("isEmergency", f.getIsEmergency());
            return m;
        }).toList());
        return Result.success(data);
    }

    /** 用药/日程提醒（未来日程任务） */
    @GetMapping("/reminder")
    public Result<Map<String, Object>> reminder() {
        Elder elder = currentElder();
        if (elder == null) {
            throw new BusinessException(404, "未关联老人档案");
        }
        List<ScheduleTask> tasks = scheduleTaskMapper.selectList(new LambdaQueryWrapper<ScheduleTask>()
                .eq(ScheduleTask::getElderId, elder.getId())
                .ge(ScheduleTask::getTaskDate, LocalDate.now().atStartOfDay())
                .orderByAsc(ScheduleTask::getTaskDate));
        Map<String, Object> data = new HashMap<>();
        data.put("elderName", elder.getName());
        data.put("tasks", tasks);
        return Result.success(data);
    }

    /** 满意度打分：会话结束后家属对专家评分 */
    @PostMapping("/rate")
    public Result<Boolean> rate(@RequestBody Map<String, Object> body) {
        Elder elder = currentElder();
        if (elder == null) {
            throw new BusinessException(404, "未关联老人档案");
        }
        Object cidObj = body.get("conversationId");
        Object ratingObj = body.get("rating");
        if (cidObj == null || ratingObj == null) {
            throw new BusinessException(400, "参数缺失");
        }
        Long conversationId = Long.valueOf(String.valueOf(cidObj));
        Integer rating = Integer.valueOf(String.valueOf(ratingObj));
        String feedback = body.get("feedback") != null ? String.valueOf(body.get("feedback")) : null;
        Consult consult = consultMapper.selectOne(new LambdaQueryWrapper<Consult>()
                .eq(Consult::getConversationId, conversationId)
                .last("LIMIT 1"));
        if (consult == null) {
            throw new BusinessException(404, "未找到关联的咨询记录");
        }
        if (consult.getElderId() != null && !consult.getElderId().equals(elder.getId())) {
            throw new BusinessException(403, "无权评价他人的咨询");
        }
        consult.setRating(rating);
        consult.setFeedback(feedback);
        consult.setStatus("COMPLETED");
        consultMapper.updateById(consult);
        return Result.success(true);
    }

    /** 可咨询专家列表（供家属发起在线咨询） */
    @GetMapping("/experts")
    public Result<List<Map<String, Object>>> experts() {
        List<Expert> experts = expertMapper.selectList(new LambdaQueryWrapper<Expert>()
                .isNotNull(Expert::getUserId)
                .orderByAsc(Expert::getName));
        List<Map<String, Object>> list = experts.stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("userId", e.getUserId());
            m.put("name", e.getName());
            m.put("title", e.getTitle());
            m.put("specialty", e.getSpecialty());
            m.put("institution", e.getInstitution());
            return m;
        }).toList();
        return Result.success(list);
    }

    private String fmt(LocalDateTime t) {
        return t != null ? t.format(FMT) : "";
    }

    private Elder currentElder() {
        Long userId = SecurityUtil.getLoginUserId();
        List<Long> elderIds = elderFamilyMapper.selectElderIdsByFamilyUserId(userId);
        if (elderIds.isEmpty()) {
            return null;
        }
        return elderMapper.selectById(elderIds.get(0));
    }
}
