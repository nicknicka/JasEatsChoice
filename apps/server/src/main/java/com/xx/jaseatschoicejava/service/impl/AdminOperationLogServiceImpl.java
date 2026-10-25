package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.AdminOperationLog;
import com.xx.jaseatschoicejava.mapper.AdminOperationLogMapper;
import com.xx.jaseatschoicejava.service.AdminOperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 管理员操作日志服务实现
 */
@Service
public class AdminOperationLogServiceImpl extends ServiceImpl<AdminOperationLogMapper, AdminOperationLog> implements AdminOperationLogService {

    private static final Logger logger = LoggerFactory.getLogger(AdminOperationLogServiceImpl.class);

    @Override
    public void logLogin(Long adminId, String adminUsername, boolean success, String details) {
        logOperation(adminId, adminUsername, "认证", "登录", null, null,
                success ? "登录成功" : ("登录失败: " + details),
                success ? "SUCCESS" : "FAILED");
    }

    @Override
    public void logUserOperation(Long adminId, String adminUsername, String operation, Long targetId, String details) {
        logOperation(adminId, adminUsername, "用户管理", operation, "用户", targetId, details, "SUCCESS");
    }

    @Override
    public void logMerchantOperation(Long adminId, String adminUsername, String operation, Long targetId, String details) {
        logOperation(adminId, adminUsername, "商家管理", operation, "商家", targetId, details, "SUCCESS");
    }

    @Override
    public void logOrderOperation(Long adminId, String adminUsername, String operation, Long targetId, String details) {
        logOperation(adminId, adminUsername, "订单管理", operation, "订单", targetId, details, "SUCCESS");
    }

    @Override
    public void logDishOperation(Long adminId, String adminUsername, String operation, Long targetId, String details) {
        logOperation(adminId, adminUsername, "菜品管理", operation, "菜品", targetId, details, "SUCCESS");
    }

    @Override
    public void logFinanceOperation(Long adminId, String adminUsername, String operation, Long targetId, String details) {
        logOperation(adminId, adminUsername, "财务管理", operation, "财务", targetId, details, "SUCCESS");
    }

    @Override
    public void logSystemOperation(Long adminId, String adminUsername, String operation, String details) {
        logOperation(adminId, adminUsername, "系统设置", operation, null, null, details, "SUCCESS");
    }

    @Override
    public void logOperation(Long adminId, String adminUsername, String module, String operation,
                             String targetType, Long targetId, String details, String status) {
        try {
            AdminOperationLog log = new AdminOperationLog();
            log.setAdminId(adminId);
            log.setUsername(adminUsername);
            log.setModuleName(module);
            log.setOperationType(operation);
            log.setOperationDesc(details != null ? details : operation);
            log.setRequestMethod("SYSTEM");
            log.setRequestUrl("/admin/" + module.toLowerCase());
            log.setRequestParams(targetId != null ? "targetId=" + targetId : "");
            log.setResponseResult(status);
            log.setIpAddress(getClientIp());
            log.setStatus(status);
            log.setCreateTime(LocalDateTime.now());
            log.setExecuteTime(0);

            save(log);

            // 同时使用SLF4J记录日志，便于控制台查看
            logger.info("[管理员操作] 管理员: {} (ID: {}), 模块: {}, 操作: {}, 目标: {} (ID: {}), 详情: {}, 状态: {}",
                    adminUsername, adminId, module, operation, targetType, targetId, details, status);
        } catch (Exception e) {
            logger.error("[管理员操作] 记录操作日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取客户端IP地址
     * TODO: 实际应用中应该从请求上下文中获取真实IP
     */
    private String getClientIp() {
        try {
            // 这里可以从RequestContextHolder中获取Request
            // 暂时返回一个默认值
            return "SYSTEM";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}
