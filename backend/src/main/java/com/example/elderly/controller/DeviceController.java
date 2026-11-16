package com.example.elderly.controller;

import com.example.elderly.common.PageResult;
import com.example.elderly.common.Result;
import com.example.elderly.dto.device.DeviceBindDTO;
import com.example.elderly.dto.device.DeviceQueryDTO;
import com.example.elderly.dto.device.DeviceSaveDTO;
import com.example.elderly.dto.device.DeviceUpdateDTO;
import com.example.elderly.service.IDeviceService;
import com.example.elderly.vo.DeviceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "设备管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/device")
public class DeviceController {
    private final IDeviceService deviceService;
    @GetMapping("/list")
    public Result<PageResult<DeviceVO>> list(@Validated DeviceQueryDTO dto){
        PageResult<DeviceVO> page = deviceService.listDevice(dto);
        return Result.success(page);
    }
    @PostMapping
    public Result<?> add(@Validated @RequestBody DeviceSaveDTO dto){
        deviceService.addDevice(dto);
        return Result.success();
    }
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Validated @RequestBody DeviceUpdateDTO dto){
        deviceService.updateDevice(id,dto);
        return Result.success();
    }
    @DeleteMapping("{id}")
    public Result<?> delete(@PathVariable Long id){
        deviceService.deleteDevice(id);
        return Result.success();
    }
    @PostMapping("/bind")
    public Result<?> bind(@Validated @RequestBody DeviceBindDTO dto){
        deviceService.bindElder(dto);
        return Result.success();
    }
    @PostMapping("/unbind")
    public Result<?> unbind(@RequestParam Long deviceId){
        deviceService.unbindElder(deviceId);
        return Result.success();
    }
}
