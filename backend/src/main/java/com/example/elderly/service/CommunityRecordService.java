package com.example.elderly.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.elderly.dto.community.CommunityQueryDTO;
import com.example.elderly.dto.community.CommunityReviewDTO;
import com.example.elderly.dto.community.CommunitySaveDTO;
import com.example.elderly.entity.CommunityRecord;

public interface CommunityRecordService extends IService<CommunityRecord> {
    IPage<CommunityRecord> pageList(CommunityQueryDTO dto);
    Long add(CommunitySaveDTO dto);
    boolean edit(Long id, CommunitySaveDTO dto);
    boolean review(Long id, CommunityReviewDTO dto);
}
