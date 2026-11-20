package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.elderly.common.BackupStatusEnum;
import com.example.elderly.dto.BackupOperateDTO;
import com.example.elderly.dto.PageQueryDTO;
import com.example.elderly.entity.DataBackup;
import com.example.elderly.mapper.DataBackupMapper;
import com.example.elderly.service.IDataBackupService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DataBackupServiceImpl extends ServiceImpl<DataBackupMapper, DataBackup> implements IDataBackupService {
    @Resource
    private DataBackupMapper dataBackupMapper;

    /**
     * 备份文件存放目录。默认相对当前工作目录的 backup/，可用 app.backup.dir
     * 或环境变量 APP_BACKUP_DIR 覆盖（绝对路径亦可），避免硬编码盘符导致换机器失败。
     */
    @Value("${app.backup.dir:backup}")
    private String backupDir;

    @Override
    public IPage<DataBackup> pageList(PageQueryDTO dto) {
        Page<DataBackup> page = new Page<>(Long.valueOf(dto.getPage()), Long.valueOf(dto.getSize()));
        return baseMapper.selectPage(page, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataBackup createBackup(BackupOperateDTO dto) {
        File dir = resolveBackupDir();
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = dto.getType().getCode() + "_" + timeStr + ".sql.zip";
        String filePath = new File(dir, fileName).getAbsolutePath();

        DataBackup backup = new DataBackup();
        backup.setFileName(fileName);
        backup.setBackupTime(LocalDateTime.now());
        backup.setType(dto.getType() == null ? null : dto.getType().getCode());
        backup.setFilePath(filePath);

        boolean backupSuccess = execMysqlDump(filePath);
        if (backupSuccess) {
            File file = new File(filePath);
            double sizeMb = file.length() / 1024.0 / 1024;
            backup.setFileSize(String.format("%.2fMB", sizeMb));
            backup.setStatus(BackupStatusEnum.SUCCESS.getCode());
        } else {
            backup.setStatus(BackupStatusEnum.FAILED.getCode());
            backup.setFileSize("0MB");
        }
        save(backup);
        return backup;
    }


    /** 解析并创建备份目录：相对路径基于启动时的工作目录，绝对路径原样使用。 */
    private File resolveBackupDir() {
        File dir = new File(backupDir);
        if (!dir.isAbsolute()) {
            dir = new File(System.getProperty("user.dir"), backupDir);
        }
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("无法创建备份目录: " + dir.getAbsolutePath());
        }
        return dir;
    }

    private boolean execMysqlDump(String filePath) {
        try {
            File mock = new File(filePath);
            mock.createNewFile();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void downloadBackup(Long id, HttpServletResponse response) {

        DataBackup backup = this.getById(id);
        if (backup == null) {
            throw new RuntimeException("备份记录不存在");
        }
        File file = new File(backup.getFilePath());
        if (!file.exists()) {
            throw new RuntimeException("备份文件丢失");
        }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + backup.getFileName());
        try {
            Files.copy(file.toPath(), response.getOutputStream());
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreBackup(Long id) {
        DataBackup backup = this.getById(id);
        if (backup == null || !BackupStatusEnum.SUCCESS.getCode().equals(backup.getStatus())) {
            throw new RuntimeException("无效备份文件");
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeBackup(Long id) {
        DataBackup backup = this.getById(id);
        if (backup != null) {
            File file = new File(backup.getFilePath());
            if (file.exists()) {
                file.delete();
            }
        }
        return removeById(id);
    }
}
