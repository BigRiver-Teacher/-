package com.example.elderly.controller;

import com.example.elderly.common.Result;
import com.example.elderly.service.MonitorEchartService;
import com.example.elderly.vo.echart.EchartPieVO;
import com.example.elderly.vo.echart.EchartTimeCurveVO;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@Tag(name = "实时监控图表")
@RestController
@RequestMapping("/api/echart/monitor")
public class MonitorEchartController {
    private final MonitorEchartService monitorEchartService;

    @GetMapping("/device-status")
    public Result<List<EchartPieVO>> deviceStatus(){
        return Result.success(monitorEchartService.getMonitorDeviceStatus());
    }
    @GetMapping("/acceleration")
    public Result<EchartTimeCurveVO>acceleration(@RequestParam Long elderId){
        EchartTimeCurveVO vo = monitorEchartService.getAccelerationCurve(elderId);
        return Result.success(vo);
    }
    @GetMapping("/heartbeat")
    public Result<EchartTimeCurveVO>heartbeat(@RequestParam Long elderId){
        EchartTimeCurveVO vo = monitorEchartService.getHeartbeatCurve(elderId);
        return Result.success(vo);
    }
}
