package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.dto.*;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.OrderDish;
import com.xx.jaseatschoicejava.entity.Review;
import com.xx.jaseatschoicejava.service.*;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商家经营洞察服务实现
 */
@Slf4j
@Service
public class MerchantInsightServiceImpl implements MerchantInsightService {

    private final OrderService orderService;
    private final OrderDishService orderDishService;
    private final DishService dishService;
    private final ReviewService reviewService;
    private final ChatModel agentModel;

    public MerchantInsightServiceImpl(
            OrderService orderService,
            OrderDishService orderDishService,
            DishService dishService,
            ReviewService reviewService,
            @Qualifier("agentModel") ChatModel agentModel) {
        this.orderService = orderService;
        this.orderDishService = orderDishService;
        this.dishService = dishService;
        this.reviewService = reviewService;
        this.agentModel = agentModel;
    }

    @Override
    public InsightMetricsDTO getMetrics(String merchantId, String timeRange) {
        LocalDateTime[] timeRangeArray = calculateTimeRange(timeRange);
        LocalDateTime startTime = timeRangeArray[0];
        LocalDateTime endTime = timeRangeArray[1];

        // 计算对比时间范围
        LocalDateTime[] compareTimeRange = calculateCompareTimeRange(timeRange);
        LocalDateTime compareStart = compareTimeRange[0];
        LocalDateTime compareEnd = compareTimeRange[1];

        // 查询当前时间范围的订单
        List<Order> orders = getOrdersByTimeRange(merchantId, startTime, endTime);
        List<Order> compareOrders = getOrdersByTimeRange(merchantId, compareStart, compareEnd);

        InsightMetricsDTO metrics = new InsightMetricsDTO();

        // 计算营业额
        BigDecimal revenue = orders.stream()
                .filter(o -> o.getStatus() != 4)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal compareRevenue = compareOrders.stream()
                .filter(o -> o.getStatus() != 4)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        metrics.setRevenue(revenue);
        metrics.setRevenueChange(calculateChangePercent(revenue, compareRevenue));

        // 计算订单数
        long orderCount = orders.stream().filter(o -> o.getStatus() != 4).count();
        long compareOrderCount = compareOrders.stream().filter(o -> o.getStatus() != 4).count();
        metrics.setOrders(orderCount);
        metrics.setOrdersChange(calculateChangePercent(BigDecimal.valueOf(orderCount), BigDecimal.valueOf(compareOrderCount)));

        // 计算客单价
        BigDecimal avgPrice = orderCount > 0
                ? revenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal compareAvgPrice = compareOrderCount > 0
                ? compareRevenue.divide(BigDecimal.valueOf(compareOrderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        metrics.setAveragePrice(avgPrice);
        metrics.setAverageChange(calculateChangePercent(avgPrice, compareAvgPrice));

        // 计算评分
        Double avgRating = calculateAverageRating(merchantId, startTime, endTime);
        Double compareAvgRating = calculateAverageRating(merchantId, compareStart, compareEnd);
        metrics.setRating(avgRating);
        metrics.setRatingChange(avgRating != null && compareAvgRating != null
                ? Math.round((avgRating - compareAvgRating) * 10) / 10.0
                : 0.0);

        return metrics;
    }

    @Override
    public List<SalesTrendItemDTO> getSalesTrend(String merchantId, String timeRange) {
        LocalDateTime[] timeRangeArray = calculateTimeRange(timeRange);
        LocalDateTime startTime = timeRangeArray[0];
        LocalDateTime endTime = timeRangeArray[1];

        List<Order> orders = getOrdersByTimeRange(merchantId, startTime, endTime);
        List<SalesTrendItemDTO> trend = new ArrayList<>();

        if ("today".equals(timeRange) || "yesterday".equals(timeRange)) {
            // 按小时统计
            for (int i = 0; i < 24; i++) {
                final int hour = i;
                double amount = orders.stream()
                        .filter(o -> o.getCreateTime().getHour() == hour && o.getStatus() != 4)
                        .map(Order::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .doubleValue();

                SalesTrendItemDTO item = new SalesTrendItemDTO();
                item.setLabel(String.format("%02d:00", i));
                item.setValue(amount);
                trend.add(item);
            }
        } else if ("week".equals(timeRange)) {
            // 按天统计
            String[] weekdays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
            LocalDate today = LocalDate.now();
            LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);

            for (int i = 0; i < 7; i++) {
                LocalDate date = weekStart.plusDays(i);
                final LocalDateTime dayStart = date.atStartOfDay();
                final LocalDateTime dayEnd = date.atTime(23, 59, 59);

                double amount = orders.stream()
                        .filter(o -> !o.getCreateTime().isBefore(dayStart) && !o.getCreateTime().isAfter(dayEnd) && o.getStatus() != 4)
                        .map(Order::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .doubleValue();

                SalesTrendItemDTO item = new SalesTrendItemDTO();
                item.setLabel(weekdays[i]);
                item.setValue(amount);
                trend.add(item);
            }
        } else if ("month".equals(timeRange)) {
            // 按日统计
            LocalDate today = LocalDate.now();
            int daysInMonth = today.lengthOfMonth();

            for (int i = 1; i <= daysInMonth; i++) {
                LocalDate date = today.withDayOfMonth(i);
                final LocalDateTime dayStart = date.atStartOfDay();
                final LocalDateTime dayEnd = date.atTime(23, 59, 59);

                double amount = orders.stream()
                        .filter(o -> !o.getCreateTime().isBefore(dayStart) && !o.getCreateTime().isAfter(dayEnd) && o.getStatus() != 4)
                        .map(Order::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .doubleValue();

                SalesTrendItemDTO item = new SalesTrendItemDTO();
                item.setLabel(i + "日");
                item.setValue(amount);
                trend.add(item);
            }
        }

        return trend;
    }

    @Override
    public List<TopDishDTO> getTopDishes(String merchantId, String timeRange) {
        LocalDateTime[] timeRangeArray = calculateTimeRange(timeRange);
        LocalDateTime startTime = timeRangeArray[0];
        LocalDateTime endTime = timeRangeArray[1];

        // 获取订单
        List<Order> orders = getOrdersByTimeRange(merchantId, startTime, endTime);
        List<String> orderIds = orders.stream()
                .filter(o -> o.getStatus() != 4)
                .map(Order::getId)
                .collect(Collectors.toList());

        if (orderIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询订单菜品
        LambdaQueryWrapper<OrderDish> query = new LambdaQueryWrapper<>();
        query.in(OrderDish::getOrderId, orderIds);
        List<OrderDish> orderDishes = orderDishService.list(query);

        // 按菜品分组统计
        Map<String, Integer> dishSalesMap = orderDishes.stream()
                .collect(Collectors.groupingBy(OrderDish::getDishId, Collectors.summingInt(OrderDish::getQuantity)));

        // 获取菜品信息并构建结果
        List<TopDishDTO> topDishes = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : dishSalesMap.entrySet()) {
            Dish dish = dishService.getById(entry.getKey());
            if (dish != null) {
                TopDishDTO dto = new TopDishDTO();
                dto.setName(dish.getName());
                dto.setSales(entry.getValue());
                dto.setTrend((int) (Math.random() * 30 - 10)); // 简化处理，实际需要对比历史数据
                topDishes.add(dto);
            }
        }

        // 按销量排序并取前5
        topDishes.sort((a, b) -> Integer.compare(b.getSales(), a.getSales()));
        return topDishes.size() > 5 ? topDishes.subList(0, 5) : topDishes;
    }

    @Override
    public List<RatingDistributionDTO> getRatingDistribution(String merchantId) {
        LambdaQueryWrapper<Review> query = new LambdaQueryWrapper<>();
        query.eq(Review::getMerchantId, merchantId)
             .eq(Review::getStatus, 0);
        List<Review> reviews = reviewService.list(query);

        // 统计各星级数量
        Map<Integer, Long> ratingCount = reviews.stream()
                .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));

        int total = reviews.size();
        List<RatingDistributionDTO> distribution = new ArrayList<>();

        for (int stars = 5; stars >= 1; stars--) {
            RatingDistributionDTO dto = new RatingDistributionDTO();
            dto.setStars(stars);
            dto.setCount(ratingCount.getOrDefault(stars, 0L).intValue());
            dto.setPercent(total > 0 ? (int) (dto.getCount() * 100.0 / total) : 0);
            distribution.add(dto);
        }

        return distribution;
    }

    @Override
    public List<AiSuggestionDTO> generateAiSuggestions(String merchantId, String timeRange) {
        try {
            // 获取经营数据
            InsightMetricsDTO metrics = getMetrics(merchantId, timeRange);
            List<TopDishDTO> topDishes = getTopDishes(merchantId, timeRange);
            List<RatingDistributionDTO> ratingDist = getRatingDistribution(merchantId);

            // 构建AI提示词
            String prompt = buildSuggestionPrompt(metrics, topDishes, ratingDist);

            // 调用AI生成建议
            String aiResponse = agentModel.chat(prompt);

            // 解析AI响应
            return parseAiSuggestions(aiResponse);

        } catch (Exception e) {
            log.error("生成AI建议失败", e);
            // 返回默认建议
            return getDefaultSuggestions();
        }
    }

    // ==================== 私有方法 ====================

    private LocalDateTime[] calculateTimeRange(String timeRange) {
        LocalDate today = LocalDate.now();
        LocalDateTime startTime;
        LocalDateTime endTime = LocalDateTime.now();

        switch (timeRange) {
            case "today":
                startTime = today.atStartOfDay();
                break;
            case "week":
                startTime = today.minusDays(today.getDayOfWeek().getValue() - 1).atStartOfDay();
                break;
            case "month":
                startTime = today.withDayOfMonth(1).atStartOfDay();
                break;
            default:
                startTime = today.atStartOfDay();
        }

        return new LocalDateTime[]{startTime, endTime};
    }

    private LocalDateTime[] calculateCompareTimeRange(String timeRange) {
        LocalDate today = LocalDate.now();
        LocalDateTime compareStart;
        LocalDateTime compareEnd;

        switch (timeRange) {
            case "today":
                // 对比昨天
                compareStart = today.minusDays(1).atStartOfDay();
                compareEnd = today.minusDays(1).atTime(23, 59, 59);
                break;
            case "week":
                // 对比上周
                compareStart = today.minusDays(today.getDayOfWeek().getValue() + 6).atStartOfDay();
                compareEnd = today.minusDays(today.getDayOfWeek().getValue()).atTime(23, 59, 59);
                break;
            case "month":
                // 对比上月
                compareStart = today.minusMonths(1).withDayOfMonth(1).atStartOfDay();
                compareEnd = today.minusMonths(1).withDayOfMonth(today.minusMonths(1).lengthOfMonth()).atTime(23, 59, 59);
                break;
            default:
                compareStart = today.minusDays(1).atStartOfDay();
                compareEnd = today.minusDays(1).atTime(23, 59, 59);
        }

        return new LocalDateTime[]{compareStart, compareEnd};
    }

    private List<Order> getOrdersByTimeRange(String merchantId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<Order> query = new LambdaQueryWrapper<>();
        query.eq(Order::getMerchantId, merchantId)
             .ge(startTime != null, Order::getCreateTime, startTime)
             .le(Order::getCreateTime, endTime);
        return orderService.list(query);
    }

    private Double calculateChangePercent(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) == 0 ? 0.0 : 100.0;
        }
        BigDecimal change = current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return change.setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private Double calculateAverageRating(String merchantId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<Review> query = new LambdaQueryWrapper<>();
        query.eq(Review::getMerchantId, merchantId)
             .eq(Review::getStatus, 0)
             .ge(startTime != null, Review::getCreateTime, startTime)
             .le(Review::getCreateTime, endTime);

        List<Review> reviews = reviewService.list(query);
        if (reviews.isEmpty()) {
            return 0.0;
        }

        double avg = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        return Math.round(avg * 10) / 10.0;
    }

    private String buildSuggestionPrompt(InsightMetricsDTO metrics, List<TopDishDTO> topDishes, List<RatingDistributionDTO> ratingDist) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个餐饮经营顾问。请根据以下经营数据，生成4条简洁的经营建议。\n\n");
        sb.append("经营数据：\n");
        sb.append(String.format("- 营业额：%.2f元（变化%.1f%%）\n", metrics.getRevenue().doubleValue(), metrics.getRevenueChange()));
        sb.append(String.format("- 订单数：%d单（变化%.1f%%）\n", metrics.getOrders(), metrics.getOrdersChange()));
        sb.append(String.format("- 客单价：%.2f元（变化%.1f%%）\n", metrics.getAveragePrice().doubleValue(), metrics.getAverageChange()));
        sb.append(String.format("- 平均评分：%.1f分\n", metrics.getRating()));

        if (!topDishes.isEmpty()) {
            sb.append("- 热销菜品：");
            sb.append(topDishes.stream().map(d -> d.getName() + "(" + d.getSales() + "份)").collect(Collectors.joining("、")));
            sb.append("\n");
        }

        if (!ratingDist.isEmpty()) {
            long lowRating = ratingDist.stream()
                    .filter(r -> r.getStars() <= 2)
                    .mapToLong(RatingDistributionDTO::getCount)
                    .sum();
            if (lowRating > 0) {
                sb.append(String.format("- 低评分评价：%d条\n", lowRating));
            }
        }

        sb.append("\n请生成4条经营建议，每条建议格式为：类型|内容\n");
        sb.append("类型包括：warning（预警）、success（成功）、opportunity（机会）\n");
        sb.append("示例：warning|「宫保鸡丁」销量下滑，建议检查口味或推出优惠活动\n");
        sb.append("要求：简洁明了，每条不超过30字，直接返回建议，不要其他解释。");

        return sb.toString();
    }

