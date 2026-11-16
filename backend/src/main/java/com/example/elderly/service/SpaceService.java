package com.example.elderly.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.elderly.dto.space.SpaceQueryDTO;
import com.example.elderly.dto.space.SpaceSaveDTO;
import com.example.elderly.entity.Space;
import com.example.elderly.vo.PageVO;

public interface SpaceService extends IService<Space> {
    PageVO<Space> pageList(SpaceQueryDTO dto);

    void saveOrUpdateSpace(SpaceSaveDTO dto);

    void deleteSpace(Long id);

    Space getDetailById(Long id);
}
