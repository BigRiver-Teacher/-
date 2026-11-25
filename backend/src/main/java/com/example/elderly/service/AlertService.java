package com.example.elderly.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.elderly.entity.Alert;
import com.example.elderly.dto.alert.AlertBatchDTO;
import com.example.elderly.dto.alert.AlertProcessDTO;
import com.example.elderly.dto.alert.AlertQueryDTO;
import com.example.elderly.dto.alert.AlertCreateDTO;

public interface AlertService extends IService<Alert> {
    IPage<Alert> pageList(AlertQueryDTO dto);
    boolean processAlert(Long id, AlertProcessDTO dto);
    boolean ignoreAlert(Long id);
    boolean readAlert(Long id);
    boolean batchProcess(AlertBatchDTO dto);
    boolean batchIgnore(AlertBatchDTO dto);
    /**
     * 创建告警（供视频检测等外部能力写入），返回新记录 ID。
     */
    Long createAlert(AlertCreateDTO dto);
}
