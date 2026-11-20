package com.example.elderly.controller;

import com.example.elderly.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.elderly.common.PageResult;
import com.example.elderly.common.Result;
import com.example.elderly.dto.healthNote.HealthNoteAddDTO;
import com.example.elderly.dto.healthNote.HealthNoteQueryDTO;
import com.example.elderly.entity.HealthNote;
import com.example.elderly.service.IHealthNoteService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "健康档案")
@RestController
@RequestMapping("/api/health-note")
@RequiredArgsConstructor
public class HealthNoteController {
    private final IHealthNoteService healthNoteService;
    @GetMapping("/list")
    public Result<PageResult<HealthNote>> list(@Validated HealthNoteQueryDTO dto){
        return Result.success(PageResult.build(healthNoteService.pageList(dto)));
    }
    @PostMapping
    public Result<Long> add(@Validated @RequestBody HealthNoteAddDTO dto){
        Long id = healthNoteService.add(dto);
        return Result.success(id);
    }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @NotNull Long id){
        Boolean flag = healthNoteService.deleteById(id);
        if(!flag){
            throw new BusinessException(400,"备注不存在");
        }
        return Result.success();
    }

}
