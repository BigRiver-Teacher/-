package com.example.elderly.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.elderly.dto.consult.ConsultCreateDTO;
import com.example.elderly.dto.consult.ConsultQueryDTO;
import com.example.elderly.entity.Consult;

public interface ConsultService extends IService<Consult> {
    IPage<Consult> pageList(ConsultQueryDTO dto);
    Long createConsult(ConsultCreateDTO dto);
    Boolean assignConsult(Long id,Long expertId);
    Boolean completeConsult(Long consultId,String reply,
                            Integer rating,String feedback);
    Boolean assignExpert(Long id,Long expertId);
}
