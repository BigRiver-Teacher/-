package com.example.elderly.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderly.common.Result;
import com.example.elderly.entity.Alert;
import com.example.elderly.entity.DataBackup;
import com.example.elderly.entity.Device;
import com.example.elderly.entity.OperationLog;
import com.example.elderly.entity.User;
import com.example.elderly.mapper.AlertMapper;
import com.example.elderly.mapper.DataBackupMapper;
import com.example.elderly.mapper.DeviceMapper;
import com.example.elderly.mapper.OperationLogMapper;
import com.example.elderly.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统概览统计接口
 */
@Tag(name = "系统概览统计")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stats")
public class StatsController {
    private final UserMapper userMapper;
    private final DataBackupMapper dataBackupMapper;
    private final OperationLogMapper operationLogMapper;
    private final DeviceMapper deviceMapper;
    private final AlertMapper alertMapper;

    /**
     * 系统概览：注册用户数 / 生成报告数（报告管理操作次数） / 在线设备数 / 待处理预警数
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Long registeredUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getDeleted, 0));
        Long reports = operationLogMapper.selectCount(
                new LambdaQueryWrapper<OperationLog>().like(OperationLog::getModule, "报告"));
        Long backups = dataBackupMapper.selectCount(new LambdaQueryWrapper<DataBackup>());
        Long onlineDevices = deviceMapper.selectCount(
                new LambdaQueryWrapper<Device>().eq(Device::getStatus, "ONLINE"));
        Long pendingWarnings = alertMapper.selectCount(
                new LambdaQueryWrapper<Alert>().in(Alert::getStatus, "pending", "processing"));
        Map<String, Object> data = new HashMap<>();
        data.put("registeredUsers", registeredUsers);
        data.put("generatedReports", reports + backups);
        data.put("onlineDevices", onlineDevices);
        data.put("pendingWarnings", pendingWarnings);
        return Result.success(data);
    }
}
