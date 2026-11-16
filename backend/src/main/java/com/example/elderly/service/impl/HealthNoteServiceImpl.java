package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.elderly.dto.healthNote.HealthNoteAddDTO;
import com.example.elderly.dto.healthNote.HealthNoteQueryDTO;
import com.example.elderly.entity.FallRecord;
import com.example.elderly.entity.GaitRecord;
import com.example.elderly.entity.HealthNote;
import com.example.elderly.mapper.HealthNoteMapper;
import com.example.elderly.service.IHealthNoteService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class HealthNoteServiceImpl extends ServiceImpl<HealthNoteMapper, HealthNote> implements IHealthNoteService {
    @Override
    public IPage<HealthNote> pageList(HealthNoteQueryDTO dto) {
        Page<HealthNote> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<HealthNote> wrapper = Wrappers.lambdaQuery();
        wrapper.like(StringUtils.hasText(dto.getElderName()),
                     HealthNote::getElderName,dto.getElderName())
                .eq(StringUtils.hasText(dto.getNoteType()),
                      HealthNote::getNoteType,dto.getNoteType())
                .eq(StringUtils.hasText(dto.getImportance()),
                        HealthNote::getImportance,dto.getImportance())
                .eq(StringUtils.hasText(dto.getFollowStatus()),
                        HealthNote::getFallowStatus,dto.getFollowStatus())
                .ge(dto.getStartRecordTime()!=null,
                        HealthNote::getRecordTime,dto.getStartRecordTime())
                .le(dto.getEndRecordTime()!=null,
                        HealthNote::getRecordTime,dto.getEndRecordTime())
                .orderByDesc(HealthNote::getRecordTime);
        baseMapper.selectPage(page,wrapper);
        return page;
    }

    @Override
    public Long add(HealthNoteAddDTO dto) {
        HealthNote note = new HealthNote();
        note.setElderId(dto.getElderId());
        note.setElderName(dto.getElderName());
        note.setRecordTime(dto.getRecordTime());
        note.setNoteType(dto.getNoteType());
        note.setContent(dto.getContent());
        note.setImportance(dto.getImportance());
        note.setFallowStatus(dto.getFallowStatus());
        note.setRecorder(dto.getRecorder());
        save(note);
        return note.getId();
    }

    @Override
    public Boolean deleteById(Long id) {
        HealthNote note = getById(id);
        if(note==null){
            return false;
        }
        return removeById(id);
    }
}
