package com.example.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.elderly.entity.OperationLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    /** 清理指定时间之前的日志（阶段4 回归发现：原缺失映射导致 500，此处补齐）。 */
    @Delete("DELETE FROM operation_log WHERE operate_time < #{beforeDate}")
    int deleteBeforeDate(@Param("beforeDate") LocalDateTime beforeDate);

    /** 按条件导出日志列表（不分页，供导出使用）。 */
    @Select("<script>"
            + "SELECT id, operate_time, operator, operate_type, module, content, ip, status, error_msg "
            + "FROM operation_log"
            + "<where>"
            + "<if test='type != null and type != \"\"'> AND operate_type = #{type}</if>"
            + "<if test='module != null and module != \"\"'> AND module = #{module}</if>"
            + "<if test='start != null'> AND operate_time &gt;= #{start}</if>"
            + "<if test='end != null'> AND operate_time &lt;= #{end}</if>"
            + "</where>"
            + " ORDER BY operate_time DESC"
            + "</script>")
    List<OperationLog> selectExportList(@Param("type") String type,
                                        @Param("module") String module,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);
}
