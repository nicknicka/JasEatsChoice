package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.dto.ReorderResponseDTO;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.OrderDish;

import java.util.List;

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
}
