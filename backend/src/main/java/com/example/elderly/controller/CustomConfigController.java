package com.example.elderly.controller;

import lombok.RequiredArgsConstructor;
import com.example.elderly.common.Result;
import com.example.elderly.dto.custom.DashboardConfigDTO;
import com.example.elderly.dto.custom.MobileConfigDTO;
import com.example.elderly.dto.custom.SidebarConfigDTO;
import com.example.elderly.service.CustomConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "自定义配置")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/custom")
public class CustomConfigController {
    private final CustomConfigService customConfigService;
    @GetMapping("/sidebar")
    public Result<SidebarConfigDTO> getSidebar(){
        return Result.success(customConfigService.getSidebarConfig());
    }
    @PutMapping("/sidebar")
    public Result<Boolean> saveSidebar(@RequestBody SidebarConfigDTO dto){
        customConfigService.saveSidebarConfig(dto);
        return Result.success(true);
    }
    @PostMapping("/sidebar/reset")
    public Result<Boolean> resetSidebar(){
        customConfigService.resetSidebar();
        return Result.success(true);
    }
    @GetMapping("/dashboard")
    public Result<DashboardConfigDTO> getDashboard(){
        return Result.success(customConfigService.getDashboardConfig());
    }
    @PutMapping("/dashboard")
    public Result<Boolean>saveDashboard(@RequestBody DashboardConfigDTO dto){
        customConfigService.saveDashboardConfig(dto);
        return Result.success(true);
    }
    @PostMapping("/dashboard/reset")
    public Result<Boolean> resetDashboard(){
        customConfigService.resetDashboard();
        return Result.success(true);
    }
    @GetMapping("/mobile")
    public Result<MobileConfigDTO> getMobile(){
        return Result.success(customConfigService.getMobileConfig());
    }
    @PutMapping("/mobile")
    public Result<Boolean> saveMobile(@RequestBody MobileConfigDTO dto){
       customConfigService.saveMobileConfig(dto);
       return Result.success(true);
    }
    @PostMapping("/mobile/reset")
    public Result<Boolean> resetMobile(){
        customConfigService.resetMobile();
        return Result.success(true);
    }
}
