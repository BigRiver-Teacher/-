package com.example.elderly.service;

import com.example.elderly.common.PageResult;
import com.example.elderly.dto.Expert.ExpertQueryDTO;
import com.example.elderly.dto.Expert.ExpertSaveDTO;
import com.example.elderly.dto.Expert.ExpertScheduleDTO;
import com.example.elderly.dto.Expert.ExpertUpdateDTO;
import com.example.elderly.vo.ExpertVO;

public interface IExpertService {
    PageResult<ExpertVO> listExpert(ExpertQueryDTO dto);
    void addExpert(ExpertSaveDTO dto);
    void updateExpert(Long id, ExpertUpdateDTO dto);
    void deleteExpert(Long id);
    void updateSchedule(Long id, ExpertScheduleDTO dto);
    ExpertVO getExpertDetail(Long id);
}
