package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping
    public ResponseResult<?> createOrder(@RequestBody Order order) {
        boolean success = orderService.save(order);
        if (success) {
            return ResponseResult.success(order.getId()); // 返回订单ID
        }
        return ResponseResult.fail("500", "创建订单失败");
    }

    /**
     * 根据用户ID获取订单列表
     */
    @GetMapping("/user/{userId}")
    public ResponseResult<?> getOrdersByUserId(@PathVariable Long userId) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getUserId, userId);
        List<Order> orders = orderService.list(queryWrapper);
        return ResponseResult.success(orders);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public ResponseResult<?> getOrderDetail(@PathVariable Long orderId) {
        Order order = orderService.getById(orderId);
        if (order != null) {
            return ResponseResult.success(order);
        }
        return ResponseResult.fail("404", "订单不存在");
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{orderId}/status")
    public ResponseResult<?> updateOrderStatus(@PathVariable Long orderId, @RequestParam Integer status) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(status);
        boolean success = orderService.updateById(order);
        if (success) {
            return ResponseResult.success("更新成功");
        }
        return ResponseResult.fail("500", "更新失败");
    }
}
