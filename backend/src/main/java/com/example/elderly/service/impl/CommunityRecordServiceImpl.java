package com.example.elderly.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.elderly.dto.community.CommunityQueryDTO;
import com.example.elderly.dto.community.CommunityReviewDTO;
import com.example.elderly.dto.community.CommunitySaveDTO;
import com.example.elderly.entity.CommunityRecord;
import com.example.elderly.mapper.CommunityRecordMapper;
import com.example.elderly.service.CommunityRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CommunityRecordServiceImpl extends ServiceImpl<CommunityRecordMapper,CommunityRecord> implements CommunityRecordService {


    @Override
    public IPage<CommunityRecord> pageList(CommunityQueryDTO dto) {
        Page<CommunityRecord> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<CommunityRecord> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.hasText(dto.getElderName())){
           wrapper.like(CommunityRecord::getElderName,dto.getElderName());
        }
        if(StringUtils.hasText(dto.getReviewStatus())){
            wrapper.like(CommunityRecord::getReviewStatus,dto.getReviewStatus());
        }
        if(StringUtils.hasText(dto.getAlertType())){
            wrapper.like(CommunityRecord::getAlertType,dto.getAlertType());
        }
        wrapper.orderByDesc(CommunityRecord::getFullTime);
        return baseMapper.selectPage(page,wrapper);
    }

    @Override
    @Transactional
    public Long add(CommunitySaveDTO dto) {
        CommunityRecord record = new CommunityRecord();
        record.setCode("CM"+ UUID.randomUUID().toString().substring(0,8));
        record.setElderName(dto.getElderName());
        record.setAlertType(dto.getAlertType());
        record.setAction(dto.getAction());
        record.setHandler(dto.getHandler());
        record.setHandlerId(dto.getHandlerId());
        record.setResult(dto.getResult());
        record.setDescription(dto.getDescription());
        record.setAttachments(JSON.toJSONString(dto.getAttachments()));
        record.setCommunity(dto.getCommunity());
        record.setAlertCode(dto.getAlertCode());
        record.setReviewStatus("pending");
        LocalDateTime now = LocalDateTime.now();
       record.setFullTime(now);
       save( record);
        return record.getId();
    }

    @Override
    @Transactional
    public boolean edit(Long id, CommunitySaveDTO dto) {
        CommunityRecord record = getById(id);
        if(record==null){
            return false;
        }
        record.setElderName(dto.getElderName());
        record.setAlertType(dto.getAlertType());
        record.setAction(dto.getAction());
        record.setHandlerId(dto.getHandlerId());
        record.setHandler(dto.getHandler());
        record.setResult(dto.getResult());
        record.setDescription(dto.getDescription());
        record.setAttachments(JSON.toJSONString(dto.getAttachments()));
        record.setCommunity(dto.getCommunity());
        record.setAlertCode(dto.getAlertCode());
        return updateById(record);
    }

    @Override
    @Transactional
    public boolean review(Long id, CommunityReviewDTO dto) {
        CommunityRecord record = getById(id);
        if(record==null){
            return false;
        }
        record.setReview(dto.getReview() != null ? dto.getReview() : dto.getReviewer());
        record.setReviewStatus(dto.getReviewStatus());
        record.setReviewTime(LocalDateTime.now());
        record.setRejectReason(dto.getRejectReason());
        return updateById(record);
    }
}
