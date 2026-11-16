package com.example.elderly.controller;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.elderly.common.PageResult;
import com.example.elderly.common.Result;
import com.example.elderly.dto.community.CommunityQueryDTO;
import com.example.elderly.dto.community.CommunityReviewDTO;
import com.example.elderly.dto.community.CommunitySaveDTO;
import com.example.elderly.entity.CommunityRecord;
import com.example.elderly.service.CommunityRecordService;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "社区联动")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community")
public class CommunityRecordController {
    private final CommunityRecordService communityRecordService;
    @GetMapping("/list")
    public Result<PageResult<CommunityRecord>> list(@Validated CommunityQueryDTO dto){
        return Result.success(PageResult.build(communityRecordService.pageList(dto)));
    }
    @PostMapping
    public Result<String> add(@Validated @RequestBody CommunitySaveDTO dto){
        Long id = communityRecordService.add(dto);
        return Result.success("添加成功,ID:"+id);
    }
    @PutMapping("/{id}")
    public Result<String> edit(@PathVariable Long id,@Validated @RequestBody CommunitySaveDTO dto){
        boolean ok=communityRecordService.edit(id,dto);
        return ok? Result.success("编辑成功"):Result.fail(400,"警告不存在");
    }
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id){
        boolean ok=communityRecordService.removeById(id);
        return ok?Result.success("删除成功"):Result.fail(400,"警告不存在");
    }
    @PutMapping("/review/{id}")
    public Result<String> review(@PathVariable Long id,@Validated @RequestBody CommunityReviewDTO dto){
        boolean ok=communityRecordService.review(id,dto);
        return ok?Result.success("审核成功"):Result.fail(400,"警告不存在");
    }
}
