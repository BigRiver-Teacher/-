package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.elderly.dto.consult.ConsultCreateDTO;
import com.example.elderly.dto.consult.ConsultQueryDTO;
import com.example.elderly.entity.Consult;
import com.example.elderly.mapper.ConsultMapper;
import com.example.elderly.service.ConsultService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class ConsultServiceImpl extends ServiceImpl<ConsultMapper, Consult> implements ConsultService {
    @Override
    public IPage<Consult> pageList(ConsultQueryDTO dto) {
        Page<Consult> page = new Page<>(dto.getPages(), dto.getSize());
        return this.page(page,
                Wrappers.lambdaQuery(Consult.class)
                        .like(StringUtils.hasText(dto.getElderName()),
                                Consult::getElderName,dto.getElderName())
                        .like(StringUtils.hasText(dto.getExpertName()),
                                Consult::getExpertName,dto.getExpertName())
                        .eq(StringUtils.hasText(dto.getStatus()),
                                Consult::getStatus,dto.getStatus())
                        .eq(StringUtils.hasText(dto.getUrgency()),
                                Consult::getUrgency,dto.getUrgency())
                        .orderByDesc(Consult::getCreateTime)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createConsult(ConsultCreateDTO dto) {
        Consult consult = new Consult();
        consult.setElderName(dto.getElderName());
        consult.setType(dto.getType());
        consult.setContent(dto.getContent());
        consult.setStatus("pending");
        consult.setUrgency(dto.getUrgency());
        consult.setConsultTime(LocalDateTime.now());
        this.save(consult);
        return consult.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean assignConsult(Long id, Long expertId) {
        Consult consult = this.getById(id);
        if(consult==null){
            throw new RuntimeException("咨询记录不存在");
        }
        if(!"pending".equalsIgnoreCase(consult.getStatus())){
            throw new RuntimeException("仅待分配咨询可配专家");
        }
        consult.setExpertId(expertId);
        consult.setStatus("ongoing");
        return this.updateById(consult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean completeConsult(Long consultId, String reply, Integer rating, String feedback) {
        Consult consult = this.getById(consultId);
        if(consult==null){
            throw new RuntimeException("咨询记录不存在");
        }
        if(!"ongoing".equalsIgnoreCase(consult.getStatus())){
            throw new RuntimeException("仅进行中的咨询可结束");
        }
        consult.setReply(reply);
        consult.setRating(rating);
        consult.setFeedback(feedback);
        consult.setStatus("completed");
        return this.updateById(consult);
    }

    @Override
    public Boolean assignExpert(Long id, Long expertId) {
        LambdaUpdateWrapper<Consult> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(Consult::getId, id)
                .set(Consult::getExpertId, expertId);
        return update(updateWrapper);
    }
}
