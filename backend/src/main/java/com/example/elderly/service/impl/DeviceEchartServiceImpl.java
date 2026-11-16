package com.example.elderly.service.impl;

import com.example.elderly.dto.echart.EchartDeviceFaultDTO;
import com.example.elderly.dto.echart.EchartDeviceStatDTO;
import com.example.elderly.dto.echart.EchartPieDTO;
import com.example.elderly.dto.echart.EchartTrendDTO;
import com.example.elderly.mapper.DeviceEchartMapper;
import com.example.elderly.service.DeviceEchartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeviceEchartServiceImpl implements DeviceEchartService {
    @Autowired
    private DeviceEchartMapper deviceEchartMapper;

    @Override
    public EchartDeviceStatDTO getDeviceStatistics() {
        EchartDeviceStatDTO dto = new EchartDeviceStatDTO();
        List<EchartPieDTO> statusList = deviceEchartMapper.selectDeviceStatusTotal();
        int total = 0, online = 0, offline = 0, fault = 0;
        for (EchartPieDTO s : statusList) {
            total += s.getValue() == null ? 0 : s.getValue();
            if ("ONLINE".equalsIgnoreCase(s.getName())) online = s.getValue();
            else if ("OFFLINE".equalsIgnoreCase(s.getName())) offline = s.getValue();
            else if ("FAULT".equalsIgnoreCase(s.getName()) || "MAINTENANCE".equalsIgnoreCase(s.getName())) fault += s.getValue();
        }
        dto.setTotalDevices(total);
        dto.setOnlineDevices(online);
        dto.setOfflineDevices(offline);
        dto.setFaultDevices(fault);
        Integer bound = deviceEchartMapper.selectBoundDeviceCount();
        dto.setBindRate(total == 0 ? 0 : (int) Math.round(bound * 100.0 / total));
        dto.setTodayAlertCount(deviceEchartMapper.selectTodayAlertCount());
        return dto;
    }

    private static final DateTimeFormatter TREND_FORMAT = DateTimeFormatter.ofPattern("MM-dd");

    /** 最近 7 天（含今天）的日期刻度，保证无数据日期也显示为 0 而不是空刻度 */
    private List<String> last7Days() {
        List<String> days = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            days.add(today.minusDays(i).format(TREND_FORMAT));
        }
        return days;
    }

    private EchartTrendDTO buildTrend(List<EchartPieDTO> raw, String seriesName) {
        EchartTrendDTO dto = new EchartTrendDTO();
        List<String> categories = last7Days();
        Map<String, Integer> valueMap = new HashMap<>();
        if (raw != null) {
            for (EchartPieDTO item : raw) {
                if (item == null || item.getName() == null) continue;
                valueMap.put(item.getName().trim(), item.getValue() == null ? 0 : item.getValue());
            }
        }
        List<Integer> values = new ArrayList<>();
        for (String day : categories) {
            values.add(valueMap.getOrDefault(day, 0));
        }
        dto.setCategories(categories);
        EchartTrendDTO.EchartSeries series = new EchartTrendDTO.EchartSeries();
        series.setName(seriesName);
        series.setData(values);
        dto.setSeries(List.of(series));
        return dto;
    }

    @Override
    public EchartTrendDTO getOnlineTrend() {
        return buildTrend(deviceEchartMapper.selectOnlineTrendRaw(), "在线设备数");
    }

    @Override
    public EchartTrendDTO getAlertTrend() {
        return buildTrend(deviceEchartMapper.selectAlertTrendRaw(), "告警数量");
    }

    @Override
    public EchartDeviceFaultDTO getFaultStatistics() {
        List<EchartPieDTO> raw = deviceEchartMapper.selectFaultRaw();
        EchartDeviceFaultDTO dto = new EchartDeviceFaultDTO();
        List<String> categories = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (EchartPieDTO item : raw) {
            categories.add(item.getName());
            values.add(item.getValue());
        }
        dto.setCategories(categories);
        EchartTrendDTO.EchartSeries series = new EchartTrendDTO.EchartSeries();
        series.setName("故障数量");
        series.setData(values);
        dto.setSeries(List.of(series));
        return dto;
    }
}