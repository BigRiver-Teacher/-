package com.example.elderly.mapper;

import com.example.elderly.dto.echart.EchartPieDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PersonEchartMapper {

    @Select("SELECT '护工人员' name, COUNT(1) value FROM caregiver " +
            "UNION ALL SELECT '康养专家' name, COUNT(1) value FROM expert " +
            "UNION ALL SELECT '平台工作人员' name, COUNT(1) value FROM user WHERE role IN ('admin','staff')")
    List<EchartPieDTO> selectPersonRatio();

    @Select("SELECT '高级护工' name, COUNT(1) value FROM caregiver WHERE position = 'SENIOR_CAREGIVER' " +
            "UNION ALL SELECT '陪护员' name, COUNT(1) value FROM caregiver WHERE position = 'CAREGIVER' " +
            "UNION ALL SELECT '护理主管' name, COUNT(1) value FROM caregiver WHERE position = 'NURSING_SUPERVISOR' " +
            "UNION ALL SELECT '康复护理师' name, COUNT(1) value FROM caregiver WHERE position = 'REHAB_NURSE' " +
            "UNION ALL SELECT '心理辅导师' name, COUNT(1) value FROM caregiver WHERE position = 'PSYCHO_COUNSELOR' " +
            "UNION ALL SELECT '康养专家' name, COUNT(1) value FROM expert")
    List<EchartPieDTO> selectPersonTrendRaw();

    @Select("SELECT '高级护工' name, COUNT(1) value FROM caregiver WHERE position = 'SENIOR_CAREGIVER' " +
            "UNION ALL SELECT '陪护员' name, COUNT(1) value FROM caregiver WHERE position = 'CAREGIVER' " +
            "UNION ALL SELECT '护理主管' name, COUNT(1) value FROM caregiver WHERE position = 'NURSING_SUPERVISOR' " +
            "UNION ALL SELECT '康复护理师' name, COUNT(1) value FROM caregiver WHERE position = 'REHAB_NURSE' " +
            "UNION ALL SELECT '心理辅导师' name, COUNT(1) value FROM caregiver WHERE position = 'PSYCHO_COUNSELOR'")
    List<EchartPieDTO> selectPersonRadarRaw();

    @Select("SELECT CASE WHEN age<60 THEN '60岁以下' " +
            "WHEN age BETWEEN 60 AND 64 THEN '60-64岁' " +
            "WHEN age BETWEEN 65 AND 69 THEN '65-69岁' " +
            "WHEN age BETWEEN 70 AND 74 THEN '70-74岁' " +
            "WHEN age BETWEEN 75 AND 79 THEN '75-79岁' " +
            "WHEN age BETWEEN 80 AND 84 THEN '80-84岁' " +
            "ELSE '85岁以上' END name, COUNT(1) value FROM elder " +
            "GROUP BY CASE WHEN age<60 THEN '60岁以下' " +
            "WHEN age BETWEEN 60 AND 64 THEN '60-64岁' " +
            "WHEN age BETWEEN 65 AND 69 THEN '65-69岁' " +
            "WHEN age BETWEEN 70 AND 74 THEN '70-74岁' " +
            "WHEN age BETWEEN 75 AND 79 THEN '75-79岁' " +
            "WHEN age BETWEEN 80 AND 84 THEN '80-84岁' " +
            "ELSE '85岁以上' END ORDER BY name")
    List<EchartPieDTO> selectElderAgePie();

    @Select("SELECT CASE WHEN gender IN ('MALE','男') THEN '男' ELSE '女' END name, COUNT(1) value FROM elder GROUP BY CASE WHEN gender IN ('MALE','男') THEN '男' ELSE '女' END")
    List<EchartPieDTO> selectElderGenderPie();
}