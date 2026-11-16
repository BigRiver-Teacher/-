package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.elderly.common.PageResult;
import com.example.elderly.dto.Expert.ExpertQueryDTO;
import com.example.elderly.dto.Expert.ExpertSaveDTO;
import com.example.elderly.dto.Expert.ExpertScheduleDTO;
import com.example.elderly.dto.Expert.ExpertUpdateDTO;
import com.example.elderly.entity.Device;
import com.example.elderly.entity.Expert;
import com.example.elderly.mapper.ExpertMapper;
import com.example.elderly.service.IExpertService;
import com.example.elderly.vo.DeviceVO;
import com.example.elderly.vo.ExpertVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpertServiceImpl extends ServiceImpl<ExpertMapper, Expert>implements IExpertService {
    private final ExpertMapper expertMapper;
    @Override
    public PageResult<ExpertVO> listExpert(ExpertQueryDTO dto) {
        Page<Expert> page = new Page<>(dto.getPages(), dto.getSize());
        LambdaQueryWrapper<Expert> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.hasText(dto.getName())){
            wrapper.like(Expert::getName,dto.getName());
        }
        if(StringUtils.hasText(dto.getTitle())){
            wrapper.eq(Expert::getTitle,dto.getTitle());
        }
        if(StringUtils.hasText(dto.getSpecialty())){
            wrapper.like(Expert::getSpecialty,dto.getSpecialty());
        }
        if(StringUtils.hasText(dto.getStatus())){
            wrapper.like(Expert::getStatus,dto.getStatus());
        }
        wrapper.orderByDesc(Expert::getCreateTime);
        IPage<Expert> pageData = expertMapper.selectPage(page, wrapper);
        List<ExpertVO> voList = pageData.getRecords().stream().map(this::convertVO)
                .collect(Collectors.toList());
        return new PageResult<>(pageData.getTotal(),voList);

    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void addExpert(ExpertSaveDTO dto) {
        Expert expert = new Expert();
        expert.setName(dto.getName());
        expert.setTitle(dto.getTitle());
        expert.setSpecialty(dto.getSpecialty());
        expert.setInstitution(dto.getInstitution());
        expert.setPhone(dto.getPhone());
        expert.setStatus(dto.getStatus());
        expert.setSchedule(dto.getSchedule());
        expert.setTodayCount(0);
        expert.setTotalCount(0);
        expertMapper.insert(expert);
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void updateExpert(Long id, ExpertUpdateDTO dto) {
        Expert expert = getById(id);
        if(expert==null){
            throw new RuntimeException("专家不存在");
        }
        expert.setName(dto.getName());
        expert.setTitle(dto.getTitle());
        expert.setSpecialty(dto.getSpecialty());
        expert.setInstitution(dto.getInstitution());
        expert.setPhone(dto.getPhone());
        expert.setStatus(dto.getStatus());
        expert.setRating(dto.getRating());
        updateById( expert);
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void deleteExpert(Long id) {
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void updateSchedule(Long id, ExpertScheduleDTO dto) {
        Expert expert = getById(id);
        if(expert==null){
            throw new RuntimeException("专家不存在");
        }
        expert.setSchedule(dto.getSchedule());
        updateById(expert);
    }

    @Override
    public ExpertVO getExpertDetail(Long id) {
        Expert expert = getById(id);
        return convertVO(expert);
    }
    private ExpertVO convertVO(Expert entity){
        ExpertVO vo = new ExpertVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setTitle(entity.getTitle());
        vo.setSpecialty(entity.getSpecialty());
        vo.setInstitution(entity.getInstitution());
        vo.setPhone(entity.getPhone());
        vo.setStatus(entity.getStatus());
        vo.setTodayCount(entity.getTodayCount());
        vo.setTotalCount(entity.getTotalCount());
        vo.setRating(entity.getRating());
        vo.setSchedule(entity.getSchedule());
        return vo;
    }
}
