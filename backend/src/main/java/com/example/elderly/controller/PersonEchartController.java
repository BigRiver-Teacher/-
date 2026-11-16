package com.example.elderly.controller;

import com.example.elderly.common.Result;
import com.example.elderly.dto.echart.EchartPieDTO;
import com.example.elderly.dto.echart.EchartRadarDTO;
import com.example.elderly.dto.echart.EchartTrendDTO;
import com.example.elderly.service.PersonEchartService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "人员统计图表")
@RestController
@RequestMapping("/api/echart/person")
@RequiredArgsConstructor
public class PersonEchartController {
    private final PersonEchartService personEchartService;
    @GetMapping("/ratio")
    public Result<List<EchartPieDTO>> getPersonRatio(){
        return Result.success(personEchartService.getPersonRatio());
    }
    @GetMapping("/trend")
    public Result<EchartTrendDTO>getPersonTrend(){
        return Result.success(personEchartService.getPersonTrend());
    }
    @GetMapping("/caregiver-distribution")
    public Result<EchartRadarDTO>getCaregiverDistribution(){
        return Result.success(personEchartService.getCaregiverDistribution());
    }
    @GetMapping("/age-distribution")
    public Result<List<EchartPieDTO>>getElderAgeDistribution(){
        return Result.success(personEchartService.getElderAgeDistribution());
    }
    @GetMapping("/gender-distribution")
    public Result<List<EchartPieDTO>>getElderGenderDistribution(){
        return Result.success(personEchartService.getElderGenderDistribution());
    }
}
