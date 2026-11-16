package com.example.elderly.controller;

import com.example.elderly.common.PageResult;
import com.example.elderly.common.Result;
import com.example.elderly.dto.Expert.ExpertQueryDTO;
import com.example.elderly.dto.Expert.ExpertSaveDTO;
import com.example.elderly.dto.Expert.ExpertScheduleDTO;
import com.example.elderly.dto.Expert.ExpertUpdateDTO;
import com.example.elderly.service.IExpertService;
import com.example.elderly.vo.ExpertVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Tag(name = "专家管理")
@RestController
@RequestMapping("/api/expert")
public class ExpertController {
    private final IExpertService expertService;
    @GetMapping("/list")
    public Result<PageResult<ExpertVO>>list(@Validated ExpertQueryDTO dto){
        PageResult<ExpertVO> page = expertService.listExpert(dto);
        return Result.success(page);
    }
    @PostMapping
    public Result<?> add(@Validated @RequestBody ExpertSaveDTO dto){
        expertService.addExpert(dto);
        return Result.success();
    }
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Validated @RequestBody ExpertUpdateDTO dto){
        expertService.updateExpert(id,dto);
        return Result.success();
    }
    @DeleteMapping("{id}")
    public Result<?> delete(@PathVariable Long id){
        expertService.deleteExpert(id);
        return Result.success();
    }
    @PutMapping("/schedule/{id}")
    public Result<?>schedule(@PathVariable Long id,
                             @Validated @RequestBody ExpertScheduleDTO dto){
        expertService.updateSchedule(id,dto);
        return Result.success();
    }
    @GetMapping("/{id}")
    public Result<ExpertVO> get(@PathVariable Long id){
        ExpertVO vo = expertService.getExpertDetail(id);
        return Result.success(vo);
    }
}
