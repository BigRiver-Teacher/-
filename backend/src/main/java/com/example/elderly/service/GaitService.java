package com.example.elderly.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.elderly.dto.GaitDTO;
import com.example.elderly.entity.GaitRecord;
import com.example.elderly.vo.PageVO;

public interface GaitService extends IService<GaitRecord> {
    PageVO<GaitRecord> listPage(Integer page,
                                Integer size, String name, String result);
    Long addGait(GaitDTO dto);
    Boolean deleteGait(Long id);
}
