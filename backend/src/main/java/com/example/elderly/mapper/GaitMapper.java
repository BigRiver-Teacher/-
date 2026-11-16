package com.example.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.elderly.entity.GaitRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GaitMapper extends BaseMapper<GaitRecord> {
}
