package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.OrderDish;
import com.xx.jaseatschoicejava.mapper.OrderMapper;
import com.xx.jaseatschoicejava.service.OrderDishService;
import com.xx.jaseatschoicejava.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderDishService orderDishService;

    /**
     * 创建订单并保存订单菜品(事务方法)
     * @param order 订单信息
     * @param orderDishes 订单菜品列表
     * @return 是否创建成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createOrderWithDishes(Order order, List<OrderDish> orderDishes) {
        try {
            log.info("开始创建订单,订单ID: {}, 菜品数量: {}", order.getId(),
                    orderDishes != null ? orderDishes.size() : 0);

            // 1. 保存订单
            boolean orderSaved = this.save(order);
            if (!orderSaved) {
                log.error("保存订单失败,订单ID: {}", order.getId());
                return false;
            }
            log.info("订单保存成功,订单ID: {}", order.getId());

            // 2. 保存订单菜品(如果有)
            if (orderDishes != null && !orderDishes.isEmpty()) {
                // 为每个菜品设置订单ID
                for (OrderDish orderDish : orderDishes) {
                    orderDish.setOrderId(order.getId());
                }

                // 批量保存订单菜品
                boolean dishesSaved = orderDishService.saveBatch(orderDishes);
                if (!dishesSaved) {
                    log.error("保存订单菜品失败,订单ID: {}", order.getId());
                    throw new RuntimeException("保存订单菜品失败");
                }
                log.info("订单菜品保存成功,订单ID: {}, 菜品数量: {}", order.getId(), orderDishes.size());
            }

            log.info("订单和菜品创建成功,订单ID: {}", order.getId());
            return true;
        } catch (Exception e) {
            log.error("创建订单和菜品失败,订单ID: {}", order.getId(), e);
            throw new RuntimeException("创建订单和菜品失败: " + e.getMessage(), e);
        }
    }
}
