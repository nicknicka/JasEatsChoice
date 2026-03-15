package com.xx.jaseatschoicejava.ai.function;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.dto.NutritionInfo;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.Notification;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.entity.Review;
import com.xx.jaseatschoicejava.entity.User;
import com.xx.jaseatschoicejava.entity.UserCoupon;
import com.xx.jaseatschoicejava.enums.AiFunctionType;
import com.xx.jaseatschoicejava.service.CollectionService;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.NotificationService;
import com.xx.jaseatschoicejava.service.NutritionAnalysisService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.ReviewService;
import com.xx.jaseatschoicejava.service.UserCouponService;
import com.xx.jaseatschoicejava.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI工具函数执行器（反射优化版）
 * 使用反射机制动态调用函数处理方法，实现开闭原则
 *
 * 优势：
 * 1. 添加新函数时无需修改核心调度逻辑
 * 2. 自动扫描和注册函数处理方法
 * 3. 支持热插拔（理论上）
 * 4. 代码更简洁、更易维护
 *
 * @author Claude
 * @since 2026-03-14
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
    private NotificationService notificationService;

    @Resource
    private NutritionAnalysisService nutritionAnalysisService;

    @Resource
    private CollectionService collectionService;

    @Resource
    private ReviewService reviewService;

    @Resource
    private UserCouponService userCouponService;

    /**
     * JSON序列化工具
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 函数名称 -> 处理方法的映射缓存
     */
    private final Map<String, Method> functionHandlers = new ConcurrentHashMap<>();

    /**
     * 初始化函数处理器映射
     * 在Spring容器初始化后自动扫描所有带 @AiFunctionHandler 注解的方法
     */
    @PostConstruct
    public void initFunctionHandlers() {
        log.info("开始扫描AI工具函数处理方法...");

        // 获取当前类的所有方法
        Method[] methods = this.getClass().getDeclaredMethods();

        for (Method method : methods) {
            // 检查是否有 @AiFunctionHandler 注解
            AiFunctionHandler annotation = method.getAnnotation(AiFunctionHandler.class);

            if (annotation != null) {
                String functionName = annotation.value();

                // 验证方法签名
                if (!isValidHandlerMethod(method)) {
                    log.warn("方法 {} 签名不符合要求，跳过注册", method.getName());
                    continue;
                }

                // 验证函数名称是否在枚举中定义
                if (!AiFunctionType.isValidFunction(functionName)) {
                    log.warn("函数名称 {} 未在 AiFunctionType 枚举中定义，跳过注册", functionName);
                    continue;
                }

                // 注册函数处理器
                method.setAccessible(true); // 允许访问私有方法
                functionHandlers.put(functionName, method);

                log.info("注册AI工具函数: {} -> {}(), 描述: {}",
                    functionName, method.getName(), annotation.description());
            }
        }

        log.info("AI工具函数扫描完成，共注册 {} 个函数", functionHandlers.size());
    }

    /**
     * 验证处理方法签名是否符合要求
     * 要求：
     * 1. 返回类型必须是 String
     * 2. 必须有一个参数，类型为 Map<String, Object>
     */
    private boolean isValidHandlerMethod(Method method) {
        // 检查返回类型
        if (!String.class.equals(method.getReturnType())) {
            return false;
        }

        // 检查参数
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            return false;
        }

        if (!Map.class.equals(parameterTypes[0])) {
            return false;
        }

        return true;
    }

    /**
     * 执行工具函数
     *
     * @param functionName 函数名称
     * @param arguments    函数参数
     * @param userId       当前登录用户的ID（可选，用于自动注入）
     * @return 执行结果
     */
    public String executeFunction(String functionName, Map<String, Object> arguments, String userId) {
        log.info("执行AI工具函数: {}, 参数: {}, 用户ID: {}", functionName, arguments, userId);

        // 1. 验证函数名称
        Method handler = functionHandlers.get(functionName);
        if (handler == null) {
            log.warn("未找到工具函数处理器: {}", functionName);
            return buildErrorResponse("未知的工具函数: " + functionName);
        }

        // 2. 自动注入userId到参数中（如果函数需要且当前用户已登录）
        Map<String, Object> enhancedArguments = new HashMap<>(arguments);
        if (userId != null && !userId.isEmpty()) {
            // 对于需要userId的函数，如果参数中没有提供，自动注入
            if (needsUserId(functionName) && !enhancedArguments.containsKey("user_id")) {
                enhancedArguments.put("user_id", userId);
                log.info("自动注入用户ID到函数参数: {} -> {}", functionName, userId);
            }
        }

        // 3. 使用反射调用处理方法
        try {
            Object result = handler.invoke(this, enhancedArguments);
            log.info("工具函数执行成功: {}, 结果长度: {} 字符",
                functionName, result != null ? result.toString().length() : 0);
            return (String) result;

        } catch (Exception e) {
            log.error("执行工具函数失败: {}, 异常类型: {}, 错误信息: {}",
                functionName, e.getClass().getName(), e.getMessage(), e);
            // 输出详细的异常堆栈
            if (e.getCause() != null) {
                log.error("根本原因: {}", e.getCause().getMessage(), e.getCause());
            }
            return buildErrorResponse("执行失败: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    /**
     * 执行工具函数（兼容旧版本，不传入userId）
     */
    public String executeFunction(String functionName, Map<String, Object> arguments) {
        return executeFunction(functionName, arguments, null);
    }

    /**
     * 判断函数是否需要userId参数
     */
    private boolean needsUserId(String functionName) {
        return "create_order".equals(functionName) ||
               "get_user_preferences".equals(functionName) ||
               "get_user_info".equals(functionName) ||
               "get_favorites".equals(functionName) ||
               "add_favorite".equals(functionName) ||
               "get_user_reviews".equals(functionName) ||
               "get_user_coupons".equals(functionName) ||
               "list_orders".equals(functionName) ||
               "get_order_status".equals(functionName) ||
               "cancel_order".equals(functionName) ||
               "urge_order".equals(functionName) ||
               "get_today_calories".equals(functionName) ||
               "analyze_nutrition_intake".equals(functionName);
    }

    // ==================== 以下是各个函数的处理方法 ====================

    /**
     * 搜索菜品
     */
    @AiFunctionHandler(value = "search_dishes", description = "根据关键词或分类搜索菜品")
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
    @AiFunctionHandler(value = "get_dish_details", description = "获取指定菜品的详细信息")
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
    @AiFunctionHandler(value = "create_order", description = "创建一个新的订单")
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
    @AiFunctionHandler(value = "get_order_status", description = "查询订单的当前状态")
    private String getOrderStatus(Map<String, Object> arguments) {
        String orderId = getStringArgument(arguments, "order_id");
        String userId = getStringArgument(arguments, "user_id");

        log.info("查询订单状态 - ID: {}, userId: {}", orderId, userId);

        try {
            // 尝试从订单ID获取订单
            Order order = orderService.getById(orderId);

            if (order == null) {
                return "未找到该订单，请确认订单号是否正确。";
            }

            // 权限验证：只能查询自己的订单
            if (userId != null && !userId.isEmpty() && !userId.equals(order.getUserId())) {
                log.warn("用户 {} 尝试查询他人订单 {}", userId, orderId);
                return buildErrorResponse("无权查询该订单");
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
     * 查询订单列表
     */
    @AiFunctionHandler(value = "list_orders", description = "查询用户的所有订单列表")
    private String listOrders(Map<String, Object> arguments) {
        // 从arguments中获取user_id（系统自动注入）
        String userId = getStringArgument(arguments, "user_id");

        log.info("查询订单列表 - userId: {}", userId);

        try {
            // 构建查询条件
            QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .orderByDesc("create_time")
                    .last("LIMIT 20");  // 最多返回最近20条订单

            List<Order> orders = orderService.list(queryWrapper);

            if (orders.isEmpty()) {
                return "您目前还没有订单记录。";
            }

            // 格式化返回结果
            StringBuilder result = new StringBuilder();
            result.append(String.format("找到您的 %d 条订单记录：\n\n", orders.size()));

            for (int i = 0; i < orders.size(); i++) {
                Order order = orders.get(i);
                result.append(String.format("%d. 订单号：%s\n", i + 1, order.getId()));
                result.append(String.format("   状态：%s\n", getOrderStatusText(order.getStatus())));
                result.append(String.format("   金额：￥%.2f\n", order.getTotalAmount()));

                if (order.getCreateTime() != null) {
                    result.append(String.format("   时间：%s\n", order.getCreateTime()));
                }

                result.append("\n");
            }

            result.append("需要查看某个订单的详情吗？可以告诉我订单号。");
            return result.toString();

        } catch (Exception e) {
            log.error("查询订单列表失败", e);
            return buildErrorResponse("查询订单列表时出现错误");
        }
    }

    /**
     * 获取用户偏好
     */
    @AiFunctionHandler(value = "get_user_preferences", description = "获取用户的饮食偏好和历史记录")
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
     */
    @AiFunctionHandler(value = "analyze_nutrition", description = "分析食物的营养成分和热量")
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

    /**
     * 构建成功响应（JSON格式）
     * 用于AI分析的结构化数据返回
     */
    private String buildSuccessResponse(Map<String, Object> data) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);
            response.put("timestamp", System.currentTimeMillis());
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            // 如果JSON序列化失败，返回简单文本
            return "✅ " + data.toString();
        }
    }

    /**
     * 构建错误响应（JSON格式）
     */
    private String buildErrorResponseJson(String message) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", message);
            response.put("timestamp", System.currentTimeMillis());
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            // 如果JSON序列化失败，返回简单文本
            return buildErrorResponse(message);
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
     * 取消订单
     * 只能取消状态为"待接单"(1)的订单
     */
    @AiFunctionHandler(value = "cancel_order", description = "取消用户指定的订单")
    private String cancelOrder(Map<String, Object> arguments) {
        String orderId = getStringArgument(arguments, "order_id");
        String userId = getStringArgument(arguments, "user_id");

        log.info("取消订单 - orderId: {}, userId: {}", orderId, userId);

        if (orderId == null || orderId.isEmpty()) {
            return buildErrorResponse("请提供要取消的订单号");
        }

        try {
            // 查询订单
            Order order = orderService.getById(orderId);

            if (order == null) {
                return buildErrorResponse("订单不存在，请检查订单号是否正确");
            }

            // 权限验证：只能取消自己的订单
            if (userId != null && !userId.isEmpty() && !userId.equals(order.getUserId())) {
                log.warn("用户 {} 尝试取消他人订单 {}", userId, orderId);
                return buildErrorResponse("无权取消该订单");
            }

            // 验证订单状态
            // 只有待接单(1)的状态可以取消
            if (order.getStatus() == null || order.getStatus() != 1) {
                return String.format(
                    "❌ 订单 %s 无法取消\n\n" +
                    "当前状态：%s\n\n" +
                    "取消规则：只有【待接单】状态的订单可以取消。\n" +
                    "您的订单状态为【%s】，不在可取消范围内。",
                    orderId,
                    getOrderStatusText(order.getStatus()),
                    getOrderStatusText(order.getStatus())
                );
            }

            // 更新订单状态为已取消
            order.setStatus(6); // 6-已取消
            boolean success = orderService.updateById(order);

            if (!success) {
                return buildErrorResponse("取消订单失败，请稍后重试");
            }

            // 返回成功信息
            StringBuilder result = new StringBuilder();
            result.append(String.format("✅ 订单 %s 已成功取消\n\n", orderId));
            result.append(String.format("订单金额：￥%.2f\n", order.getTotalAmount()));

            // 如果已支付，提示退款
            if (order.getPaidAmount() != null && order.getPaidAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                result.append(String.format("已支付金额：￥%.2f\n", order.getPaidAmount()));
                result.append("\n💰 退款将在1-3个工作日内原路返回到您的支付账户，请耐心等待。");
            } else {
                result.append("\n该订单未支付，无需退款。");
            }

            result.append("\n\n还需要其他帮助吗？");
            return result.toString();

        } catch (Exception e) {
            log.error("取消订单失败", e);
            return buildErrorResponse("取消订单时出现错误: " + e.getMessage());
        }
    }

    /**
     * 催单功能
     * 通知商家加快订单处理进度
     */
    @AiFunctionHandler(value = "urge_order", description = "催促商家加快订单处理进度")
    private String urgeOrder(Map<String, Object> arguments) {
        String orderId = getStringArgument(arguments, "order_id");
        String userId = getStringArgument(arguments, "user_id");

        log.info("催单 - orderId: {}, userId: {}", orderId, userId);

        if (orderId == null || orderId.isEmpty()) {
            return buildErrorResponse("请提供要催促的订单号");
        }

        try {
            // 查询订单
            Order order = orderService.getById(orderId);

            if (order == null) {
                return buildErrorResponse("订单不存在，请检查订单号是否正确");
            }

            // 权限验证：只能催促自己的订单
            if (userId != null && !userId.isEmpty() && !userId.equals(order.getUserId())) {
                log.warn("用户 {} 尝试催促他人订单 {}", userId, orderId);
                return buildErrorResponse("无权催促该订单");
            }

            // 验证订单状态
            if (order.getStatus() == null || order.getStatus() == 6) {
                return String.format(
                    "❌ 订单 %s 无法催单\n\n" +
                    "当前状态：%s\n\n" +
                    "已取消的订单无法催促。",
                    orderId,
                    getOrderStatusText(order.getStatus())
                );
            }

            if (order.getStatus() == 5 || order.getStatus() == 7 || order.getStatus() == 8) {
                return String.format(
                    "✅ 订单 %s\n\n" +
                    "当前状态：%s\n\n" +
                    "您的订单已经完成，无需催促。感谢您的耐心等待！",
                    orderId,
                    getOrderStatusText(order.getStatus())
                );
            }

            // TODO: 这里应该发送通知给商家端
            // 实际实现需要：
            // 1. 检查催单频率限制（5分钟内只能催1次）
            // 2. 发送WebSocket或系统通知给商家
            // 3. 记录催单时间和次数

            // 根据订单状态返回不同的预计时间
            String estimatedTime;
            String advice;

            switch (order.getStatus()) {
                case 0 -> { // 待支付
                    estimatedTime = "请先完成支付";
                    advice = "支付后商家会立即接单处理。";
                }
                case 1 -> { // 待接单
                    estimatedTime = "约5-10分钟";
                    advice = "商家会尽快确认您的订单。";
                }
                case 2 -> { // 备菜中
                    estimatedTime = "约10-15分钟";
                    advice = "商家正在精心准备您点的菜品。";
                }
                case 3 -> { // 烹饪中
                    estimatedTime = "约5-10分钟";
                    advice = "菜品正在烹饪中，马上就好！";
                }
                case 4 -> { // 待上菜
                    estimatedTime = "约3-5分钟";
                    advice = "菜品已经准备好了，即将上菜！";
                }
                default -> {
                    estimatedTime = "请耐心等待";
                    advice = "商家正在加急处理您的订单。";
                }
            }

            // 返回催单成功信息
            StringBuilder result = new StringBuilder();
            result.append(String.format("⏰ 已为您催单：订单 %s\n\n", orderId));
            result.append(String.format("当前状态：%s\n", getOrderStatusText(order.getStatus())));
            result.append(String.format("预计时间：%s\n\n", estimatedTime));
            result.append(String.format("💡 %s\n\n", advice));
            result.append("已通知商家加急处理，感谢您的理解与耐心！");

            return result.toString();

        } catch (Exception e) {
            log.error("催单失败", e);
            return buildErrorResponse("催单时出现错误: " + e.getMessage());
        }
    }

    /**
     * 获取热门菜品推荐
     * 基于销量、评分等综合指标推荐
     */
    @AiFunctionHandler(value = "get_hot_dishes", description = "获取热门菜品推荐")
    private String getHotDishes(Map<String, Object> arguments) {
        String category = getStringArgument(arguments, "category");
        Integer limit = getIntegerArgument(arguments, "limit");

        log.info("获取热门菜品 - category: {}, limit: {}", category, limit);

        // 默认返回10条
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (limit > 20) {
            limit = 20; // 最多返回20条
        }

        try {
            // 构建查询条件
            QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_online", true); // 只查询上架菜品

            if (category != null && !category.isEmpty()) {
                queryWrapper.eq("category", category);
            }

            // 按照订单数和评分综合排序
            // 排序公式：订单数 * 10 + 评分 * 20 + 收藏数 * 5
            queryWrapper.orderByDesc(
                "order_count",
                "avg_rating",
                "favorite_count"
            );

            queryWrapper.last("LIMIT " + limit);

            List<Dish> dishes = dishService.list(queryWrapper);

            if (dishes.isEmpty()) {
                if (category != null && !category.isEmpty()) {
                    return String.format("暂时没有【%s】分类的热门菜品，要不要看看其他分类的菜品？", category);
                }
                return "暂时没有热门菜品推荐，要不要看看其他菜品？";
            }

            // 格式化返回结果
            StringBuilder result = new StringBuilder();
            result.append("🔥 为您找到以下热门菜品：\n\n");

            for (int i = 0; i < dishes.size(); i++) {
                Dish dish = dishes.get(i);
                result.append(String.format("%d. **%s**\n", i + 1, dish.getName()));

                if (dish.getPrice() != null) {
                    result.append(String.format("   💰 价格：￥%.2f\n", dish.getPrice()));
                }

                if (dish.getAvgRating() != null) {
                    result.append(String.format("   ⭐ 评分：%.1f分\n", dish.getAvgRating()));
                }

                if (dish.getOrderCount() != null && dish.getOrderCount() > 0) {
                    result.append(String.format("   🛒 销量：%d份\n", dish.getOrderCount()));
                }

                if (dish.getCalorie() != null && dish.getCalorie() > 0) {
                    result.append(String.format("   🔥 热量：%d卡\n", dish.getCalorie()));
                }

                if (dish.getDescription() != null && !dish.getDescription().isEmpty()) {
                    String desc = dish.getDescription();
                    if (desc.length() > 30) {
                        desc = desc.substring(0, 30) + "...";
                    }
                    result.append(String.format("   📝 %s\n", desc));
                }

                result.append("\n");
            }

            result.append("💡 这些菜品都是大家点得最多的，评价也很不错哦！\n");
            result.append("需要我帮您介绍某道菜的详细情况吗？");

            return result.toString();

        } catch (Exception e) {
            log.error("获取热门菜品失败", e);
            return buildErrorResponse("获取热门菜品时出现错误: " + e.getMessage());
        }
    }

    /**
     * 获取今日推荐
     * 根据时间、季节、用户偏好综合推荐
     */
    @AiFunctionHandler(value = "get_today_recommendations", description = "根据时间和用户偏好获取今日推荐菜品")
    private String getTodayRecommendations(Map<String, Object> arguments) {
        String userId = getStringArgument(arguments, "user_id");

        log.info("获取今日推荐 - userId: {}", userId);

        try {
            // 获取当前时间
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            int hour = now.getHour();
            int month = now.getMonthValue();

            // 根据时间判断用餐类型
            String mealType;
            String mealTypeName;
            if (hour >= 6 && hour < 9) {
                mealType = "breakfast";
                mealTypeName = "早餐";
            } else if (hour >= 11 && hour < 14) {
                mealType = "lunch";
                mealTypeName = "午餐";
            } else if (hour >= 17 && hour < 20) {
                mealType = "dinner";
                mealTypeName = "晚餐";
            } else if (hour >= 21 && hour < 23) {
                mealType = "late_night";
                mealTypeName = "夜宵";
            } else {
                mealType = "lunch"; // 默认推荐午餐
                mealTypeName = "午餐";
            }

            // 构建查询条件
            QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_online", true);

            // 根据用餐时间筛选合适的菜品
            if (mealType.equals("breakfast")) {
                // 早餐推荐：主食、汤品、饮品
                queryWrapper.in("category", Arrays.asList("主食", "汤品", "饮品", "小吃"));
            } else if (mealType.equals("lunch")) {
                // 午餐推荐：菜肴、主食
                queryWrapper.in("category", Arrays.asList("菜肴", "主食"));
            } else if (mealType.equals("dinner")) {
                // 晚餐推荐：菜肴、汤品
                queryWrapper.in("category", Arrays.asList("菜肴", "汤品"));
            } else if (mealType.equals("late_night")) {
                // 夜宵推荐：小吃、饮品
                queryWrapper.in("category", Arrays.asList("小吃", "饮品", "甜点"));
            }

            // 综合排序：评分、销量、推荐得分
            queryWrapper.orderByDesc("score", "avg_rating", "order_count");
            queryWrapper.last("LIMIT 8");

            List<Dish> dishes = dishService.list(queryWrapper);

            if (dishes.isEmpty()) {
                return "暂时没有为您推荐合适的菜品，要不看看我们的热门菜品？";
            }

            // 格式化返回结果
            StringBuilder result = new StringBuilder();

            // 根据季节添加推荐理由
            String seasonAdvice = "";
            if (month >= 3 && month <= 5) {
                seasonAdvice = "春季宜清淡，为您推荐以下时令菜品：";
            } else if (month >= 6 && month <= 8) {
                seasonAdvice = "夏季炎热，为您推荐以下清爽菜品：";
            } else if (month >= 9 && month <= 11) {
                seasonAdvice = "秋季进补，为您推荐以下养生菜品：";
            } else {
                seasonAdvice = "冬季寒冷，为您推荐以下温补菜品：";
            }

            result.append(String.format("📅 今日%s推荐\n\n", mealTypeName));
            result.append(seasonAdvice + "\n\n");

            for (int i = 0; i < dishes.size(); i++) {
                Dish dish = dishes.get(i);
                result.append(String.format("%d. **%s**\n", i + 1, dish.getName()));

                if (dish.getPrice() != null) {
                    result.append(String.format("   💰 ￥%.2f", dish.getPrice()));
                }

                if (dish.getCategory() != null) {
                    result.append(String.format("  |  📂 %s", dish.getCategory()));
                }

                result.append("\n");

                // 添加推荐理由
                String reason = getRecommendationReason(dish, mealType);
                result.append(String.format("   💡 %s\n", reason));

                result.append("\n");
            }

            result.append("✨ 这些都是根据当前时间和季节为您精心挑选的！\n");
            result.append("需要我详细介绍某道菜吗？或者直接帮您下单？");

            return result.toString();

        } catch (Exception e) {
            log.error("获取今日推荐失败", e);
            return buildErrorResponse("获取今日推荐时出现错误: " + e.getMessage());
        }
    }

    /**
     * 获取时间场景推荐
     * 根据用餐时间场景（早餐/午餐/晚餐/夜宵）推荐菜品
     */
    @AiFunctionHandler(value = "get_time_recommendations", description = "根据用餐时间场景推荐菜品")
    private String getTimeRecommendations(Map<String, Object> arguments) {
        String mealType = getStringArgument(arguments, "meal_type");

        log.info("获取时间场景推荐 - mealType: {}", mealType);

        // 如果没有指定用餐类型，根据当前时间自动判断
        if (mealType == null || mealType.isEmpty()) {
            int hour = java.time.LocalDateTime.now().getHour();
            if (hour >= 6 && hour < 9) {
                mealType = "breakfast";
            } else if (hour >= 11 && hour < 14) {
                mealType = "lunch";
            } else if (hour >= 17 && hour < 20) {
                mealType = "dinner";
            } else if (hour >= 21 && hour < 23) {
                mealType = "late_night";
            } else {
                mealType = "lunch"; // 默认午餐
            }
        }

        try {
            String mealTypeName;
            List<String> categories;
            String recommendation;
            int limit;

            switch (mealType) {
                case "breakfast":
                    mealTypeName = "早餐";
                    categories = Arrays.asList("主食", "汤品", "饮品", "小吃");
                    recommendation = "早餐要吃得营养，为您推荐以下菜品：";
                    limit = 6;
                    break;

                case "lunch":
                    mealTypeName = "午餐";
                    categories = Arrays.asList("菜肴", "主食");
                    recommendation = "午餐要吃得饱，为您推荐以下菜品：";
                    limit = 10;
                    break;

                case "dinner":
                    mealTypeName = "晚餐";
                    categories = Arrays.asList("菜肴", "汤品");
                    recommendation = "晚餐要吃得丰富，为您推荐以下菜品：";
                    limit = 10;
                    break;

                case "late_night":
                    mealTypeName = "夜宵";
                    categories = Arrays.asList("小吃", "饮品", "甜点");
                    recommendation = "夜宵要吃得清淡，为您推荐以下菜品：";
                    limit = 6;
                    break;

                default:
                    return buildErrorResponse("不支持的用餐类型: " + mealType);
            }

            // 查询菜品
            QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_online", true);
            queryWrapper.in("category", categories);
            queryWrapper.orderByDesc("score", "avg_rating", "order_count");
            queryWrapper.last("LIMIT " + limit);

            List<Dish> dishes = dishService.list(queryWrapper);

            if (dishes.isEmpty()) {
                return String.format("暂时没有合适的%s推荐，要不要看看其他菜品？", mealTypeName);
            }

            // 格式化返回结果
            StringBuilder result = new StringBuilder();
            result.append(String.format("🕐 %s时间到！%s\n\n", mealTypeName, recommendation));

            for (int i = 0; i < dishes.size(); i++) {
                Dish dish = dishes.get(i);
                result.append(String.format("%d. **%s**", i + 1, dish.getName()));

                if (dish.getPrice() != null) {
                    result.append(String.format(" - ￥%.2f", dish.getPrice()));
                }

                result.append("\n");

                // 添加营养信息
                if (dish.getCalorie() != null && dish.getCalorie() > 0) {
                    result.append(String.format("   🔥 %d卡路里", dish.getCalorie()));
                }

                if (dish.getEstimatedCookingMinutes() != null) {
                    result.append(String.format("  |  ⏱️ 预计%s分钟",
                        dish.getEstimatedCookingMinutes()));
                }

                result.append("\n");
            }

            result.append(String.format("\n💡 这些都是适合%s的菜品，需要我帮您推荐搭配吗？", mealTypeName));

            return result.toString();

        } catch (Exception e) {
            log.error("获取时间场景推荐失败", e);
            return buildErrorResponse("获取时间场景推荐时出现错误: " + e.getMessage());
        }
    }

    /**
     * 获取菜品推荐理由
     */
    private String getRecommendationReason(Dish dish, String mealType) {
        StringBuilder reason = new StringBuilder();

        // 基于评分的理由
        if (dish.getAvgRating() != null && dish.getAvgRating().compareTo(new java.math.BigDecimal("4.5")) >= 0) {
            reason.append("评分高达").append(dish.getAvgRating()).append("分，");
        }

        // 基于销量的理由
        if (dish.getOrderCount() != null && dish.getOrderCount() > 100) {
            reason.append("销量超过").append(dish.getOrderCount()).append("份，");
        }

        // 基于用餐时间的理由
        if (mealType.equals("breakfast")) {
            if ("主食".equals(dish.getCategory()) || "汤品".equals(dish.getCategory())) {
                reason.append("营养丰富易消化，");
            }
        } else if (mealType.equals("lunch")) {
            if ("菜肴".equals(dish.getCategory())) {
                reason.append("午餐必备，");
            }
        } else if (mealType.equals("dinner")) {
            reason.append("晚餐佳选，");
        } else if (mealType.equals("late_night")) {
            reason.append("清淡不油腻，");
        }

        // 基于热量的理由
        if (dish.getCalorie() != null) {
            if (dish.getCalorie() < 300) {
                reason.append("低热量健康之选。");
            } else if (dish.getCalorie() > 600) {
                reason.append("能量充足。");
            } else {
                reason.append("营养均衡。");
            }
        } else {
            if (reason.length() > 0) {
                reason.setLength(reason.length() - 1); // 移除最后的逗号
            }
            reason.append("推荐尝试！");
        }

        return reason.toString();
    }

    /**
     * 获取今日热量统计
     * 统计用户今日摄入的卡路里
     */
    @AiFunctionHandler(value = "get_today_calories", description = "统计用户今日摄入的热量")
    private String getTodayCalories(Map<String, Object> arguments) {
        String userId = getStringArgument(arguments, "user_id");
        String date = getStringArgument(arguments, "date");

        // 如果没有指定日期，默认今天
        if (date == null || date.isEmpty()) {
            date = java.time.LocalDate.now().toString();
        }

        log.info("获取今日热量统计 - userId: {}, date: {}", userId, date);

        try {
            // 查询用户当天的订单
            QueryWrapper<Order> orderQuery = new QueryWrapper<>();
            orderQuery.eq("user_id", userId)
                    .eq("status", 5) // 已送达的订单
                    .like("create_time", date)
                    .orderByDesc("create_time");

            List<Order> orders = orderService.list(orderQuery);

            if (orders.isEmpty()) {
                return String.format("📅 %s\n\n今天还没有摄入热量记录哦。\n\n要不要看看我们的菜品，开始今天的健康饮食？", date);
            }

            // 简化版：假设每个订单平均热量为500卡（实际应从订单详情计算）
            int totalCalories = orders.size() * 500;
            int targetCalories = 2000; // 成人每日推荐摄入量

            // 计算百分比
            double percentage = (totalCalories * 100.0) / targetCalories;
            String status;
            if (percentage < 80) {
                status = "摄入不足";
            } else if (percentage <= 120) {
                status = "正常范围";
            } else {
                status = "摄入过量";
            }

            // 格式化返回结果
            StringBuilder result = new StringBuilder();
            result.append(String.format("📅 %s 热量统计\n\n", date));
            result.append(String.format("🔥 今日摄入：%d 卡路里\n", totalCalories));
            result.append(String.format("🎯 推荐摄入：%d 卡路里\n", targetCalories));
            result.append(String.format("📊 完成度：%.1f%%\n", percentage));
            result.append(String.format("✅ 状态：%s\n\n", status));

            if (percentage < 80) {
                result.append(String.format("💡 还可以摄入约%d卡路里的健康食品。\n\n", targetCalories - totalCalories));
                result.append("建议：可以适当增加一些蛋白质和蔬菜的摄入。");
            } else if (percentage > 120) {
                result.append("💡 今日热量摄入已超标。\n\n");
                result.append("建议：注意饮食均衡，适当增加运动消耗多余热量。");
            } else {
                result.append("✨ 热量摄入在正常范围内，请继续保持！");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("获取今日热量统计失败", e);
            return buildErrorResponse("获取今日热量统计时出现错误: " + e.getMessage());
        }
    }

    /**
     * 分析营养摄入
     * 分析用户的营养摄入情况
     */
    @AiFunctionHandler(value = "analyze_nutrition_intake", description = "分析用户的营养摄入情况")
    private String analyzeNutritionIntake(Map<String, Object> arguments) {
        String userId = getStringArgument(arguments, "user_id");
        String date = getStringArgument(arguments, "date");

        // 如果没有指定日期，默认今天
        if (date == null || date.isEmpty()) {
            date = java.time.LocalDate.now().toString();
        }

        log.info("分析营养摄入 - userId: {}, date: {}", userId, date);

        try {
            // 查询用户当天的订单
            QueryWrapper<Order> orderQuery = new QueryWrapper<>();
            orderQuery.eq("user_id", userId)
                    .eq("status", 5)
                    .like("create_time", date);

            List<Order> orders = orderService.list(orderQuery);

            if (orders.isEmpty()) {
                return String.format("📊 %s 营养分析\n\n今天还没有摄入记录，无法分析营养情况。\n\n建议先添加一些订单记录。", date);
            }

            // 简化版营养分析（实际应从菜品详情计算）
            int protein = 65; // 蛋白质（克）
            int carbs = 250; // 碳水（克）
            int fat = 55; // 脂肪（克）

            // 目标值
            int targetProtein = 80;
            int targetCarbs = 300;
            int targetFat = 60;

            // 格式化返回结果
            StringBuilder result = new StringBuilder();
            result.append(String.format("📊 %s 营养摄入分析\n\n", date));
            result.append("📈 营养成分明细：\n\n");

            // 蛋白质
            double proteinPercent = (protein * 100.0) / targetProtein;
            result.append(String.format("🥩 蛋白质：%dg / %dg (%.1f%%) - %s\n",
                protein, targetProtein, proteinPercent,
                proteinPercent < 80 ? "⚠️ 略低" : proteinPercent > 120 ? "⚠️ 偏高" : "✅ 正常"));

            // 碳水化合物
            double carbsPercent = (carbs * 100.0) / targetCarbs;
            result.append(String.format("🍞 碳水化合物：%dg / %dg (%.1f%%) - %s\n",
                carbs, targetCarbs, carbsPercent,
                carbsPercent < 80 ? "⚠️ 略低" : carbsPercent > 120 ? "⚠️ 偏高" : "✅ 正常"));

            // 脂肪
            double fatPercent = (fat * 100.0) / targetFat;
            result.append(String.format("🥑 脂肪：%dg / %dg (%.1f%%) - %s\n\n",
                fat, targetFat, fatPercent,
                fatPercent < 80 ? "⚠️ 略低" : fatPercent > 120 ? "⚠️ 偏高" : "✅ 正常"));

            // 建议
            result.append("💡 营养建议：\n\n");

            if (proteinPercent < 80) {
                result.append("- 蛋白质摄入略低，建议增加豆类、蛋奶或瘦肉的摄入\n");
            }
            if (carbsPercent < 80) {
                result.append("- 碳水化合物摄入略低，可以适当增加主食\n");
            }
            if (fatPercent > 120) {
                result.append("- 脂肪摄入偏高，建议选择低油低脂的菜品\n");
            }

            result.append("\n✨ 保持均衡饮食，享受健康生活！");

            return result.toString();

        } catch (Exception e) {
            log.error("分析营养摄入失败", e);
            return buildErrorResponse("分析营养摄入时出现错误: " + e.getMessage());
        }
    }

    /**
     * 计算BMI
     * 计算用户的身体质量指数
     */
    @AiFunctionHandler(value = "calculate_bmi", description = "计算用户的BMI指数")
    private String calculateBMI(Map<String, Object> arguments) {
        Integer height = getIntegerArgument(arguments, "height");
        Integer weight = getIntegerArgument(arguments, "weight");

        log.info("计算BMI - height: {}, weight: {}", height, weight);

        if (height == null || height <= 0) {
            return buildErrorResponse("请提供有效的身高（厘米）");
        }
        if (weight == null || weight <= 0) {
            return buildErrorResponse("请提供有效的体重（公斤）");
        }

        try {
            // BMI = 体重(kg) / 身高(m)^2
            double heightInMeters = height / 100.0;
            double bmi = weight / (heightInMeters * heightInMeters);

            // 判断BMI等级
            String category;
            String advice;
            String color;

            if (bmi < 18.5) {
                category = "偏瘦";
                color = "⚠️";
                advice = "建议适当增加营养摄入，可以多食用富含蛋白质的食物。";
            } else if (bmi < 24) {
                category = "正常";
                color = "✅";
                advice = "您的体重在健康范围内，请继续保持均衡饮食和适量运动。";
            } else if (bmi < 28) {
                category = "偏胖";
                color = "⚠️";
                advice = "建议控制饮食总热量，增加蔬菜水果摄入，每周运动3-5次。";
            } else {
                category = "肥胖";
                color = "❗";
                advice = "建议咨询营养师制定减重计划，控制饮食并坚持规律运动。";
            }

            // 格式化返回结果
            StringBuilder result = new StringBuilder();
            result.append("📊 BMI健康指数\n\n");
            result.append(String.format("📏 身高：%d 厘米\n", height));
            result.append(String.format("⚖️ 体重：%d 公斤\n\n", weight));
            result.append(String.format("🎯 您的BMI：%.1f\n", bmi));
            result.append(String.format("%s 状态：%s\n\n", color, category));
            result.append("💡 健康建议：\n");
            result.append(String.format("标准范围：18.5 - 24.0\n\n"));
            result.append(advice);

            return result.toString();

        } catch (Exception e) {
            log.error("计算BMI失败", e);
            return buildErrorResponse("计算BMI时出现错误: " + e.getMessage());
        }
    }

    /**
     * 获取健康建议
     * 根据用户情况提供健康饮食建议
     */
    @AiFunctionHandler(value = "get_health_advice", description = "根据用户情况提供健康饮食建议")
    private String getHealthAdvice(Map<String, Object> arguments) {
        String userId = getStringArgument(arguments, "user_id");

        log.info("获取健康建议 - userId: {}", userId);

        try {
            // 获取当前时间
            int hour = java.time.LocalDateTime.now().getHour();
            int month = java.time.LocalDateTime.now().getMonthValue();

            StringBuilder result = new StringBuilder();
            result.append("🏥 健康饮食建议\n\n");

            // 基于时间的建议
            result.append("📅 时段建议：\n");
            if (hour >= 6 && hour < 9) {
                result.append("- 早餐：建议选择高蛋白、高纤维食物，如鸡蛋、牛奶、全麦面包\n");
                result.append("- 避免空腹喝咖啡或浓茶\n");
            } else if (hour >= 11 && hour < 14) {
                result.append("- 午餐：建议主食+蔬菜+蛋白质搭配\n");
                result.append("- 细嚼慢咽，控制食量，七八分饱即可\n");
            } else if (hour >= 17 && hour < 20) {
                result.append("- 晚餐：建议清淡为主，避免油腻\n");
                result.append("- 晚餐后2小时内不要立即入睡\n");
            } else {
                result.append("- 夜宵：尽量避免，如需进食选择易消化食物\n");
                result.append("- 控制份量，避免增加肠胃负担\n");
            }

            result.append("\n🌡️ 季节建议：\n");
            if (month >= 3 && month <= 5) {
                result.append("- 春季：多食用时令蔬菜，如韭菜、菠菜、春笋\n");
                result.append("- 注意养肝护肝，少吃酸味多吃甜\n");
            } else if (month >= 6 && month <= 8) {
                result.append("- 夏季：多饮水，多吃清热解暑食物，如绿豆、苦瓜\n");
                result.append("- 避免过度贪凉，少食生冷\n");
            } else if (month >= 9 && month <= 11) {
                result.append("- 秋季：润燥为主，多食梨、银耳、蜂蜜\n");
                result.append("- 适当进补，增强体质\n");
            } else {
                result.append("- 冬季：温补阳气，多食羊肉、牛肉、核桃\n");
                result.append("- 注意保暖，适度进补\n");
            }

            result.append("\n🍽️ 饮食原则：\n");
            result.append("1. 食物多样化，每天摄入12种以上食物\n");
            result.append("2. 三餐规律，定时定量\n");
            result.append("3. 控制油盐糖，清淡饮食\n");
            result.append("4. 适量运动，保持健康体重\n");
            result.append("5. 充足饮水，每天1500-1700ml\n");

            result.append("\n✨ 希望这些建议对您有帮助！");

            return result.toString();

        } catch (Exception e) {
            log.error("获取健康建议失败", e);
            return buildErrorResponse("获取健康建议时出现错误: " + e.getMessage());
        }
    }

    // ==================== 第四阶段：用户服务功能 ====================

    /**
     * 查看收藏列表
     *
     * @param arguments 参数（无需参数，系统自动注入user_id）
     * @return 收藏列表
     */
    @AiFunctionHandler(value = "get_favorites", description = "查看用户的收藏列表")
    private String getFavorites(Map<String, Object> arguments) {
        // 从arguments中获取user_id（系统自动注入）
        String userId = getStringArgument(arguments, "user_id");

        try {
            log.info("查看收藏列表，用户ID: {}", userId);

            // 使用Service方法查询用户的菜品收藏
            List<com.xx.jaseatschoicejava.entity.UserCollection> collections =
                collectionService.getCollectionsByUserIdAndType(userId, "dish");

            if (collections == null || collections.isEmpty()) {
                return "💝 您还没有收藏任何菜品哦。\n\n" +
                       "在浏览菜品时，点击收藏按钮就可以将喜欢的菜品添加到收藏列表啦！";
            }

            StringBuilder result = new StringBuilder();
            result.append("## 💝 您的收藏列表\n\n");
            result.append("共 **").append(collections.size()).append("** 个菜品\n\n");
            result.append("---\n\n");

            int count = 0;
            for (com.xx.jaseatschoicejava.entity.UserCollection collection : collections) {
                // 根据collectableId查询菜品详情
                Dish dish = dishService.getById(collection.getCollectableId());

                if (dish != null) {
                    count++;
                    result.append("### ").append(count).append(". ").append(dish.getName()).append("\n\n");
                    result.append("- 💰 **价格**：￥").append(dish.getPrice()).append("\n");
                    result.append("- 📂 **分类**：").append(dish.getCategory()).append("\n");
                    result.append("- ⭐ **评分**：").append(dish.getAvgRating()).append(" 分\n");
                    result.append("- 🕒 **收藏时间**：").append(collection.getCreateTime()).append("\n");
                    result.append("\n---\n\n");
                }
            }

            if (count == 0) {
                return "💝 您收藏的菜品暂时都不在了。\n\n" +
                       "可能已被商家下架，去看看其他菜品吧！";
            }

            result.append("💡 **提示**：需要详细介绍某道菜，或者直接下单吗？");

            return result.toString();

        } catch (Exception e) {
            log.error("查看收藏列表失败", e);
            return buildErrorResponse("查看收藏列表时出现错误: " + e.getMessage());
        }
    }

    /**
     * 添加收藏
     *
     * @param arguments 参数 {dish_id: 菜品ID, dish_name: 菜品名称（可选），系统自动注入user_id}
     * @return 添加结果
     */
    @AiFunctionHandler(value = "add_favorite", description = "添加菜品到收藏")
    private String addFavorite(Map<String, Object> arguments) {
        // 从arguments中获取user_id（系统自动注入）
        String userId = getStringArgument(arguments, "user_id");
        String dishId = (String) arguments.get("dish_id");
        String dishName = (String) arguments.get("dish_name");

        try {
            log.info("添加收藏，用户ID: {}, 菜品ID: {}, 菜品名称: {}",
                     userId, dishId, dishName);

            // 参数验证
            if (dishId == null || dishId.trim().isEmpty()) {
                return buildErrorResponse("缺少必需参数：菜品ID");
            }

            // 检查菜品是否存在
            Dish dish = dishService.getById(dishId);
            if (dish == null) {
                return buildErrorResponse("菜品不存在，请检查菜品ID是否正确");
            }

            // 检查是否已收藏
            boolean isCollected = collectionService.isCollected(userId, "dish", dishId);
            if (isCollected) {
                String name = dishName != null ? dishName : dish.getName();
                return "⚠️ 您已经收藏过「" + name + "」了，无需重复收藏哦。\n\n" +
                       "💝 在收藏列表中可以查看所有已收藏的菜品。";
            }

            // 创建收藏记录
            com.xx.jaseatschoicejava.entity.UserCollection collection =
                new com.xx.jaseatschoicejava.entity.UserCollection();
            collection.setUserId(userId);
            collection.setCollectableType("dish");
            collection.setCollectableId(dishId);
            collection.setCreateTime(java.time.LocalDateTime.now());

            com.xx.jaseatschoicejava.entity.UserCollection result =
                collectionService.addCollection(collection);

            if (result != null && result.getId() != null) {
                String name = dishName != null ? dishName : dish.getName();
                return "✅ 已成功将「" + name + "」添加到收藏！\n\n" +
                       "💝 您可以在收藏列表中查看所有收藏的菜品。";
            } else {
                return buildErrorResponse("添加收藏失败，请稍后重试");
            }

        } catch (Exception e) {
            log.error("添加收藏失败", e);
            return buildErrorResponse("添加收藏时出现错误: " + e.getMessage());
        }
    }

    /**
     * 获取用户评价列表
     *
     * @param arguments 参数 {limit: 返回数量限制（可选，默认20）}
     * @param userId 用户ID
     * @return 评价列表
     */
    @AiFunctionHandler(value = "get_user_reviews", description = "获取用户的评价列表")
    private String getUserReviews(Map<String, Object> arguments) {
        // 从arguments中获取user_id（系统自动注入）
        String userId = getStringArgument(arguments, "user_id");

        try {
            // 获取限制参数，默认20
            int limit = 20;
            if (arguments.containsKey("limit")) {
                try {
                    limit = Math.min(50, Math.max(1, ((Number) arguments.get("limit")).intValue()));
                } catch (Exception e) {
                    log.warn("解析limit参数失败，使用默认值20");
                }
            }

            log.info("获取用户评价列表，用户ID: {}, 限制: {}", userId, limit);

            // 查询用户的评价记录
            QueryWrapper<Review> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .eq("status", 0)  // 正常状态
                       .orderByDesc("create_time");

            List<Review> reviews = reviewService.list(queryWrapper);

            if (reviews == null || reviews.isEmpty()) {
                return "📝 您还没有发表过任何评价。\n\n" +
                       "完成订单后，可以对菜品和商家进行评价，分享您的用餐体验！";
            }

            // 限制返回数量
            List<Review> limitedReviews = reviews.stream()
                    .limit(limit)
                    .collect(java.util.stream.Collectors.toList());

            StringBuilder result = new StringBuilder();
            result.append("## 📝 您的评价列表\n\n");
            result.append("共 **").append(reviews.size()).append("**")
                  .append(" 条评价")
                  .append(limit < reviews.size() ? "，显示最近 **" + limit + "** 条" : "")
                  .append("\n\n");
            result.append("---\n\n");

            int count = 0;
            for (Review review : limitedReviews) {
                count++;

                // 获取菜品或商家信息
                String targetName = "";
                if (review.getDishId() != null && !review.getDishId().isEmpty()) {
                    Dish dish = dishService.getById(review.getDishId());
                    targetName = dish != null ? dish.getName() : "未知菜品";
                } else {
                    targetName = "商家评价";
                }

                // 生成星级
                StringBuilder stars = new StringBuilder();
                for (int i = 0; i < 5; i++) {
                    stars.append(i < review.getRating() ? "⭐" : "☆");
                }

                result.append("### ").append(count).append(". ").append(targetName).append("\n\n");
                result.append("- **评分**：").append(stars).append("\n");

                if (review.getContent() != null && !review.getContent().isEmpty()) {
                    result.append("- **评价**：").append(review.getContent()).append("\n");
                }

                result.append("- **时间**：").append(review.getCreateTime()).append("\n");
                result.append("\n---\n\n");
            }

            result.append("💡 **提示**：您可以在订单详情中查看和修改评价。");

            return result.toString();

        } catch (Exception e) {
            log.error("获取用户评价列表失败", e);
            return buildErrorResponse("获取用户评价列表时出现错误: " + e.getMessage());
        }
    }

    /**
     * 获取用户优惠券列表
     *
     * @param arguments 参数（无需参数）
     * @param userId 用户ID
     * @return 优惠券列表
     */
    @AiFunctionHandler(value = "get_user_coupons", description = "获取用户的可用优惠券")
    private String getUserCoupons(Map<String, Object> arguments) {
        // 从arguments中获取user_id（系统自动注入）
        String userId = getStringArgument(arguments, "user_id");

        try {
            log.info("获取用户优惠券列表，用户ID: {}", userId);

            // 查询用户的优惠券
            QueryWrapper<UserCoupon> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .in("status", Arrays.asList("available", "used", "expired"))
                       .orderByDesc("create_time");

            List<UserCoupon> coupons = userCouponService.list(queryWrapper);

            if (coupons == null || coupons.isEmpty()) {
                return "🎫 您暂时还没有优惠券。\n\n" +
                       "💡 多关注平台活动，有机会获得优惠券哦！";
            }

            // 分类统计
            long availableCount = coupons.stream()
                    .filter(c -> "available".equals(c.getStatus()))
                    .count();
            long usedCount = coupons.stream()
                    .filter(c -> "used".equals(c.getStatus()))
                    .count();
            long expiredCount = coupons.stream()
                    .filter(c -> "expired".equals(c.getStatus()))
                    .count();

            StringBuilder result = new StringBuilder();

            // 标题和总数
            result.append("## 🎫 您的优惠券\n\n");
            result.append("共 **").append(coupons.size()).append("** 张优惠券\n\n");

            // 统计信息
            result.append("### 📊 统计\n\n");
            result.append("- ✅ **可用**：").append(availableCount).append(" 张\n");
            result.append("- 📝 **已使用**：").append(usedCount).append(" 张\n");
            result.append("- ⏰ **已过期**：").append(expiredCount).append(" 张\n");
            result.append("\n---\n\n");

            // 显示可用优惠券
            List<UserCoupon> availableCoupons = coupons.stream()
                    .filter(c -> "available".equals(c.getStatus()))
                    .collect(java.util.stream.Collectors.toList());

            if (!availableCoupons.isEmpty()) {
                result.append("## ✅ 可用优惠券\n\n");
                result.append("共 **").append(availableCoupons.size()).append("** 张\n\n");
                result.append("---\n\n");

                int count = 0;
                for (UserCoupon coupon : availableCoupons) {
                    count++;
                    result.append("### ").append(count).append(". ").append(coupon.getName()).append("\n\n");
                    result.append("- 💰 **优惠金额**：￥").append(coupon.getAmount()).append("\n");

                    if (coupon.getMinAmount() != null && coupon.getMinAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                        result.append("- 📌 **满减条件**：订单满 ￥").append(coupon.getMinAmount()).append(" 可用\n");
                    }

                    if (coupon.getExpireTime() != null) {
                        result.append("- ⏰ **有效期至**：").append(coupon.getExpireTime()).append("\n");
                    }

                    result.append("\n---\n\n");
                }

                result.append("💡 **提示**：下单时系统会自动使用最优优惠券哦！\n");
            } else {
                result.append("## 😔 暂无可用优惠券\n\n");
                result.append("💡 多关注平台活动，有机会获得优惠券哦！\n");
            }

            // 显示即将过期的优惠券
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime threeDaysLater = now.plusDays(3);

            List<UserCoupon> expiringSoon = availableCoupons.stream()
                    .filter(c -> c.getExpireTime() != null
                            && c.getExpireTime().isBefore(threeDaysLater)
                            && c.getExpireTime().isAfter(now))
                    .collect(java.util.stream.Collectors.toList());

            if (!expiringSoon.isEmpty()) {
                result.append("\n---\n\n");
                result.append("## ⚠️ 即将过期提醒\n\n");

                for (UserCoupon coupon : expiringSoon) {
                    result.append("- 「").append(coupon.getName()).append("」")
                          .append(" 将于 **").append(coupon.getExpireTime()).append("** 过期\n");
                }

                result.append("\n💡 **温馨提示**：赶紧使用吧，不要浪费了！\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("获取用户优惠券列表失败", e);
            return buildErrorResponse("获取用户优惠券列表时出现错误: " + e.getMessage());
        }
    }

    /**
     * 获取用户信息
     *
     * @param arguments 参数（无需参数，系统自动注入user_id）
     * @return 用户信息
     */
    @AiFunctionHandler(value = "get_user_info", description = "获取用户的详细信息和档案")
    private String getUserInfo(Map<String, Object> arguments) {
        // 从arguments中获取user_id（系统自动注入）
        String userId = getStringArgument(arguments, "user_id");

        try {
            log.info("获取用户信息，用户ID: {}", userId);

            // 查询用户基本信息
            User user = userService.getById(userId);

            if (user == null) {
                return buildErrorResponse("未找到用户信息");
            }

            StringBuilder result = new StringBuilder();
            result.append("👤 **用户档案**\n\n");

            // ========== 基本信息 ==========
            result.append("📋 **基本信息**\n");

            if (user.getNickname() != null && !user.getNickname().isEmpty()) {
                result.append("   昵称：").append(user.getNickname()).append("\n");
            }

            // 手机号脱敏显示
            if (user.getPhone() != null) {
                String maskedPhone = user.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
                result.append("   手机：").append(maskedPhone).append("\n");
            }

            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                result.append("   邮箱：").append(user.getEmail()).append("\n");
            }

            if (user.getLocation() != null && !user.getLocation().isEmpty()) {
                result.append("   地区：").append(user.getLocation()).append("\n");
            }

            if (user.getGender() != null && !user.getGender().isEmpty()) {
                String genderText = "male".equals(user.getGender()) ? "男" :
                                   "female".equals(user.getGender()) ? "女" : "其他";
                result.append("   性别：").append(genderText).append("\n");
            }

            if (user.getBio() != null && !user.getBio().isEmpty()) {
                result.append("   简介：").append(user.getBio()).append("\n");
            }

            // 注册时间
            if (user.getCreateTime() != null) {
                result.append("   注册时间：").append(user.getCreateTime()).append("\n");
            }

            result.append("\n");

            // ========== 身体数据 ==========
            result.append("💪 **身体数据**\n");

            boolean hasBodyData = false;

            if (user.getHeight() != null && user.getHeight() > 0) {
                result.append("   身高：").append(user.getHeight()).append(" cm\n");
                hasBodyData = true;
            }

            if (user.getWeight() != null && user.getWeight() > 0) {
                result.append("   体重：").append(user.getWeight()).append(" kg\n");
                hasBodyData = true;
            }

            // 计算BMI
            if (user.getHeight() != null && user.getHeight() > 0
                    && user.getWeight() != null && user.getWeight() > 0) {
                double heightInMeters = user.getHeight() / 100.0;
                double bmi = user.getWeight() / (heightInMeters * heightInMeters);
                result.append("   BMI：").append(String.format("%.1f", bmi));

                // BMI健康提示
                if (bmi < 18.5) {
                    result.append(" （偏瘦）\n");
                } else if (bmi < 24) {
                    result.append(" （正常）✅\n");
                } else if (bmi < 28) {
                    result.append(" （偏胖）\n");
                } else {
                    result.append(" （肥胖）⚠️\n");
                }

                hasBodyData = true;
            }

            if (!hasBodyData) {
                result.append("   暂无身体数据\n");
            }

            result.append("\n");

            // ========== 饮食偏好 ==========
            result.append("🍽️ **饮食偏好**\n");

            boolean hasDietPreference = false;

            if (user.getDietGoal() != null && !user.getDietGoal().isEmpty()) {
                result.append("   饮食目标：").append(user.getDietGoal()).append("\n");
                hasDietPreference = true;
            }

            // 过敏信息
            if (user.getAllergies() != null && user.getAllergies().has("allergies")) {
                try {
                    com.fasterxml.jackson.databind.JsonNode allergiesNode = user.getAllergies().get("allergies");
                    if (allergiesNode.isArray() && allergiesNode.size() > 0) {
                        result.append("   过敏食材：");
                        StringBuilder allergies = new StringBuilder();
                        for (com.fasterxml.jackson.databind.JsonNode item : allergiesNode) {
                            if (allergies.length() > 0) {
                                allergies.append("、");
                            }
                            allergies.append(item.asText());
                        }
                        result.append(allergies).append("\n");
                        hasDietPreference = true;
                    }
                } catch (Exception e) {
                    log.debug("解析过敏信息失败", e);
                }
            }

            // 偏好标签
            if (user.getPreferTags() != null) {
                try {
                    if (user.getPreferTags().isArray() && user.getPreferTags().size() > 0) {
                        result.append("   偏好标签：");
                        StringBuilder tags = new StringBuilder();
                        for (com.fasterxml.jackson.databind.JsonNode item : user.getPreferTags()) {
                            if (tags.length() > 0) {
                                tags.append("、");
                            }
                            tags.append(item.asText());
                        }
                        result.append(tags).append("\n");
                        hasDietPreference = true;
                    }
                } catch (Exception e) {
                    log.debug("解析偏好标签失败", e);
                }
            }

            if (!hasDietPreference) {
                result.append("   暂无饮食偏好设置\n");
            }

            result.append("\n");

            // ========== 账户状态 ==========
            result.append("🔐 **账户状态**\n");
            result.append("   支付密码：").append(user.getHasPaymentPassword() != null && user.getHasPaymentPassword() ? "已设置 ✅" : "未设置 ⚠️").append("\n");
            result.append("   商家账号：").append(user.getMerchantId() != null && !user.getMerchantId().isEmpty() ? "已开通 👔" : "未开通").append("\n");

            result.append("\n");

            // ========== 温馨提示 ==========
            result.append("💡 **温馨提示**\n");
            result.append("   • 完善身体数据，我可以为您提供更精准的营养建议\n");
            result.append("   • 设置饮食偏好和过敏信息，帮您避开不适合的食物\n");
            result.append("   • 设置支付密码，让您的支付更安全\n\n");

            result.append("✨ 需要帮您更新这些信息，或者有其他问题吗？");

            return result.toString();

        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return buildErrorResponse("获取用户信息时出现错误: " + e.getMessage());
        }
    }

    /**
     * 查询通知列表
     */
    @AiFunctionHandler(value = "list_notifications", description = "查询用户的通知消息列表")
    private String listNotifications(Map<String, Object> arguments) {
        // 从arguments中获取user_id（系统自动注入）
        String userId = getStringArgument(arguments, "user_id");

        try {
            log.info("查询通知列表，用户ID: {}", userId);

            // 查询通知列表（按发送时间倒序，最多20条）
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Notification> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .orderByDesc("send_time")
                    .last("LIMIT 20");

            java.util.List<Notification> notifications = notificationService.list(queryWrapper);

            if (notifications == null || notifications.isEmpty()) {
                return "📭 **暂无通知**\n\n您目前没有收到任何通知消息。";
            }

            // 统计未读数量
            long unreadCount = notifications.stream()
                    .filter(n -> n.getReadStatus() != null && !n.getReadStatus())
                    .count();

            StringBuilder result = new StringBuilder();
            result.append("📬 **通知消息**\n\n");
            result.append(String.format("共收到 %d 条通知", notifications.size()));
            if (unreadCount > 0) {
                result.append(String.format("，其中 %d 条未读 🔴", unreadCount));
            }
            result.append("\n\n");

            // 按类型分组统计
            Map<String, Long> typeCount = notifications.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            n -> n.getType() != null ? n.getType() : "system",
                            java.util.stream.Collectors.counting()
                    ));

            result.append("**通知类型统计：**\n");
            if (typeCount.containsKey("order")) {
                result.append("  • 订单消息：").append(typeCount.get("order")).append(" 条\n");
            }
            if (typeCount.containsKey("system")) {
                result.append("  • 系统通知：").append(typeCount.get("system")).append(" 条\n");
            }
            if (typeCount.containsKey("promotion")) {
                result.append("  • 优惠活动：").append(typeCount.get("promotion")).append(" 条\n");
            }

            result.append("\n**最近通知：**\n");

            // 显示前5条通知详情
            int displayCount = Math.min(5, notifications.size());
            for (int i = 0; i < displayCount; i++) {
                Notification notification = notifications.get(i);

                // 未读标记
                String readFlag = (notification.getReadStatus() != null && !notification.getReadStatus()) ? "🔴 " : "";

                // 类型图标
                String typeIcon = "📢";
                if ("order".equals(notification.getType())) {
                    typeIcon = "📦";
                } else if ("promotion".equals(notification.getType())) {
                    typeIcon = "🎁";
                }

                result.append(String.format("\n%d. %s%s **%s**\n",
                        i + 1, readFlag, typeIcon, notification.getTitle()));

                // 内容摘要（最多100字）
                String content = notification.getContent();
                if (content != null && content.length() > 100) {
                    content = content.substring(0, 100) + "...";
                }
                if (content != null && !content.isEmpty()) {
                    result.append("   ").append(content).append("\n");
                }

                // 时间
                if (notification.getSendTime() != null) {
                    result.append("   ").append(formatNotificationTime(notification.getSendTime())).append("\n");
                }
            }

            if (notifications.size() > 5) {
                result.append(String.format("\n... 还有 %d 条更早的通知\n", notifications.size() - 5));
            }

            result.append("\n💡 **温馨提示：**\n");
            result.append("   • 及时查看未读通知，避免错过重要信息\n");
            result.append("   • 订单状态变化会通过通知提醒您\n");

            return result.toString();

        } catch (Exception e) {
            log.error("查询通知列表失败", e);
            return buildErrorResponse("查询通知时出现错误: " + e.getMessage());
        }
    }

    /**
     * 格式化通知时间显示
     */
    private String formatNotificationTime(java.time.LocalDateTime sendTime) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        long hours = java.time.Duration.between(sendTime, now).toHours();

        if (hours < 1) {
            long minutes = java.time.Duration.between(sendTime, now).toMinutes();
            return minutes + "分钟前";
        } else if (hours < 24) {
            return hours + "小时前";
        } else if (hours < 24 * 7) {
            long days = hours / 24;
            return days + "天前";
        } else {
            return sendTime.toLocalDate().toString();
        }
    }
}
