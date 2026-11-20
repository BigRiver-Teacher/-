package com.example.elderly.controller;

import com.example.elderly.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderly.common.Result;
import com.example.elderly.dto.Expert.ExpertScheduleApplyDTO;
import com.example.elderly.dto.Expert.ExpertScheduleReviewDTO;
import com.example.elderly.entity.Expert;
import com.example.elderly.entity.ExpertScheduleApply;
import com.example.elderly.entity.User;
import com.example.elderly.mapper.ExpertMapper;
import com.example.elderly.mapper.ExpertScheduleApplyMapper;
import com.example.elderly.mapper.UserMapper;
import com.example.elderly.service.impl.NotificationHelper;
import com.example.elderly.util.SecurityUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "专家排班请假")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expert/schedule/apply")
public class ExpertScheduleApplyController {

    private final ExpertScheduleApplyMapper applyMapper;
    private final ExpertMapper expertMapper;
    private final UserMapper userMapper;
    private final NotificationHelper notificationHelper;

    @PostMapping
    public Result<Long> apply(@Validated @RequestBody ExpertScheduleApplyDTO dto) {
        Expert expert = currentExpert();
        if (expert == null) {
            throw new BusinessException(404, "专家档案不存在");
        }
        ExpertScheduleApply apply = new ExpertScheduleApply();
        apply.setExpertId(expert.getId());
        apply.setExpertName(expert.getName());
        apply.setApplyType(dto.getApplyType());
        apply.setStartTime(dto.getStartTime());
        apply.setEndTime(dto.getEndTime());
        apply.setReason(dto.getReason());
        apply.setStatus("PENDING");
        apply.setCreateTime(LocalDateTime.now());
        apply.setIsDeleted(0);
        applyMapper.insert(apply);
        return Result.success(apply.getId());
    }

    @GetMapping("/list")
    public Result<List<ExpertScheduleApply>> myList() {
        Expert expert = currentExpert();
        if (expert == null) {
            throw new BusinessException(404, "专家档案不存在");
        }
        List<ExpertScheduleApply> list = applyMapper.selectList(new LambdaQueryWrapper<ExpertScheduleApply>()
                .eq(ExpertScheduleApply::getExpertId, expert.getId())
                .eq(ExpertScheduleApply::getIsDeleted, 0)
                .orderByDesc(ExpertScheduleApply::getCreateTime));
        return Result.success(list);
    }

    @GetMapping("/all")
    public Result<List<ExpertScheduleApply>> all() {
        List<ExpertScheduleApply> list = applyMapper.selectList(new LambdaQueryWrapper<ExpertScheduleApply>()
                .eq(ExpertScheduleApply::getIsDeleted, 0)
                .orderByDesc(ExpertScheduleApply::getCreateTime));
        return Result.success(list);
    }

    @PutMapping("/{id}/review")
    public Result<Boolean> review(@PathVariable Long id, @Validated @RequestBody ExpertScheduleReviewDTO dto) {
        Long userId = SecurityUtil.getLoginUserId();
        User reviewer = userMapper.selectById(userId);
        ExpertScheduleApply apply = applyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException(404, "申请不存在");
        }
        apply.setStatus(dto.getStatus());
        apply.setReviewerId(userId);
        apply.setReviewerName(reviewer != null ? reviewer.getRealName() : "");
        apply.setReviewComment(dto.getComment());
        apply.setReviewTime(LocalDateTime.now());
        applyMapper.updateById(apply);

        // 通知申请人审批结果
        if (apply.getExpertId() != null) {
            Expert applicant = expertMapper.selectById(apply.getExpertId());
            if (applicant != null && applicant.getUserId() != null) {
                String typeText = "LEAVE".equals(apply.getApplyType()) ? "请假" : "换班";
                String statusText = "APPROVED".equals(dto.getStatus()) ? "已通过" : "已驳回";
                notificationHelper.notify(applicant.getUserId(), "请假审批结果",
                        "您的" + typeText + "申请" + statusText, "SYSTEM", "/expert/schedule");
            }
        }
        return Result.success(true);
    }

    private Expert currentExpert() {
        Long userId = SecurityUtil.getLoginUserId();
        return expertMapper.selectOne(new LambdaQueryWrapper<Expert>()
                .eq(Expert::getUserId, userId)
                .last("LIMIT 1"));
    }
}
