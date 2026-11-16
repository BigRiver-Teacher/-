package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.elderly.dto.caregiver.CaregiverQueryDTO;
import com.example.elderly.dto.caregiver.CaregiverSaveDTO;
import com.example.elderly.entity.Caregiver;
import com.example.elderly.mapper.CareGiverMapper;
import com.example.elderly.service.CaregiverService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CaregiverServiceImpl extends ServiceImpl<CareGiverMapper, Caregiver>implements CaregiverService {
    @Override
    public IPage<Caregiver> pageList(CaregiverQueryDTO dto) {
        Page<Caregiver> page = new Page<>(dto.getPages(), dto.getSize());
        LambdaQueryWrapper<Caregiver> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(StringUtils.hasText(dto.getName()),
                Caregiver::getName,dto.getName())
                        .eq(StringUtils.hasText(dto.getPosition()),
                Caregiver::getPosition,dto.getPosition())
                        .eq(StringUtils.hasText(dto.getStatus()),
                Caregiver::getStatus,dto.getStatus())
                        .orderByDesc(Caregiver::getCreateTime);
        return this.page(page,queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCaregiver(CaregiverSaveDTO dto) {
        Caregiver caregiver = new Caregiver();
        caregiver.setName(dto.getName());
        caregiver.setAge(dto.getAge());
        caregiver.setPosition(dto.getPosition());
        caregiver.setPhone(dto.getPhone());
        caregiver.setAddress(dto.getAddress());
        caregiver.setStatus(dto.getStatus());
        this.save(caregiver);
        return caregiver.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCaregiver(Long id, CaregiverSaveDTO dto) {
        Caregiver caregiver = this.getById(id);
        if(caregiver==null){
            throw new RuntimeException("陪护人员不存在");
        }
        caregiver.setName(dto.getName());
        caregiver.setAge(dto.getAge());
        caregiver.setPosition(dto.getPosition());
        caregiver.setPhone(dto.getPhone());
        caregiver.setAddress(dto.getAddress());
        caregiver.setStatus(dto.getStatus());
        return this.updateById(caregiver);
    }
}
