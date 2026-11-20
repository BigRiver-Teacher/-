package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.elderly.dto.ElderDTO;
import com.example.elderly.entity.Elder;
import com.example.elderly.mapper.ElderMapper;
import com.example.elderly.service.ElderService;
import com.example.elderly.util.DataScopeUtil;
import com.example.elderly.util.LoginUserUtil;
import com.example.elderly.vo.PageVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
public class ElderServiceImpl extends ServiceImpl<ElderMapper, Elder> implements ElderService {

    @Resource
    private DataScopeUtil dataScopeUtil;

    @Override
    public PageVO<Elder> listPage(Integer page, Integer size, String name, String phone) {
        Page<Elder> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Elder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name),
                Elder::getName,name);
        wrapper.like(StringUtils.hasText(phone),
                Elder::getPhone,phone);
        // 数据范围过滤：非管理员仅能访问其服务关系内的老人
        List<Long> elderIds = dataScopeUtil.getAccessibleElderIds(LoginUserUtil.getUserId());
        if (elderIds != null) {
            if (elderIds.isEmpty()) {
                wrapper.in(Elder::getId, Collections.singletonList(-1L));
            } else {
                wrapper.in(Elder::getId, elderIds);
            }
        }
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
        Elder elder = getById(id);
        if (elder == null) {
            return null;
        }
        // 数据范围过滤：无权限访问该老人时返回 null（等同不存在）
        List<Long> elderIds = dataScopeUtil.getAccessibleElderIds(LoginUserUtil.getUserId());
        if (elderIds != null && !elderIds.contains(id)) {
            return null;
        }
        return elder;
    }
}
