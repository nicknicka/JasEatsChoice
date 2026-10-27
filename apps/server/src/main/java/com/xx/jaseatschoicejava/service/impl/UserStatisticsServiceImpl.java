package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.entity.ConsumeHistory;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.OrderDish;
import com.xx.jaseatschoicejava.entity.UserCollection;
import com.xx.jaseatschoicejava.mapper.DishFeatureMapper;
import com.xx.jaseatschoicejava.service.CollectionService;
import com.xx.jaseatschoicejava.service.ConsumeHistoryService;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.OrderDishService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.UserStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatisticsServiceImpl implements UserStatisticsService {

    private static final ZoneId STATISTICS_ZONE = ZoneId.of("Asia/Shanghai");

    private final OrderService orderService;
    private final OrderDishService orderDishService;
    private final DishService dishService;
    private final DishFeatureMapper dishFeatureMapper;
    private final ConsumeHistoryService consumeHistoryService;
    private final CollectionService collectionService;

    @Override
    public Map<String, Object> getConsumeStatistics(String userId, Integer days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        // 查询指定时间范围内的订单
        LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(Order::getUserId, userId)
                .ge(Order::getCreatedAt, startTime)
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
                        o -> o.getCreatedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));

        Map<String, BigDecimal> dailyAmount = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().toLocalDate().toString(),
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
        if (days == null || days <= 0) {
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("totalCalories", BigDecimal.ZERO);
            emptyResult.put("avgCalories", BigDecimal.ZERO);
            emptyResult.put("dailyCalories", Collections.emptyList());
            return emptyResult;
        }

        LocalDate endDate = LocalDate.now(STATISTICS_ZONE);
        LocalDate startDate = endDate.minusDays(days - 1);
        LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(Order::getUserId, userId)
                .ge(Order::getCreatedAt, startDate.atStartOfDay())
                .lt(Order::getCreatedAt, endDate.plusDays(1).atStartOfDay())
                .eq(Order::getStatus, 3); // 已完成

        List<Order> orders = orderService.list(orderQuery);

        Map<String, BigDecimal> caloriesByDate = new HashMap<>();
        if (!orders.isEmpty()) {
            List<String> orderIds = orders.stream()
                    .map(Order::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!orderIds.isEmpty()) {
                LambdaQueryWrapper<OrderDish> orderDishQuery = new LambdaQueryWrapper<>();
                orderDishQuery.in(OrderDish::getOrderId, orderIds);
                List<OrderDish> orderDishes = orderDishService.list(orderDishQuery);

                List<String> dishIds = orderDishes.stream()
                        .map(OrderDish::getDishId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());

                Map<String, Dish> dishMap = Collections.emptyMap();
                if (!dishIds.isEmpty()) {
                    LambdaQueryWrapper<Dish> dishQuery = new LambdaQueryWrapper<>();
                    dishQuery.in(Dish::getId, dishIds);
                    dishMap = dishService.list(dishQuery).stream()
                            .collect(Collectors.toMap(Dish::getId, dish -> dish));
                }

                Map<String, BigDecimal> nutritionCalories = new HashMap<>();
                if (!dishIds.isEmpty()) {
                    for (Map<String, Object> row : dishFeatureMapper.selectNutritionCaloriesByDishIds(dishIds)) {
                        Object dishId = row.get("dishId");
                        Object calories = row.get("calories");
                        if (dishId != null && calories != null) {
                            nutritionCalories.put(dishId.toString(), new BigDecimal(calories.toString()));
                        }
                    }
                }

                Map<String, LocalDate> orderDates = orders.stream()
                        .filter(order -> order.getId() != null && order.getCreatedAt() != null)
                        .collect(Collectors.toMap(Order::getId, order -> order.getCreatedAt().toLocalDate()));

                for (OrderDish orderDish : orderDishes) {
                    Dish dish = dishMap.get(orderDish.getDishId());
                    LocalDate orderDate = orderDates.get(orderDish.getOrderId());
                    if (orderDate == null) {
                        continue;
                    }

                    BigDecimal unitCalories = nutritionCalories.get(orderDish.getDishId());
                    if (unitCalories == null && dish != null && dish.getCalorie() != null) {
                        unitCalories = BigDecimal.valueOf(dish.getCalorie());
                    }
                    if (unitCalories == null) {
                        continue;
                    }

                    int quantity = orderDish.getQuantity() == null ? 0 : orderDish.getQuantity();
                    BigDecimal calories = unitCalories.multiply(BigDecimal.valueOf(quantity));
                    caloriesByDate.merge(orderDate.toString(), calories, BigDecimal::add);
                }
            }
        }

        List<Map<String, Object>> dailyCalories = new ArrayList<>();
        BigDecimal totalCalories = BigDecimal.ZERO;

        for (int i = 0; i < days; i++) {
            String dateStr = startDate.plusDays(i).toString();
            BigDecimal calories = caloriesByDate.getOrDefault(dateStr, BigDecimal.ZERO);
            totalCalories = totalCalories.add(calories);

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
                .ge(Order::getCreatedAt, startTime);

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
        result.put("cancelled", statusCount.getOrDefault(4, 0L)); // 已取消

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
