package com.example.elderly.service.impl;

import com.example.elderly.dto.echart.EchartPieDTO;
import com.example.elderly.dto.echart.EchartRadarDTO;
import com.example.elderly.dto.echart.EchartTrendDTO;
import com.example.elderly.mapper.PersonEchartMapper;
import com.example.elderly.service.PersonEchartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PersonEchartServiceImpl implements PersonEchartService {
    @Autowired
    private PersonEchartMapper personEchartMapper;

    @Override
    public List<EchartPieDTO> getPersonRatio() {
        return personEchartMapper.selectPersonRatio();
    }

    @Override
    public EchartTrendDTO getPersonTrend() {
        List<EchartPieDTO> raw = personEchartMapper.selectPersonTrendRaw();
        EchartTrendDTO dto = new EchartTrendDTO();
        List<String> categories = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (EchartPieDTO item : raw) {
            categories.add(item.getName());
            values.add(item.getValue());
        }
        dto.setCategories(categories);
        EchartTrendDTO.EchartSeries series = new EchartTrendDTO.EchartSeries();
        series.setName("新增人数");
        series.setData(values);
        dto.setSeries(List.of(series));
        return dto;
    }

    @Override
    public EchartRadarDTO getCaregiverDistribution() {
        List<EchartPieDTO> raw = personEchartMapper.selectPersonRadarRaw();
        EchartRadarDTO dto = new EchartRadarDTO();
        List<EchartRadarDTO.Indicator> indicators = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (EchartPieDTO item : raw) {
            EchartRadarDTO.Indicator ind = new EchartRadarDTO.Indicator();
            ind.setName(item.getName());
            ind.setMax(100);
            indicators.add(ind);
            values.add(item.getValue());
        }
        dto.setIndicator(indicators);
        EchartRadarDTO.RadarSeries series = new EchartRadarDTO.RadarSeries();
        series.setName("当前水平");
        series.setValue(values);
        dto.setSeries(List.of(series));
        return dto;
    }

    @Override
    public List<EchartPieDTO> getElderAgeDistribution() {
        return personEchartMapper.selectElderAgePie();
    }

    @Override
    public List<EchartPieDTO> getElderGenderDistribution() {
        return personEchartMapper.selectElderGenderPie();
    }
}