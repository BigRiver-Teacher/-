package com.example.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.elderly.entity.ElderCaregiver;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ElderCaregiverMapper extends BaseMapper<ElderCaregiver> {

    @Select("SELECT DISTINCT elder_id FROM elder_caregiver WHERE is_deleted = 0 AND caregiver_id = " +
            "(SELECT id FROM caregiver WHERE user_id = #{userId} LIMIT 1)")
    List<Long> selectElderIdsByCaregiverUserId(@Param("userId") Long userId);
}
