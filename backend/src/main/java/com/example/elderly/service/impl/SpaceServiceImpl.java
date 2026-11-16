package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.elderly.dto.space.SpaceQueryDTO;
import com.example.elderly.dto.space.SpaceSaveDTO;
import com.example.elderly.entity.Space;
import com.example.elderly.entity.SpaceInspection;
import com.example.elderly.mapper.SpaceInspectionMapper;
import com.example.elderly.mapper.SpaceMapper;
import com.example.elderly.service.SpaceService;
import com.example.elderly.vo.PageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {
    private final SpaceInspectionMapper inspectionMapper;

    @Override
    public PageVO<Space> pageList(SpaceQueryDTO dto) {
        Page<Space> page = new Page<>(dto.getPage(), dto.getSize());
        var wrapper = Wrappers.lambdaQuery(Space.class);
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like(Space::getName, dto.getKeyword()).or().like(Space::getCode, dto.getKeyword()));
        }
        if (StringUtils.hasText(dto.getOccupancyStatus())) {
            wrapper.eq(Space::getOccupancyStatus, dto.getOccupancyStatus());
        }
        if (StringUtils.hasText(dto.getSafetyRating())) {
            wrapper.eq(Space::getSafetyRating, dto.getSafetyRating());
        }
        IPage<Space> pageData = baseMapper.selectPage(page, wrapper);
        PageVO<Space> vo = new PageVO<>();
        vo.setTotal(pageData.getTotal());
        vo.setList(pageData.getRecords());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateSpace(SpaceSaveDTO dto) {
        Space entity = new Space();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setBuilding(dto.getBuilding());
        entity.setFloor(dto.getFloor());
        entity.setLayout(dto.getLayout());
        entity.setArea(dto.getArea());
        entity.setOccupancyStatus(dto.getOccupancyStatus());
        entity.setElderId(dto.getElderId());
        entity.setMoveInDate(dto.getMoveInDate());
        entity.setFacilities(dto.getFacilities());
        entity.setRisks(dto.getRisks());
        entity.setStatus(dto.getStatus());
        entity.setSafetyRating(dto.getSafetyRating());
        entity.setNextInspection(dto.getNextInspection());
        entity.setRemark(dto.getRemark());
        if (dto.getId() == null) {
            entity.setCreateTime(LocalDateTime.now());
            entity.setCode("SPACE-" + System.currentTimeMillis());
        }
        entity.setUpdateTime(LocalDateTime.now());
        saveOrUpdate(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSpace(Long id) {
        inspectionMapper.delete(Wrappers.lambdaQuery(SpaceInspection.class).eq(SpaceInspection::getSpaceId, id));
        removeById(id);
    }

    @Override
    public Space getDetailById(Long id) {
        return getById(id);
    }
}
