package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.elderly.dto.schedule.ScheduleTaskProcessDTO;
import com.example.elderly.dto.schedule.ScheduleTaskQueryDTO;
import com.example.elderly.entity.ScheduleTask;
import com.example.elderly.mapper.ScheduleTaskMapper;
import com.example.elderly.service.IScheduleTaskService;
import org.springframework.util.StringUtils;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ScheduleTaskServiceImpl extends ServiceImpl<ScheduleTaskMapper, ScheduleTask> implements IScheduleTaskService {
    @Override
    public IPage<ScheduleTask> taskPage(ScheduleTaskQueryDTO dto) {
        Page<ScheduleTask> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<ScheduleTask> wrapper = Wrappers.lambdaQuery(ScheduleTask.class);
        if (StringUtils.hasText(dto.getDate())) {
            LocalDate day = LocalDate.parse(dto.getDate());
            wrapper.between(ScheduleTask::getTaskDate, day.atStartOfDay(), day.atTime(23, 59, 59));
        }
        wrapper.like(StringUtils.hasText(dto.getElderName()), ScheduleTask::getElderName, dto.getElderName())
                .eq(StringUtils.hasText(dto.getTaskType()), ScheduleTask::getTaskType, dto.getTaskType())
                .eq(StringUtils.hasText(dto.getPriority()), ScheduleTask::getPriority, dto.getPriority())
                .eq(StringUtils.hasText(dto.getTaskStatus()), ScheduleTask::getTaskStatus, dto.getTaskStatus())
                .orderByAsc(ScheduleTask::getPriority)
                .orderByDesc(ScheduleTask::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public ScheduleTask getTaskDetail(Long id) {
        return getById(id);
    }

    @Override
    public Boolean processTask(Long taskId, ScheduleTaskProcessDTO dto) {
        ScheduleTask task = getById(taskId);
        if(task==null){
            return false;
        }
        task.setTaskStatus(dto.getTaskStatus());
        task.setHandleResult(dto.getHandleResult());
        task.setHandler(dto.getHandler());
        task.setNote(dto.getNote());
        if("completed".equals(dto.getTaskStatus())){
            task.setCompleteTime(LocalDateTime.now());
        }
        return updateById(task);
    }
}
