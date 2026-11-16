package com.example.elderly.controller;

import com.example.elderly.common.Result;
import com.example.elderly.dto.space.SpaceInspectionSaveDTO;
import com.example.elderly.dto.space.SpaceQueryDTO;
import com.example.elderly.dto.space.SpaceSaveDTO;
import com.example.elderly.entity.Space;
import com.example.elderly.entity.SpaceInspection;
import com.example.elderly.service.SpaceInspectionService;
import com.example.elderly.service.SpaceService;
import com.example.elderly.vo.PageVO;
import com.example.elderly.vo.SpaceDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "适老化空间")
@RestController
@RequestMapping("/api/space")
@RequiredArgsConstructor
public class SpaceController {
    private final SpaceService spaceService;
    private final SpaceInspectionService inspectionService;

    @GetMapping("/list")
    public Result<PageVO<Space>> list(SpaceQueryDTO dto) {
        PageVO<Space> page = spaceService.pageList(dto);
        return Result.success(page);
    }

    @PostMapping
    public Result<Void> add(@RequestBody @Validated SpaceSaveDTO dto) {
        spaceService.saveOrUpdateSpace(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> edit(@PathVariable Long id, @RequestBody @Validated SpaceSaveDTO dto) {
        dto.setId(id);
        spaceService.saveOrUpdateSpace(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        spaceService.deleteSpace(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<SpaceDetailVO> detail(@PathVariable Long id) {
        Space space = spaceService.getDetailById(id);
        List<SpaceInspection> records = inspectionService.listBySpaceId(id);
        SpaceDetailVO vo = new SpaceDetailVO();
        vo.setSpace(space);
        vo.setInspectionHistory(records);
        return Result.success(vo);
    }

    @PostMapping("/inspection")
    public Result<Void> addInspect(@RequestBody @Validated SpaceInspectionSaveDTO dto) {
        inspectionService.addInspection(dto);
        return Result.success();
    }
}
