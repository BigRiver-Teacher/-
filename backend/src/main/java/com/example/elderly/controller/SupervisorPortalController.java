package com.example.elderly.controller;

import com.example.elderly.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderly.annotation.RequireRole;
import com.example.elderly.common.Result;
import com.example.elderly.entity.Caregiver;
import com.example.elderly.entity.CaregiverSchedule;
import com.example.elderly.entity.ElderCaregiver;
import com.example.elderly.mapper.CareGiverMapper;
import com.example.elderly.mapper.CaregiverScheduleMapper;
import com.example.elderly.mapper.ElderCaregiverMapper;
import com.example.elderly.util.SecurityUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 陪护主管门户：按社区查看全组护工与老人、课程表式排班。
 */
@Tag(name = "陪护主管门户")
@RestController
@RequestMapping("/api/supervisor/portal")
@RequireRole({"supervisor"})
public class SupervisorPortalController {

    @Resource
    private CareGiverMapper caregiverMapper;
    @Resource
    private CaregiverScheduleMapper scheduleMapper;
    @Resource
    private ElderCaregiverMapper elderCaregiverMapper;

    /** 主管档案（含社区） */
    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        Caregiver sup = currentCaregiver();
        if (sup == null) {
            throw new BusinessException(404, "主管档案不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", sup.getId());
        data.put("name", sup.getName());
        data.put("position", sup.getPosition());
        data.put("community", sup.getCommunity());
        return Result.success(data);
    }

    /** 全组视角：本社区护工列表 + 各自负责的老人 */
    @GetMapping("/team")
    public Result<Map<String, Object>> team() {
        Caregiver sup = currentCaregiver();
        if (sup == null) {
            throw new BusinessException(404, "主管档案不存在");
        }
        String community = sup.getCommunity();

        List<Caregiver> caregivers = caregiverMapper.selectList(new LambdaQueryWrapper<Caregiver>()
                .eq(StringUtils.hasText(community), Caregiver::getCommunity, community)
                .ne(Caregiver::getId, sup.getId())
                .orderByAsc(Caregiver::getName));

        List<Map<String, Object>> members = new ArrayList<>();
        for (Caregiver cg : caregivers) {
            List<ElderCaregiver> relations = elderCaregiverMapper.selectList(new LambdaQueryWrapper<ElderCaregiver>()
                    .eq(ElderCaregiver::getCaregiverId, cg.getId())
                    .eq(ElderCaregiver::getIsDeleted, 0));
            List<String> elderNames = relations.stream()
                    .map(ElderCaregiver::getElderName)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .collect(Collectors.toList());
            Map<String, Object> m = new HashMap<>();
            m.put("id", cg.getId());
            m.put("name", cg.getName());
            m.put("position", cg.getPosition());
            m.put("phone", cg.getPhone());
            m.put("elderCount", elderNames.size());
            m.put("elders", elderNames);
            members.add(m);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("supervisor", sup.getName());
        data.put("community", community);
        data.put("members", members);
        return Result.success(data);
    }

    /** 课程表排班查询：按社区 + 日期范围 */
    @GetMapping("/schedule")
    public Result<List<CaregiverSchedule>> schedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        Caregiver sup = currentCaregiver();
        if (sup == null) {
            throw new BusinessException(404, "主管档案不存在");
        }
        LocalDate s = start != null ? start : LocalDate.now();
        LocalDate e = end != null ? end : LocalDate.now().plusDays(6);
        List<CaregiverSchedule> list = scheduleMapper.selectList(new LambdaQueryWrapper<CaregiverSchedule>()
                .eq(StringUtils.hasText(sup.getCommunity()), CaregiverSchedule::getCommunity, sup.getCommunity())
                .ge(CaregiverSchedule::getWorkDate, s)
                .le(CaregiverSchedule::getWorkDate, e)
                .eq(CaregiverSchedule::getIsDeleted, 0)
                .orderByAsc(CaregiverSchedule::getWorkDate)
                .orderByAsc(CaregiverSchedule::getCaregiverName));
        return Result.success(list);
    }

    /** 保存排班（同人同日期覆盖） */
    @PostMapping("/schedule/save")
    public Result<Long> saveSchedule(@RequestBody CaregiverSchedule req) {
        Caregiver sup = currentCaregiver();
        if (sup == null) {
            throw new BusinessException(404, "主管档案不存在");
        }
        if (req.getCaregiverId() == null || req.getWorkDate() == null) {
            throw new BusinessException(400, "陪护与日期不能为空");
        }
        Caregiver cg = caregiverMapper.selectById(req.getCaregiverId());
        if (cg == null) {
            throw new BusinessException(404, "陪护不存在");
        }
        if (StringUtils.hasText(sup.getCommunity()) && !sup.getCommunity().equals(cg.getCommunity())) {
            throw new BusinessException(403, "无权给本社区以外的陪护排班");
        }
        CaregiverSchedule exist = scheduleMapper.selectOne(new LambdaQueryWrapper<CaregiverSchedule>()
                .eq(CaregiverSchedule::getCaregiverId, req.getCaregiverId())
                .eq(CaregiverSchedule::getWorkDate, req.getWorkDate())
                .eq(CaregiverSchedule::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (exist != null) {
            exist.setShift(req.getShift());
            exist.setStartTime(req.getStartTime());
            exist.setEndTime(req.getEndTime());
            exist.setNote(req.getNote());
            exist.setUpdateTime(LocalDateTime.now());
            scheduleMapper.updateById(exist);
            return Result.success(exist.getId());
        }
        req.setId(null);
        req.setCaregiverName(cg.getName());
        req.setCommunity(cg.getCommunity());
        req.setCreateTime(LocalDateTime.now());
        req.setUpdateTime(LocalDateTime.now());
        req.setIsDeleted(0);
        scheduleMapper.insert(req);
        return Result.success(req.getId());
    }

    /** 删除排班 */
    @DeleteMapping("/schedule/{id}")
    public Result<Boolean> deleteSchedule(@PathVariable Long id) {
        Caregiver sup = currentCaregiver();
        if (sup == null) {
            throw new BusinessException(404, "主管档案不存在");
        }
        CaregiverSchedule item = scheduleMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(404, "排班不存在");
        }
        if (StringUtils.hasText(sup.getCommunity()) && !sup.getCommunity().equals(item.getCommunity())) {
            throw new BusinessException(403, "无权删除本社区以外的排班");
        }
        item.setIsDeleted(1);
        item.setUpdateTime(LocalDateTime.now());
        scheduleMapper.updateById(item);
        return Result.success(true);
    }

    private Caregiver currentCaregiver() {
        Long userId = SecurityUtil.getLoginUserId();
        return caregiverMapper.selectOne(new LambdaQueryWrapper<Caregiver>()
                .eq(Caregiver::getUserId, userId)
                .last("LIMIT 1"));
    }
}
