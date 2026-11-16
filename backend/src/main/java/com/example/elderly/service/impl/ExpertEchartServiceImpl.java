package com.example.elderly.service.impl;

import com.example.elderly.dto.echart.EchartTrendDTO;
import com.example.elderly.vo.echart.EchartLineBarVO.SeriesItem;
import com.example.elderly.mapper.ExpertEchartMapper;
import com.example.elderly.service.ExpertEchartService;
import com.example.elderly.vo.echart.EchartLineBarVO;
import com.example.elderly.vo.echart.EchartPieVO;
import com.example.elderly.vo.echart.EchartStatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExpertEchartServiceImpl implements ExpertEchartService {
    private final ExpertEchartMapper expertEchartMapper;

    @Override
    public EchartStatVO getExpertStatistics() {
        return expertEchartMapper.selectExpertStatistics();
    }

    @Override
    public List<EchartPieVO> getExpertTitleDistribution() {
        return expertEchartMapper.selectExpertTitleDistribution();
    }

    @Override
    public EchartLineBarVO getExpertFieldDistribution() {
        List<EchartPieVO> raw = expertEchartMapper.selectExpertFieldDistribution();
        EchartLineBarVO vo = new EchartLineBarVO();
        List<String> categories = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        raw.forEach(item -> {
            categories.add(item.getName());
            values.add(item.getValue());
        });
        vo.setCategories(categories);
        SeriesItem line = new SeriesItem();
        line.setName("专家数量");
        line.setData(values);
        SeriesItem bar = new SeriesItem();
        bar.setName("专家数量");
        bar.setData(values);
        vo.setLineSeries(List.of(line));
        vo.setBarSeries(List.of(bar));
        return vo;
    }

    @Override
    public EchartLineBarVO getConsultationTrend() {
        List<EchartPieVO> raw = expertEchartMapper.selectConsultTrend();
        EchartLineBarVO vo = new EchartLineBarVO();
        List<String> months = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        // 固定输出最近 7 天刻度，缺失日期补 0，避免图表出现全 0 或空刻度
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        LocalDate today = LocalDate.now();
        Map<String, Integer> valueMap = new HashMap<>();
        if (raw != null) {
            raw.forEach(item -> {
                if (item != null && item.getName() != null) {
                    valueMap.put(item.getName().trim(), item.getValue() == null ? 0 : item.getValue());
                }
            });
        }
        for (int i = 6; i >= 0; i--) {
            String day = today.minusDays(i).format(fmt);
            months.add(day);
            counts.add(valueMap.getOrDefault(day, 0));
        }
        vo.setCategories(months);
        SeriesItem line = new SeriesItem();
        line.setName("咨询量");
        line.setData(counts);
        SeriesItem bar = new SeriesItem();
        bar.setName("咨询量");
        bar.setData(counts);
        vo.setLineSeries(List.of(line));
        vo.setBarSeries(List.of(bar));
        return vo;
    }

    @Override
    public List<EchartPieVO> getSatisfactionDistribution() {
        return expertEchartMapper.selectSatisfactionDistribution();
    }
}
