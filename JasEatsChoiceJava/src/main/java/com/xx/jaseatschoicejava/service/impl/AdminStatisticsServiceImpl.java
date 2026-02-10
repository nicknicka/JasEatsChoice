package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.mapper.OrderMapper;
import com.xx.jaseatschoicejava.mapper.UserMapper;
import com.xx.jaseatschoicejava.mapper.MerchantMapper;
import com.xx.jaseatschoicejava.service.AdminStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员统计Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStatisticsServiceImpl implements AdminStatisticsService {

    // 注入必要的Mapper
    private final UserMapper userMapper;
    private final MerchantMapper merchantMapper;
    private final OrderMapper orderMapper;

    @Override
    public Map<String, Object> getDashboardStatistics(int days) {
        // 计算日期范围
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

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
        Long newUsers = userMapper.selectCount(
                new LambdaQueryWrapper<>()
                        .ge(com.xx.jaseatschoicejava.entity.User::getCreateTime, todayStart)
        );

        // 总商家数
        Long totalMerchants = merchantMapper.selectCount(null);

        // 今日新增商家
        Long newMerchants = merchantMapper.selectCount(
                new LambdaQueryWrapper<>()
                        .ge(com.xx.jaseatschoicejava.entity.Merchant::getCreateTime, todayStart)
        );

        // 总订单数
        Long totalOrders = orderMapper.selectCount(null);

        // 今日新订单
        Long newOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<>()
                        .ge(com.xx.jaseatschoicejava.entity.Order::getCreateTime, todayStart)
        );

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
            Long newUsers = userMapper.selectCount(
                    new LambdaQueryWrapper<>()
                            .ge(com.xx.jaseatschoicejava.entity.User::getCreateTime, dayStart)
                            .lt(com.xx.jaseatschoicejava.entity.User::getCreateTime, dayEnd)
            );

            // 当天新增商家
            Long newMerchants = merchantMapper.selectCount(
                    new LambdaQueryWrapper<>()
                            .ge(com.xx.jaseatschoicejava.entity.Merchant::getCreateTime, dayStart)
                            .lt(com.xx.jaseatschoicejava.entity.Merchant::getCreateTime, dayEnd)
            );

            // 当天订单数
            Long totalOrders = orderMapper.selectCount(
                    new LambdaQueryWrapper<>()
                            .ge(com.xx.jaseatschoicejava.entity.Order::getCreateTime, dayStart)
                            .lt(com.xx.jaseatschoicejava.entity.Order::getCreateTime, dayEnd)
            );

            // 当天已完成订单
            Long completedOrders = orderMapper.selectCount(
                    new LambdaQueryWrapper<>()
                            .ge(com.xx.jaseatschoice.entity.Order::getCreateTime, dayStart)
                            .lt(com.xx.jaseatschoice.entity.Order::getCreateTime, dayEnd)
                            .eq(com.xx.jaseatschoice.entity.Order::getStatus, "completed")
            );

            // 当天收入
            BigDecimal revenue = calculateTotalRevenue(dayStart);

            // 活跃用户（有订单的用户数）
            Long activeUsers = orderMapper.selectCount(
                    new LambdaQueryWrapper<>()
                            .ge(com.xx.jaseatschoicejava.entity.Order::getCreateTime, dayStart)
                            .lt(com.xx.jaseatschoice.entity.Order::getCreateTime, dayEnd)
            );

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
     * 计算总收入
     */
    private BigDecimal calculateTotalRevenue(LocalDateTime startTime) {
        LambdaQueryWrapper<com.xx.jaseatschoicejava.entity.Order> queryWrapper = new LambdaQueryWrapper<>();
        if (startTime != null) {
            queryWrapper.ge(com.xx.jaseatschoicejava.entity.Order::getCreateTime, startTime);
        }
        queryWrapper.eq(com.xx.jaseatschoicejava.entity.Order::getStatus, "completed");

        List<com.xx.jaseatschoicejava.entity.Order> orders = orderMapper.selectList(queryWrapper);
        return orders.stream()
                .map(com.xx.jaseatschoicejava.entity.Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
