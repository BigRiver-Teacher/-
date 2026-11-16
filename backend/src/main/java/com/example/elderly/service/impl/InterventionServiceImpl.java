package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.elderly.dto.intervention.InterventionAuditDTO;
import com.example.elderly.dto.intervention.InterventionCompleteDTO;
import com.example.elderly.dto.intervention.InterventionQueryDTO;
import com.example.elderly.dto.intervention.InterventionSaveDTO;
import com.example.elderly.entity.Intervention;
import com.example.elderly.entity.InterventionRecord;
import com.example.elderly.mapper.InterventionMapper;
import com.example.elderly.mapper.InterventionRecordMapper;
import com.example.elderly.service.InterventionService;
import com.example.elderly.vo.PageVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InterventionServiceImpl extends ServiceImpl<InterventionMapper, Intervention> implements InterventionService {
    private final InterventionRecordMapper recordMapper;

    @Override
    public PageVO<Intervention> pageList(InterventionQueryDTO dto) {
        Page<Intervention> page = new Page<>(dto.getPage(), dto.getSize());
        var wrapper = Wrappers.lambdaQuery(Intervention.class);
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like(Intervention::getName, dto.getKeyword()).or().like(Intervention::getCode, dto.getKeyword()));
        }
        if (StringUtils.hasText(dto.getType())){
            wrapper.eq(Intervention::getType, dto.getType());
        }
        if (StringUtils.hasText(dto.getStatus())){
            wrapper.eq(Intervention::getStatus, dto.getStatus());
        }
        if (StringUtils.hasText(dto.getPriority())){
            wrapper.eq(Intervention::getPriority, dto.getPriority());
        }
        IPage<Intervention> pageData = baseMapper.selectPage(page, wrapper);
        PageVO<Intervention> vo = new PageVO<>();
        vo.setTotal(pageData.getTotal());
        vo.setList(pageData.getRecords());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addIntervention(InterventionSaveDTO dto) {
        Intervention entity = new Intervention();
        entity.setCode("INTER-" + System.currentTimeMillis());
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setPriority(dto.getPriority());
        entity.setStatus("draft"); // 新建默认草稿
        entity.setLeader(dto.getLeader());
        entity.setTotalWeeks(dto.getTotalWeeks());
        entity.setCurrentWeek(1);
        entity.setFrequency(dto.getFrequency());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setAssociatedElders(dto.getAssociatedElders());
        entity.setGoals(dto.getGoals());
        entity.setMeasures(dto.getMeasures());
        entity.setNotes(dto.getNotes());
        entity.setProgress(0);
        entity.setExecutedCount(0);
        entity.setTotalCount(dto.getTotalWeeks() * 7);
        entity.setAbnormalCount(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateIntervention(Long id, InterventionSaveDTO dto) {
        Intervention entity = getById(id);
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setPriority(dto.getPriority());
        entity.setLeader(dto.getLeader());
        entity.setTotalWeeks(dto.getTotalWeeks());
        entity.setFrequency(dto.getFrequency());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setAssociatedElders(dto.getAssociatedElders());
        entity.setGoals(dto.getGoals());
        entity.setMeasures(dto.getMeasures());
        entity.setNotes(dto.getNotes());
        entity.setUpdateTime(LocalDateTime.now());
        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteIntervention(Long id) {
        // 删除关联执行记录
        recordMapper.delete(Wrappers.lambdaQuery(InterventionRecord.class).eq(InterventionRecord::getInterventionId, id));
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditIntervention(Long id, InterventionAuditDTO dto) {
        Intervention entity = getById(id);
        entity.setStatus(dto.getStatus());
        entity.setUpdateTime(LocalDateTime.now());
        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pauseIntervention(Long id) {
        Intervention entity = getById(id);
        entity.setStatus("pending");
        entity.setUpdateTime(LocalDateTime.now());
        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void terminateIntervention(Long id) {
        Intervention entity = getById(id);
        entity.setStatus("terminated");
        entity.setUpdateTime(LocalDateTime.now());
        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeIntervention(Long id, InterventionCompleteDTO dto) {
        Intervention entity = getById(id);
        entity.setStatus("completed");
        entity.setRating(dto.getRating());
        entity.setUpdateTime(LocalDateTime.now());
        updateById(entity);
    }

    @Override
    public Intervention getDetailById(Long id) {
        return getById(id);
    }
}
