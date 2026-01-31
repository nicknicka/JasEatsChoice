package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.AdminOperationLog;

/**
 * 管理员操作日志服务接口
 */
public interface AdminOperationLogService extends IService<AdminOperationLog> {

    /**
     * 记录登录操作
     * @param adminId 管理员ID
     * @param adminUsername 管理员用户名
     * @param success 是否成功
     * @param details 详情信息
     */
    void logLogin(Long adminId, String adminUsername, boolean success, String details);

    /**
     * 记录用户管理操作
     * @param adminId 管理员ID
     * @param adminUsername 管理员用户名
     * @param operation 操作类型（查看、编辑、删除等）
     * @param targetId 目标ID
     * @param details 详情信息
     */
    void logUserOperation(Long adminId, String adminUsername, String operation, Long targetId, String details);

    /**
     * 记录商家管理操作
     * @param adminId 管理员ID
     * @param adminUsername 管理员用户名
     * @param operation 操作类型
     * @param targetId 目标ID
     * @param details 详情信息
     */
    void logMerchantOperation(Long adminId, String adminUsername, String operation, Long targetId, String details);

    /**
     * 记录订单管理操作
     * @param adminId 管理员ID
     * @param adminUsername 管理员用户名
     * @param operation 操作类型
     * @param targetId 目标ID
     * @param details 详情信息
     */
    void logOrderOperation(Long adminId, String adminUsername, String operation, Long targetId, String details);

    /**
     * 记录菜品管理操作
     * @param adminId 管理员ID
     * @param adminUsername 管理员用户名
     * @param operation 操作类型
     * @param targetId 目标ID
     * @param details 详情信息
     */
    void logDishOperation(Long adminId, String adminUsername, String operation, Long targetId, String details);

    /**
     * 记录财务管理操作
     * @param adminId 管理员ID
     * @param adminUsername 管理员用户名
     * @param operation 操作类型
     * @param targetId 目标ID
     * @param details 详情信息
     */
    void logFinanceOperation(Long adminId, String adminUsername, String operation, Long targetId, String details);

    /**
     * 记录系统设置操作
     * @param adminId 管理员ID
     * @param adminUsername 管理员用户名
     * @param operation 操作类型
     * @param details 详情信息
     */
    void logSystemOperation(Long adminId, String adminUsername, String operation, String details);

    /**
     * 通用日志记录方法
     * @param adminId 管理员ID
     * @param adminUsername 管理员用户名
     * @param module 模块名称
     * @param operation 操作类型
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param details 详情信息
     * @param status 状态
     */
    void logOperation(Long adminId, String adminUsername, String module, String operation,
                      String targetType, Long targetId, String details, String status);
}
