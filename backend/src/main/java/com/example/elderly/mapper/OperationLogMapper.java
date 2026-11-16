package com.example.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.elderly.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
    int deleteBeforeDate(@Param("beforeDate") LocalDateTime beforeDate);

    List<OperationLog> selectExportList(@Param("type") String type,
                                        @Param("module") String module,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);
}
