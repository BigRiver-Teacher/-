package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.elderly.dto.GaitDTO;
import com.example.elderly.entity.GaitRecord;
import com.example.elderly.mapper.GaitMapper;
import com.example.elderly.service.GaitService;
import com.example.elderly.util.DataScopeUtil;
import com.example.elderly.util.LoginUserUtil;
import com.example.elderly.vo.PageVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;


@Service
public class GaitServiceImpl extends ServiceImpl<GaitMapper,GaitRecord> implements  GaitService {

    @Resource
    private DataScopeUtil dataScopeUtil;

    @Override
    public PageVO<GaitRecord> listPage(Integer page, Integer size, String name, String result) {
        Page<GaitRecord> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<GaitRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name),
                GaitRecord::getName,name);
        wrapper.eq(StringUtils.hasText(result),
                GaitRecord::getResult,result);
        // 数据范围过滤
        List<Long> elderIds = dataScopeUtil.getAccessibleElderIds(LoginUserUtil.getUserId());
        if (elderIds != null) {
            if (elderIds.isEmpty()) {
                wrapper.in(GaitRecord::getElderId, Collections.singletonList(-1L));
            } else {
                wrapper.in(GaitRecord::getElderId, elderIds);
            }
        }
        Page<GaitRecord> gaitPage = baseMapper.selectPage(pageObj, wrapper);
        PageVO<GaitRecord> vo = new PageVO<>();
        vo.setList(gaitPage.getRecords());
        vo.setTotal(gaitPage.getTotal());
        return vo;
    }

    @Override
    public Long addGait(GaitDTO dto) {
        GaitRecord record = new GaitRecord();
        record.setName(dto.getName());
        record.setElderId(dto.getElderId());
        record.setDate(dto.getDate());
        record.setSpeed(dto.getSpeed());
        record.setFrequency(dto.getFrequency());
        record.setStepLength(dto.getStepLength());
        record.setResult(dto.getResult());
        record.setScore(dto.getScore());
        save(record);
        return record.getId();
    }

    @Override
    public Boolean deleteGait(Long id) {
        return removeById(id);
    }
}
