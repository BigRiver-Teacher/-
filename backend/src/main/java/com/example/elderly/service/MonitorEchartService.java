package com.example.elderly.service;

import com.example.elderly.vo.echart.EchartPieVO;
import com.example.elderly.vo.echart.EchartTimeCurveVO;

import java.util.List;

public interface MonitorEchartService {
    List<EchartPieVO> getMonitorDeviceStatus();

    EchartTimeCurveVO getAccelerationCurve(Long elderId);

    EchartTimeCurveVO getHeartbeatCurve(Long elderId);
}
