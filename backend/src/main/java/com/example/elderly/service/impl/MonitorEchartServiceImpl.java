package com.example.elderly.service.impl;

import com.example.elderly.mapper.MonitorEchartMapper;
import com.example.elderly.service.MonitorEchartService;
import com.example.elderly.vo.echart.EchartPieVO;
import com.example.elderly.vo.echart.EchartTimeCurveVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonitorEchartServiceImpl implements MonitorEchartService {
    private final MonitorEchartMapper monitorEchartMapper;
    @Override
    public List<EchartPieVO> getMonitorDeviceStatus() {
        return monitorEchartMapper.selectMonitorDeviceStatus();
    }

    @Override
    public EchartTimeCurveVO getAccelerationCurve(Long elderId) {
        List<EchartPieVO> raw = monitorEchartMapper.selectAccelerationData(elderId);
        EchartTimeCurveVO vo = new EchartTimeCurveVO();
        ArrayList<String> xAxis = new ArrayList<>();
        ArrayList<Integer> values = new ArrayList<>();
        raw.forEach(item->{
            xAxis.add(item.getName());
            values.add(item.getValue());
        });
        vo.setCategories(xAxis);
        EchartTimeCurveVO.SeriesItem series = new EchartTimeCurveVO.SeriesItem();
        series.setName("步速(cm/s)");
        series.setData(values);
        vo.setSeries(List.of(series));
        return vo;
    }

    @Override
    public EchartTimeCurveVO getHeartbeatCurve(Long elderId) {
        List<EchartPieVO> raw = monitorEchartMapper.selectHeartbeatData(elderId);
        EchartTimeCurveVO vo = new EchartTimeCurveVO();
        ArrayList<String> xAxis = new ArrayList<>();
        ArrayList<Integer> values = new ArrayList<>();
        raw.forEach(item->{
            xAxis.add(item.getName());
            values.add(item.getValue());
        });
        vo.setCategories(xAxis);
        EchartTimeCurveVO.SeriesItem series = new EchartTimeCurveVO.SeriesItem();
        series.setName("步频(步/分)");
        series.setData(values);
        vo.setSeries(List.of(series));
        return vo;
    }
}
