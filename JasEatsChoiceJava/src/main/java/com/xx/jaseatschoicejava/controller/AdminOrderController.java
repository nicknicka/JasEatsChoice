package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-订单管理控制器
 */
@Api(tags = "管理员-订单管理")
@RestController
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 获取订单列表（分页）
     */
    @ApiOperation("获取订单列表")
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('admin:order:list')")
    public ResponseEntity<IPage<Order>> getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {

        Page<Order> pageParam = new Page<>(page, pageSize);
        IPage<Order> result = orderService.page(pageParam);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取订单详情
     */
    @ApiOperation("获取订单详情")
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyAuthority('admin:order:detail')")
    public ResponseEntity<Map<String, Object>> getOrderDetail(@PathVariable Long orderId) {
        Order order = orderService.getById(orderId);

        Map<String, Object> response = new HashMap<>();
        if (order != null) {
            response.put("success", true);
            response.put("order", order);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "订单不存在");
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * 修改订单状态
     */
    @ApiOperation("修改订单状态")
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyAuthority('admin:order:status')")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {

        String status = request.get("status");

        // TODO: 实现订单状态修改逻辑
        // 1. 验证状态转换是否合法
        // 2. 更新订单状态
        // 3. 发送通知
        // 4. 记录操作日志

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "状态修改成功");
        return ResponseEntity.ok(response);
    }
}
