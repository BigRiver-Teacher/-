package com.example.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.elderly.entity.ElderFamily;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ElderFamilyMapper extends BaseMapper<ElderFamily> {

    @Select("SELECT DISTINCT elder_id FROM elder_family WHERE is_deleted = 0 AND family_user_id = #{userId}")
    List<Long> selectElderIdsByFamilyUserId(@Param("userId") Long userId);
}
