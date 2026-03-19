package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.OrderCreateDTO;
import com.xx.jaseatschoicejava.dto.OrderDishVO;
import com.xx.jaseatschoicejava.dto.ReorderResponseDTO;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.OrderDish;
import com.xx.jaseatschoicejava.entity.PaymentRecord;
import com.xx.jaseatschoicejava.enums.NotificationTypeEnum;
import com.xx.jaseatschoicejava.service.OrderDishService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.PaymentService;
import com.xx.jaseatschoicejava.service.WalletService;
import com.xx.jaseatschoicejava.util.NotificationUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
     * 根据商家ID获取订单列表
     * @param merchantId 商家ID
     * @param today 是否只查询今日订单，默认为true
     */
    @GetMapping("/merchant/{merchantId}")
    public ResponseResult<?> getOrdersByMerchantId(
            @PathVariable String merchantId,
            @RequestParam(defaultValue = "true") boolean today) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getMerchantId, merchantId);

        // 根据参数决定是否筛选今日订单
        if (today) {
            // 筛选今日订单（根据创建时间）
            // 获取今天的开始时间（00:00:00）和结束时间（23:59:59）
            java.time.LocalDateTime todayStart = java.time.LocalDateTime.now().toLocalDate().atStartOfDay();
            java.time.LocalDateTime todayEnd = todayStart.plusDays(1).minusNanos(1);

            queryWrapper.ge(Order::getCreateTime, todayStart);
            queryWrapper.le(Order::getCreateTime, todayEnd);
            log.info("商家{}查询今日订单", merchantId);
        } else {
            log.info("商家{}查询全部订单", merchantId);
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Order::getCreateTime);

        List<Order> orders = orderService.list(queryWrapper);
        log.info("商家{}订单数量：{}", merchantId, orders.size());
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
                // 根据状态发送通知（5状态系统：0-待支付、1-待接单、2-制作中、3-已完成、4-已取消）
                switch (status) {
                    case 2: // 制作中
                        NotificationUtil.createOrderNotification(
                            order.getUserId(),
                            NotificationTypeEnum.ORDER_PREPARING_COMPLETE,
                            orderId,
                            "商家已接单，正在制作中"
                        );
                        break;
                    case 3: // 已完成
                        NotificationUtil.createOrderNotification(
                            order.getUserId(),
                            NotificationTypeEnum.ORDER_DELIVERED,
                            orderId,
                            "订单已完成"
                        );
                        break;
                }
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

            // 更新订单状态为已取消(4)
            order.setStatus(4);
            order.setUpdateTime(java.time.LocalDateTime.now());
            boolean success = orderService.updateById(order);
            if (success) {
                log.info("订单取消成功，订单ID：{}", orderId);

                // 通知用户订单已取消
                NotificationUtil.createOrderNotification(
                    order.getUserId(),
                    NotificationTypeEnum.ORDER_CANCELLED,
                    orderId,
                    "已取消"
                );

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

    /**
     * 再来一单 - 智能复购
     * 检查菜品状态、价格变动，并推荐替换菜品
     */
    @ApiOperation("再来一单 - 智能复购")
    @PostMapping("/{orderId}/reorder")
    public ResponseResult<?> reorder(@PathVariable String orderId) {
        try {
            log.info("再来一单请求，订单ID：{}", orderId);

            ReorderResponseDTO reorderResponse = orderService.reorder(orderId);

            if (reorderResponse.getAllItemsUnavailable()) {
                return ResponseResult.fail("400", "抱歉，订单中的所有菜品均已下架或库存不足");
            }

            log.info("再来一单处理成功，订单ID：{}, 菜品变动数：{}",
                orderId, reorderResponse.getSoldOutCount() + reorderResponse.getPriceIncreasedCount());

            return ResponseResult.success(reorderResponse);
        } catch (RuntimeException e) {
            log.error("再来一单失败，订单ID：{}，错误信息：{}", orderId, e.getMessage());
            return ResponseResult.fail("400", e.getMessage());
        } catch (Exception e) {
            log.error("再来一单异常，订单ID：{}", orderId, e);
            return ResponseResult.fail("500", "再来一单失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户订单统计
     */
    @ApiOperation("获取用户订单统计")
    @GetMapping("/user/{userId}/statistics")
    public ResponseResult<?> getUserOrderStatistics(@PathVariable String userId) {
        try {
            // 查询所有订单
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getUserId, userId);
            List<Order> orders = orderService.list(queryWrapper);

            // 初始化统计数据（5状态系统：0-待支付、1-待接单、2-制作中、3-已完成、4-已取消）
            int inProgress = 0;  // 进行中订单（1-待接单、2-制作中）
            int pending = 0;      // 待确认订单（0-待支付、1-待接单）
            int pendingComment = 0;  // 待评价订单（5状态系统中已无此概念，保留为0）

            for (Order order : orders) {
                int status = order.getStatus();

                // 统计进行中订单（待接单、制作中）
                if (status == 1 || status == 2) {
                    inProgress++;
                }

                // 统计待确认订单（待支付、待接单）
                if (status == 0 || status == 1) {
                    pending++;
                }

                // 5状态系统无待评价概念，已完成即评价
            }

            // 构建返回结果
            java.util.Map<String, Object> statistics = new java.util.HashMap<>();
            statistics.put("inProgress", inProgress);
            statistics.put("pending", pending);
            statistics.put("pendingComment", 0);  // 5状态系统无待评价，固定为0

            log.info("用户{}订单统计：进行中={}, 待确认={}", userId, inProgress, pending);

            return ResponseResult.success(statistics);
        } catch (Exception e) {
            log.error("获取用户订单统计失败，用户ID：{}", userId, e);
            return ResponseResult.fail("500", "获取订单统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取订单可回退的状态选项
     */
    @GetMapping("/{orderId}/rollback-options")
    @ApiOperation(value = "获取订单可回退的状态选项")
    public ResponseResult<?> getRollbackOptions(@PathVariable String orderId) {
        try {
            Order order = orderService.getById(orderId);
            if (order == null) {
                return ResponseResult.fail("404", "订单不存在");
            }

            List<Integer> options = orderService.getRollbackOptions(order.getStatus());
            return ResponseResult.success(Map.of(
                    "orderId", orderId,
                    "currentStatus", order.getStatus(),
                    "rollbackOptions", options
            ));
        } catch (Exception e) {
            log.error("获取订单回退选项失败，订单ID：{}", orderId, e);
            return ResponseResult.fail("500", "获取回退选项失败：" + e.getMessage());
        }
    }

    /**
     * 订单状态回退
     */
    @PutMapping("/{orderId}/rollback")
    @ApiOperation(value = "订单状态回退")
    public ResponseResult<?> rollbackStatus(
            @PathVariable String orderId,
            @RequestBody java.util.Map<String, Object> request) {

        try {
            Integer targetStatus = (Integer) request.get("targetStatus");
            String reason = (String) request.get("reason");
            String operatorId = (String) request.get("operatorId");

            if (targetStatus == null) {
                return ResponseResult.fail("400", "缺少目标状态参数");
            }

            if (reason == null || reason.trim().isEmpty()) {
                return ResponseResult.fail("400", "请提供回退原因");
            }

            boolean success = orderService.rollbackStatus(orderId, targetStatus, reason, operatorId);

            if (success) {
                return ResponseResult.success("订单状态回退成功");
            } else {
                return ResponseResult.fail("500", "订单状态回退失败");
            }

        } catch (Exception e) {
            log.error("订单状态回退失败，订单ID：{}", orderId, e);
            return ResponseResult.fail("500", "订单状态回退失败：" + e.getMessage());
        }
    }
}
