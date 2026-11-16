package com.example.elderly.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.elderly.dto.intervention.InterventionRecordSaveDTO;
import com.example.elderly.entity.InterventionRecord;
import com.example.elderly.vo.PageVO;

public interface InterventionRecordService extends IService<InterventionRecord> {
    void addRecord(InterventionRecordSaveDTO dto);

    PageVO<InterventionRecord> listByInterventionId(Long interventionId, Integer page, Integer size);
}
