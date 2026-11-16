package com.example.elderly.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.elderly.entity.Alert;
import com.example.elderly.dto.alert.AlertBatchDTO;
import com.example.elderly.dto.alert.AlertProcessDTO;
import com.example.elderly.dto.alert.AlertQueryDTO;

public interface AlertService extends IService<Alert> {
    IPage<Alert> pageList(AlertQueryDTO dto);
    boolean processAlert(Long id, AlertProcessDTO dto);
    boolean ignoreAlert(Long id);
    boolean readAlert(Long id);
    boolean batchProcess(AlertBatchDTO dto);
    boolean batchIgnore(AlertBatchDTO dto);
}
