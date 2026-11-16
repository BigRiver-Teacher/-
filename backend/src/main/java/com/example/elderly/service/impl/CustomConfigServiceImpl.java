package com.example.elderly.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderly.dto.custom.DashboardConfigDTO;
import com.example.elderly.dto.custom.MobileConfigDTO;
import com.example.elderly.dto.custom.SidebarConfigDTO;
import com.example.elderly.entity.CustomConfig;
import com.example.elderly.mapper.CustomConfigMapper;
import com.example.elderly.service.CustomConfigService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class CustomConfigServiceImpl implements CustomConfigService {
   @Resource
   private CustomConfigMapper customConfigMapper;
   private static final String TYPE_SIDEBA="sidebar";
   private static final String TYPE_DASHBOARD="dashboard";
   private static final String TYPE_MOBILE="mobile";

    @Override
    public SidebarConfigDTO getSidebarConfig() {
        CustomConfig config = getConfigByType(TYPE_SIDEBA);
        if(config==null){
            return defaultSidebar();
        }
        return JSON.parseObject(config.getConfigJson(),SidebarConfigDTO.class);
    }

    @Override
    public void saveSidebarConfig(SidebarConfigDTO dto) {
        saveConfig(TYPE_SIDEBA,JSON.toJSONString(dto));
    }

    @Override
    public void resetSidebar() {
        saveConfig(TYPE_SIDEBA,JSON.toJSONString(defaultSidebar()));
    }

    @Override
    public DashboardConfigDTO getDashboardConfig() {
        CustomConfig config = getConfigByType(TYPE_DASHBOARD);
        if(config==null){
            return defaultDashboard();
        }
        return JSON.parseObject(config.getConfigJson(),DashboardConfigDTO.class);
    }

    @Override
    public void saveDashboardConfig(DashboardConfigDTO dto) {
        saveConfig(TYPE_DASHBOARD,JSON.toJSONString(dto));
    }

    @Override
    public void resetDashboard() {
        saveConfig(TYPE_DASHBOARD,JSON.toJSONString(defaultDashboard()));
    }

    @Override
    public MobileConfigDTO getMobileConfig() {
        CustomConfig config = getConfigByType(TYPE_MOBILE);
        if(config==null){
            return defaultMobile();
        }
        return JSON.parseObject(config.getConfigJson(),MobileConfigDTO.class);
    }

    @Override
    public void saveMobileConfig(MobileConfigDTO dto) {
        saveConfig(TYPE_MOBILE,JSON.toJSONString(dto));
    }

    @Override
    public void resetMobile() {
        saveConfig(TYPE_MOBILE,JSON.toJSONString(defaultMobile()));
    }
    private CustomConfig getConfigByType(String type){
        LambdaQueryWrapper<CustomConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomConfig::getConfigType,type);
        return customConfigMapper.selectOne(wrapper);
    }
    private void saveConfig(String type,String json){
        CustomConfig exist = getConfigByType(type);
        if(exist==null){
            exist=new CustomConfig();
            exist.setConfigJson( json);
            exist.setConfigType(type);
            customConfigMapper.insert(exist);
        }else{
            exist.setConfigJson( json);
            customConfigMapper.updateById(exist);
        }
    }
    private SidebarConfigDTO defaultSidebar(){
        SidebarConfigDTO dto = new SidebarConfigDTO();
        return dto;
    }
    private DashboardConfigDTO defaultDashboard(){
        DashboardConfigDTO dto = new DashboardConfigDTO();
        return dto;
    }
    private MobileConfigDTO defaultMobile() {
        MobileConfigDTO dto = new MobileConfigDTO();
        return dto;
    }
}
