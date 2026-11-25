package com.example.elderly.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.elderly.common.PageResult;
import com.example.elderly.common.Result;
import com.example.elderly.dto.alert.AlertBatchDTO;
import com.example.elderly.dto.alert.AlertProcessDTO;
import com.example.elderly.dto.alert.AlertQueryDTO;
import com.example.elderly.dto.alert.AlertCreateDTO;
import com.example.elderly.entity.Alert;
import com.example.elderly.vo.AlertVO;
import java.util.stream.Collectors;
import com.example.elderly.service.AlertService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "告警管理")
@RestController
@RequestMapping("/api/alert")
@RequiredArgsConstructor
public class AlertController {
    private final AlertService alertService;

    /**
     * 创建告警：供视频检测等能力把识别结果写入业务告警列表。
     * 返回新告警的 ID。
     */
    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody AlertCreateDTO dto) {
        return Result.success(alertService.createAlert(dto));
    }

    @GetMapping("/list")    public Result<PageResult<AlertVO>> list(@Valid AlertQueryDTO dto){
        IPage<Alert> page = alertService.pageList(dto);
        PageResult<AlertVO> result = PageResult.build(page.getTotal(),
                page.getRecords().stream().map(AlertController::toVO).collect(Collectors.toList()));
        return Result.success(result);
    }

    private static String mapLevel(String level) {
        if (level == null) return null;
        return switch (level.toUpperCase()) {
            case "CRITICAL", "HIGH" -> "high";
            case "MEDIUM" -> "medium";
            default -> "low";
        };
    }

    private static AlertVO toVO(Alert a) {
        AlertVO vo = new AlertVO();
        vo.setId(a.getId());
        vo.setElderlyName(a.getElderName());
        vo.setElderlyId(a.getElderId());
        vo.setWarningType(a.getAlertType());
        vo.setPriority(mapLevel(a.getLevel()));
        vo.setDescription(a.getDescription());
        vo.setTime(a.getAlertTime());
        vo.setLocation(a.getLocation());
        vo.setStatus(a.getStatus() == null ? null : a.getStatus().toLowerCase());
        vo.setHandler(a.getHandler());
        vo.setHandleTime(a.getHandleTime());
        vo.setContactPhone(a.getContactPhone());
        vo.setEmergencyContact(a.getEmergencyContact());
        vo.setSuggestedAction(a.getSuggestedAction());
        return vo;
    }
    @PutMapping("/process/{id}")
    public Result<String> process(@PathVariable Long id,
                                  @Validated @RequestBody AlertProcessDTO dto){
        boolean ok = alertService.processAlert(id, dto);
        return ok?Result.success("处理成功"):Result.fail(400,"警告不存在");
    }
    @PutMapping("/ignore/{id}")
    public Result<String> ignore(@PathVariable Long id){
        boolean ok = alertService.ignoreAlert(id);
        return ok?Result.success("忽略成功"):Result.fail(400,"警告不存在");

    }
    @PutMapping("/read/{id}")
    public Result<String> read(@PathVariable Long id){
        boolean ok = alertService.readAlert(id);
        return ok?Result.success():Result.fail(400,"警告不存在");
    }
    @PutMapping("/batch-process")
    public Result<String> batchProcess(@Validated @RequestBody AlertBatchDTO dto){
        alertService.batchProcess(dto);
        return Result.success("批量处理成功");
    }
    @PutMapping("/batch-ignore")
    public Result<String> batchIgnore(@Validated @RequestBody AlertBatchDTO dto){
        alertService.batchIgnore(dto);
        return Result.success("批量忽略成功");
    }
}
