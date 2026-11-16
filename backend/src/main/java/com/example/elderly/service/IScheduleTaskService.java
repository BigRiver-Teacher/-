package com.example.elderly.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.elderly.dto.schedule.ScheduleTaskProcessDTO;
import com.example.elderly.dto.schedule.ScheduleTaskQueryDTO;
import com.example.elderly.entity.ScheduleTask;
public interface IScheduleTaskService extends IService<ScheduleTask> {
    IPage<ScheduleTask> taskPage(ScheduleTaskQueryDTO dto);

    ScheduleTask getTaskDetail(Long id);
    Boolean processTask(Long taskId,
                        ScheduleTaskProcessDTO dto);
}
