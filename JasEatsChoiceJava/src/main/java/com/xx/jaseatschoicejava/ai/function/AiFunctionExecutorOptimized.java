package com.xx.jaseatschoicejava.ai.function;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.jaseatschoicejava.dto.NutritionInfo;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.enums.AiFunctionType;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.NutritionAnalysisService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI工具函数执行器（优化版）
 * 使用枚举替代硬编码字符串，使用真实数据服务
 *
 * @author Claude
 * @since 2026-03-13
 */
@Slf4j
@Component
public class AiFunctionExecutorOptimized {

    @Resource
    private DishService dishService;

    @Resource
    private OrderService orderService;

    @Resource
    private UserService userService;

    @Resource
    private NutritionAnalysisService nutritionAnalysisService;

    /**
     * 执行工具函数
     *
     * @param functionName 函数名称
     * @param arguments    函数参数
     * @return 执行结果
     */
    public String executeFunction(String functionName, Map<String, Object> arguments) {
        log.info("执行AI工具函数: {}, 参数: {}", functionName, arguments);

        // 1. 验证函数名称
        AiFunctionType functionType = AiFunctionType.fromFunctionName(functionName);
        if (functionType == null) {
            log.warn("未知的工具函数: {}", functionName);
            return buildErrorResponse("未知的工具函数: " + functionName);
        }

        // 2. 根据枚举类型执行相应函数
        try {
            return switch (functionType) {
                case SEARCH_DISHES -> searchDishes(arguments);
                case GET_DISH_DETAILS -> getDishDetails(arguments);
                case CREATE_ORDER -> createOrder(arguments);
                case GET_ORDER_STATUS -> getOrderStatus(arguments);
                case GET_USER_PREFERENCES -> getUserPreferences(arguments);
                case ANALYZE_NUTRITION -> analyzeNutrition(arguments);
            };

        } catch (Exception e) {
            log.error("执行工具函数失败: {}", functionName, e);
            return buildErrorResponse("执行失败: " + e.getMessage());
        }
    }

