package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.SystemLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 系统日志Mapper接口
 */
@Mapper
public interface SystemLogMapper extends BaseMapper<SystemLog> {

    /**
     * 分页查询系统日志列表
     */
    @Select("<script>" +
            "SELECT * FROM t_system_log " +
            "WHERE 1=1 " +
            "<if test='operatorName != null and operatorName != \"\"'>" +
            "AND operator_name LIKE CONCAT('%', #{operatorName}, '%') " +
            "</if>" +
            "<if test='module != null and module != \"\"'>" +
            "AND module = #{module} " +
            "</if>" +
            "<if test='operationType != null and operationType != \"\"'>" +
            "AND operation_type = #{operationType} " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND status = #{status} " +
            "</if>" +
            "<if test='startTime != null'>" +
            "AND create_time &gt;= #{startTime} " +
            "</if>" +
            "<if test='endTime != null'>" +
            "AND create_time &lt;= #{endTime} " +
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    IPage<SystemLog> selectLogPage(Page<SystemLog> page,
                                   @Param("operatorName") String operatorName,
                                   @Param("module") String module,
                                   @Param("operationType") String operationType,
                                   @Param("status") String status,
                                   @Param("startTime") java.time.LocalDateTime startTime,
                                   @Param("endTime") java.time.LocalDateTime endTime);

    /**
     * 统计操作次数（按操作类型）
     */
    @Select("SELECT COUNT(*) FROM t_system_log WHERE operation_type = #{operationType}")
    Long countByOperationType(@Param("operationType") String operationType);
}
