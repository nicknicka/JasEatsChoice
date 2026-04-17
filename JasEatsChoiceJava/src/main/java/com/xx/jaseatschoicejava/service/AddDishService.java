package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.dto.*;
import com.xx.jaseatschoicejava.entity.AddDishRequest;

import java.util.List;

/**
 * 加菜服务接口
 */
public interface AddDishService extends IService<AddDishRequest> {

    /**
     * 创建加菜请求
     * @param dto 创建加菜请求DTO
     * @param requestUserId 请求用户ID
     * @return 加菜请求ID
     */
    String createAddDishRequest(CreateAddDishDTO dto, String requestUserId);

    /**
     * 批量审核加菜请求
     * @param dto 批量审核DTO
     * @return 审核结果
     */
    ReviewResultDTO batchReview(BatchReviewDTO dto);

    /**
     * 撤回加菜请求
     * @param requestId 请求ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean withdrawRequest(String requestId, String userId);

    /**
     * 获取待审核列表
     * @param groupOrderId 群订单ID
     * @return 待审核列表
     */
    List<AddDishRequestVO> getReviewList(String groupOrderId);

    /**
     * 获取加菜历史
     * @param groupOrderId 群订单ID
     * @return 加菜历史列表
     */
    List<AddDishRequestVO> getHistory(String groupOrderId);

    /**
     * 检查饮食禁忌冲突
     * @param dto 检查DTO
     * @return 冲突检测结果
     */
    AllergyCheckResultDTO checkAllergyConflict(CreateAddDishDTO dto);

    /**
     * 处理超时请求（定时任务调用）
     */
    void handleTimeoutRequests();

    /**
     * 发送提醒通知（定时任务调用）
     */
    void sendReminders();

    /**
     * 检查加菜权限
     * @param groupOrderId 群订单ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean checkAddDishPermission(String groupOrderId, String userId);

    /**
     * 获取加菜设置
     * @param groupOrderId 群订单ID
     * @return 加菜设置
     */
    AddDishSettingDTO getSetting(String groupOrderId);

    /**
     * 更新加菜设置
     * @param dto 设置DTO
     * @return 是否成功
     */
    boolean updateSetting(AddDishSettingDTO dto);
}
