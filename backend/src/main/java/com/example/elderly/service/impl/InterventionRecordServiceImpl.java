package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.elderly.dto.intervention.InterventionRecordSaveDTO;
import com.example.elderly.entity.InterventionRecord;
import com.example.elderly.mapper.InterventionMapper;
import com.example.elderly.mapper.InterventionRecordMapper;
import com.example.elderly.service.InterventionRecordService;
import com.example.elderly.vo.PageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor

public class InterventionRecordServiceImpl extends ServiceImpl<InterventionRecordMapper, InterventionRecord> implements InterventionRecordService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRecord(InterventionRecordSaveDTO dto) {
        InterventionRecord record = new InterventionRecord();
        record.setInterventionId(dto.getInterventionId());
        record.setElderName(dto.getElderName());
        record.setExecutor(dto.getExecutor());
        record.setStatus(dto.getStatus());
        record.setRating(dto.getRating());
        record.setNote(dto.getNote());
        record.setAttachments(dto.getAttachments());
        record.setExecuteDate(dto.getExecuteDate());
        record.setCreateTime(LocalDateTime.now());
        save(record);
    }

    @Override
    public PageVO<InterventionRecord> listByInterventionId(Long interventionId, Integer page, Integer size) {
        Page<InterventionRecord> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<InterventionRecord> wrapper = Wrappers.lambdaQuery(InterventionRecord.class)
                .eq(InterventionRecord::getInterventionId, interventionId)
                .orderByDesc(InterventionRecord::getExecuteDate);
        IPage<InterventionRecord> pageData = baseMapper.selectPage(pageObj, wrapper);
        PageVO<InterventionRecord> vo = new PageVO<>();
        vo.setTotal(pageData.getTotal());
        vo.setList(pageData.getRecords());
        return vo;
    }
}
