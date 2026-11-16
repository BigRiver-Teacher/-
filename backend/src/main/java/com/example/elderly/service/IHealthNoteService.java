package com.example.elderly.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.elderly.dto.healthNote.HealthNoteAddDTO;
import com.example.elderly.dto.healthNote.HealthNoteQueryDTO;
import com.example.elderly.entity.HealthNote;

public interface IHealthNoteService extends IService<HealthNote> {
    IPage<HealthNote> pageList(HealthNoteQueryDTO dto);

    Long add(HealthNoteAddDTO dto);
    Boolean deleteById(Long id);
}
