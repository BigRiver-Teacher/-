package com.example.elderly.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.elderly.dto.push.PushBatchRetryDTO;
import com.example.elderly.dto.push.PushLogQueryDTO;
import com.example.elderly.entity.PushLog;

public interface PushLogService extends IService<PushLog> {
    IPage<PushLog> pageList(PushLogQueryDTO dto);
    boolean retryPush(Long id);
    boolean batchRetry(PushBatchRetryDTO dto);
}
