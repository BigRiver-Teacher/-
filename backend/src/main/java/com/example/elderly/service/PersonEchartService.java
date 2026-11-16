package com.example.elderly.service;

import com.example.elderly.dto.echart.EchartPieDTO;
import com.example.elderly.dto.echart.EchartRadarDTO;
import com.example.elderly.dto.echart.EchartTrendDTO;

import java.util.List;

public interface PersonEchartService {
    List<EchartPieDTO> getPersonRatio();
    EchartTrendDTO getPersonTrend();
    EchartRadarDTO getCaregiverDistribution();
    List<EchartPieDTO>getElderAgeDistribution();
    List<EchartPieDTO>getElderGenderDistribution();
}
