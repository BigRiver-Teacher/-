package com.example.elderly.controller;

import com.example.elderly.common.Result;
import com.example.elderly.dto.intervention.*;
import com.example.elderly.entity.Intervention;
import com.example.elderly.entity.InterventionRecord;
import com.example.elderly.service.InterventionRecordService;
import com.example.elderly.service.InterventionService;
import com.example.elderly.vo.PageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "干预方案")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/intervention")
public class InterventionController {
    private final InterventionService interventionService;
    private final InterventionRecordService recordService;

    @GetMapping("/list")
    public Result<PageVO<Intervention>> list(InterventionQueryDTO dto) {
        return Result.success(interventionService.pageList(dto));
    }

    @PostMapping
    public Result<Void> add(@RequestBody @Validated InterventionSaveDTO dto) {
        interventionService.addIntervention(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> edit(@PathVariable Long id, @RequestBody @Validated InterventionSaveDTO dto) {
        interventionService.updateIntervention(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        interventionService.deleteIntervention(id);
        return Result.success();
    }

    @PutMapping("/review/{id}")
    public Result<Void> audit(@PathVariable Long id, @RequestBody @Validated InterventionAuditDTO dto) {
        interventionService.auditIntervention(id, dto);
        return Result.success();
    }

    @PutMapping("/pause/{id}")
    public Result<Void> pause(@PathVariable Long id) {
        interventionService.pauseIntervention(id);
        return Result.success();
    }

    @PutMapping("/terminate/{id}")
    public Result<Void> terminate(@PathVariable Long id) {
        interventionService.terminateIntervention(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    public Result<Void> complete(@PathVariable Long id, @RequestBody @Validated InterventionCompleteDTO dto) {
        interventionService.completeIntervention(id, dto);
        return Result.success();
    }

    @PostMapping("/record")
    public Result<Void> addRecord(@RequestBody @Validated InterventionRecordSaveDTO dto) {
        recordService.addRecord(dto);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Intervention> detail(@PathVariable Long id) {
        Intervention intervention = interventionService.getDetailById(id);
        return Result.success(intervention);
    }

    @GetMapping("/records/{id}")
    public Result<PageVO<InterventionRecord>> recordList(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        PageVO<InterventionRecord> pageData = recordService.listByInterventionId(id, page, size);
        return Result.success(pageData);
    }
}
