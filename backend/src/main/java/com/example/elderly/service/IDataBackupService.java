package com.example.elderly.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.elderly.dto.BackupOperateDTO;
import com.example.elderly.dto.PageQueryDTO;
import com.example.elderly.entity.DataBackup;
import jakarta.servlet.http.HttpServletResponse;

public interface IDataBackupService extends IService<DataBackup> {
    IPage<DataBackup> pageList(PageQueryDTO dto);

    DataBackup createBackup(BackupOperateDTO dto);

    void downloadBackup(Long id, HttpServletResponse response);

    boolean restoreBackup(Long id);

    boolean removeBackup(Long id);
}
