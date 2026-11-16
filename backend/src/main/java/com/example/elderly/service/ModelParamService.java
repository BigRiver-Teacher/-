package com.example.elderly.service;

import com.example.elderly.dto.model.ModelParamSaveDTO;
import com.example.elderly.vo.model.ModelParamVO;
import com.example.elderly.vo.model.ModelParamVersionVO;

import java.util.List;

public interface ModelParamService {
    ModelParamVO getCurrentParams();

    void saveParams(ModelParamSaveDTO dto, String operator);

    void resetDefault(String operator);

    List<ModelParamVersionVO> getVersionHistory();

    void rollbackVersion(String targetVersion, String operator);
}
