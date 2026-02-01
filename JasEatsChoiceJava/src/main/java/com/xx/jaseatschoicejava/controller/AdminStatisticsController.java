package com.xx.jaseatschoicejava.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Tutorial;
import com.xx.jaseatschoicejava.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员-统计数据控制器（更新版）
 */
@Api(tags = "管理员-统计数据")
@RestController
@RequestMapping("/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatisticsController {

    @Autowired
    private UserService userService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private DishService dishService;

    @Autowired
    private TutorialService tutorialService;

    /**
     * 获取控制台统计数据
     */
    @ApiOperation("获取控制台统计数据")
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('admin:statistics:view')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {

        Map<String, Object> stats = new HashMap<>();

        // 用户统计
        stats.put("totalUsers", userService.count());
        stats.put("todayNewUsers", getTodayNewUsers());

        // 商家统计
        stats.put("totalMerchants", merchantService.count());

        // 订单统计
        stats.put("todayOrders", getTodayOrders());
        stats.put("todayRevenue", getTodayRevenue());

        // 待审核数量
        stats.put("pendingAudits", getPendingAuditsCount());

        // 系统告警（示例：可以根据实际情况添加）
        stats.put("systemAlerts", 0);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户统计数据
     */
    @ApiOperation("获取用户统计数据")
    @GetMapping("/users")
    @PreAuthorize("hasAnyAuthority('admin:statistics:view')")
    public ResponseEntity<Map<String, Object>> getUserStats(
            @RequestParam(required = false) Integer days) {

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.count());
        stats.put("todayNewUsers", getTodayNewUsers());

        // 本周新增用户
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        long weekNewUsers = userService.count(new QueryWrapper<User>()
            .ge("create_time", weekStart));
        stats.put("weekNewUsers", weekNewUsers);

        // 本月新增用户
        LocalDateTime monthStart = LocalDateTime.now().minusDays(30);
        long monthNewUsers = userService.count(new QueryWrapper<User>()
            .ge("create_time", monthStart));
        stats.put("monthNewUsers", monthNewUsers);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取订单统计数据
     */
    @ApiOperation("获取订单统计数据")
    @GetMapping("/orders")
    @PreAuthorize("hasAnyAuthority('admin:statistics:view')")
    public ResponseEntity<Map<String, Object>> getOrderStats(
            @RequestParam(required = false) Integer days) {

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orderService.count());
        stats.put("todayOrders", getTodayOrders());

        // 本周订单数
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        long weekOrders = orderService.count(new QueryWrapper<Order>()
            .ge("create_time", weekStart));
        stats.put("weekOrders", weekOrders);

        // 本月订单数
        LocalDateTime monthStart = LocalDateTime.now().minusDays(30);
        long monthOrders = orderService.count(new QueryWrapper<Order>()
            .ge("create_time", monthStart));
        stats.put("monthOrders", monthOrders);

        // 今日收入
        stats.put("todayRevenue", getTodayRevenue());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取收入统计数据
     */
    @ApiOperation("获取收入统计数据")
    @GetMapping("/revenue")
    @PreAuthorize("hasAnyAuthority('admin:statistics:view')")
    public ResponseEntity<Map<String, Object>> getRevenueStats(
            @RequestParam(required = false) Integer days) {

        Map<String, Object> stats = new HashMap<>();

        // 今日收入
        stats.put("todayRevenue", getTodayRevenue());

        // 本周收入
        stats.put("weekRevenue", getRevenueByDays(7));

        // 本月收入
        stats.put("monthRevenue", getRevenueByDays(30));

        // 总收入
        stats.put("totalRevenue", getTotalRevenue());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);
        return ResponseEntity.ok(response);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取今日新增用户数
     */
    private long getTodayNewUsers() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        return userService.count(new QueryWrapper<User>()
            .ge("create_time", todayStart));
    }

    /**
     * 获取今日订单数
     */
    private long getTodayOrders() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        return orderService.count(new QueryWrapper<Order>()
            .ge("create_time", todayStart)
            .ne("status", 0)); // 排除待支付订单
    }

    /**
     * 获取今日收入
     */
    private double getTodayRevenue() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        // 查询今日已支付订单
        List<Order> todayOrders = orderService.list(new QueryWrapper<Order>()
            .ge("create_time", todayStart)
            .ne("status", 0)
            .isNotNull("paid_amount"));

        // 汇总收入
        return todayOrders.stream()
            .map(Order::getPaidAmount)
            .filter(amount -> amount != null)
            .map(BigDecimal::doubleValue)
            .mapToDouble(Double::doubleValue)
            .sum();
    }

    /**
     * 获取待审核数量
     */
    private long getPendingAuditsCount() {
        long pendingCount = 0;

        // 待审核商家
        pendingCount += merchantService.count(new QueryWrapper<Merchant>()
            .eq("audit_status", "PENDING"));

        // 待审核菜品
        pendingCount += dishService.count(new QueryWrapper<Dish>()
            .eq("audit_status", "PENDING"));

        // 待审核教程
        pendingCount += tutorialService.count(new QueryWrapper<Tutorial>()
            .eq("review_status", "PENDING"));

        return pendingCount;
    }

    /**
     * 获取指定天数内的收入
     */
    private double getRevenueByDays(int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        List<Order> orders = orderService.list(new QueryWrapper<Order>()
            .ge("create_time", startTime)
            .ne("status", 0)
            .isNotNull("paid_amount"));

        return orders.stream()
            .map(Order::getPaidAmount)
            .filter(amount -> amount != null)
            .map(BigDecimal::doubleValue)
            .mapToDouble(Double::doubleValue)
            .sum();
    }

    /**
     * 获取总收入
     */
    private double getTotalRevenue() {
        List<Order> orders = orderService.list(new QueryWrapper<Order>()
            .ne("status", 0)
            .isNotNull("paid_amount"));

        return orders.stream()
            .map(Order::getPaidAmount)
            .filter(amount -> amount != null)
            .map(BigDecimal::doubleValue)
            .mapToDouble(Double::doubleValue)
            .sum();
    }
}
