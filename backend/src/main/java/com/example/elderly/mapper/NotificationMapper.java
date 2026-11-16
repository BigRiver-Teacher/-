package com.example.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.elderly.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    @Select("SELECT COUNT(1) FROM notification WHERE user_id=#{userId} AND `read`=false")
    Long countUnRead(@Param("userId") Long userId);
}