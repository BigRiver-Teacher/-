package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderly.dto.model.ModelParamSaveDTO;
import com.example.elderly.entity.ModelParam;
import com.example.elderly.entity.ModelParamVersion;
import com.example.elderly.mapper.ModelParamMapper;
import com.example.elderly.mapper.ModelParamVersionMapper;
import com.example.elderly.service.ModelParamService;
import com.example.elderly.vo.model.ModelParamVO;
import com.example.elderly.vo.model.ModelParamVersionVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModelParamServiceImpl implements ModelParamService {
    @Resource
    private ModelParamMapper modelParamMapper;
    @Resource
    private ModelParamVersionMapper modelParamVersionMapper;
    private static final DateTimeFormatter VERSION_FORMAT=DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private ModelParam getDefaultParam() {
        ModelParam param = new ModelParam();
        ModelParam.DetectionParam detection = new ModelParam.DetectionParam();
        detection.setSensitivity(5);
        detection.setAcceleration(2.0);
        detection.setAngle(30.0);
        detection.setTimeWindow(30);
        detection.setConfidence(80);
        param.setDetection(detection);


        ModelParam.AlertParam alert = new ModelParam.AlertParam();
        alert.setDelay(5);
        alert.setCooldown(10);
        alert.setThreshold(3);
        param.setAlert(alert);


        ModelParam.DeviceParam device = new ModelParam.DeviceParam();
        device.setSensorFrequency(10);
        device.setCameraFps(15);
        device.setOfflineTimeout(30);
        param.setDevice(device);


        ModelParam.EvaluationParam evaluation = new ModelParam.EvaluationParam();
        evaluation.setGaitThreshold(20);
        evaluation.setActivityThreshold(15);
        evaluation.setRiskCycle(7);
        param.setEvaluation(evaluation);
        return param;
    }

    @Override
    public ModelParamVO getCurrentParams() {
        ModelParamVO vo = new ModelParamVO();
        ModelParam param = modelParamMapper.selectOne(new LambdaQueryWrapper<ModelParam>().last("LIMIT 1"));
        if (param == null) {
            param = getDefaultParam();
            modelParamMapper.insert(param);
        }
        vo.setDetection(param.getDetection());
        vo.setAlert(param.getAlert());
        vo.setDevice(param.getDevice());
        vo.setEvaluation(param.getEvaluation());

        ModelParamVO.VersionInfo versionInfo = new ModelParamVO.VersionInfo();
        List<ModelParamVersion> versionList = modelParamVersionMapper.selectList(new LambdaQueryWrapper<>());
        String currentVersion = param.getUpdateTime().format(VERSION_FORMAT);
        versionInfo.setCurrent(currentVersion);
        versionInfo.setStatus("running");
        versionInfo.setLastUpdate(param.getUpdateTime());
        versionInfo.setTotalVersions(versionList.size());
        vo.setVersion(versionInfo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveParams(ModelParamSaveDTO dto, String operator) {
        ModelParam updateParam = new ModelParam();
        updateParam.setDetection(dto.getDetection());
        updateParam.setAlert(dto.getAlert());
        updateParam.setDevice(dto.getDevice());
        updateParam.setEvaluation(dto.getEvaluation());
        ModelParam exist = modelParamMapper.selectOne(new LambdaQueryWrapper<ModelParam>().last("LIMIT 1"));
        if (exist == null) {
            modelParamMapper.insert(updateParam);
        } else {
            updateParam.setId(exist.getId());
            modelParamMapper.updateById(updateParam);
        }

        String newVersion = LocalDateTime.now().format(VERSION_FORMAT);
        ModelParamVersion version = new ModelParamVersion();
        version.setVersion(newVersion);
        version.setDetection(dto.getDetection());
        version.setAlert(dto.getAlert());
        version.setDevice(dto.getDevice());
        version.setEvaluation(dto.getEvaluation());
        version.setOperator(operator);
        version.setDescription(dto.getDescription() == null ? "常规参数修改" : dto.getDescription());
        modelParamVersionMapper.insert(version);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetDefault(String operator) {
        ModelParam defaultParam = getDefaultParam();
        ModelParam exist = modelParamMapper.selectOne(new LambdaQueryWrapper<ModelParam>().last("LIMIT 1"));
        if (exist != null) {
            defaultParam.setId(exist.getId());
            modelParamMapper.updateById(defaultParam);
        } else {
            modelParamMapper.insert(defaultParam);
        }
        String version = LocalDateTime.now().format(VERSION_FORMAT);
        ModelParamVersion record = new ModelParamVersion();
        record.setVersion(version);
        record.setDetection(defaultParam.getDetection());
        record.setAlert(defaultParam.getAlert());
        record.setDevice(defaultParam.getDevice());
        record.setEvaluation(defaultParam.getEvaluation());
        record.setOperator(operator);
        record.setDescription("恢复系统默认参数");
        modelParamVersionMapper.insert(record);
    }

    @Override
    public List<ModelParamVersionVO> getVersionHistory() {
        LambdaQueryWrapper<ModelParamVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ModelParamVersion::getCreateTime);
        List<ModelParamVersion> list=modelParamVersionMapper.selectList(wrapper);
        return list.stream().map(item->{
            ModelParamVersionVO vo = new ModelParamVersionVO();
            vo.setVersion(item.getVersion());
            vo.setTime(item.getCreateTime());
            vo.setUser(item.getOperator());
            vo.setDescription(item.getDescription());
            return vo;
        }).collect(Collectors.toList());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackVersion(String targetVersion, String operator) {
        ModelParamVersion target = modelParamVersionMapper.selectOne(new LambdaQueryWrapper<ModelParamVersion>()
                .eq(ModelParamVersion::getVersion, targetVersion));
        if (target == null) {
            throw new RuntimeException("目标版本不存在");
        }
        ModelParam param = new ModelParam();
        param.setDetection(target.getDetection());
        param.setAlert(target.getAlert());
        param.setDevice(target.getDevice());
        param.setEvaluation(target.getEvaluation());
        ModelParam exist = modelParamMapper.selectOne(new LambdaQueryWrapper<ModelParam>().last("LIMIT 1"));
        if (exist != null) {
            param.setId(exist.getId());
            modelParamMapper.updateById(param);
        } else {
            modelParamMapper.insert(param);
        }
        String newVersion = LocalDateTime.now().format(VERSION_FORMAT);
        ModelParamVersion rollbackRecord = new ModelParamVersion();
        rollbackRecord.setVersion(newVersion);
        rollbackRecord.setDetection(target.getDetection());
        rollbackRecord.setAlert(target.getAlert());
        rollbackRecord.setDevice(target.getDevice());
        rollbackRecord.setEvaluation(target.getEvaluation());
        rollbackRecord.setOperator(operator);
        rollbackRecord.setDescription("版本回滚至:" + targetVersion);
        modelParamVersionMapper.insert(rollbackRecord);
    }
}
