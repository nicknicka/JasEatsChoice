package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.service.MerchantService;
import com.xx.jaseatschoicejava.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private MerchantService merchantService;

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
     * 获取订单状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "待支付";
            case 1: return "待接单";
            case 2: return "备菜中";
            case 3: return "烹饪中";
            case 4: return "待上菜";
            case 5: return "已送达";
            case 6: return "已取消";
            case 7: return "已完成";
            default: return "未知";
        }
    }

    /**
     * 获取订单状态代码（用于前端匹配）
     */
    private String getStatusCode(Integer status) {
        if (status == null) {
            return "PENDING";
        }
        switch (status) {
            case 0: return "PENDING";       // 待支付
            case 1: return "CONFIRMED";     // 待接单
            case 2: return "PREPARING";     // 备菜中
            case 3: return "PREPARING";     // 烹饪中
            case 4: return "DELIVERING";    // 待上菜
            case 5: return "COMPLETED";     // 已送达
            case 6: return "CANCELLED";     // 已取消
            case 7: return "COMPLETED";     // 已完成
            default: return "PENDING";
        }
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
