package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.elderly.dto.space.SpaceInspectionSaveDTO;
import com.example.elderly.entity.SpaceInspection;
import com.example.elderly.mapper.SpaceInspectionMapper;
import com.example.elderly.service.SpaceInspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpaceInspectionServiceImpl extends ServiceImpl<SpaceInspectionMapper, SpaceInspection> implements SpaceInspectionService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addInspection(SpaceInspectionSaveDTO dto) {
        SpaceInspection entity = new SpaceInspection();
        entity.setSpaceId(dto.getSpaceId());
        entity.setDate(dto.getDate());
        entity.setResult(dto.getResult());
        entity.setInspector(dto.getInspector());
        entity.setType(dto.getType());
        entity.setIssue(dto.getIssue());
        entity.setRepairStatus(dto.getRepairStatus());
        entity.setDeadline(dto.getDeadline());
        entity.setCreateTime(LocalDateTime.now());
        save(entity);
    }

    @Override
    public List<SpaceInspection> listBySpaceId(Long spaceId) {
        return list(Wrappers.lambdaQuery(SpaceInspection.class)
                .eq(SpaceInspection::getSpaceId, spaceId)
                .orderByDesc(SpaceInspection::getDate));
    }
}
