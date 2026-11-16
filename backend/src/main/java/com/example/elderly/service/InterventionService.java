package com.example.elderly.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.elderly.dto.intervention.InterventionAuditDTO;
import com.example.elderly.dto.intervention.InterventionCompleteDTO;
import com.example.elderly.dto.intervention.InterventionQueryDTO;
import com.example.elderly.dto.intervention.InterventionSaveDTO;
import com.example.elderly.entity.Intervention;
import com.example.elderly.vo.PageVO;

public interface InterventionService extends IService<Intervention> {
    PageVO<Intervention> pageList(InterventionQueryDTO dto);

    void addIntervention(InterventionSaveDTO dto);

    void updateIntervention(Long id, InterventionSaveDTO dto);

    void deleteIntervention(Long id);

    void auditIntervention(Long id, InterventionAuditDTO dto);

    void pauseIntervention(Long id);

    void terminateIntervention(Long id);

    void completeIntervention(Long id, InterventionCompleteDTO dto);

    Intervention getDetailById(Long id);
}
