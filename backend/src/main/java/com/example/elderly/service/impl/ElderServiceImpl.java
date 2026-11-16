package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.elderly.dto.ElderDTO;
import com.example.elderly.entity.Elder;
import com.example.elderly.mapper.ElderMapper;
import com.example.elderly.service.ElderService;
import com.example.elderly.vo.PageVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ElderServiceImpl extends ServiceImpl<ElderMapper, Elder> implements ElderService {
    @Override
    public PageVO<Elder> listPage(Integer page, Integer size, String name, String phone) {
        Page<Elder> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Elder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name),
                Elder::getName,name);
        wrapper.like(StringUtils.hasText(phone),
                Elder::getPhone,phone);
        Page<Elder> elderPage = baseMapper.selectPage(pageObj, wrapper);
        PageVO<Elder> vo = new PageVO<>();
        vo.setTotal(elderPage.getTotal());
        vo.setList(elderPage.getRecords());
        return vo;
    }

    @Override
    public Long addElder(ElderDTO dto) {
        Elder elder = new Elder();
        elder.setName(dto.getName());
        elder.setGender(dto.getGender());
        elder.setAge(dto.getAge());
        elder.setIdCard(dto.getIdCard());
        elder.setBirthDate(dto.getBirthDate());
        elder.setEthnicity(dto.getEthnicity());
        elder.setPhone(dto.getPhone());
        elder.setAddress(dto.getAddress());
        elder.setResidenceType(dto.getResidenceType());
        elder.setRemark(dto.getRemark());
        elder.setDeviceBound(false);
        save(elder);
        return elder.getId();
    }

    @Override
    public Boolean updateElder(Long id, ElderDTO dto) {
        Elder elder = getById(id);
        if(elder==null){
            throw new RuntimeException("该老人档案不存在");
        }
        elder.setName(dto.getName());
        elder.setGender(dto.getGender());
        elder.setAge(dto.getAge());
        elder.setIdCard(dto.getIdCard());
        elder.setBirthDate(dto.getBirthDate());
        elder.setEthnicity(dto.getEthnicity());
        elder.setPhone(dto.getPhone());
        elder.setAddress(dto.getAddress());
        elder.setResidenceType(dto.getResidenceType());
        elder.setRemark(dto.getRemark());

        return updateById(elder);
    }

    @Override
    public Boolean deleteElder(Long id) {
        return removeById(id);
    }

    @Override
    public Elder getElderById(Long id) {
        return getById(id);
    }
}
