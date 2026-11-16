package com.example.elderly.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.elderly.dto.caregiver.CaregiverQueryDTO;
import com.example.elderly.dto.caregiver.CaregiverSaveDTO;
import com.example.elderly.entity.Caregiver;

public interface CaregiverService extends IService<Caregiver> {
    IPage<Caregiver> pageList(CaregiverQueryDTO dto);
    Long addCaregiver(CaregiverSaveDTO dto);
    Boolean updateCaregiver(Long id, CaregiverSaveDTO dto);
}
