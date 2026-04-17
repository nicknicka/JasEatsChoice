package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.GroupOrder;
import com.xx.jaseatschoicejava.entity.GroupOrderMember;
import com.xx.jaseatschoicejava.entity.GroupOrderDish;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 群订单服务
 */
public interface GroupOrderService extends IService<GroupOrder> {

    /**
     * 创建群订单
     * @param groupOrder 群订单信息
     * @param dishItems 菜品列表
     * @return 创建是否成功
     */
    boolean createGroupOrder(GroupOrder groupOrder, List<GroupOrderDish> dishItems);

    /**
     * 根据群ID获取群订单列表
     * @param groupId 群ID
     * @return 群订单列表
     */
    List<GroupOrder> getGroupOrdersByGroupId(String groupId, Integer status, Integer page, Integer size);

    /**
     * 根据群订单ID获取群订单详情
     * @param groupOrderId 群订单ID
     * @return 群订单详情
     */
    GroupOrder getGroupOrderDetail(String groupOrderId);

    /**
     * 根据群订单ID获取菜品列表
     * @param groupOrderId 群订单ID
     * @return 菜品列表
     */
    List<GroupOrderDish> getGroupOrderDishes(String groupOrderId);

    /**
     * 获取用户参与或发起的拼单列表
     * @param userId 用户ID
     * @param status 状态筛选
     * @param page 页码
     * @param size 每页数量
     * @return 拼单列表
     */
    List<GroupOrder> getUserGroupOrders(String userId, Integer status, Integer page, Integer size);

    /**
     * 按订单码加入拼单
     * @param orderCode 订单码
     * @param userId 用户ID
     * @return 加入结果
     */
    Map<String, Object> joinByOrderCode(String orderCode, String userId);

    /**
     * 替换用户选菜记录
     * @param groupOrderId 拼单ID
     * @param userId 用户ID
     * @param dishes 菜品列表
     * @return 保存后的记录
     */
    List<Map<String, Object>> saveUserSelections(String groupOrderId, String userId, List<Map<String, Object>> dishes);

    /**
     * 获取用户选菜记录
     * @param groupOrderId 拼单ID
     * @param userId 用户ID
     * @return 选菜记录
     */
    List<Map<String, Object>> getUserSelections(String groupOrderId, String userId);

    /**
     * 获取结算信息
     * @param groupOrderId 拼单ID
     * @param userId 用户ID
     * @return 结算信息
     */
    Map<String, Object> getSettlement(String groupOrderId, String userId);

    /**
     * 退出拼单
     * @param groupOrderId 拼单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean leaveGroupOrder(String groupOrderId, String userId);

    /**
     * 获取未离开的拼单成员
     * @param groupOrderId 拼单ID
     * @return 成员列表
     */
    List<GroupOrderMember> getActiveGroupOrderMembers(String groupOrderId);

    /**
     * 检查用户是否仍为拼单成员
     * @param groupOrderId 拼单ID
     * @param userId 用户ID
     * @return 是否是有效成员
     */
    boolean isActiveGroupOrderMember(String groupOrderId, String userId);

    /**
     * 确保拼单成员关系存在
     * @param groupOrderId 拼单ID
     * @param userId 用户ID
     * @param role 角色
     * @param inviteBy 邀请人ID
     * @return 成员关系
     */
    GroupOrderMember ensureGroupOrderMember(String groupOrderId, String userId, String role, String inviteBy);

    /**
     * 同步成员支付状态
     * @param groupOrderId 拼单ID
     * @param userId 用户ID
     * @param paidAmount 支付金额
     * @param paid 是否已支付
     */
    void syncGroupOrderMemberPaymentStatus(String groupOrderId, String userId, BigDecimal paidAmount, boolean paid);

    /**
     * 将拼单全部成员置为已支付
     * @param groupOrderId 拼单ID
     */
    void syncAllGroupOrderMembersPaid(String groupOrderId);

    /**
     * 获取拼单运行态信息，统一产出锁单、人数、成员与当前用户能力字段
     * @param groupOrder 群订单
     * @param currentUserId 当前用户ID
     * @return 运行态信息
     */
    Map<String, Object> getGroupOrderRuntimeState(GroupOrder groupOrder, String currentUserId);

    /**
     * 确认成团并写入真实锁单字段
     * @param groupOrderId 群订单ID
     * @return 更新后的群订单
     */
    GroupOrder confirmGroupOrder(String groupOrderId);

    /**
     * 刷新支付后的群订单状态，并在可用时同步成员支付真值
     * @param groupOrderId 群订单ID
     * @param userId 支付用户ID
     * @param paidAmount 支付金额
     * @param paymentType 支付类型 single/all
     * @return 更新后的群订单
     */
    GroupOrder refreshGroupOrderPaymentState(String groupOrderId, String userId, BigDecimal paidAmount, String paymentType);
}