    /**
     * 搜索菜品
     */
    private String searchDishes(Map<String, Object> arguments) {
        String keyword = getStringArgument(arguments, "keyword");
        String category = getStringArgument(arguments, "category");

        log.info("搜索菜品 - 关键词: {}, 分类: {}", keyword, category);

        try {
            // 构建查询条件
            QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();

            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.like("name", keyword);
            }

            if (category != null && !category.isEmpty()) {
                queryWrapper.eq("category", category);
            }

            queryWrapper.eq("is_online", true)
                    .orderByDesc("avg_rating")
                    .last("LIMIT 10");

            List<Dish> dishes = dishService.list(queryWrapper);

            if (dishes.isEmpty()) {
                return "抱歉，没有找到相关的菜品。您可以尝试其他关键词或分类。";
            }

            // 格式化返回结果
            StringBuilder result = new StringBuilder("找到以下菜品：\n\n");
            for (int i = 0; i < dishes.size(); i++) {
                Dish dish = dishes.get(i);
                result.append(String.format("%d. %s\n", i + 1, dish.getName()));
                result.append(String.format("   价格：￥%.2f", dish.getPrice()));

                if (dish.getCalorie() != null) {
                    result.append(String.format(" | 热量：%d kcal", dish.getCalorie()));
                }

                if (dish.getDescription() != null && !dish.getDescription().isEmpty()) {
                    result.append(String.format("\n   简介：%s", dish.getDescription()));
                }

                result.append("\n\n");
            }

            result.append(String.format("共找到%d道菜品，需要查看详情或下单吗？", dishes.size()));
            return result.toString();

        } catch (Exception e) {
            log.error("搜索菜品失败", e);
            return buildErrorResponse("搜索菜品时出现错误");
        }
    }

    /**
     * 获取菜品详情
     */
    private String getDishDetails(Map<String, Object> arguments) {
        String dishId = getStringArgument(arguments, "dish_id");

        log.info("获取菜品详情 - ID: {}", dishId);

        try {
            Dish dish = dishService.getById(dishId);

            if (dish == null) {
                return "未找到该菜品，请检查菜品ID是否正确。";
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("《%s》详细信息：\n\n", dish.getName()));
            result.append(String.format("💰 价格：￥%.2f\n", dish.getPrice()));

            if (dish.getCategory() != null) {
                result.append(String.format("🏷️ 分类：%s\n", dish.getCategory()));
            }

            if (dish.getCalorie() != null) {
                result.append(String.format("🔥 热量：%d kcal\n", dish.getCalorie()));
            }

            if (dish.getDescription() != null && !dish.getDescription().isEmpty()) {
                result.append(String.format("📝 简介：%s\n", dish.getDescription()));
            }

            if (dish.getAvgRating() != null) {
                result.append(String.format("⭐ 评分：%.1f\n", dish.getAvgRating()));
            }

            result.append("\n需要了解更多营养信息或下单吗？");
            return result.toString();

        } catch (Exception e) {
            log.error("获取菜品详情失败", e);
            return buildErrorResponse("获取菜品详情时出现错误");
        }
    }

    /**
     * 创建订单
     */
    private String createOrder(Map<String, Object> arguments) {
        log.info("创建订单 - 参数: {}", arguments);

        try {
            // 1. 解析参数
            List<Map<String, Object>> dishItems = getArrayArgument(arguments, "dish_items");
            String address = getStringArgument(arguments, "address");
            String userId = getStringArgument(arguments, "user_id");

            // 2. 参数验证
            if (dishItems == null || dishItems.isEmpty()) {
                return buildErrorResponse("请至少选择一道菜品");
            }

            if (address == null || address.isEmpty()) {
                return buildErrorResponse("请提供配送地址");
            }

            // 如果没有userId，使用默认值
            if (userId == null || userId.isEmpty()) {
                userId = "ai_user_" + System.currentTimeMillis();
                log.warn("未提供用户ID，使用临时ID: {}", userId);
            }

            // 3. 查询菜品信息并计算总金额
            List<com.xx.jaseatschoicejava.entity.OrderDish> orderDishes = new ArrayList<>();
            java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;

            for (Map<String, Object> item : dishItems) {
                String dishId = getStringArgument(item, "dish_id");
                Integer quantity = getIntegerArgument(item, "quantity");

                if (dishId == null) {
                    return buildErrorResponse("菜品ID不能为空");
                }

                if (quantity == null || quantity <= 0) {
                    quantity = 1; // 默认数量为1
                }

                // 查询菜品信息
                com.xx.jaseatschoicejava.entity.Dish dish = dishService.getById(dishId);
                if (dish == null) {
                    log.warn("菜品不存在: {}, 跳过", dishId);
                    continue;
                }

                // 创建订单菜品
                com.xx.jaseatschoicejava.entity.OrderDish orderDish = new com.xx.jaseatschoicejava.entity.OrderDish();
                orderDish.setDishId(dishId);
                orderDish.setQuantity(quantity);
                orderDish.setPrice(dish.getPrice());
                orderDishes.add(orderDish);

                // 累计总金额
                totalAmount = totalAmount.add(dish.getPrice().multiply(new java.math.BigDecimal(quantity)));
            }

            if (orderDishes.isEmpty()) {
                return buildErrorResponse("没有有效的菜品，请检查菜品ID是否正确");
            }

            // 4. 创建订单对象
            com.xx.jaseatschoicejava.entity.Order order = new com.xx.jaseatschoicejava.entity.Order();
            order.setUserId(userId);
            order.setTotalAmount(totalAmount);
            order.setAddress(address);
            order.setStatus(0); // 待支付
            order.setCreateTime(java.time.LocalDateTime.now());

            // 5. 调用订单服务创建订单
            boolean success = orderService.createOrderWithDishes(order, orderDishes);

            if (!success) {
                return buildErrorResponse("订单创建失败，请稍后重试");
            }

            // 6. 返回成功结果
            StringBuilder result = new StringBuilder();
            result.append("订单创建成功！🎉\n\n");
            result.append("📋 订单详情：\n");
            result.append(String.format("- 订单号：%s\n", order.getId()));
            result.append(String.format("- 菜品数量：%d道\n", orderDishes.size()));
            result.append(String.format("- 订单总金额：￥%.2f\n", totalAmount));
            result.append(String.format("- 配送地址：%s\n", address));
            result.append(String.format("- 订单状态：%s\n", getOrderStatusText(order.getStatus())));

            result.append("\n预计30分钟内送达，谢谢您的订购！🍴");
            return result.toString();

        } catch (Exception e) {
            log.error("创建订单失败", e);
            return buildErrorResponse("创建订单时出现错误: " + e.getMessage());
        }
    }

    /**
     * 查询订单状态
     */
    private String getOrderStatus(Map<String, Object> arguments) {
        String orderId = getStringArgument(arguments, "order_id");

        log.info("查询订单状态 - ID: {}", orderId);

        try {
            // 尝试从订单ID获取订单
            Order order = orderService.getById(orderId);

            if (order == null) {
                return "未找到该订单，请确认订单号是否正确。";
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("订单 %s 的当前状态：\n\n", orderId));
            result.append(String.format("📦 订单状态：%s\n", getOrderStatusText(order.getStatus())));
            result.append(String.format("💰 总金额：￥%.2f\n", order.getTotalAmount()));

            if (order.getCreateTime() != null) {
                result.append(String.format("🕐 下单时间：%s\n", order.getCreateTime()));
            }

            result.append("\n需要其他帮助吗？");
            return result.toString();

        } catch (Exception e) {
            log.error("查询订单状态失败", e);
            return buildErrorResponse("查询订单状态时出现错误");
        }
    }

    /**
     * 获取订单状态文本
     */
    private String getOrderStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }

        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "待接单";
            case 2 -> "备菜中";
            case 3 -> "烹饪中";
            case 4 -> "待上菜";
            case 5 -> "已送达";
            case 6 -> "已取消";
            case 7 -> "待评价";
            case 8 -> "已评价";
            default -> "未知状态";
        };
    }

    /**
     * 获取用户偏好
     */
    private String getUserPreferences(Map<String, Object> arguments) {
        String userId = getStringArgument(arguments, "user_id");

        log.info("获取用户偏好 - ID: {}", userId);

        try {
            User user = userService.getById(userId);

            if (user == null) {
                return "未找到该用户信息。";
            }

            StringBuilder result = new StringBuilder();
            result.append("用户饮食偏好：\n\n");

            if (user.getNickname() != null) {
                result.append(String.format("👤 用户：%s\n", user.getNickname()));
            }

            // TODO: 这里应该从用户偏好表获取详细的饮食偏好信息
            // 目前先返回基础信息
            result.append("\n当前显示的是基础信息，需要我帮您推荐菜品吗？");
            return result.toString();

        } catch (Exception e) {
            log.error("获取用户偏好失败", e);
            return buildErrorResponse("获取用户偏好时出现错误");
        }
    }

    /**
     * 分析营养信息
     * 使用真实的营养分析服务，替代硬编码数据
     */
    private String analyzeNutrition(Map<String, Object> arguments) {
        String foodName = getStringArgument(arguments, "food_name");

        log.info("分析营养信息 - 食物: {}", foodName);

        if (foodName == null || foodName.isEmpty()) {
            return buildErrorResponse("请提供要分析的食物名称");
        }

        try {
            // 调用营养分析服务
            NutritionInfo nutritionInfo = nutritionAnalysisService.analyzeNutrition(foodName);

            // 返回格式化的营养信息
            return nutritionInfo.toFormattedText();

        } catch (Exception e) {
            log.error("营养分析失败", e);
            return buildErrorResponse("营养分析时出现错误: " + e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取字符串参数
     */
    private String getStringArgument(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    /**
     * 获取整数参数
     */
    private Integer getIntegerArgument(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取数组参数
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getArrayArgument(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof List) {
            return (List<Map<String, Object>>) value;
        }
        return new ArrayList<>();
    }

    /**
     * 构建错误响应
     */
    private String buildErrorResponse(String message) {
        return "❌ " + message;
    }
}
