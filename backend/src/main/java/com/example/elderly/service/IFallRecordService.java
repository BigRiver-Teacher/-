package com.example.elderly.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.elderly.dto.fall.FallRecordAddDTO;
import com.example.elderly.dto.fall.FallRecordQueryDTO;
import com.example.elderly.dto.fall.FallRecordUpdateDTO;
import com.example.elderly.entity.FallRecord;


public interface IFallRecordService extends IService<FallRecord> {
    IPage<FallRecord> pageList(FallRecordQueryDTO dto);

    Long add(FallRecordAddDTO dto);

    Boolean update(FallRecordUpdateDTO dto);

    Boolean removeByIdValid(Long id);
}
