package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.SystemLog;

/**
 * 系统日志服务接口
 */
public interface SystemLogService extends IService<SystemLog> {

    /**
     * 分页查询系统日志
     */
    IPage<SystemLog> getLogPage(Page<SystemLog> page, String operatorName, String module,
                                String operationType, String status,
                                java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);

    /**
     * 记录日志
     */
    boolean saveLog(SystemLog systemLog);

    /**
     * 记录操作日志（便捷方法）
     */
    void logOperation(String operationType, String module, String description,
                     Long operatorId, String operatorName, String operatorType,
                     String method, String params, String result,
                     long executeTime, String ip, String status);

    /**
     * 统计操作次数（按操作类型）
     */
    Long countByOperationType(String operationType);

    /**
     * 清理过期日志
     */
    boolean cleanExpiredLogs(int days);
}
