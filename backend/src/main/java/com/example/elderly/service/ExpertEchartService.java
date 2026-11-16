package com.example.elderly.service;

import com.example.elderly.vo.echart.EchartLineBarVO;
import com.example.elderly.vo.echart.EchartPieVO;
import com.example.elderly.vo.echart.EchartStatVO;

import java.util.List;

public interface ExpertEchartService {
    EchartStatVO getExpertStatistics();


    List<EchartPieVO> getExpertTitleDistribution();


    EchartLineBarVO getExpertFieldDistribution();


    EchartLineBarVO getConsultationTrend();

    List<EchartPieVO> getSatisfactionDistribution();
}
