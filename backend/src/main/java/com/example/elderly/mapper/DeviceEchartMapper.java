package com.example.elderly.mapper;

import com.example.elderly.dto.echart.EchartPieDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DeviceEchartMapper {

    @Select("SELECT status name, COUNT(1) value FROM device GROUP BY status")
    List<EchartPieDTO> selectDeviceStatusTotal();

    @Select("SELECT device_type name, COUNT(1) value FROM device GROUP BY device_type")
    List<EchartPieDTO> selectDeviceTypeDistribution();

    @Select("SELECT DATE_FORMAT(last_online_time,'%m-%d') name, COUNT(1) value FROM device " +
            "WHERE last_online_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) GROUP BY DATE_FORMAT(last_online_time,'%m-%d') ORDER BY MAX(last_online_time)")
    List<EchartPieDTO> selectOnlineTrendRaw();

    @Select("SELECT DATE_FORMAT(alert_time,'%m-%d') name, COUNT(1) value FROM alert " +
            "WHERE alert_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) GROUP BY DATE_FORMAT(alert_time,'%m-%d') ORDER BY MAX(alert_time)")
    List<EchartPieDTO> selectAlertTrendRaw();

    @Select("SELECT alert_type name, COUNT(1) value FROM alert GROUP BY alert_type")
    List<EchartPieDTO> selectFaultRaw();

    @Select("SELECT COUNT(1) FROM device WHERE bind_elder_id IS NOT NULL")
    Integer selectBoundDeviceCount();

    @Select("SELECT COUNT(1) FROM alert WHERE alert_time >= CURDATE() AND is_deleted = 0")
    Integer selectTodayAlertCount();
}