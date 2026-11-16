package com.example.elderly.service;

import com.example.elderly.dto.echart.EchartDeviceFaultDTO;
import com.example.elderly.dto.echart.EchartDeviceStatDTO;
import com.example.elderly.dto.echart.EchartTrendDTO;

public interface DeviceEchartService {
    EchartDeviceStatDTO getDeviceStatistics();
    EchartTrendDTO getOnlineTrend();
    EchartTrendDTO getAlertTrend();
    EchartDeviceFaultDTO getFaultStatistics();
}
