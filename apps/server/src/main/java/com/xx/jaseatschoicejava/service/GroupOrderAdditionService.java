package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.GroupOrderAddition;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 群订单加菜Service接口
 */
public interface GroupOrderAdditionService extends IService<GroupOrderAddition> {

    /**
     * 发起加菜请求
     * @param groupId 群组ID
     * @param userId 加菜用户ID
     * @param dishes 菜品列表
     * @return 加菜记录
     */
    GroupOrderAddition requestAddDish(String groupId, String userId, List<Map<String, Object>> dishes);

    /**
     * 审核加菜请求
     * @param additionId 加菜记录ID
     * @param approved 是否通过
     * @param rejectReason 驳回原因
     * @return 是否成功
     */
    boolean reviewAddition(String additionId, boolean approved, String rejectReason);

    /**
     * 检查并处理超时的加菜请求（15分钟自动驳回）
     * @return 处理的记录数
     */
    int checkTimeoutAdditions();

    /**
     * 统一支付加菜池
     * @param groupOrderId 群订单ID
     * @return 是否成功
     */
    boolean payAdditionPool(String groupOrderId);

    /**
     * 获取待审核的加菜列表
     * @param groupOrderId 群订单ID
     * @return 加菜列表
     */
    List<GroupOrderAddition> getPendingAdditions(String groupOrderId);

    /**
     * 获取待支付的加菜列表
     * @param groupOrderId 群订单ID
     * @return 加菜列表
     */
    List<GroupOrderAddition> getApprovedPendingPayments(String groupOrderId);

    /**
     * 计算加菜总金额
     * @param dishes 菜品列表
     * @return 总金额
     */
    BigDecimal calculateTotalAmount(List<Map<String, Object>> dishes);
}
