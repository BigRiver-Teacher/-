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
import com.example.elderly.entity.Alert;
import com.example.elderly.mapper.AlertMapper;
import com.example.elderly.service.AlertService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
@Service
public class AlertServiceImpl extends ServiceImpl<AlertMapper, Alert> implements AlertService {


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
}
