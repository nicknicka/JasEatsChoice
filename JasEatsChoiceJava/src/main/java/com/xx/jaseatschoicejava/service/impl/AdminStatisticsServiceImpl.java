package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.entity.Merchant;
import com.xx.jaseatschoicejava.mapper.OrderMapper;
import com.xx.jaseatschoicejava.mapper.UserMapper;
import com.xx.jaseatschoicejava.mapper.MerchantMapper;
import com.xx.jaseatschoicejava.service.AdminStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员统计Service实现
 */
@Service
public class AdminStatisticsServiceImpl implements AdminStatisticsService {

    private static final Logger log = LoggerFactory.getLogger(AdminStatisticsServiceImpl.class);

    // 注入必要的Mapper
    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final OrderMapper orderMapper;

    // 手动创建构造函数（替代 Lombok @RequiredArgsConstructor）
    public AdminStatisticsServiceImpl(UserMapper userMapper, MerchantMapper merchantMapper, OrderMapper orderMapper) {
        this.userMapper = userMapper;
        this.merchantMapper = merchantMapper;
        this.orderMapper = orderMapper;
    }

    @Override
    public Map<String, Object> getDashboardStatistics(int days) {
        // 获取核心指标
        Map<String, Object> summary = getSummaryStatistics();

        // 获取每日详细数据
        List<Map<String, Object>> dailyData = getDailyData(days);

        // 获取趋势数据
        Map<String, Object> trends = getTrendData(days);

        // 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("summary", summary);
        result.put("daily", dailyData);
        result.put("trends", trends);
        result.put("days", days);

        log.info("获取统计数据成功，天数：{}", days);
        return result;
    }

    /**
     * 获取核心指标统计
     */
    private Map<String, Object> getSummaryStatistics() {
        // 总用户数
        Long totalUsers = userMapper.selectCount(null);

        // 今日新增用户
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.ge("create_time", todayStart);
        Long newUsers = userMapper.selectCount(userWrapper);

        // 总商家数
        Long totalMerchants = merchantMapper.selectCount(null);

        // 今日新增商家
        QueryWrapper<Merchant> merchantWrapper = new QueryWrapper<>();
        merchantWrapper.ge("create_time", todayStart);
        Long newMerchants = merchantMapper.selectCount(merchantWrapper);

        // 总订单数
        Long totalOrders = orderMapper.selectCount(null);

        // 今日新订单
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.ge("create_time", todayStart);
        Long newOrders = orderMapper.selectCount(orderWrapper);

        // 总收入（已完成订单）
        BigDecimal totalRevenue = calculateTotalRevenue(null);

        // 今日收入
        BigDecimal newRevenue = calculateTotalRevenue(todayStart);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalUsers", totalUsers);
        summary.put("newUsers", newUsers);
        summary.put("totalMerchants", totalMerchants);
        summary.put("newMerchants", newMerchants);
        summary.put("totalOrders", totalOrders);
        summary.put("newOrders", newOrders);
        summary.put("totalRevenue", totalRevenue);
        summary.put("newRevenue", newRevenue);

        return summary;
    }

    /**
     * 获取每日详细数据
     */
    private List<Map<String, Object>> getDailyData(int days) {
        List<Map<String, Object>> dailyList = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            // 当天新增用户
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.ge("create_time", dayStart);
            userWrapper.lt("create_time", dayEnd);
            Long newUsers = userMapper.selectCount(userWrapper);

            // 当天新增商家
            QueryWrapper<Merchant> merchantWrapper = new QueryWrapper<>();
            merchantWrapper.ge("create_time", dayStart);
            merchantWrapper.lt("create_time", dayEnd);
            Long newMerchants = merchantMapper.selectCount(merchantWrapper);

            // 当天订单数
            QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
            orderWrapper.ge("create_time", dayStart);
            orderWrapper.lt("create_time", dayEnd);
            Long totalOrders = orderMapper.selectCount(orderWrapper);

            // 当天已完成订单（状态7=已完成）
            QueryWrapper<Order> completedOrderWrapper = new QueryWrapper<>();
            completedOrderWrapper.ge("create_time", dayStart);
            completedOrderWrapper.lt("create_time", dayEnd);
            completedOrderWrapper.eq("status", 7);
            Long completedOrders = orderMapper.selectCount(completedOrderWrapper);

            // 当天收入
            BigDecimal revenue = calculateTotalRevenue(dayStart);

            // 活跃用户（有订单的用户数）
            QueryWrapper<Order> activeUserWrapper = new QueryWrapper<>();
            activeUserWrapper.ge("create_time", dayStart);
            activeUserWrapper.lt("create_time", dayEnd);
            Long activeUsers = orderMapper.selectCount(activeUserWrapper);

            // 平均订单金额
            BigDecimal averageOrderAmount = completedOrders > 0
                    ? revenue.divide(BigDecimal.valueOf(completedOrders), 2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("newUsers", newUsers);
            dayData.put("newMerchants", newMerchants);
            dayData.put("totalOrders", totalOrders);
            dayData.put("completedOrders", completedOrders);
            dayData.put("revenue", revenue);
            dayData.put("activeUsers", activeUsers);
            dayData.put("averageOrderAmount", averageOrderAmount);

            dailyList.add(dayData);
        }

        return dailyList;
    }

    /**
     * 获取趋势数据
     */
    private Map<String, Object> getTrendData(int days) {
        List<Map<String, Object>> dailyData = getDailyData(days);

        List<String> dates = dailyData.stream()
                .map(d -> (String) d.get("date"))
                .collect(Collectors.toList());

        List<Long> newUsersData = dailyData.stream()
                .map(d -> (Long) d.get("newUsers"))
                .collect(Collectors.toList());

        List<Long> ordersData = dailyData.stream()
                .map(d -> (Long) d.get("totalOrders"))
                .collect(Collectors.toList());

        List<BigDecimal> revenueData = dailyData.stream()
                .map(d -> (BigDecimal) d.get("revenue"))
                .collect(Collectors.toList());

        Map<String, Object> trends = new HashMap<>();
        trends.put("dates", dates);
        trends.put("newUsers", newUsersData);
        trends.put("orders", ordersData);
        trends.put("revenue", revenueData);

        return trends;
    }

    /**
     * 计算总收入（优化版：使用SQL SUM函数）
     */
    private BigDecimal calculateTotalRevenue(LocalDateTime startTime) {
        // 使用SQL SUM函数直接在数据库层面计算，避免加载所有订单到内存
        return orderMapper.sumCompletedOrdersRevenue(startTime);
    }
}
