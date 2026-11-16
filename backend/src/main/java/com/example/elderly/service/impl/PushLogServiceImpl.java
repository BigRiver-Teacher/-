package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.elderly.common.PushStatusEnum;
import com.example.elderly.dto.push.PushBatchRetryDTO;
import com.example.elderly.dto.push.PushLogQueryDTO;
import com.example.elderly.entity.PushLog;
import com.example.elderly.mapper.PushLogMapper;
import com.example.elderly.service.PushLogService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PushLogServiceImpl extends ServiceImpl<PushLogMapper, PushLog> implements PushLogService {
    @Override
    public IPage<PushLog> pageList(PushLogQueryDTO dto) {
        Page<PushLog> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<PushLog> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.hasText(dto.getChannel())){
            wrapper.eq(PushLog::getChannel,dto.getChannel());
        }
        if(StringUtils.hasText(dto.getStatus())){
            wrapper.eq(PushLog::getStatus,dto.getStatus());
        }
        if(StringUtils.hasText(dto.getElderName())){
            wrapper.like(PushLog::getElderName,dto.getElderName());
        }
        wrapper.orderByDesc(PushLog::getTime);

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public boolean retryPush(Long id) {
        PushLog log=getById(id);
        if(log==null){
            return false;
        }
        log.setStatus(PushStatusEnum.SENDING.getCode());
        log.setRetryCount(log.getRetryCount()+1);
        return updateById(log);
    }

    @Override
    @Transactional
    public boolean batchRetry(PushBatchRetryDTO dto) {
        for (Long lid : dto.getIds()) {
            retryPush(lid);
        }
        return true;
    }
}
