package com.example.elderly.mapper;

import com.example.elderly.vo.echart.EchartPieVO;
import com.example.elderly.vo.echart.EchartStatVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExpertEchartMapper {
    @Select("SELECT " +
            "(SELECT COUNT(1) FROM expert) totalExperts, " +
            "(SELECT COUNT(1) FROM consult) totalConsultations, " +
            "ROUND(IFNULL((SELECT AVG(rating) FROM consult WHERE rating IS NOT NULL),0),2) avgSatisfaction, " +
            "(SELECT COUNT(1) FROM consult WHERE status = 'PENDING') pendingConsultations")
    EchartStatVO selectExpertStatistics();


    @Select("SELECT title name, COUNT(1) value FROM expert GROUP BY title")
    List<EchartPieVO> selectExpertTitleDistribution();


    @Select("SELECT specialty name, COUNT(1) value FROM expert GROUP BY specialty")
    List<EchartPieVO> selectExpertFieldDistribution();


    @Select("SELECT DATE_FORMAT(consult_time,'%m-%d') name, COUNT(1) value FROM consult " +
            "WHERE consult_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) GROUP BY DATE_FORMAT(consult_time,'%m-%d') ORDER BY MAX(consult_time)")
    List<EchartPieVO> selectConsultTrend();


    @Select("SELECT CONCAT(rating,'分') name, COUNT(1) value FROM consult WHERE rating IS NOT NULL GROUP BY rating")
    List<EchartPieVO> selectSatisfactionDistribution();
}
