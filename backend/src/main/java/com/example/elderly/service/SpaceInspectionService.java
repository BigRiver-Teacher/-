package com.example.elderly.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.elderly.dto.space.SpaceInspectionSaveDTO;
import com.example.elderly.entity.SpaceInspection;

import java.util.List;

public interface SpaceInspectionService extends IService<SpaceInspection> {
    void addInspection(SpaceInspectionSaveDTO dto);

    List<SpaceInspection> listBySpaceId(Long spaceId);
}
