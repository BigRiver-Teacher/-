package com.example.elderly.controller;

import com.example.elderly.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderly.common.Result;
import com.example.elderly.entity.Consult;
import com.example.elderly.entity.Expert;
import com.example.elderly.mapper.ConsultMapper;
import com.example.elderly.mapper.ExpertMapper;
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
 * 专家端门户接口：当前登录专家的个人档案与工作台统计。
 */
@Tag(name = "专家门户")
@RestController
@RequestMapping("/api/expert/portal")
public class ExpertPortalController {

    @Resource
    private ExpertMapper expertMapper;
    @Resource
    private ConsultMapper consultMapper;

    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        Expert expert = currentExpert();
        if (expert == null) {
            throw new BusinessException(404, "专家档案不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", expert.getId());
        data.put("name", expert.getName());
        data.put("title", expert.getTitle());
        data.put("specialty", expert.getSpecialty());
        data.put("institution", expert.getInstitution());
        data.put("phone", expert.getPhone());
        data.put("rating", expert.getRating());
        data.put("schedule", expert.getSchedule());
        data.put("todayCount", expert.getTodayCount());
        data.put("totalCount", expert.getTotalCount());
        return Result.success(data);
    }

    @GetMapping("/workbench")
    public Result<Map<String, Object>> workbench() {
        Expert expert = currentExpert();
        if (expert == null) {
            throw new BusinessException(404, "专家档案不存在");
        }
        Long expertId = expert.getId();
        // 今日咨询数
        Long todayCount = consultMapper.selectCount(new LambdaQueryWrapper<Consult>()
                .eq(Consult::getExpertId, expertId)
                .ge(Consult::getConsultTime, LocalDate.now().atStartOfDay()));
        // 待回复（status=pending）
        Long pendingCount = consultMapper.selectCount(new LambdaQueryWrapper<Consult>()
                .eq(Consult::getExpertId, expertId)
                .eq(Consult::getStatus, "pending"));
        // 累计咨询数
        Long totalCount = consultMapper.selectCount(new LambdaQueryWrapper<Consult>()
                .eq(Consult::getExpertId, expertId));

        Map<String, Object> data = new HashMap<>();
        data.put("expertName", expert.getName());
        data.put("title", expert.getTitle());
        data.put("todayCount", todayCount);
        data.put("pendingCount", pendingCount);
        data.put("totalCount", totalCount);
        data.put("rating", expert.getRating());

        // 近 7 天咨询趋势（供折线图）
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            Long count = consultMapper.selectCount(new LambdaQueryWrapper<Consult>()
                    .eq(Consult::getExpertId, expertId)
                    .ge(Consult::getConsultTime, day.atStartOfDay())
                    .lt(Consult::getConsultTime, day.plusDays(1).atStartOfDay()));
            Map<String, Object> point = new HashMap<>();
            point.put("date", day.toString().substring(5));
            point.put("count", count);
            trend.add(point);
        }
        data.put("trend", trend);
        return Result.success(data);
    }

    private Expert currentExpert() {
        Long userId = SecurityUtil.getLoginUserId();
        return expertMapper.selectOne(new LambdaQueryWrapper<Expert>()
                .eq(Expert::getUserId, userId)
                .last("LIMIT 1"));
    }
}