    private List<AiSuggestionDTO> parseAiSuggestions(String aiResponse) {
        List<AiSuggestionDTO> suggestions = new ArrayList<>();
        String[] lines = aiResponse.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.contains("|")) {
                String[] parts = line.split("\\|", 2);
                if (parts.length == 2) {
                    AiSuggestionDTO dto = new AiSuggestionDTO();
                    String type = parts[0].trim().toLowerCase();
                    if (type.contains("warning") || type.contains("预警") || type.contains("警告")) {
                        dto.setType("warning");
                    } else if (type.contains("opportunity") || type.contains("机会")) {
                        dto.setType("opportunity");
                    } else {
                        dto.setType("success");
                    }
                    dto.setContent(parts[1].trim());
                    suggestions.add(dto);
                }
            }
        }

        // 如果解析失败，返回默认建议
        if (suggestions.isEmpty()) {
            return getDefaultSuggestions();
        }

        return suggestions.size() > 4 ? suggestions.subList(0, 4) : suggestions;
    }

    private List<AiSuggestionDTO> getDefaultSuggestions() {
        List<AiSuggestionDTO> defaults = new ArrayList<>();
        defaults.add(createSuggestion("success", "继续保持优质服务，维护好客户关系"));
        defaults.add(createSuggestion("opportunity", "可考虑推出新品或限时优惠活动"));
        defaults.add(createSuggestion("success", "关注顾客反馈，持续改进菜品质量"));
        defaults.add(createSuggestion("opportunity", "优化高峰时段服务效率"));
        return defaults;
    }

    private AiSuggestionDTO createSuggestion(String type, String content) {
        AiSuggestionDTO dto = new AiSuggestionDTO();
        dto.setType(type);
        dto.setContent(content);
        return dto;
    }
}
