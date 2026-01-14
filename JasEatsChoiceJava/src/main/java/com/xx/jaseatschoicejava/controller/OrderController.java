package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.OrderDish;
import com.xx.jaseatschoicejava.entity.PaymentRecord;
import com.xx.jaseatschoicejava.service.OrderDishService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.PaymentService;
import com.xx.jaseatschoicejava.service.WalletService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单控制器
 */
@Slf4j
@Api(tags = "订单管理")
@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final WalletService walletService;
    private final OrderDishService orderDishService;

    /**
     * 创建订单
     */
    @PostMapping
    public ResponseResult<?> createOrder(@RequestBody Order order) {
        log.info("开始创建订单，订单信息：{}", order);
        boolean success = orderService.save(order);
        if (success) {
            log.info("订单创建成功，订单ID：{}", order.getId());
            return ResponseResult.success(order.getId()); // 返回订单ID
        }
        log.error("订单创建失败");
        return ResponseResult.fail("500", "创建订单失败");
    }

    /**
     * 根据用户ID获取订单列表
     */
    @GetMapping("/user/{userId}")
    public ResponseResult<?> getOrdersByUserId(@PathVariable String userId) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getUserId, userId);
        List<Order> orders = orderService.list(queryWrapper);
        return ResponseResult.success(orders);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public ResponseResult<?> getOrderDetail(@PathVariable String orderId) {
        Order order = orderService.getById(orderId);
        if (order != null) {
            return ResponseResult.success(order);
        }
        return ResponseResult.fail("404", "订单不存在");
    }

    /**
     * 获取订单的菜品列表
     */
    @GetMapping("/{orderId}/dishes")
    public ResponseResult<?> getOrderDishes(@PathVariable String orderId) {
        LambdaQueryWrapper<OrderDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDish::getOrderId, orderId);
        List<OrderDish> orderDishes = orderDishService.list(queryWrapper);
        return ResponseResult.success(orderDishes);
    }

    /**
     * 根据商家ID获取订单列表
     */
    @GetMapping("/merchant/{merchantId}")
    public ResponseResult<?> getOrdersByMerchantId(@PathVariable String merchantId) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getMerchantId, merchantId);
        List<Order> orders = orderService.list(queryWrapper);
        return ResponseResult.success(orders);
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{orderId}/status")
    public ResponseResult<?> updateOrderStatus(@PathVariable String orderId, @RequestParam Integer status) {
        try {
            // 先查询订单是否存在
            Order order = orderService.getById(orderId);
            if (order == null) {
                return ResponseResult.fail("404", "订单不存在");
            }

            // 只更新状态字段
            order.setStatus(status);
            boolean success = orderService.updateById(order);
            if (success) {
                return ResponseResult.success("更新成功");
            }
            return ResponseResult.fail("500", "更新失败");
        } catch (Exception e) {
            log.error("更新订单状态失败，订单ID：{}，状态：{}", orderId, status, e);
            return ResponseResult.fail("500", "更新失败：" + e.getMessage());
        }
    }

    /**
     * 订单支付
     */
    @ApiOperation("订单支付")
    @PostMapping("/{orderId}/pay")
    public ResponseResult<?> payOrder(
        @PathVariable String orderId,
        @ApiParam("用户ID") @RequestParam String userId,
        @ApiParam("支付方式") @RequestParam(defaultValue = "wallet") String paymentMethod
    ) {
        try {
            // 获取订单信息
            Order order = orderService.getById(orderId);
            if (order == null) {
                return ResponseResult.fail("404", "订单不存在");
            }

            // 检查订单状态
            if (order.getStatus() != 0) {
                return ResponseResult.fail("400", "订单状态异常，无法支付");
            }

            // 检查余额
            if ("wallet".equals(paymentMethod)) {
                boolean enough = walletService.checkBalance(order.getUserId(), order.getTotalAmount());
                if (!enough) {
                    return ResponseResult.fail("400", "余额不足");
                }
            }

            // 创建支付记录
            PaymentRecord paymentRecord = paymentService.createPayment(
                orderId,
                order.getUserId(),
                order.getMerchantId(),
                order.getTotalAmount(),
                paymentMethod
            );

            // 处理支付
            boolean success = paymentService.processPayment(paymentRecord.getPaymentNo());
            if (success) {
                return ResponseResult.success("支付成功");
            } else {
                return ResponseResult.fail("500", "支付失败");
            }

        } catch (Exception e) {
            log.error("订单支付失败，订单ID：{}", orderId, e);
            return ResponseResult.fail("500", "支付失败：" + e.getMessage());
        }
    }

    /**
     * 获取订单支付记录
     */
    @ApiOperation("获取订单支付记录")
    @GetMapping("/{orderId}/payment")
    public ResponseResult<?> getOrderPayment(@PathVariable String orderId) {
        try {
            PaymentRecord paymentRecord = paymentService.getPaymentByOrderId(orderId);
            if (paymentRecord != null) {
                return ResponseResult.success(paymentRecord);
            }
            return ResponseResult.fail("404", "支付记录不存在");
        } catch (Exception e) {
            log.error("获取支付记录失败，订单ID：{}", orderId, e);
            return ResponseResult.fail("500", "获取支付记录失败：" + e.getMessage());
        }
    }
}
