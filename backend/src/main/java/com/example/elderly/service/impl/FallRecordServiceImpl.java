package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.elderly.dto.fall.FallRecordAddDTO;
import com.example.elderly.dto.fall.FallRecordQueryDTO;
import com.example.elderly.dto.fall.FallRecordUpdateDTO;
import com.example.elderly.entity.FallRecord;
import com.example.elderly.mapper.FallRecordMapper;
import com.example.elderly.service.IFallRecordService;
import com.example.elderly.util.DataScopeUtil;
import com.example.elderly.util.LoginUserUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
public class FallRecordServiceImpl extends ServiceImpl<FallRecordMapper, FallRecord> implements IFallRecordService {

    @Resource
    private DataScopeUtil dataScopeUtil;

    @Override
    public IPage<FallRecord> pageList(FallRecordQueryDTO dto) {
        Page<FallRecord> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<FallRecord> wrapper = Wrappers.lambdaQuery();
        wrapper.like(StringUtils.hasText(dto.getElderName()),
                        FallRecord::getElderName,dto.getElderName())
                .eq(StringUtils.hasText(dto.getSeverity()),
                        FallRecord::getSeverity,dto.getSeverity())
                .eq(StringUtils.hasText(dto.getRecoverStatus()),
                        FallRecord::getRecoverStatus,dto.getRecoverStatus())
                .ge(dto.getStartFallTime()!=null,
                        FallRecord::getFallTime,dto.getStartFallTime())
                .le(dto.getEndFallTime()!=null,
                        FallRecord::getFallTime,dto.getEndFallTime());
        // 数据范围过滤
        List<Long> elderIds = dataScopeUtil.getAccessibleElderIds(LoginUserUtil.getUserId());
        if (elderIds != null) {
            if (elderIds.isEmpty()) {
                wrapper.in(FallRecord::getElderId, Collections.singletonList(-1L));
            } else {
                wrapper.in(FallRecord::getElderId, elderIds);
            }
        }
        return baseMapper.selectPage(page,wrapper);
    }

    @Override
    public Long add(FallRecordAddDTO dto) {
        FallRecord record = new FallRecord();
        record.setElderId(dto.getElderId());
        record.setElderName(dto.getElderName());
        record.setFallTime(dto.getFallTime());
        record.setLocation(dto.getLocation());
        record.setCause(dto.getCause());
        record.setInjuryPart(dto.getInjuryPart());
        record.setSeverity(dto.getSeverity());
        record.setRecoverStatus(dto.getRecoverStatus());
        record.setTreatment(dto.getTreatment());
        record.setHandler(dto.getHandler());
        save(record);
        return record.getId();
    }

    @Override
    public Boolean update(FallRecordUpdateDTO dto) {
        FallRecord record = getById(dto.getId());
        if(record==null){
            return false;
        }
        record.setElderId(dto.getElderId());
        record.setElderName(dto.getElderName());
        record.setFallTime(dto.getFallTime());
        record.setLocation(dto.getLocation());
        record.setCause(dto.getCause());
        record.setInjuryPart(dto.getInjuryPart());
        record.setSeverity(dto.getSeverity());
        record.setRecoverStatus(dto.getRecoverStatus());
        record.setTreatment(dto.getTreatment());
        record.setHandler(dto.getHandler());
        return updateById( record);
    }

    @Override
    public Boolean removeByIdValid(Long id) {
        FallRecord record = getById(id);
        if(record==null){
            return false;
        }
        return removeById(id);
    }
}
