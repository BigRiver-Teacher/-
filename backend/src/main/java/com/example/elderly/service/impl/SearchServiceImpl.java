package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderly.entity.Alert;
import com.example.elderly.entity.Caregiver;
import com.example.elderly.entity.Device;
import com.example.elderly.entity.Elder;
import com.example.elderly.mapper.AlertMapper;
import com.example.elderly.mapper.CareGiverMapper;
import com.example.elderly.mapper.DeviceMapper;
import com.example.elderly.mapper.ElderMapper;
import com.example.elderly.service.SearchService;
import com.example.elderly.vo.SearchVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {
    @Resource
    private ElderMapper elderMapper;
    @Resource
    private DeviceMapper deviceMapper;
    @Resource
    private AlertMapper alterMapper;
    @Resource
    private CareGiverMapper caregiverMapper;
    @Override
    public List<SearchVO> globalSearch(String keyword) {
        ArrayList<SearchVO> result = new ArrayList<>();
        if(keyword==null||keyword.trim().isEmpty()){
            return result;
        }
        String key=keyword.trim();
        // ========== 1.老人查询 ==========
        LambdaQueryWrapper<Elder> elderWrapper = new LambdaQueryWrapper<>();
        elderWrapper.like(Elder::getName, key)
                .or().like(Elder::getPhone, key);
        List<Elder> elderList = elderMapper.selectList(elderWrapper);
        elderList.forEach(item -> {
            SearchVO vo = new SearchVO();
            vo.setModule("elder");
            vo.setTitle(item.getName());
            vo.setDesc("居住类型：" + item.getResidenceType() + " 电话：" + item.getPhone());
            vo.setRoute("/home/elder/base");
            vo.setBusinessId(item.getId());
            result.add(vo);
        });

        LambdaQueryWrapper<Device> deviceWrapper = new LambdaQueryWrapper<>();
        deviceWrapper.like(Device::getDeviceName, key)
                .or().like(Device::getDeviceSn, key);
        List<Device> deviceList = deviceMapper.selectList(deviceWrapper);
        deviceList.forEach(item -> {
            SearchVO vo = new SearchVO();
            vo.setModule("device");
            vo.setTitle(item.getDeviceName() + "(" + item.getDeviceSn() + ")");
            vo.setDesc("状态：" + item.getStatus() + " 绑定老人：" + item.getBindElderName());
            vo.setRoute("/home/device");
            vo.setBusinessId(item.getId());
            result.add(vo);
        });


        LambdaQueryWrapper<Alert> alertWrapper = new LambdaQueryWrapper<>();
        alertWrapper.like(Alert::getElderName, key)
                .or().like(Alert::getAlertType, key);
        List<Alert> alertList = alterMapper.selectList(alertWrapper);
        alertList.forEach(item -> {
            SearchVO vo = new SearchVO();
            vo.setModule("alert");
            vo.setTitle(item.getElderName() + "-" + item.getAlertType());
            vo.setDesc("告警级别：" + item.getLevel() + " 状态：" + item.getStatus());
            vo.setRoute("/home/alert/list");
            vo.setBusinessId(item.getId());
            result.add(vo);
        });

        // ========== 4.陪护人员查询 ==========
        LambdaQueryWrapper<Caregiver> caregiverWrapper = new LambdaQueryWrapper<>();
        caregiverWrapper.like(Caregiver::getName, key)
                .or().like(Caregiver::getPhone, key);
        List<Caregiver> caregiverList = caregiverMapper.selectList(caregiverWrapper);
        caregiverList.forEach(item -> {
            SearchVO vo = new SearchVO();
            vo.setModule("caregiver");
            vo.setTitle(item.getName());
            vo.setDesc("职位：" + item.getPosition() + " 电话：" + item.getPhone());
            vo.setRoute("/home/caregiver");
            vo.setBusinessId(item.getId());
            result.add(vo);
        });
        return result;
    }
}
