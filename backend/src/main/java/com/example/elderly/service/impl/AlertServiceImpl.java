package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.elderly.common.AlertHandleStatusEnum;
import com.example.elderly.dto.alert.AlertBatchDTO;
import com.example.elderly.dto.alert.AlertProcessDTO;
import com.example.elderly.dto.alert.AlertQueryDTO;
import com.example.elderly.dto.alert.AlertCreateDTO;
import com.example.elderly.entity.Alert;
import com.example.elderly.mapper.AlertMapper;
import com.example.elderly.service.AlertService;
import com.example.elderly.util.DataScopeUtil;
import com.example.elderly.util.LoginUserUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
@Service
public class AlertServiceImpl extends ServiceImpl<AlertMapper, Alert> implements AlertService {

    @Resource
    private DataScopeUtil dataScopeUtil;

    @Override
    public IPage<Alert> pageList(AlertQueryDTO dto) {
        Page<Alert> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.hasLength(dto.getAlertType())){
            wrapper.eq(Alert::getAlertType,dto.getAlertType());
        }

        if(StringUtils.hasLength(dto.getLevel())){
            wrapper.eq(Alert::getLevel,dto.getLevel());
        }
        if(StringUtils.hasLength(dto.getStatus())){
            wrapper.eq(Alert::getStatus,dto.getStatus());
        }
        if(StringUtils.hasLength(dto.getElderName())){
            wrapper.eq(Alert::getElderName,dto.getElderName());
        }
        if(dto.getStartDate()!=null){
            wrapper.eq(Alert::getAlertTime,dto.getStartDate());
        }
        if(dto.getEndDate()!=null){
            wrapper.eq(Alert::getAlertTime,dto.getEndDate());
        }
        // 数据范围过滤：非管理员仅能访问其服务关系内老人的告警
        List<Long> elderIds = dataScopeUtil.getAccessibleElderIds(LoginUserUtil.getUserId());
        if (elderIds != null) {
            if (elderIds.isEmpty()) {
                wrapper.in(Alert::getElderId, Collections.singletonList(-1L));
            } else {
                wrapper.in(Alert::getElderId, elderIds);
            }
        }
        wrapper.orderByDesc(Alert::getAlertTime);
        return baseMapper.selectPage(page,wrapper);
    }

    @Override
    @Transactional
    public boolean processAlert(Long id, AlertProcessDTO dto) {
        Alert alert = getById(id);
        if(alert==null){
            return false;
        }
        alert.setStatus(AlertHandleStatusEnum.RESOLVED.getCode());
        alert.setHandler(dto.getHandler());
        alert.setHandleNote(dto.getHandleNote());
        alert.setHandleTime(LocalDateTime.now());
        alert.setRead(1);

        return updateById(alert);
    }

    @Override
    @Transactional
    public boolean ignoreAlert(Long id) {
        Alert alert = getById(id);
        if(alert==null){
            return false;
        }
        alert.setStatus(AlertHandleStatusEnum.IGNORED.getCode());
        alert.setRead(1);
        return updateById( alert);
    }

    @Override
    public boolean readAlert(Long id) {
        Alert alert = getById(id);
        if(alert==null){
            return false;
        }
        alert.setRead(1);
        return updateById( alert);
    }

    @Override
    public boolean batchProcess(AlertBatchDTO dto) {
        for (Long aid : dto.getIds()) {
            Alert alert = getById(aid);
            if(alert==null){
                 continue;
            }
            alert.setStatus(AlertHandleStatusEnum.RESOLVED.getCode());
            alert.setHandler(dto.getHandle());
            alert.setHandleNote(dto.getHandleNote());
            alert.setHandleTime(LocalDateTime.now());
            alert.setRead(1);
            updateById(alert);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean batchIgnore(AlertBatchDTO dto) {
        for (Long aid : dto.getIds()) {
            Alert alert = getById(aid);
            if(alert==null){
                continue;
            }
            alert.setStatus(AlertHandleStatusEnum.IGNORED.getCode());
            alert.setRead(1);
            updateById(alert);
        }
        return true;
    }

    /**
     * 创建告警。
     *
     * 供视频检测等外部能力写入：跌倒属于最高优先级，默认 urgent + pending + 未读；
     * 前端列表的类型筛选用 'FALL'、级别筛选用 urgent/important/normal，
     * 因此这里不使用 AlertTypeEnum 里的历史拼写码。
     */
    @Override
    @Transactional
    public Long createAlert(AlertCreateDTO dto) {
        Alert alert = new Alert();
        alert.setElderName(dto.getElderName());
        alert.setElderId(dto.getElderId());
        alert.setAlertType(StringUtils.hasText(dto.getAlertType()) ? dto.getAlertType() : "FALL");
        alert.setLevel(StringUtils.hasText(dto.getLevel()) ? dto.getLevel() : "urgent");
        alert.setDescription(dto.getDescription());
        alert.setAlertTime(dto.getAlertTime() != null ? dto.getAlertTime() : LocalDateTime.now());
        alert.setLocation(dto.getLocation());
        alert.setContactPhone(dto.getContactPhone());
        alert.setEmergencyContact(dto.getEmergencyContact());
        alert.setSuggestedAction(dto.getSuggestedAction());
        alert.setStatus(AlertHandleStatusEnum.PENDING.getCode());
        alert.setRead(0);
        save(alert);
        return alert.getId();
    }
}
