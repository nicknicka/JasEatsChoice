package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.entity.ConsumeHistory;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.UserCollection;
import com.xx.jaseatschoicejava.service.CollectionService;
import com.xx.jaseatschoicejava.service.ConsumeHistoryService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.UserStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatisticsServiceImpl implements UserStatisticsService {

    private final OrderService orderService;
    private final ConsumeHistoryService consumeHistoryService;
    private final CollectionService collectionService;

    @Override
    public Map<String, Object> getConsumeStatistics(String userId, Integer days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        // 查询指定时间范围内的订单
        LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(Order::getUserId, userId)
                .ge(Order::getCreateTime, startTime)
                .in(Order::getStatus, Arrays.asList(1, 2, 3, 4)); // 待接单、制作中、已完成、已评价

        List<Order> orders = orderService.list(orderQuery);

        // 统计数据
        int totalOrders = orders.size();
        BigDecimal totalAmount = orders.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal completedAmount = orders.stream()
                .filter(o -> o.getStatus() >= 3) // 已完成或已评价
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgOrderAmount = totalOrders > 0
                ? totalAmount.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 按日期分组统计
        Map<String, Long> dailyOrders = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreateTime().toLocalDate().toString(),
                        Collectors.counting()
                ));

        Map<String, BigDecimal> dailyAmount = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreateTime().toLocalDate().toString(),
                        Collectors.mapping(
                                o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        Map<String, Object> result = new HashMap<>();
        result.put("totalOrders", totalOrders);
        result.put("totalAmount", totalAmount);
        result.put("completedAmount", completedAmount);
        result.put("avgOrderAmount", avgOrderAmount);
        result.put("dailyOrders", dailyOrders);
        result.put("dailyAmount", dailyAmount);

        return result;
    }

    @Override
    public Map<String, Object> getCaloriesStatistics(String userId, Integer days) {
        // 由于订单中没有直接存储卡路里信息，这里使用模拟数据
        // 实际应该从订单菜品中计算卡路里总和

        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(Order::getUserId, userId)
                .ge(Order::getCreateTime, startTime)
                .in(Order::getStatus, Arrays.asList(3, 4)); // 已完成或已评价

        List<Order> orders = orderService.list(orderQuery);

        // 模拟卡路里数据（实际应从菜品营养表计算）
        Random random = new Random();
        List<Map<String, Object>> dailyCalories = new ArrayList<>();
        BigDecimal totalCalories = BigDecimal.ZERO;

        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            String dateStr = date.toLocalDate().toString();

            // 查询当天的订单
            long orderCount = orders.stream()
                    .filter(o -> o.getCreateTime().toLocalDate().equals(date.toLocalDate()))
                    .count();

            // 模拟卡路里摄入（每单约500-800卡）
            double calories = orderCount > 0 ? (double) (500 + random.nextDouble() * 300) * orderCount : 0;
            totalCalories = totalCalories.add(BigDecimal.valueOf(calories));

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dateStr);
            dayData.put("calories", calories);
            dailyCalories.add(dayData);
        }

        // 计算平均每日卡路里
        BigDecimal avgCalories = days > 0
                ? totalCalories.divide(BigDecimal.valueOf(days), 0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> result = new HashMap<>();
        result.put("totalCalories", totalCalories);
        result.put("avgCalories", avgCalories);
        result.put("dailyCalories", dailyCalories);

        return result;
    }

    @Override
    public Map<String, Object> getDietRecordsStatistics(String userId, Integer days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(Order::getUserId, userId)
                .ge(Order::getCreateTime, startTime);

        List<Order> orders = orderService.list(orderQuery);

        // 按状态统计
        Map<Integer, Long> statusCount = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

        Map<String, Object> result = new HashMap<>();
        result.put("totalOrders", orders.size());
        result.put("pending", statusCount.getOrDefault(0, 0L)); // 待支付
        result.put("waiting", statusCount.getOrDefault(1, 0L)); // 待接单
        result.put("preparing", statusCount.getOrDefault(2, 0L)); // 制作中
        result.put("completed", statusCount.getOrDefault(3, 0L)); // 已完成
        result.put("reviewed", statusCount.getOrDefault(4, 0L)); // 已评价
        result.put("cancelled", statusCount.getOrDefault(5, 0L)); // 已取消

        return result;
    }

    @Override
    public Map<String, Object> getFavoritesStatistics(String userId) {
        // 使用现有的方法获取收藏列表
        List<UserCollection> collections = collectionService.getCollectionsByUserId(userId);

        // 按收藏类型统计
        Map<String, Long> typeCount = collections.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCollectableType() != null ? c.getCollectableType() : "未分类",
                        Collectors.counting()
                ));

        Map<String, Object> result = new HashMap<>();
        result.put("totalFavorites", collections.size());
        result.put("typeCount", typeCount);

        return result;
    }

    @Override
    public Map<String, Object> getOverviewStatistics(String userId) {
        // 获取近30天的消费统计
        Map<String, Object> consumeStats = getConsumeStatistics(userId, 30);

        // 获取近7天的卡路里统计
        Map<String, Object> caloriesStats = getCaloriesStatistics(userId, 7);

        // 获取近30天的饮食记录统计
        Map<String, Object> dietStats = getDietRecordsStatistics(userId, 30);

        // 获取收藏统计
        Map<String, Object> favoritesStats = getFavoritesStatistics(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("consume", consumeStats);
        result.put("calories", caloriesStats);
        result.put("dietRecords", dietStats);
        result.put("favorites", favoritesStats);

        return result;
    }
}
