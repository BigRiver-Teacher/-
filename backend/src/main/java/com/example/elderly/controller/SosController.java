package com.example.elderly.controller;

import com.example.elderly.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderly.common.Result;
import com.example.elderly.entity.Alert;
import com.example.elderly.entity.Caregiver;
import com.example.elderly.entity.Elder;
import com.example.elderly.entity.ElderCaregiver;
import com.example.elderly.entity.SosRecord;
import com.example.elderly.mapper.AlertMapper;
import com.example.elderly.mapper.CareGiverMapper;
import com.example.elderly.mapper.ElderCaregiverMapper;
import com.example.elderly.mapper.ElderFamilyMapper;
import com.example.elderly.mapper.ElderMapper;
import com.example.elderly.mapper.SosRecordMapper;
import com.example.elderly.service.impl.NotificationHelper;
import com.example.elderly.util.SecurityUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 老人端一键呼救：家属点击 → 落库 sos_record + 生成 EMERGENCY 告警（陪护端告警中心可见）。
 */
@Tag(name = "一键呼救")
@RestController
@RequestMapping("/api/elder/sos")
public class SosController {

    @Resource
    private ElderFamilyMapper elderFamilyMapper;
    @Resource
    private ElderMapper elderMapper;
    @Resource
    private SosRecordMapper sosRecordMapper;
    @Resource
    private AlertMapper alertMapper;
    @Resource
    private ElderCaregiverMapper elderCaregiverMapper;
    @Resource
    private CareGiverMapper caregiverMapper;
    @Resource
    private NotificationHelper notificationHelper;

    @PostMapping
    public Result<Map<String, Object>> sos(@RequestBody(required = false) Map<String, String> body) {
        Long userId = SecurityUtil.getLoginUserId();
        Elder elder = currentElder(userId);
        if (elder == null) {
            throw new BusinessException(404, "未关联老人档案");
        }
        String location = (body != null && StringUtils.hasText(body.get("location")))
                ? body.get("location") : elder.getAddress();

        // 1. 落库呼救记录
        SosRecord sos = new SosRecord();
        sos.setElderId(elder.getId());
        sos.setElderName(elder.getName());
        sos.setFamilyUserId(userId);
        sos.setSosTime(LocalDateTime.now());
        sos.setLocation(location);
        sos.setStatus("PENDING");
        sos.setCreateTime(LocalDateTime.now());
        sos.setIsDeleted(0);
        sosRecordMapper.insert(sos);

        // 2. 生成 EMERGENCY 告警（陪护端按 elder_caregiver 关系可见）
        Alert alert = new Alert();
        alert.setElderId(elder.getId());
        alert.setElderName(elder.getName());
        alert.setAlertType("EMERGENCY");
        alert.setLevel("high");
        alert.setDescription("家属通过一键呼叫发起紧急求助");
        alert.setAlertTime(LocalDateTime.now());
        alert.setLocation(location);
        alert.setStatus("pending");
        alert.setContactPhone(elder.getPhone());
        alert.setEmergencyContact("家属");
        alert.setSuggestedAction("立即联系家属确认情况，安排护工上门查看");
        alert.setRead(0);
        alert.setCreateTime(LocalDateTime.now());
        alert.setIsDeleted(0);
        alertMapper.insert(alert);

        // 3. 关联告警编号
        sos.setRelateAlertCode(String.valueOf(alert.getId()));
        sosRecordMapper.updateById(sos);

        // 4. 站内通知负责该老人的护工
        List<ElderCaregiver> relations = elderCaregiverMapper.selectList(new LambdaQueryWrapper<ElderCaregiver>()
                .eq(ElderCaregiver::getElderId, elder.getId())
                .eq(ElderCaregiver::getIsDeleted, 0));
        for (ElderCaregiver rel : relations) {
            if (rel.getCaregiverId() == null) {
                continue;
            }
            Caregiver cg = caregiverMapper.selectById(rel.getCaregiverId());
            if (cg != null && cg.getUserId() != null) {
                notificationHelper.notify(cg.getUserId(), "一键呼救告警",
                        elder.getName() + " 发起紧急呼救，请尽快响应", "ALERT", "/caregiver/alert");
            }
        }
        // 5. 短信模拟通知家属（不接真实网关）
        notificationHelper.sms(elder.getName(), elder.getPhone(), "EMERGENCY",
                "【朝夕护行】" + elder.getName() + " 已发起一键呼救，请及时确认老人安全");

        Map<String, Object> data = new HashMap<>();
        data.put("sosId", sos.getId());
        data.put("alertId", alert.getId());
        data.put("alertCode", alert.getId());
        return Result.success(data);
    }

    private Elder currentElder(Long userId) {
        List<Long> elderIds = elderFamilyMapper.selectElderIdsByFamilyUserId(userId);
        if (elderIds.isEmpty()) {
            return null;
        }
        return elderMapper.selectById(elderIds.get(0));
    }
}
