package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.dto.ReorderResponseDTO;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.OrderDish;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单并保存订单菜品(事务方法)
     * @param order 订单信息
     * @param orderDishes 订单菜品列表
     * @return 是否创建成功
     */
    boolean createOrderWithDishes(Order order, List<OrderDish> orderDishes);

    /**
     * 再来一单 - 智能复购
     * @param orderId 原订单ID
     * @return 再来一单响应数据
     */
    ReorderResponseDTO reorder(String orderId);

    /**
     * 订单状态回退
     * @param orderId 订单ID
     * @param targetStatus 目标状态
     * @param reason 回退原因
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean rollbackStatus(String orderId, Integer targetStatus, String reason, String operatorId);

    /**
     * 获取订单可回退到的状态列表
     * @param currentStatus 当前状态
     * @return 可回退的状态列表
     */
    List<Integer> getRollbackOptions(Integer currentStatus);

    /**
     * 获取用户各状态订单数量统计
     * @param userId 用户ID
     * @return Map<状态名称, 数量>
     */
    Map<String, Long> getOrderCountByUserId(String userId);
}
