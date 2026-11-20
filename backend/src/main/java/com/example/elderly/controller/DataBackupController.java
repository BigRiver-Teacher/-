package com.example.elderly.controller;

import lombok.RequiredArgsConstructor;
import com.example.elderly.annotation.RequireRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.elderly.common.PageResult;
import com.example.elderly.common.Result;
import com.example.elderly.common.exception.BusinessException;
import com.example.elderly.dto.BackupOperateDTO;
import com.example.elderly.dto.PageQueryDTO;
import com.example.elderly.entity.DataBackup;
import com.example.elderly.service.IDataBackupService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "数据备份")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backup")
@RequireRole({"admin", "staff"})
public class DataBackupController {

    private final IDataBackupService dataBackupService;

    @GetMapping("/list")
    public Result<PageResult<DataBackup>> list(PageQueryDTO dto) {
        IPage<DataBackup> page = dataBackupService.pageList(dto);
        return Result.success(PageResult.build(page));
    }

    @PostMapping
    public Result<DataBackup> create(@Valid @RequestBody BackupOperateDTO dto) {
        DataBackup backup = dataBackupService.createBackup(dto);
        return Result.success(backup);
    }

    @GetMapping("/download/{id}")
    public Result<Void> download(@PathVariable Long id, HttpServletResponse response) {
        dataBackupService.downloadBackup(id, response);
        return Result.success();
    }

    @PostMapping("/restore/{id}")
    public Result<String> restore(@PathVariable Long id) {
        boolean flag = dataBackupService.restoreBackup(id);
        if (!flag) {
            throw new BusinessException(500, "备份恢复失败");
        }
        return Result.success("备份恢复成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean flag = dataBackupService.removeBackup(id);
        if (!flag) {
            throw new BusinessException(500, "备份删除失败");
        }
        return Result.success("备份删除成功");
    }
}
