package com.example.elderly.controller;

import com.example.elderly.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderly.common.Result;
import com.example.elderly.entity.Alert;
import com.example.elderly.entity.Caregiver;
import com.example.elderly.entity.Elder;
import com.example.elderly.entity.ScheduleTask;
import com.example.elderly.mapper.AlertMapper;
import com.example.elderly.mapper.CareGiverMapper;
import com.example.elderly.mapper.ElderCaregiverMapper;
import com.example.elderly.mapper.ElderMapper;
import com.example.elderly.mapper.ScheduleTaskMapper;
import com.example.elderly.util.SecurityUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 陪护端门户接口：当前陪护的档案、工作台统计、负责的老人与今日任务。
 */
@Tag(name = "陪护门户")
@RestController
@RequestMapping("/api/caregiver/portal")
public class CaregiverPortalController {

    @Resource
    private CareGiverMapper caregiverMapper;
    @Resource
    private ElderCaregiverMapper elderCaregiverMapper;
    @Resource
    private ScheduleTaskMapper scheduleTaskMapper;
    @Resource
    private AlertMapper alertMapper;
    @Resource
    private ElderMapper elderMapper;

    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        Caregiver cg = currentCaregiver();
        if (cg == null) {
            throw new BusinessException(404, "陪护档案不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", cg.getId());
        data.put("name", cg.getName());
        data.put("position", cg.getPosition());
        data.put("phone", cg.getPhone());
        data.put("community", cg.getCommunity());
        return Result.success(data);
    }

    @GetMapping("/workbench")
    public Result<Map<String, Object>> workbench() {
        Long userId = SecurityUtil.getLoginUserId();
        Caregiver cg = currentCaregiver();
        if (cg == null) {
            throw new BusinessException(404, "陪护档案不存在");
        }
        List<Long> elderIds = elderCaregiverMapper.selectElderIdsByCaregiverUserId(userId);
        // 负责老人数
        long elderCount = elderIds.size();
        // 今日任务数
        Long taskCount = scheduleTaskMapper.selectCount(new LambdaQueryWrapper<ScheduleTask>()
                .eq(ScheduleTask::getHandlerId, userId)
                .ge(ScheduleTask::getTaskDate, LocalDate.now().atStartOfDay())
                .lt(ScheduleTask::getTaskDate, LocalDate.now().plusDays(1).atStartOfDay()));
        // 待处置告警数
        long alertCount = 0;
        if (!elderIds.isEmpty()) {
            alertCount = alertMapper.selectCount(new LambdaQueryWrapper<Alert>()
                    .in(Alert::getElderId, elderIds)
                    .eq(Alert::getStatus, "pending"));
        }
        Map<String, Object> data = new HashMap<>();
        data.put("name", cg.getName());
        data.put("position", cg.getPosition());
        data.put("elderCount", elderCount);
        data.put("taskCount", taskCount);
        data.put("alertCount", alertCount);
        return Result.success(data);
    }

    @GetMapping("/elders")
    public Result<List<Elder>> myElders() {
        Long userId = SecurityUtil.getLoginUserId();
        List<Long> elderIds = elderCaregiverMapper.selectElderIdsByCaregiverUserId(userId);
        if (elderIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        return Result.success(elderMapper.selectBatchIds(elderIds));
    }

    @GetMapping("/tasks")
    public Result<List<ScheduleTask>> myTasks() {
        Long userId = SecurityUtil.getLoginUserId();
        List<ScheduleTask> tasks = scheduleTaskMapper.selectList(new LambdaQueryWrapper<ScheduleTask>()
                .eq(ScheduleTask::getHandlerId, userId)
                .ge(ScheduleTask::getTaskDate, LocalDate.now().atStartOfDay())
                .lt(ScheduleTask::getTaskDate, LocalDate.now().plusDays(1).atStartOfDay())
                .orderByAsc(ScheduleTask::getPriority));
        return Result.success(tasks);
    }

    private Caregiver currentCaregiver() {
        Long userId = SecurityUtil.getLoginUserId();
        return caregiverMapper.selectOne(new LambdaQueryWrapper<Caregiver>()
                .eq(Caregiver::getUserId, userId)
                .last("LIMIT 1"));
    }
}
