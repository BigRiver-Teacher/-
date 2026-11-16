package com.example.elderly.controller;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.elderly.common.PageResult;
import com.example.elderly.common.Result;
import com.example.elderly.dto.push.PushBatchRetryDTO;
import com.example.elderly.dto.push.PushLogQueryDTO;
import com.example.elderly.entity.PushLog;
import com.example.elderly.service.PushLogService;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "推送日志")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/push-log")
public class PushLogController {
    private final PushLogService pushLogService;
    @GetMapping("/list")
    public Result<PageResult<PushLog>> list(@Validated PushLogQueryDTO dto){
        return Result.success(PageResult.build(pushLogService.pageList(dto)));
    }
    @PostMapping("/retry/{id}")
    public Result<String>retry(@PathVariable Long id){
        boolean ok = pushLogService.retryPush(id);
        return ok?Result.success("重推已发起"):Result.fail(400, "日志不存在");
    }
    @PostMapping("/batch-retry")
    public Result<String> batchRetry(@Validated @RequestBody PushBatchRetryDTO dto){
        pushLogService.batchRetry(dto);
        return Result.success("批量重推完成");

    }
}
