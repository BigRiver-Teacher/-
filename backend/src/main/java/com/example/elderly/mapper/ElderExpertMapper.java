package com.example.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.elderly.entity.ElderExpert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ElderExpertMapper extends BaseMapper<ElderExpert> {

    @Select("SELECT DISTINCT elder_id FROM elder_expert WHERE is_deleted = 0 AND expert_id = " +
            "(SELECT id FROM expert WHERE user_id = #{userId} LIMIT 1)")
    List<Long> selectElderIdsByExpertUserId(@Param("userId") Long userId);
}
