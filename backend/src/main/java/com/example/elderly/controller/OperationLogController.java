package com.example.elderly.controller;

import lombok.RequiredArgsConstructor;
import com.example.elderly.annotation.RequireRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.elderly.common.PageResult;
import com.example.elderly.common.Result;
import com.example.elderly.common.exception.BusinessException;
import com.example.elderly.dto.PageQueryDTO;
import com.example.elderly.entity.OperationLog;
import com.example.elderly.service.IOperationLogService;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Tag(name = "操作日志")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/log")
@RequireRole({"admin", "staff"})
public class OperationLogController {
    private final IOperationLogService operationLogService;

    @GetMapping("/list")
    public Result<PageResult<OperationLog>> list(PageQueryDTO dto) {
        IPage<OperationLog> page = operationLogService.pageList(dto);
        return Result.success(PageResult.build(page));
    }

    @GetMapping("/export")
    public Result<List<OperationLog>> export(PageQueryDTO dto, HttpServletResponse response) {
        List<OperationLog> list = operationLogService.exportLog(dto);
        return Result.success(list);
    }


    @DeleteMapping("/clean")
    public Result<String> clean(@RequestParam String beforeDate) {
        LocalDateTime date;
        try {
            date = LocalDateTime.parse(beforeDate);
        } catch (DateTimeParseException e) {
            throw new BusinessException(400, "beforeDate 格式不正确，应为 yyyy-MM-ddTHH:mm:ss");
        }
        boolean flag = operationLogService.cleanLog(date);
        if (!flag) {
            throw new BusinessException(500, "清理失败");
        }
        return Result.success("清理成功");
    }
}
