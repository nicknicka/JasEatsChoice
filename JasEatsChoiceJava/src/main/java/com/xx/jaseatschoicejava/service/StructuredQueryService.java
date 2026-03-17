package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.jaseatschoicejava.entity.*;
import com.xx.jaseatschoicejava.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 结构化查询服务
 * 处理前端的结构化查询请求，返回卡片格式的数据
 *
 * @author Claude
 * @since 2026-03-15
 */
@Slf4j
@Service
public class StructuredQueryService {

    @Resource
    private OrderService orderService;

    @Resource
    private CollectionService collectionService;

    @Resource
    private ReviewService reviewService;

    @Resource
    private UserCouponService userCouponService;

    @Resource
    private UserService userService;

    @Resource
    private DishService dishService;

    /**
     * 处理结构化查询
     *
     * @param queryType 查询类型
     * @param params    查询参数
     * @param userId    用户ID
     * @return 卡片数据
     */
    public Map<String, Object> handleQuery(String queryType, Map<String, Object> params, String userId) {
        log.info("处理结构化查询：type={}, userId={}, params={}", queryType, userId, params);

        try {
            switch (queryType) {
                case "order_list":
                    return getOrderListCard(userId, params);
                case "favorite_list":
                    return getFavoriteListCard(userId, params);
                case "review_list":
                    return getReviewListCard(userId, params);
                case "coupon_list":
                    return getCouponListCard(userId, params);
                case "user_info":
                    return getUserInfoCard(userId, params);
                case "dish_list":
                    return getDishListCard(userId, params);
                default:
                    throw new IllegalArgumentException("未知的查询类型：" + queryType);
            }
        } catch (Exception e) {
            log.error("处理查询失败：type=" + queryType, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("messageType", "error_card");
            errorResult.put("summary", "查询失败");
            errorResult.put("data", Map.of(
                "error", e.getMessage(),
                "queryType", queryType
            ));
            return errorResult;
        }
    }

    /**
     * 获取订单列表卡片数据
     */
    private Map<String, Object> getOrderListCard(String userId, Map<String, Object> params) {
        log.info("获取订单列表，用户ID：{}", userId);

        // 查询订单列表
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("create_time");

        List<Order> orders = orderService.list(queryWrapper);

        // 统计待处理订单
        long pendingCount = orders.stream()
                .filter(o -> o.getStatus() != null && o.getStatus() <= 1)
                .count();

        // 构建订单数据
        List<Map<String, Object>> orderData = orders.stream()
                .map(this::buildOrderCardData)
                .collect(Collectors.toList());

        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("messageType", "order_list_card");
        data.put("summary", "找到 " + orders.size() + " 个订单");
        data.put("data", Map.of(
            "total", orders.size(),
            "pendingCount", (int) pendingCount,
            "orders", orderData
        ));

        return data;
    }

    /**
     * 构建单个订单的卡片数据
     */
    private Map<String, Object> buildOrderCardData(Order order) {
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("orderId", order.getId());
        orderMap.put("status", order.getStatus());
        orderMap.put("statusText", getStatusText(order.getStatus()));

        // 订单暂时不显示菜品详情，因为Order表没有存储菜品列表
        orderMap.put("dishCount", 0);
        orderMap.put("dishNames", Collections.singletonList("查看详情获取菜品信息"));

        orderMap.put("totalAmount", order.getTotalAmount());
        orderMap.put("createTime", formatTime(order.getCreateTime()));

        // 判断操作权限
        boolean canCancel = canCancelOrder(order);
        boolean canUrge = canUrgeOrder(order);
        orderMap.put("canCancel", canCancel);
        orderMap.put("canUrge", canUrge);

        // 构建操作按钮
        List<Map<String, String>> actions = new ArrayList<>();
        actions.add(Map.of("type", "detail", "text", "查看详情", "icon", "View"));
        if (canCancel) {
            actions.add(Map.of("type", "cancel", "text", "取消订单", "icon", "Delete"));
        }
        if (canUrge) {
            actions.add(Map.of("type", "urge", "text", "催单", "icon", "Bell"));
        }
        orderMap.put("actions", actions);

        return orderMap;
    }

    /**
     * 获取订单状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知状态";
        }
        Map<Integer, String> statusMap = Map.of(
            0, "待支付",
            1, "待接单",
            2, "备菜中",
            3, "烹饪中",
            4, "待上菜",
            5, "已送达",
            6, "已取消",
            7, "待评价",
            8, "已评价"
        );
        return statusMap.getOrDefault(status, "未知状态");
    }

    /**
     * 判断订单是否可以取消
     */
    private boolean canCancelOrder(Order order) {
        Integer status = order.getStatus();
        return status != null && (status == 0 || status == 1);
    }

    /**
     * 判断订单是否可以催单
     */
    private boolean canUrgeOrder(Order order) {
        Integer status = order.getStatus();
        return status != null && status == 2;
    }

    /**
     * 获取收藏列表卡片数据
     */
    private Map<String, Object> getFavoriteListCard(String userId, Map<String, Object> params) {
        log.info("获取收藏列表，用户ID：{}", userId);

        // 查询收藏列表
        List<com.xx.jaseatschoicejava.entity.UserCollection> collections =
                collectionService.getCollectionsByUserIdAndType(userId, "dish");

        if (collections == null || collections.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("messageType", "favorite_list_card");
            data.put("summary", "您还没有收藏任何菜品");
            data.put("data", Map.of(
                "total", 0,
                "favorites", Collections.emptyList()
            ));
            return data;
        }

        // 构建收藏数据
        List<Map<String, Object>> favoriteData = collections.stream()
                .map(collection -> {
                    Dish dish = dishService.getById(collection.getCollectableId());
                    if (dish == null) return null;

                    Map<String, Object> favMap = new HashMap<>();
                    favMap.put("dishId", dish.getId());
                    favMap.put("dishName", dish.getName());
                    favMap.put("imageUrl", dish.getImage());
                    favMap.put("price", dish.getPrice());
                    favMap.put("rating", dish.getAvgRating());
                    favMap.put("salesCount", 0);
                    favMap.put("collectionTime", formatTime(collection.getCreateTime()));

                    // 标签
                    List<String> tags = new ArrayList<>();
                    if (dish.getCategory() != null) {
                        tags.add(dish.getCategory());
                    }
                    favMap.put("tags", tags);

                    // 操作按钮
                    List<Map<String, String>> actions = new ArrayList<>();
                    actions.add(Map.of("type", "add_to_cart", "text", "加入购物车", "icon", "ShoppingCart"));
                    actions.add(Map.of("type", "remove_favorite", "text", "取消收藏", "icon", "Delete"));
                    favMap.put("actions", actions);

                    return favMap;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("messageType", "favorite_list_card");
        data.put("summary", "您收藏了 " + favoriteData.size() + " 个菜品");
        data.put("data", Map.of(
            "total", favoriteData.size(),
            "favorites", favoriteData
        ));

        return data;
    }

    /**
     * 获取评价列表卡片数据
     */
    private Map<String, Object> getReviewListCard(String userId, Map<String, Object> params) {
        log.info("获取评价列表，用户ID：{}", userId);

        // 查询评价列表
        QueryWrapper<Review> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("create_time");

        List<Review> reviews = reviewService.list(queryWrapper);

        // 计算平均评分
        double avgRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // 构建评价数据
        List<Map<String, Object>> reviewData = reviews.stream()
                .map(review -> {
                    Map<String, Object> reviewMap = new HashMap<>();
                    reviewMap.put("reviewId", review.getId());
                    reviewMap.put("orderId", review.getOrderId());

                    // 获取菜品信息
                    Dish dish = dishService.getById(review.getDishId());
                    if (dish != null) {
                        reviewMap.put("dishName", dish.getName());
                        reviewMap.put("dishImage", dish.getImage());
                    }

                    reviewMap.put("rating", review.getRating());
                    reviewMap.put("content", review.getContent());
                    reviewMap.put("images", parseImagesJson(review.getImages()));
                    reviewMap.put("createTime", formatTime(review.getCreateTime()));
                    reviewMap.put("merchantReply", "");

                    // 操作按钮
                    List<Map<String, String>> actions = new ArrayList<>();
                    actions.add(Map.of("type", "view_detail", "text", "查看详情", "icon", "View"));
                    actions.add(Map.of("type", "delete", "text", "删除评价", "icon", "Delete"));
                    reviewMap.put("actions", actions);

                    return reviewMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("messageType", "review_list_card");
        data.put("summary", "您发布了 " + reviewData.size() + " 条评价");
        data.put("data", Map.of(
            "total", reviewData.size(),
            "avgRating", String.format("%.1f", avgRating),
            "reviews", reviewData
        ));

        return data;
    }

    /**
     * 获取优惠券列表卡片数据
     */
    private Map<String, Object> getCouponListCard(String userId, Map<String, Object> params) {
        log.info("获取优惠券列表，用户ID：{}", userId);

        // 查询优惠券
        QueryWrapper<UserCoupon> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .in("status", Arrays.asList("available", "used", "expired"))
                .orderByDesc("create_time");

        List<UserCoupon> coupons = userCouponService.list(queryWrapper);

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

        // 构建优惠券数据
        List<Map<String, Object>> couponData = coupons.stream()
                .map(coupon -> {
                    Map<String, Object> couponMap = new HashMap<>();
                    couponMap.put("couponId", coupon.getId());
                    couponMap.put("name", coupon.getName());
                    couponMap.put("amount", coupon.getAmount());
                    couponMap.put("minAmount", coupon.getMinAmount());
                    couponMap.put("status", coupon.getStatus());
                    couponMap.put("expireTime", coupon.getExpireTime() != null ?
                            coupon.getExpireTime().toString() : null);
                    return couponMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("messageType", "coupon_list_card");
        data.put("summary", "您有 " + availableCount + " 张可用优惠券");
        data.put("data", Map.of(
            "total", coupons.size(),
            "availableCount", (int) availableCount,
            "usedCount", (int) usedCount,
            "expiredCount", (int) expiredCount,
            "coupons", couponData
        ));

        return data;
    }

    /**
     * 获取用户信息卡片数据
     */
    private Map<String, Object> getUserInfoCard(String userId, Map<String, Object> params) {
        log.info("获取用户信息，用户ID：{}", userId);

        User user = userService.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 构建基本信息
        Map<String, Object> basicInfo = new HashMap<>();
        basicInfo.put("nickname", user.getNickname());
        basicInfo.put("phone", maskPhone(user.getPhone()));
        basicInfo.put("email", user.getEmail());
        basicInfo.put("location", user.getLocation());
        basicInfo.put("gender", user.getGender());
        basicInfo.put("avatar", user.getAvatar());
        basicInfo.put("registerTime", user.getCreateTime() != null ?
                user.getCreateTime().toLocalDate().toString() : null);

        // 构建身体数据
        Map<String, Object> bodyData = new HashMap<>();
        bodyData.put("height", user.getHeight());
        bodyData.put("weight", user.getWeight());

        if (user.getHeight() != null && user.getHeight() > 0
                && user.getWeight() != null && user.getWeight() > 0) {
            double bmi = calculateBMI(user.getHeight(), user.getWeight());
            bodyData.put("bmi", String.format("%.1f", bmi));
            bodyData.put("bmiStatus", getBMIStatus(bmi));
            bodyData.put("bmiText", getBMIStatusText(bmi));
        }

        // 构建饮食偏好
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("dietGoal", user.getDietGoal());

        if (user.getAllergies() != null) {
            List<String> allergies = extractJsonArray(user.getAllergies().toString());
            preferences.put("allergies", allergies);
        }

        if (user.getPreferTags() != null) {
            List<String> tags = extractJsonArray(user.getPreferTags().toString());
            preferences.put("tags", tags);
        }

        // 账户状态
        Map<String, Object> accountStatus = new HashMap<>();
        accountStatus.put("hasPaymentPassword", Boolean.TRUE.equals(user.getHasPaymentPassword()));
        accountStatus.put("isMerchant", user.getMerchantId() != null && !user.getMerchantId().isEmpty());

        // 操作按钮
        List<Map<String, String>> actions = new ArrayList<>();
        actions.add(Map.of("type", "edit_profile", "text", "编辑资料", "icon", "Edit"));
        actions.add(Map.of("type", "view_health", "text", "健康分析", "icon", "TrendCharts"));

        Map<String, Object> data = new HashMap<>();
        data.put("messageType", "user_info_card");
        data.put("summary", "这是您的个人信息档案");
        data.put("data", Map.of(
            "basicInfo", basicInfo,
            "bodyData", bodyData,
            "preferences", preferences,
            "accountStatus", accountStatus,
            "actions", actions
        ));

        return data;
    }

    /**
     * 获取菜品列表卡片数据
     */
    private Map<String, Object> getDishListCard(String userId, Map<String, Object> params) {
        log.info("获取菜品列表，用户ID：{}，参数：{}", userId, params);

        // 从参数中提取查询条件
        String keyword = (String) params.get("keyword");
        String category = (String) params.get("category");

        // 构建查询条件
        QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like("name", keyword);
        }
        if (category != null && !category.isEmpty()) {
            queryWrapper.eq("category", category);
        }
        queryWrapper.eq("is_online", true)
                .orderByDesc("avg_rating");

        List<Dish> dishes = dishService.list(queryWrapper);

        // 构建菜品数据
        List<Map<String, Object>> dishData = dishes.stream()
                .map(dish -> {
                    Map<String, Object> dishMap = new HashMap<>();
                    dishMap.put("dishId", dish.getId());
                    dishMap.put("dishName", dish.getName());
                    dishMap.put("imageUrl", dish.getImage());
                    dishMap.put("price", dish.getPrice());
                    dishMap.put("rating", dish.getAvgRating());
                    dishMap.put("salesCount", 0);
                    dishMap.put("category", dish.getCategory());
                    dishMap.put("description", dish.getDescription());

                    // 标签
                    List<String> tags = new ArrayList<>();
                    if (dish.getCategory() != null) {
                        tags.add(dish.getCategory());
                    }
                    dishMap.put("tags", tags);

                    // 操作按钮
                    List<Map<String, String>> actions = new ArrayList<>();
                    actions.add(Map.of("type", "add_to_cart", "text", "加入购物车", "icon", "ShoppingCart"));
                    actions.add(Map.of("type", "add_favorite", "text", "收藏", "icon", "Star"));
                    dishMap.put("actions", actions);

                    return dishMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("messageType", "dish_list_card");
        data.put("summary", "找到 " + dishData.size() + " 道菜品");
        data.put("data", Map.of(
            "total", dishData.size(),
            "dishes", dishData
        ));

        return data;
    }

    // ==================== 辅助方法 ====================

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 提取JSON数组
     */
    private List<String> extractJsonArray(String jsonString) {
        try {
            if (jsonString == null || jsonString.isEmpty()) {
                return Collections.emptyList();
            }
            if (jsonString.startsWith("[") && jsonString.endsWith("]")) {
                String content = jsonString.substring(1, jsonString.length() - 1);
                String[] items = content.split(",");
                List<String> result = new ArrayList<>();
                for (String item : items) {
                    String trimmed = item.trim().replaceAll("[\"']", "");
                    if (!trimmed.isEmpty()) {
                        result.add(trimmed);
                    }
                }
                return result;
            }
        } catch (Exception e) {
            log.debug("解析JSON数组失败", e);
        }
        return Collections.emptyList();
    }

    /**
     * 计算BMI
     */
    private double calculateBMI(Double heightCm, Double weightKg) {
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    /**
     * 获取BMI状态
     */
    private String getBMIStatus(double bmi) {
        if (bmi < 18.5) return "underweight";
        if (bmi < 24) return "normal";
        if (bmi < 28) return "overweight";
        return "obese";
    }

    /**
     * 获取BMI状态文本
     */
    private String getBMIStatusText(double bmi) {
        if (bmi < 18.5) return "偏瘦";
        if (bmi < 24) return "正常";
        if (bmi < 28) return "偏胖";
        return "肥胖";
    }

    /**
     * 格式化时间
     */
    private String formatTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return time.format(formatter);
    }

    /**
     * 解析images JSON字段
     */
    private List<String> parseImagesJson(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            if (imagesJson.startsWith("[") && imagesJson.endsWith("]")) {
                String content = imagesJson.substring(1, imagesJson.length() - 1);
                String[] items = content.split(",");
                List<String> result = new ArrayList<>();
                for (String item : items) {
                    String trimmed = item.trim().replaceAll("[\"']", "");
                    if (!trimmed.isEmpty()) {
                        result.add(trimmed);
                    }
                }
                return result;
            }
        } catch (Exception e) {
            log.debug("解析images JSON失败", e);
        }

        return Collections.emptyList();
    }
}
