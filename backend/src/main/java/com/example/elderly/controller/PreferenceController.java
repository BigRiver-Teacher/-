package com.example.elderly.controller;

import lombok.RequiredArgsConstructor;
import com.example.elderly.common.PriorityEnum;
import com.example.elderly.common.Result;
import com.example.elderly.dto.TestPushDTO;
import com.example.elderly.dto.preference.PreferenceConfigDTO;
import com.example.elderly.service.PreferenceService;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "推送偏好")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/preference")
public class PreferenceController {
    private final PreferenceService preferenceService;
    @GetMapping("/config")
    public Result<PreferenceConfigDTO> getConfig(){
        return Result.success(preferenceService.getConfig());
    }
    @PutMapping("/config")
    public Result<String> saveConfig(@Validated @RequestBody PreferenceConfigDTO dto){
        preferenceService.saveConfig(dto);
        return Result.success("配置保存成功");
    }
    @PostMapping("/rest")
    public Result<String> reset(){
        preferenceService.resetDefault();
        return Result.success("已重置默认配置");
    }
    @PostMapping("/test-push")
    public Result<String> testPush(@Validated @RequestBody TestPushDTO dto){
        preferenceService.testPush(dto);
        return Result.success("测试推送成功");
    }
}
