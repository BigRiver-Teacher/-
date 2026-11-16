package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.elderly.dto.PageQueryDTO;
import com.example.elderly.entity.OperationLog;
import com.example.elderly.mapper.OperationLogMapper;
import com.example.elderly.service.IOperationLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog>
        implements IOperationLogService {

    @Resource
    private OperationLogMapper operationLogMapper;

    @Override
    public IPage<OperationLog> pageList(PageQueryDTO dto) {
        Page<OperationLog> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<OperationLog> wrapper = Wrappers.lambdaQuery(OperationLog.class);
        if (StringUtils.hasText(dto.getType())) {
            wrapper.eq(OperationLog::getOperateType, dto.getType());
        }
        if (StringUtils.hasText(dto.getModule())) {
            wrapper.eq(OperationLog::getModule, dto.getModule());
        }
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<OperationLog> exportLog(PageQueryDTO dto) {
        LocalDateTime start = StringUtils.hasText(dto.getStartDate())
                ? LocalDateTime.parse(dto.getStartDate()) : null;
        LocalDateTime end = StringUtils.hasText(dto.getEndDate())
                ? LocalDateTime.parse(dto.getEndDate()) : null;
        return operationLogMapper.selectExportList(dto.getType(), dto.getModule(), start, end);
    }

    @Override
    public boolean cleanLog(LocalDateTime beforeDate) {
        int count = operationLogMapper.deleteBeforeDate(beforeDate);
        return count >= 0;
    }
}
