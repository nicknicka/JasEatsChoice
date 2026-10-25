package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.dto.OrderDishVO;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.enums.NotificationTypeEnum;
import com.xx.jaseatschoicejava.service.MerchantService;
import com.xx.jaseatschoicejava.service.OrderDishService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.util.AdminContext;
import com.xx.jaseatschoicejava.util.NotificationUtil;
import com.xx.jaseatschoicejava.util.SystemLogHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-订单管理控制器（更新版）
 */
@Api(tags = "管理员-订单管理")
@RestController
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private OrderDishService orderDishService;

    /**
     * 获取订单列表（分页）
     */
    @ApiOperation("获取订单列表")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('admin:order:list')")
    public ResponseEntity<IPage<Order>> getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        Page<Order> pageParam = new Page<>(page, pageSize);

        // 构建查询条件
        var queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>();

        if (status != null) {
            queryWrapper.eq("status", status);
        }

        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("id", keyword)
                .or()
                .like("user_id", keyword)
            );
        }

        queryWrapper.orderByDesc("create_time");

        IPage<Order> result = orderService.page(pageParam, queryWrapper);

        // 为每个订单添加商家名称和状态文本
        result.getRecords().forEach(order -> {
            // 设置商家名称
            if (order.getMerchantId() != null) {
                Merchant merchant = merchantService.getById(order.getMerchantId());
                if (merchant != null) {
                    order.setMerchantName(merchant.getName());
                }
            }

            // 设置状态文本
            order.setStatusText(getStatusText(order.getStatus()));
        });

        return ResponseEntity.ok(result);
    }

    /**
     * 获取订单详情
     */
    @ApiOperation("获取订单详情")
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyAuthority('admin:order:detail')")
    public ResponseEntity<Map<String, Object>> getOrderDetail(@PathVariable String orderId) {
        Order order = orderService.getById(orderId);

        Map<String, Object> response = new HashMap<>();
        if (order != null) {
            // 添加商家名称
            if (order.getMerchantId() != null) {
                Merchant merchant = merchantService.getById(order.getMerchantId());
                if (merchant != null) {
                    order.setMerchantName(merchant.getName());
                }
            }

            // 添加状态文本
            order.setStatusText(getStatusText(order.getStatus()));

            // 获取订单菜品列表
            java.util.List<OrderDishVO> dishes = orderDishService.getOrderDishesWithDetails(orderId);

            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("orderId", order.getId());
            data.put("userId", order.getUserId());
            data.put("merchantId", order.getMerchantId());
            data.put("merchantName", order.getMerchantName());
            data.put("totalAmount", order.getTotalAmount());
            data.put("status", order.getStatus());
            data.put("statusText", order.getStatusText());
            data.put("paymentId", order.getPaymentId());
            data.put("paidAmount", order.getPaidAmount());
            data.put("paymentTime", order.getPaymentTime());
            data.put("address", order.getAddress());
            data.put("remark", order.getRemark());
            data.put("createTime", order.getCreateTime());
            data.put("updateTime", order.getUpdateTime());
            data.put("dishes", dishes);

            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "订单不存在");
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * 修改订单状态 ✨ 已实现
     */
    @ApiOperation("修改订单状态")
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyAuthority('admin:order:status')")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody Map<String, Object> request) {

        Order order = orderService.getById(orderId);
        if (order == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "订单不存在");
            return ResponseEntity.status(404).body(response);
        }

        Integer oldStatus = order.getStatus();
        Integer newStatus = (Integer) request.get("status");
        String reason = (String) request.get("reason");

        // 验证状态转换是否合法
        if (!isValidStatusTransition(oldStatus, newStatus)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "无效的状态转换：从 " + getStatusText(oldStatus) + " 到 " + getStatusText(newStatus));
            return ResponseEntity.status(400).body(response);
        }

        // 更新订单状态
        order.setStatus(newStatus);
        order.setUpdateTime(LocalDateTime.now());

        boolean success = orderService.updateById(order);

        // 根据新状态发送通知（5状态系统：0-待支付、1-待接单、2-制作中、3-已完成、4-已取消）
        if (success) {
            switch (newStatus) {
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
                case 4: // 已取消
                    NotificationUtil.createOrderNotification(
                        order.getUserId(),
                        NotificationTypeEnum.ORDER_CANCELLED,
                        orderId,
                        "订单已取消"
                    );
                    break;
            }
        }

        // 记录操作日志
        if (success) {
            SystemLogHelper.logUpdate(
                "订单管理",
                "修改订单状态：" + orderId + " 从 " + getStatusText(oldStatus) + " 到 " + getStatusText(newStatus),
                AdminContext.getAdminId(),
                AdminContext.getAdminUsername(),
                Map.of("orderId", orderId, "oldStatus", oldStatus, "newStatus", newStatus)
            );
        }

        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "状态修改成功");
            response.put("data", order);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "状态修改失败");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 批量修改订单状态 ✨ 新增
     */
    @ApiOperation("批量修改订单状态")
    @PutMapping("/batch/status")
    @PreAuthorize("hasAnyAuthority('admin:order:status')")
    public ResponseEntity<Map<String, Object>> batchUpdateOrderStatus(
            @RequestBody Map<String, Object> request) {

        @SuppressWarnings("unchecked")
        java.util.List<String> orderIds = (java.util.List<String>) request.get("orderIds");
        Integer newStatus = (Integer) request.get("status");
        String reason = (String) request.get("reason");

        if (orderIds == null || orderIds.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "订单ID列表不能为空");
            return ResponseEntity.status(400).body(response);
        }

        int successCount = 0;
        int failCount = 0;
        java.util.List<String> failedOrders = new java.util.ArrayList<>();

        for (String orderId : orderIds) {
            try {
                Order order = orderService.getById(orderId);
                if (order != null && isValidStatusTransition(order.getStatus(), newStatus)) {
                    order.setStatus(newStatus);
                    order.setUpdateTime(LocalDateTime.now());
                    if (orderService.updateById(order)) {
                        successCount++;
                    } else {
                        failCount++;
                        failedOrders.add(orderId);
                    }
                } else {
                    failCount++;
                    failedOrders.add(orderId + "(状态转换无效)");
                }
            } catch (Exception e) {
                failCount++;
                failedOrders.add(orderId + "(" + e.getMessage() + ")");
            }
        }

        // 批量通知用户（5状态系统：0-待支付、1-待接单、2-制作中、3-已完成、4-已取消）
        for (String orderId : orderIds) {
            Order updatedOrder = orderService.getById(orderId);
            if (updatedOrder != null) {
                switch (newStatus) {
                    case 2: // 制作中
                        NotificationUtil.createOrderNotification(
                            updatedOrder.getUserId(),
                            NotificationTypeEnum.ORDER_PREPARING_COMPLETE,
                            orderId,
                            "商家已接单，正在制作中"
                        );
                        break;
                    case 3: // 已完成
                        NotificationUtil.createOrderNotification(
                            updatedOrder.getUserId(),
                            NotificationTypeEnum.ORDER_DELIVERED,
                            orderId,
                            "订单已完成"
                        );
                        break;
                    case 4: // 已取消
                        NotificationUtil.createOrderNotification(
                            updatedOrder.getUserId(),
                            NotificationTypeEnum.ORDER_CANCELLED,
                            orderId,
                            "订单已取消"
                        );
                        break;
                }
            }
        }

        // 记录操作日志
        if (successCount > 0) {
            SystemLogHelper.logUpdate(
                "订单管理",
                "批量修改订单状态：" + successCount + "个成功",
                AdminContext.getAdminId(),
                AdminContext.getAdminUsername(),
                Map.of("totalCount", orderIds.size(), "successCount", successCount, "failCount", failCount)
            );
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "批量操作完成：成功" + successCount + "个，失败" + failCount + "个");
        response.put("successCount", successCount);
        response.put("failCount", failCount);
        response.put("failedOrders", failedOrders);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取订单统计 ✨ 新增
     */
    @ApiOperation("获取订单统计")
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyAuthority('admin:order:view')")
    public ResponseEntity<Map<String, Object>> getOrderStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总订单数
        long totalOrders = orderService.count();

        // 各状态订单数量
        Map<String, Long> statusCount = new HashMap<>();
        statusCount.put("pending", orderService.count(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>().eq("status", 0)));
        statusCount.put("confirmed", orderService.count(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>().eq("status", 1)));
        statusCount.put("preparing", orderService.count(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>().eq("status", 2)));
        statusCount.put("completed", orderService.count(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>().eq("status", 3)));
        statusCount.put("cancelled", orderService.count(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Order>().eq("status", 4)));

        stats.put("totalOrders", totalOrders);
        stats.put("statusCount", statusCount);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);
        return ResponseEntity.ok(response);
    }

    /**
     * 验证状态转换是否合法（5状态系统：0-待支付、1-待接单、2-制作中、3-已完成、4-已取消）
     */
    private boolean isValidStatusTransition(Integer oldStatus, Integer newStatus) {
        if (oldStatus == null || newStatus == null) {
            return false;
        }

        // 已取消(4)或已完成(3)的订单不能修改状态
        if (oldStatus == 3 || oldStatus == 4) {
            return false;
        }

        // 待支付订单只能修改为已取消(4)
        if (oldStatus == 0 && newStatus != 4) {
            return false;
        }

        // 状态只能向前推进（0->1->2->3，或者任意可取消状态->4）
        if (newStatus < oldStatus && newStatus != 4) {
            return false;
        }

        // 不允许跳过中间状态（例如不能从待支付直接到制作中）
        if (newStatus > oldStatus + 1 && newStatus != 4) {
            return false;
        }

        return true;
    }

    /**
     * 获取订单状态文本（5状态系统：0-待支付、1-待接单、2-制作中、3-已完成、4-已取消）
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "待支付";
            case 1: return "待接单";
            case 2: return "制作中";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知";
        }
    }
}
