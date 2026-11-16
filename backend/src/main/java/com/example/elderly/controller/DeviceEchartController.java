package com.example.elderly.controller;

import com.example.elderly.common.Result;
import com.example.elderly.dto.echart.EchartDeviceFaultDTO;
import com.example.elderly.dto.echart.EchartDeviceStatDTO;
import com.example.elderly.dto.echart.EchartTrendDTO;
import com.example.elderly.service.DeviceEchartService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "设备统计图表")
@RestController
@RequestMapping("/api/echart/device")
@RequiredArgsConstructor
public class DeviceEchartController {
    private final DeviceEchartService deviceEchartService;
    @GetMapping("/statistics")
    public Result<EchartDeviceStatDTO> getDeviceStatistics(){
        return Result.success(deviceEchartService.getDeviceStatistics());
    }
    @GetMapping("/online-trend")
    public Result<EchartTrendDTO> getOnlineTrend(){
        return Result.success(deviceEchartService.getOnlineTrend());
    }
    @GetMapping("/alert-trend")
    public Result<EchartTrendDTO> getAlertTrend(){
        return Result.success(deviceEchartService.getAlertTrend());
    }
    @GetMapping("/fault-statistics")
    public Result<EchartDeviceFaultDTO> getFaultStatistics(){
        return Result.success(deviceEchartService.getFaultStatistics());
    }
}
