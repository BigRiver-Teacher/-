package com.example.elderly.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.elderly.common.PageResult;
import com.example.elderly.common.Result;
import com.example.elderly.dto.schedule.ScheduleTaskProcessDTO;
import com.example.elderly.dto.schedule.ScheduleTaskQueryDTO;
import com.example.elderly.entity.ScheduleTask;
import com.example.elderly.service.IScheduleTaskService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "康养日程")
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleTaskController {
    private final IScheduleTaskService scheduleTaskService;
    @GetMapping("/tasks")
    public Result<PageResult<ScheduleTask>> taskList(@Validated ScheduleTaskQueryDTO dto){
        return Result.success(PageResult.build(scheduleTaskService.taskPage(dto)));
    }
    @GetMapping("/task/{id}")
    public Result<ScheduleTask> getDetail(@PathVariable @NotNull(message = "id不能为空")Long id){
        ScheduleTask task = scheduleTaskService.getTaskDetail(id);
        if(task==null){
            return Result.fail(404,"任务不存在");
        }
        return Result.success(task);
    }
    @PutMapping("task/{id}/process")
    public Result<Void> processTask(@PathVariable @NotNull(message = "id不能为空")Long id,
                                    @RequestBody @Validated ScheduleTaskProcessDTO dto) {
        Boolean flag = scheduleTaskService.processTask(id, dto);
        if(!flag){
            return Result.fail(400, "处理失败");
        }
        return Result.success();
    }
}
