package com.example.elderly.controller;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.elderly.common.PageResult;
import com.example.elderly.common.Result;
import com.example.elderly.dto.consult.ConsultAssignDTO;
import com.example.elderly.dto.consult.ConsultCompleteDTO;
import com.example.elderly.dto.consult.ConsultCreateDTO;
import com.example.elderly.dto.consult.ConsultQueryDTO;
import com.example.elderly.entity.Consult;
import com.example.elderly.service.ConsultService;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "康养咨询")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/consult")
public class ConsultController {
    private final ConsultService consultService;

    @GetMapping("/list")
    public Result<PageResult<Consult>> list(@Validated ConsultQueryDTO dto) {
        IPage<Consult> page = consultService.pageList(dto);
        return Result.success(PageResult.build(page));
    }

    @PostMapping
    public Result<Long> create(@RequestBody @Validated ConsultCreateDTO dto) {
        Long id = consultService.createConsult(dto);
        return Result.success(id);
    }

    @PutMapping("/assign/{id}")
    public Result<String> assign(@PathVariable Long id, @RequestBody @Validated ConsultAssignDTO dto) {
        Boolean ok = consultService.assignConsult(id, dto.getExpertId());
        return ok ? Result.success("分配专家成功") : Result.fail(500, "分配失败");
    }

    @PutMapping("/complete/{id}")
    public Result<String> complete(@PathVariable Long id, @RequestBody @Validated ConsultCompleteDTO dto) {
        Boolean ok = consultService.completeConsult(id, dto.getReply(), dto.getRating(), dto.getFeedback());
        return ok ? Result.success("咨询完成") : Result.fail(500, "操作失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        Boolean remove = consultService.removeById(id);
        return remove ? Result.success("删除成功") : Result.fail(500, "删除失败");
    }

    @GetMapping("/{id}")
    public Result<Consult> detail(@PathVariable Long id) {
        Consult consult = consultService.getById(id);
        if (consult == null) {
            return Result.fail(404, "咨询记录不存在");
        }
        return Result.success(consult);
    }
}
