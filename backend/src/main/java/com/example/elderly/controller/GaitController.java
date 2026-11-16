package com.example.elderly.controller;

import lombok.RequiredArgsConstructor;
import com.example.elderly.common.Result;
import com.example.elderly.dto.GaitDTO;
import com.example.elderly.entity.GaitRecord;
import com.example.elderly.service.GaitService;
import com.example.elderly.vo.PageVO;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "步态分析")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gait")
public class GaitController {
    private final GaitService gaitService;
    @GetMapping("/list")
    public Result<PageVO<GaitRecord>> list(
            @RequestParam(defaultValue = "1")Integer page,
            @RequestParam(defaultValue = "10")Integer size,
            @RequestParam(required = false)String name,
            @RequestParam(required = false)String result
    ){
        PageVO<GaitRecord> pageVO = gaitService.listPage(page, size, name, result);
        return Result.success(pageVO);
    }
    @PostMapping
    public Result<Long> add(@Validated @RequestBody GaitDTO dto){
        Long newId = gaitService.addGait(dto);
        return Result.success(newId);
    }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id){
        Boolean flag = gaitService.deleteGait(id);
        if(!flag){
            return Result.fail(400,"删除失败");
        }
        return Result.success();
    }
}
