package com.example.elderly.mapper;

import com.example.elderly.vo.echart.EchartPieVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MonitorEchartMapper {

    @Select("SELECT status name, COUNT(1) value FROM device GROUP BY status")
    List<EchartPieVO> selectMonitorDeviceStatus();

    @Select("SELECT DATE_FORMAT(date,'%m-%d %H:%i') name, ROUND(AVG(speed)*100,0) value FROM gait_record " +
            "WHERE elder_id=#{elderId} GROUP BY DATE_FORMAT(date,'%m-%d %H:%i') ORDER BY MAX(date) DESC LIMIT 20")
    List<EchartPieVO> selectAccelerationData(@Param("elderId") Long elderId);

    @Select("SELECT DATE_FORMAT(date,'%m-%d %H:%i') name, ROUND(AVG(frequency),0) value FROM gait_record " +
            "WHERE elder_id=#{elderId} GROUP BY DATE_FORMAT(date,'%m-%d %H:%i') ORDER BY MAX(date) DESC LIMIT 20")
    List<EchartPieVO> selectHeartbeatData(@Param("elderId") Long elderId);
}