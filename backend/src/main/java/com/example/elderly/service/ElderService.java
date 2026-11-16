package com.example.elderly.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.elderly.dto.ElderDTO;
import com.example.elderly.entity.Elder;
import com.example.elderly.vo.PageVO;

public interface ElderService extends IService<Elder> {
    PageVO<Elder> listPage(Integer page, Integer size, String name, String phone);
    Long addElder(ElderDTO dto);
    Boolean updateElder(Long id, ElderDTO dto);
    Boolean deleteElder(Long id);
    Elder getElderById(Long id);
}
