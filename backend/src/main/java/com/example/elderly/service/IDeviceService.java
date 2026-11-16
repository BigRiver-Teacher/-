package com.example.elderly.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.elderly.common.PageResult;
import com.example.elderly.dto.device.DeviceBindDTO;
import com.example.elderly.dto.device.DeviceQueryDTO;
import com.example.elderly.dto.device.DeviceSaveDTO;
import com.example.elderly.dto.device.DeviceUpdateDTO;
import com.example.elderly.entity.Device;
import com.example.elderly.vo.DeviceVO;

public interface IDeviceService extends IService<Device> {
    PageResult<DeviceVO> listDevice(DeviceQueryDTO dto);
    void addDevice(DeviceSaveDTO dto);
    void updateDevice(Long id,DeviceUpdateDTO dto);
    void deleteDevice(Long id);
    void bindElder(DeviceBindDTO dto);
    void unbindElder(Long deviceId);
    DeviceVO getDeviceDetail(Long id);
}
