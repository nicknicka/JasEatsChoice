package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.GroupOrder;
import com.xx.jaseatschoicejava.entity.GroupOrderDish;

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
}
