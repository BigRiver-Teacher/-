package com.example.elderly.controller;

import com.example.elderly.common.Result;
import com.example.elderly.service.ExpertEchartService;
import com.example.elderly.vo.echart.EchartLineBarVO;
import com.example.elderly.vo.echart.EchartPieVO;
import com.example.elderly.vo.echart.EchartStatVO;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "专家统计图表")
@RestController
@RequestMapping("/api/echart/expert")
@RequiredArgsConstructor
public class ExpertEchartController {
    private final ExpertEchartService expertEchartService;

    @GetMapping("/statistics")
    public Result<EchartStatVO> expertStatistics() {
        EchartStatVO data = expertEchartService.getExpertStatistics();
        return Result.success(data);
    }


    @GetMapping("/title-distribution")
    public Result<List<EchartPieVO>> titleDistribution() {
        List<EchartPieVO> list = expertEchartService.getExpertTitleDistribution();
        return Result.success(list);
    }

    @GetMapping("/field-distribution")
    public Result<EchartLineBarVO> fieldDistribution() {
        EchartLineBarVO vo = expertEchartService.getExpertFieldDistribution();
        return Result.success(vo);
    }

    @GetMapping("/consultation-trend")
    public Result<EchartLineBarVO> consultationTrend() {
        EchartLineBarVO vo = expertEchartService.getConsultationTrend();
        return Result.success(vo);
    }

    @GetMapping("/satisfaction-distribution")
    public Result<List<EchartPieVO>> satisfactionDistribution() {
        List<EchartPieVO> list = expertEchartService.getSatisfactionDistribution();
        return Result.success(list);
    }
}

