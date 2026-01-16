package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.OrderCreateDTO;
import com.xx.jaseatschoicejava.dto.OrderDishVO;
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
     * 创建订单(支持菜品列表)
     */
    @PostMapping
    public ResponseResult<?> createOrder(@RequestBody OrderCreateDTO orderCreateDTO) {
        log.info("开始创建订单，订单信息：{}，菜品数量：{}",
                orderCreateDTO.getOrder(),
                orderCreateDTO.getDishes() != null ? orderCreateDTO.getDishes().size() : 0);

        try {
            // 使用事务方法同时创建订单和菜品
            boolean success = orderService.createOrderWithDishes(
                    orderCreateDTO.getOrder(),
                    orderCreateDTO.getDishes()
            );

            if (success) {
                log.info("订单创建成功，订单ID：{}", orderCreateDTO.getOrder().getId());
                return ResponseResult.success(orderCreateDTO.getOrder().getId());
            } else {
                log.error("订单创建失败");
                return ResponseResult.fail("500", "创建订单失败");
            }
        } catch (Exception e) {
            log.error("创建订单异常", e);
            return ResponseResult.fail("500", "创建订单失败：" + e.getMessage());
        }
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
     * 获取订单的菜品列表（包含菜品详细信息）
     */
    @GetMapping("/{orderId}/dishes")
    public ResponseResult<?> getOrderDishes(@PathVariable String orderId) {
        List<OrderDishVO> orderDishes = orderDishService.getOrderDishesWithDetails(orderId);
        return ResponseResult.success(orderDishes);
    }

    /**
     * 根据商家ID获取今日订单列表
     */
    @GetMapping("/merchant/{merchantId}")
    public ResponseResult<?> getOrdersByMerchantId(@PathVariable String merchantId) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getMerchantId, merchantId);

        // 筛选今日订单（根据创建时间）
        // 获取今天的开始时间（00:00:00）和结束时间（23:59:59）
        java.time.LocalDateTime todayStart = java.time.LocalDateTime.now().toLocalDate().atStartOfDay();
        java.time.LocalDateTime todayEnd = todayStart.plusDays(1).minusNanos(1);

        queryWrapper.ge(Order::getCreateTime, todayStart);
        queryWrapper.le(Order::getCreateTime, todayEnd);

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Order::getCreateTime);

        List<Order> orders = orderService.list(queryWrapper);
        log.info("商家{}今日订单数量：{}", merchantId, orders.size());
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
     * 取消订单（支持退款）
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseResult<?> cancelOrder(
        @PathVariable String orderId,
        @RequestParam(required = false, defaultValue = "用户取消订单") String reason
    ) {
        try {
            // 先查询订单是否存在
            Order order = orderService.getById(orderId);
            if (order == null) {
                return ResponseResult.fail("404", "订单不存在");
            }

            // 检查订单状态：只有待支付(0)或待接单(1)的订单可以取消
            if (order.getStatus() != 0 && order.getStatus() != 1) {
                return ResponseResult.fail("400", "只有待支付或待接单的订单可以取消");
            }

            // 如果订单已支付，需要退款
            if (order.getStatus() != 0 && order.getPaidAmount() != null
                && order.getPaidAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {

                try {
                    // 调用退款服务
                    boolean refundSuccess = paymentService.refundPayment(
                        orderId,
                        order.getPaidAmount(),
                        reason
                    );

                    if (!refundSuccess) {
                        return ResponseResult.fail("500", "退款失败，无法取消订单");
                    }

                    log.info("订单退款成功，订单ID：{}，退款金额：{}", orderId, order.getPaidAmount());
                } catch (Exception e) {
                    log.error("订单退款失败，订单ID：{}", orderId, e);
                    return ResponseResult.fail("500", "退款失败：" + e.getMessage());
                }
            }

            // 更新订单状态为已取消(6)
            order.setStatus(6);
            order.setUpdateTime(java.time.LocalDateTime.now());
            boolean success = orderService.updateById(order);
            if (success) {
                log.info("订单取消成功，订单ID：{}", orderId);
                return ResponseResult.success("订单已取消");
            }
            return ResponseResult.fail("500", "取消订单失败");
        } catch (Exception e) {
            log.error("取消订单失败，订单ID：{}", orderId, e);
            return ResponseResult.fail("500", "取消订单失败：" + e.getMessage());
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
