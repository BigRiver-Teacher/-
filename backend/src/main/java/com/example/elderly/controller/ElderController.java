package com.example.elderly.controller;

import lombok.RequiredArgsConstructor;
import com.example.elderly.common.Result;
import com.example.elderly.dto.ElderDTO;
import com.example.elderly.entity.Elder;
import com.example.elderly.service.ElderService;
import com.example.elderly.vo.PageVO;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "老人档案")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/elder")
public class ElderController {
    private final ElderService elderService;
    @GetMapping("/list")
    public Result<PageVO<Elder>> list(
            @RequestParam(defaultValue = "1")Integer page,
            @RequestParam(defaultValue = "10")Integer size,
            @RequestParam(required = false)String name,
            @RequestParam(required = false)String phone
    ){
        PageVO<Elder> pageVO = elderService.listPage(page, size, name, phone);
        return Result.success(pageVO);
    }
    @PostMapping
    public Result<Long> add(@Validated @RequestBody ElderDTO dto){
        Long newId = elderService.addElder(dto);
        return Result.success(newId);
    }
    @GetMapping("/{id}")
    public Result<Elder> detail(@PathVariable Long id){
        Elder elder = elderService.getElderById(id);
        if(elder == null){
            return Result.fail(404,"该老人档案不存在");
        }
        return Result.success(elder);
    }
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Validated @RequestBody ElderDTO dto){
        Boolean flag = elderService.updateElder(id, dto);
        if(!flag){
            return Result.fail(400,"更新失败,该数据不存在");
        }
        return Result.success();
    }
    @DeleteMapping("/{id}")
    public Result<Void>delete(@PathVariable Long id){
        Boolean flag = elderService.deleteElder(id);
        if(!flag){
            return Result.fail(400,"删除失败，该老人档案不存在");
        }
        return Result.success();
    }


}
