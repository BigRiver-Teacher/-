package com.example.elderly.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.elderly.dto.PageQueryDTO;
import com.example.elderly.entity.OperationLog;

import java.time.LocalDateTime;
import java.util.List;

public interface IOperationLogService extends IService<OperationLog> {
    IPage<OperationLog> pageList(PageQueryDTO dto);

    List<OperationLog> exportLog(PageQueryDTO dto);

    boolean cleanLog(LocalDateTime beforeDate);
}
