package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.RejectRecommendation;
import com.xx.jaseatschoicejava.entity.UserPreference;
import com.xx.jaseatschoicejava.mapper.RejectRecommendationMapper;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.UserPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 个性化推荐控制器
 */
@RestController
@RequestMapping("/v1/recommend")
public class RecommendController {

    @Autowired
    private DishService dishService;

    @Autowired
    private UserPreferenceService userPreferenceService;

    @Autowired
    private RejectRecommendationMapper rejectRecommendationMapper;

    /**
     * 获取个性化推荐菜品
     */
    @GetMapping("/recommend/{userId}")
    public ResponseResult<?> getRecommendDishes(@PathVariable String userId,
                                               @RequestParam(required = false) Double longitude,
                                               @RequestParam(required = false) Double latitude) {
        Map<String, Object> recommendResult = new HashMap<>();
        List<Dish> finalDishes;

        // 获取用户推荐偏好
        UserPreference userPreference = userPreferenceService.getByUserId(userId);

        if (userPreference != null) {
            List<Dish> filteredDishes = dishService.list();

            // 根据饮食目标筛选（示例：低卡、高蛋白等）
            if (userPreference.getDietGoal() != null && !userPreference.getDietGoal().isEmpty()) {
                if ("low_calorie".equals(userPreference.getDietGoal())) {
                    filteredDishes = filteredDishes.stream()
                        .filter(dish -> dish.getCalorie() != null && dish.getCalorie() < 500)
                        .toList();
                } else if ("high_protein".equals(userPreference.getDietGoal())) {
                    // 高蛋白筛选逻辑（假设菜品表有蛋白质字段，目前没有则跳过）
                }
            }

            // 基于标签权重的个性化推荐逻辑
            if (userPreference.getTagWeights() != null && !userPreference.getTagWeights().isEmpty()) {
                try {
                    // 解析标签权重JSON字符串
                    Map<String, Double> tagWeights = new com.fasterxml.jackson.databind.ObjectMapper()
                            .readValue(userPreference.getTagWeights(),
                                     new com.fasterxml.jackson.core.type.TypeReference<Map<String, Double>>() {});

                    // 根据标签权重计算菜品评分（此处假设菜品分类作为标签匹配依据）
                    finalDishes = filteredDishes.stream()
                            .map(dish -> {
                                // 计算当前菜品的推荐评分
                                double score = 0.0;
                                if (dish.getCategory() != null && tagWeights.containsKey(dish.getCategory())) {
                                    score = tagWeights.get(dish.getCategory());
                                }
                                // 可以扩展：结合天气、位置等因素增加评分

                                return Map.entry(dish, score);
                            })
                            .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue())) // 按评分降序排序
                            .map(Map.Entry::getKey)
                            .toList();

                    recommendResult.put("dishes", finalDishes);
                    recommendResult.put("recommendReason", "Personalized recommendation based on tag weights");
                } catch (Exception e) {
                    // JSON解析失败，返回默认筛选结果
                    recommendResult.put("dishes", filteredDishes);
                    recommendResult.put("recommendReason", "Personalized recommendation with tag weights failed, using basic filtering");
                }
            } else {
                // 没有标签权重设置，返回默认筛选结果
                recommendResult.put("dishes", filteredDishes);
                recommendResult.put("recommendReason", "Personalized recommendation based on basic preferences");
            }
        } else {
            // 用户没有设置偏好，返回默认推荐
            finalDishes = dishService.list();
            recommendResult.put("dishes", finalDishes);
            recommendResult.put("recommendReason", "Default recommendation for new users");
        }

        return ResponseResult.success(recommendResult);
    }

    /**
     * 设置推荐偏好
     */
    @PutMapping("/users/{userId}/prefer")
    public ResponseResult<?> setRecommendPreference(@PathVariable String userId, @RequestBody Map<String, Object> params) {
        UserPreference preference = new UserPreference();
        preference.setUserId(userId);
        preference.setTagWeights((String) params.get("tags"));
        preference.setDisableWeatherRecommend((Boolean) params.get("disableWeatherRecommend"));

        boolean success = userPreferenceService.updatePreference(preference);
        if (success) {
            return ResponseResult.success("推荐偏好设置成功");
        }
        return ResponseResult.fail("500", "推荐偏好设置失败");
    }

    /**
     * 替换推荐菜品
     */
    @PostMapping("/recommend/{userId}/replace")
    public ResponseResult<?> replaceRecommendDishes(@PathVariable String userId, @RequestBody Map<String, Object> params) {
        // 获取用户要替换的菜品ID列表
        List<String> replaceDishIds = (List<String>) params.get("replaceDishIds");
        if (replaceDishIds == null || replaceDishIds.isEmpty()) {
            return ResponseResult.fail("400", "请提供要替换的菜品ID列表");
        }

        // 获取用户推荐偏好
        UserPreference userPreference = userPreferenceService.getByUserId(userId);
        List<Dish> candidateDishes = dishService.list();

        // 根据用户偏好筛选可替换的菜品
        if (userPreference != null) {
            if (userPreference.getDietGoal() != null && !userPreference.getDietGoal().isEmpty()) {
                if ("low_calorie".equals(userPreference.getDietGoal())) {
                    candidateDishes = candidateDishes.stream()
                        .filter(dish -> dish.getCalorie() != null && dish.getCalorie() < 500)
                        .toList();
                }
            }
        }

        // 从候选菜品中移除用户要替换的菜品，并随机选择相同数量的新菜品
        List<Dish> filteredDishes = candidateDishes.stream()
            .filter(dish -> !replaceDishIds.contains(dish.getId()) && dish.getStatus())
            .limit(replaceDishIds.size())
            .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("replacedDishes", filteredDishes);
        result.put("message", "推荐菜品替换成功");

        return ResponseResult.success(result);
    }

    /**
     * 筛选推荐菜品
     */
    @PostMapping("/recommend/{userId}/filter")
    public ResponseResult<?> filterRecommendDishes(@PathVariable String userId, @RequestBody Map<String, Object> params) {
        // 获取用户推荐偏好
        UserPreference userPreference = userPreferenceService.getByUserId(userId);
        List<Dish> filteredDishes = dishService.list();

        // 首先应用用户的个性化偏好
        if (userPreference != null) {
            if (userPreference.getDietGoal() != null && !userPreference.getDietGoal().isEmpty()) {
                if ("low_calorie".equals(userPreference.getDietGoal())) {
                    filteredDishes = filteredDishes.stream()
                        .filter(dish -> dish.getCalorie() != null && dish.getCalorie() < 500)
                        .toList();
                }
            }
        }

        // 应用请求中的筛选条件
        // 菜品分类筛选
        if (params.get("category") != null) {
            String category = (String) params.get("category");
            filteredDishes = filteredDishes.stream()
                .filter(dish -> category.equals(dish.getCategory()))
                .toList();
        }

        // 卡路里范围筛选
        if (params.get("minCalorie") != null || params.get("maxCalorie") != null) {
            Integer minCalorie = (Integer) params.get("minCalorie");
            Integer maxCalorie = (Integer) params.get("maxCalorie");
            filteredDishes = filteredDishes.stream()
                .filter(dish -> dish.getCalorie() != null &&
                        (minCalorie == null || dish.getCalorie() >= minCalorie) &&
                        (maxCalorie == null || dish.getCalorie() <= maxCalorie))
                .toList();
        }

        // 价格范围筛选
        if (params.get("minPrice") != null || params.get("maxPrice") != null) {
            BigDecimal minPrice = params.get("minPrice") instanceof String ?
                new BigDecimal((String) params.get("minPrice")) :
                (BigDecimal) params.get("minPrice");
            BigDecimal maxPrice = params.get("maxPrice") instanceof String ?
                new BigDecimal((String) params.get("maxPrice")) :
                (BigDecimal) params.get("maxPrice");
            filteredDishes = filteredDishes.stream()
                .filter(dish -> dish.getPrice() != null &&
                        (minPrice == null || dish.getPrice().compareTo(minPrice) >= 0) &&
                        (maxPrice == null || dish.getPrice().compareTo(maxPrice) <= 0))
                .toList();
        }

        // 只返回上架状态的菜品
        filteredDishes = filteredDishes.stream().filter(Dish::getStatus).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("filteredDishes", filteredDishes);
        result.put("filterParams", params);

        return ResponseResult.success(result);
    }

    /**
     * 一键生成购物清单
     */
    @GetMapping("/recipe/{userId}/shopping-list")
    public ResponseResult<?> generateShoppingList(@PathVariable String userId, @RequestParam(required = false) String date) {
        // 获取用户推荐偏好
        UserPreference userPreference = userPreferenceService.getByUserId(userId);

        // 模拟基于用户饮食目标的购物清单生成
        List<Map<String, String>> shoppingList = new ArrayList<>();

        // 低卡路里饮食推荐
        if (userPreference != null && "low_calorie".equals(userPreference.getDietGoal())) {
            Map<String, String> item1 = new HashMap<>();
            item1.put("ingredient", "Chicken breast");
            item1.put("quantity", "150g");

            Map<String, String> item2 = new HashMap<>();
            item2.put("ingredient", "Oatmeal");
            item2.put("quantity", "80g");

            Map<String, String> item3 = new HashMap<>();
            item3.put("ingredient", "Broccoli");
            item3.put("quantity", "200g");

            shoppingList.add(item1);
            shoppingList.add(item2);
            shoppingList.add(item3);
        }
        // 高蛋白饮食推荐
        else if (userPreference != null && "high_protein".equals(userPreference.getDietGoal())) {
            Map<String, String> item1 = new HashMap<>();
            item1.put("ingredient", "Beef tenderloin");
            item1.put("quantity", "250g");

            Map<String, String> item2 = new HashMap<>();
            item2.put("ingredient", "Eggs");
            item2.put("quantity", "4");

            Map<String, String> item3 = new HashMap<>();
            item3.put("ingredient", "Greek yogurt");
            item3.put("quantity", "200ml");

            shoppingList.add(item1);
            shoppingList.add(item2);
            shoppingList.add(item3);
        }
        // 默认推荐
        else {
            Map<String, String> item1 = new HashMap<>();
            item1.put("ingredient", "Chicken breast");
            item1.put("quantity", "200g");

            Map<String, String> item2 = new HashMap<>();
            item2.put("ingredient", "Rice");
            item2.put("quantity", "1 cup");

            Map<String, String> item3 = new HashMap<>();
            item3.put("ingredient", "Vegetables");
            item3.put("quantity", "250g");

            shoppingList.add(item1);
            shoppingList.add(item2);
            shoppingList.add(item3);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("shoppingList", shoppingList);
        result.put("dietGoal", userPreference != null ? userPreference.getDietGoal() : "default");
        if (date != null) {
            result.put("date", date);
        }

        return ResponseResult.success(result);
    }

    /**
     * 记录推荐拒绝行为
     */
    @PostMapping("/recommend/{userId}/reject")
    public ResponseResult<?> recordRejectBehavior(@PathVariable String userId, @RequestBody Map<String, Object> params) {
        // 获取被拒绝的菜品ID
        String dishId = (String) params.get("dishId");
        if (dishId == null) {
            return ResponseResult.fail("400", "请提供被拒绝的菜品ID");
        }

        // 获取拒绝原因（可选）
        String reason = (String) params.get("reason");

        // 将拒绝行为记录到数据库
        RejectRecommendation rejectRecord = new RejectRecommendation();
        rejectRecord.setUserId(userId);
        rejectRecord.setDishId(dishId);
        rejectRecord.setRejectTime(java.time.LocalDateTime.now());
        rejectRecord.setReason(reason);

        // 保存记录
        rejectRecommendationMapper.insert(rejectRecord);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("dishId", dishId);
        result.put("action", "reject");
        result.put("timestamp", System.currentTimeMillis());

        return ResponseResult.success(result);
    }
}
