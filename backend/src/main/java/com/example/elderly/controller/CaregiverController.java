package com.example.elderly.controller;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.elderly.common.PageResult;
import com.example.elderly.common.Result;
import com.example.elderly.dto.caregiver.CaregiverQueryDTO;
import com.example.elderly.dto.caregiver.CaregiverSaveDTO;
import com.example.elderly.entity.Caregiver;
import com.example.elderly.service.CaregiverService;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "护工管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/caregiver")
public class CaregiverController {
    private final CaregiverService caregiverService;
    @GetMapping("/list")
    public Result<PageResult<Caregiver>> list(@Validated CaregiverQueryDTO dto){
        IPage<Caregiver> page = caregiverService.pageList(dto);
        return Result.success(PageResult.build(page));
    }
    @PostMapping
    public Result<Long> add(@RequestBody @Validated CaregiverSaveDTO dto){
        Long id = caregiverService.addCaregiver(dto);
        return Result.success(id);
    }
    @PutMapping("/{id}")
    public Result<String> edit(@PathVariable Long id,
                               @RequestBody @Validated CaregiverSaveDTO dto){
        Boolean ok = caregiverService.updateCaregiver(id, dto);
        return ok?Result.success("编辑成功"):Result.fail(400,"编辑失败");
    }
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id){
        boolean remove = caregiverService.removeById(id);
        return remove?Result.success("删除成功"):Result.fail(400,"删除失败");
    }
}
