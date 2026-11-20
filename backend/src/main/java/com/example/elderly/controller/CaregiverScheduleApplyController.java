package com.example.elderly.controller;

import com.example.elderly.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderly.annotation.RequireRole;
import com.example.elderly.common.Result;
import com.example.elderly.dto.caregiver.CaregiverScheduleApplyDTO;
import com.example.elderly.dto.caregiver.CaregiverScheduleReviewDTO;
import com.example.elderly.entity.Caregiver;
import com.example.elderly.entity.CaregiverScheduleApply;
import com.example.elderly.entity.User;
import com.example.elderly.mapper.CareGiverMapper;
import com.example.elderly.mapper.CaregiverScheduleApplyMapper;
import com.example.elderly.mapper.UserMapper;
import com.example.elderly.service.impl.NotificationHelper;
import com.example.elderly.util.SecurityUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 陪护请假/换班申请：护工提交申请，陪护主管按社区审批。
 */
@Tag(name = "陪护请假审批")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/caregiver/schedule/apply")
public class CaregiverScheduleApplyController {

    private final CaregiverScheduleApplyMapper applyMapper;
    private final CareGiverMapper caregiverMapper;
    private final UserMapper userMapper;
    private final NotificationHelper notificationHelper;

    /** 护工提交请假/换班申请 */
    @PostMapping
    public Result<Long> apply(@Validated @RequestBody CaregiverScheduleApplyDTO dto) {
        Caregiver cg = currentCaregiver();
        if (cg == null) {
            throw new BusinessException(404, "陪护档案不存在");
        }
        CaregiverScheduleApply apply = new CaregiverScheduleApply();
        apply.setCaregiverId(cg.getId());
        apply.setCaregiverName(cg.getName());
        apply.setCommunity(cg.getCommunity());
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

    /** 护工查看自己的申请记录 */
    @GetMapping("/list")
    public Result<List<CaregiverScheduleApply>> myList() {
        Caregiver cg = currentCaregiver();
        if (cg == null) {
            throw new BusinessException(404, "陪护档案不存在");
        }
        List<CaregiverScheduleApply> list = applyMapper.selectList(new LambdaQueryWrapper<CaregiverScheduleApply>()
                .eq(CaregiverScheduleApply::getCaregiverId, cg.getId())
                .eq(CaregiverScheduleApply::getIsDeleted, 0)
                .orderByDesc(CaregiverScheduleApply::getCreateTime));
        return Result.success(list);
    }

    /** 主管查看本社区全部申请 */
    @RequireRole({"supervisor"})
    @GetMapping("/all")
    public Result<List<CaregiverScheduleApply>> all() {
        Caregiver sup = currentCaregiver();
        List<CaregiverScheduleApply> list = applyMapper.selectList(new LambdaQueryWrapper<CaregiverScheduleApply>()
                .eq(CaregiverScheduleApply::getIsDeleted, 0)
                .eq(sup != null && StringUtils.hasText(sup.getCommunity()),
                        CaregiverScheduleApply::getCommunity, sup != null ? sup.getCommunity() : null)
                .orderByDesc(CaregiverScheduleApply::getCreateTime));
        return Result.success(list);
    }

    /** 主管审批（仅本社区） */
    @RequireRole({"supervisor"})
    @PutMapping("/{id}/review")
    public Result<Boolean> review(@PathVariable Long id, @Validated @RequestBody CaregiverScheduleReviewDTO dto) {
        Long userId = SecurityUtil.getLoginUserId();
        User reviewer = userMapper.selectById(userId);
        Caregiver sup = currentCaregiver();
        CaregiverScheduleApply apply = applyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException(404, "申请不存在");
        }
        if (sup != null && StringUtils.hasText(sup.getCommunity())
                && !sup.getCommunity().equals(apply.getCommunity())) {
            throw new BusinessException(403, "无权审批本社区以外的申请");
        }
        apply.setStatus(dto.getStatus());
        apply.setReviewerId(userId);
        apply.setReviewerName(reviewer != null ? reviewer.getRealName() : "");
        apply.setReviewComment(dto.getComment());
        apply.setReviewTime(LocalDateTime.now());
        applyMapper.updateById(apply);

        // 通知申请人审批结果
        if (apply.getCaregiverId() != null) {
            Caregiver applicant = caregiverMapper.selectById(apply.getCaregiverId());
            if (applicant != null && applicant.getUserId() != null) {
                String typeText = "LEAVE".equals(apply.getApplyType()) ? "请假" : "换班";
                String statusText = "APPROVED".equals(dto.getStatus()) ? "已通过" : "已驳回";
                notificationHelper.notify(applicant.getUserId(), "请假审批结果",
                        "您的" + typeText + "申请" + statusText, "SYSTEM", "/caregiver/workbench");
            }
        }
        return Result.success(true);
    }

    private Caregiver currentCaregiver() {
        Long userId = SecurityUtil.getLoginUserId();
        return caregiverMapper.selectOne(new LambdaQueryWrapper<Caregiver>()
                .eq(Caregiver::getUserId, userId)
                .last("LIMIT 1"));
    }
}
