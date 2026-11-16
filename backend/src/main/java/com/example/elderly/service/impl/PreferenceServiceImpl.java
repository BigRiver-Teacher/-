package com.example.elderly.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderly.dto.TestPushDTO;
import com.example.elderly.dto.preference.PreferenceConfigDTO;
import java.util.List;
import com.example.elderly.entity.PreferenceConfig;
import com.example.elderly.mapper.PreferenceConfigMapper;
import com.example.elderly.service.PreferenceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PreferenceServiceImpl implements PreferenceService {
    @Resource
    private PreferenceConfigMapper configMapper;
    @Override
    public PreferenceConfigDTO getConfig() {
        LambdaQueryWrapper<PreferenceConfig> wrapper = new LambdaQueryWrapper<>();
        PreferenceConfig config = configMapper.selectOne(wrapper.last("LIMIT 1"));
        if(config==null){
            return new PreferenceConfigDTO();
        }
        PreferenceConfigDTO dto = new PreferenceConfigDTO();

        dto.setChannels(parseList(config.getChannels(), PreferenceConfigDTO.ChannelItem.class));
        dto.setLevelFilters(parseList(config.getLevelFiters(), PreferenceConfigDTO.LevelFilterItem.class));
        dto.setScenes(parseList(config.getScenes(), PreferenceConfigDTO.SceneItem.class));
        dto.setSilentPeriods(parseList(config.getSilentPeriods(), PreferenceConfigDTO.SilentPeriodItem.class));
        dto.setTemplates(parseList(config.getTemplates(), PreferenceConfigDTO.TemplateItem.class));
        dto.setReceivers(parseList(config.getReceivers(), PreferenceConfigDTO.ReceiverItem.class));
        return dto;
    }

    private <T> List<T> parseList(String json, Class<T> clazz) {
        try {
            return JSON.parseArray(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional
    public boolean saveConfig(PreferenceConfigDTO dto) {
        LambdaQueryWrapper<PreferenceConfig> wrapper = new LambdaQueryWrapper<>();
        PreferenceConfig cfg = configMapper.selectOne(wrapper.last("LIMIT 1"));
        if(cfg==null){
            cfg=new PreferenceConfig();
        }
        cfg.setChannels(JSON.toJSONString(dto.getChannels()));
        cfg.setLevelFiters(JSON.toJSONString(dto.getLevelFilters()));
        cfg.setScenes(JSON.toJSONString(dto.getScenes()));
        cfg.setSilentPeriods(JSON.toJSONString(dto.getSilentPeriods()));
        cfg.setTemplates(JSON.toJSONString(dto.getTemplates()));
        cfg.setReceivers(JSON.toJSONString(dto.getReceivers()));
        if(cfg.getId()==null){
            configMapper.insert(cfg);
        }else{
            configMapper.updateById(cfg);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean resetDefault() {
        configMapper.delete(new LambdaQueryWrapper<>());
        return true;
    }

    @Override
    public boolean testPush(TestPushDTO dto) {
        return true;
    }
}
