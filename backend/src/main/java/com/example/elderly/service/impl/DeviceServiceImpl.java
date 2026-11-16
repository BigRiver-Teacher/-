package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.elderly.common.PageResult;
import com.example.elderly.dto.device.DeviceBindDTO;
import com.example.elderly.dto.device.DeviceQueryDTO;
import com.example.elderly.dto.device.DeviceSaveDTO;
import com.example.elderly.dto.device.DeviceUpdateDTO;
import com.example.elderly.entity.Device;
import com.example.elderly.mapper.DeviceMapper;
import com.example.elderly.service.IDeviceService;
import com.example.elderly.vo.DeviceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements IDeviceService {
    private final DeviceMapper deviceMapper;
    @Override
    public PageResult<DeviceVO> listDevice(DeviceQueryDTO dto) {
        Page<Device> page = new Page<>(dto.getPages(), dto.getSize());
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.hasText(dto.getDeviceSn())){
            wrapper.like(Device::getDeviceSn,dto.getDeviceSn());
        }
        if(StringUtils.hasText(dto.getDeviceName())){
            wrapper.like(Device::getDeviceName,dto.getDeviceName());
        }
        if(StringUtils.hasText(dto.getStatus())){
            wrapper.eq(Device::getStatus,dto.getStatus());
        }
        if(dto.getBindElderId()!=null){
            wrapper.eq(Device::getBindElderId,dto.getBindElderId());

        }
        wrapper.orderByDesc(Device::getCreateTime);
        IPage<Device> pageData = deviceMapper.selectPage(page, wrapper);
        List<DeviceVO> voList = pageData.getRecords().stream().map(this::convertVO)
                .collect(Collectors.toList());
        return new PageResult<>(pageData.getTotal(),voList);
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void addDevice(DeviceSaveDTO dto) {
        Device device = new Device();
        device.setDeviceSn(dto.getDeviceSn());
        device.setDeviceName(dto.getDeviceName());
        device.setDeviceType(dto.getDeviceType());
        device.setLocation(dto.getLocation());
        device.setInstallDate(dto.getInstallDate());
        device.setAlertRule(dto.getAlertRule());
        device.setStatus("pending");
        deviceMapper.insert(device);
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void updateDevice(Long id,DeviceUpdateDTO dto) {
        Device device = getById(id);
        if(device==null){
            throw new RuntimeException("设备不存在");
        }
        device.setDeviceName(dto.getDeviceName());
        device.setDeviceType(dto.getDeviceType());
        device.setLocation(dto.getLocation());
        device.setAlertRule(dto.getAlertRule());
        deviceMapper.updateById(device);
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void deleteDevice(Long id) {
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void bindElder(DeviceBindDTO dto) {
        Device device = getById(dto.getDeviceId());
        if(device==null){
            throw new RuntimeException("设备不存在");
        }
        device.setBindElderId(Long.valueOf(dto.getElderId()));
        device.setBindTime(LocalDateTime.now());
        updateById( device);
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void unbindElder(Long deviceId) {
        Device device = getById(deviceId);
        if(device==null){
            throw new RuntimeException("设备不存在");
        }
        device.setBindElderId(null);
        device.setBindTime(null);
        updateById(device);
    }

    @Override
    public DeviceVO getDeviceDetail(Long id) {
        Device device = getById(id);
        return convertVO(device);
    }
    private DeviceVO convertVO(Device entity){
        DeviceVO vo = new DeviceVO();
        vo.setId(entity.getId());
        vo.setDeviceSn(entity.getDeviceSn());
        vo.setDeviceName(entity.getDeviceName());
        vo.setDeviceType(entity.getDeviceType());
        vo.setStatus(entity.getStatus());
        vo.setBattery(entity.getBattery());
        vo.setFirmware(entity.getFirmware());
        vo.setBindElder(entity.getBindElderName());
        vo.setBindElder(entity.getBindElderName());
        vo.setBindElderId(entity.getBindElderId());
        vo.setBindTime(entity.getBindTime());
        vo.setLastOnline(entity.getLastOnlineTime());
        vo.setLocation(entity.getLocation());
        vo.setInstallDate(entity.getInstallDate());
        vo.setAlertRule(entity.getAlertRule());
        return vo;
    }

}
