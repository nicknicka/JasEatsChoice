package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.SystemLog;
import com.xx.jaseatschoicejava.mapper.SystemLogMapper;
import com.xx.jaseatschoicejava.service.SystemLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 系统日志服务实现
 */
@Service
public class SystemLogServiceImpl extends ServiceImpl<SystemLogMapper, SystemLog>
        implements SystemLogService {

    @Override
    public IPage<SystemLog> getLogPage(Page<SystemLog> page, String operatorName, String module,
                                       String operationType, String status,
                                       LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.selectLogPage(page, operatorName, module, operationType,
                                       status, startTime, endTime);
    }

    @Override
    public boolean saveLog(SystemLog systemLog) {
        if (systemLog.getCreateTime() == null) {
            systemLog.setCreateTime(LocalDateTime.now());
        }
        return save(systemLog);
    }

    @Override
    public void logOperation(String operationType, String module, String description,
                            Long operatorId, String operatorName, String operatorType,
                            String method, String params, String result,
                            long executeTime, String ip, String status) {
        SystemLog log = new SystemLog();
        log.setOperationType(operationType);
        log.setModule(module);
        log.setDescription(description);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setOperatorType(operatorType);
        log.setMethod(method);
        log.setParams(params);
        log.setResult(result);
        log.setExecuteTime(executeTime);
        log.setIp(ip);
        log.setStatus(status != null ? status : "SUCCESS");
        saveLog(log);
    }

    @Override
    public Long countByOperationType(String operationType) {
        return baseMapper.countByOperationType(operationType);
    }

    @Override
    public boolean cleanExpiredLogs(int days) {
        LocalDateTime expireDate = LocalDateTime.now().minusDays(days);
        QueryWrapper<SystemLog> wrapper = new QueryWrapper<>();
        wrapper.lt("create_time", expireDate);
        return remove(wrapper);
    }
}
