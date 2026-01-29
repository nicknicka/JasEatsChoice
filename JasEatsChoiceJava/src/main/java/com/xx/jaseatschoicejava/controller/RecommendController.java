package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.dto.RecommendationRequestDTO;
import com.xx.jaseatschoicejava.dto.RecommendationResultDTO;
import com.xx.jaseatschoicejava.dto.UserBehaviorDTO;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.UserBehavior;
import com.xx.jaseatschoicejava.entity.UserProfile;
import com.xx.jaseatschoicejava.mapper.RejectRecommendationMapper;
import com.xx.jaseatschoicejava.service.RecommendationService;
import com.xx.jaseatschoicejava.service.UserBehaviorService;
import com.xx.jaseatschoicejava.service.UserProfileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 个性化推荐控制器
 * 整合多种推荐策略，提供智能推荐服务
 */
@Slf4j
@RestController
@RequestMapping("/v1/recommendations")
@Api(tags = "推荐系统管理")
public class RecommendController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserBehaviorService userBehaviorService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private RejectRecommendationMapper rejectRecommendationMapper;

    /**
     * 获取个性化推荐菜品（主接口）
     * 整合多种召回策略和排序策略
     */
    @GetMapping("/{userId}")
    @ApiOperation(value = "获取个性化推荐", notes = "基于用户画像、协同过滤、热门菜品等多种策略生成推荐")
    public ResponseResult<?> getRecommendDishes(
            @ApiParam(value = "用户ID", required = true) @PathVariable String userId,
            @ApiParam(value = "推荐场景：home/personal/cart/dish_detail") @RequestParam(required = false, defaultValue = "home") String scene,
            @ApiParam(value = "返回数量") @RequestParam(required = false, defaultValue = "20") Integer limit,
            @ApiParam(value = "时段：早餐/午餐/晚餐/宵夜") @RequestParam(required = false) String timePeriod,
            @ApiParam(value = "天气：sunny/rainy/hot/cold") @RequestParam(required = false) String weather) {

        try {
            // 构建推荐请求
            RecommendationRequestDTO request = new RecommendationRequestDTO();
            request.setUserId(userId);
            request.setScene(scene);
            request.setLimit(limit);

            // 设置上下文信息
            Map<String, Object> context = new HashMap<>();
            if (timePeriod != null) {
                context.put("timePeriod", timePeriod);
            }
            if (weather != null) {
                context.put("weather", weather);
            }
            request.setContext(context);

            // 调用推荐服务
            List<RecommendationResultDTO> recommendations = recommendationService.getRecommendations(request);

            // 异步记录推荐展示行为
            UserBehaviorDTO behaviorDTO = new UserBehaviorDTO();
            behaviorDTO.setUserId(userId);
            behaviorDTO.setBehaviorType("view");
            behaviorDTO.setItemType("recommendation");
            behaviorDTO.setContext(context);
            userBehaviorService.recordBehaviorAsync(behaviorDTO);

            Map<String, Object> result = new HashMap<>();
            result.put("recommendations", recommendations);
            result.put("total", recommendations.size());
            result.put("scene", scene);
            result.put("timestamp", System.currentTimeMillis());

            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("获取推荐失败：userId={}", userId, e);
            return ResponseResult.fail("500", "获取推荐失败：" + e.getMessage());
        }
    }

    /**
     * 刷新推荐（用户主动刷新）
     */
    @PostMapping("/{userId}/refresh")
    @ApiOperation(value = "刷新推荐", notes = "用户主动刷新推荐列表")
    public ResponseResult<?> refreshRecommendations(
            @ApiParam(value = "用户ID", required = true) @PathVariable String userId) {

        try {
            List<RecommendationResultDTO> recommendations = recommendationService.refreshRecommendations(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("recommendations", recommendations);
            result.put("total", recommendations.size());
            result.put("timestamp", System.currentTimeMillis());

            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("刷新推荐失败：userId={}", userId, e);
            return ResponseResult.fail("500", "刷新推荐失败：" + e.getMessage());
        }
    }

    /**
     * 记录用户行为反馈
     */
    @PostMapping("/feedback")
    @ApiOperation(value = "记录推荐反馈", notes = "记录用户对推荐的点击、下单等反馈行为")
    public ResponseResult<?> recordFeedback(
            @ApiParam(value = "反馈信息", required = true) @RequestBody Map<String, Object> params) {

        try {
            String userId = (String) params.get("userId");
            String dishId = (String) params.get("dishId");
            String recommendationId = (String) params.get("recommendationId");
            Boolean isClicked = (Boolean) params.get("isClicked");
            Boolean isOrdered = (Boolean) params.get("isOrdered");

            // 更新推荐记录
            recommendationService.recordFeedback(userId, dishId, recommendationId, isClicked, isOrdered);

            // 异步记录用户行为
            if (Boolean.TRUE.equals(isClicked)) {
                UserBehaviorDTO behaviorDTO = new UserBehaviorDTO();
                behaviorDTO.setUserId(userId);
                behaviorDTO.setBehaviorType("click");
                behaviorDTO.setItemType("dish");
                behaviorDTO.setItemId(dishId);
                userBehaviorService.recordBehaviorAsync(behaviorDTO);
            }

            if (Boolean.TRUE.equals(isOrdered)) {
                UserBehaviorDTO behaviorDTO = new UserBehaviorDTO();
                behaviorDTO.setUserId(userId);
                behaviorDTO.setBehaviorType("order");
                behaviorDTO.setItemType("dish");
                behaviorDTO.setItemId(dishId);
                userBehaviorService.recordBehaviorAsync(behaviorDTO);
            }

            return ResponseResult.success("反馈记录成功");
        } catch (Exception e) {
            log.error("记录反馈失败", e);
            return ResponseResult.fail("500", "记录反馈失败：" + e.getMessage());
        }
    }

    /**
     * 记录推荐拒绝行为
     */
    @PostMapping("/{userId}/reject")
    @ApiOperation(value = "拒绝推荐", notes = "用户拒绝某个推荐菜品")
    public ResponseResult<?> recordRejectBehavior(
            @ApiParam(value = "用户ID", required = true) @PathVariable String userId,
            @ApiParam(value = "拒绝信息", required = true) @RequestBody Map<String, Object> params) {

        try {
            String dishId = (String) params.get("dishId");
            String reason = (String) params.get("reason");

            // 记录拒绝行为
            com.xx.jaseatschoicejava.entity.RejectRecommendation rejectRecord =
                new com.xx.jaseatschoicejava.entity.RejectRecommendation();
            rejectRecord.setUserId(userId);
            rejectRecord.setDishId(dishId);
            rejectRecord.setRejectTime(java.time.LocalDateTime.now());
            rejectRecord.setReason(reason);
            rejectRecommendationMapper.insert(rejectRecord);

            // 异步记录用户行为
            UserBehaviorDTO behaviorDTO = new UserBehaviorDTO();
            behaviorDTO.setUserId(userId);
            behaviorDTO.setBehaviorType("reject");
            behaviorDTO.setItemType("dish");
            behaviorDTO.setItemId(dishId);
            userBehaviorService.recordBehaviorAsync(behaviorDTO);

            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("dishId", dishId);
            result.put("action", "reject");
            result.put("timestamp", System.currentTimeMillis());

            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("记录拒绝行为失败：userId={}", userId, e);
            return ResponseResult.fail("500", "记录拒绝行为失败：" + e.getMessage());
        }
    }

    /**
     * 替换推荐菜品
     */
    @PostMapping("/{userId}/replace")
    @ApiOperation(value = "替换推荐", notes = "用户不满意的推荐菜品，替换为其他菜品")
    public ResponseResult<?> replaceRecommendDishes(
            @ApiParam(value = "用户ID", required = true) @PathVariable String userId,
            @ApiParam(value = "要替换的菜品ID列表", required = true) @RequestBody Map<String, Object> params) {

        try {
            @SuppressWarnings("unchecked")
            List<String> replaceDishIds = (List<String>) params.get("replaceDishIds");

            if (replaceDishIds == null || replaceDishIds.isEmpty()) {
                return ResponseResult.fail("400", "请提供要替换的菜品ID列表");
            }

            List<Dish> replacedDishes = recommendationService.replaceRecommendDishes(userId, replaceDishIds);

            Map<String, Object> result = new HashMap<>();
            result.put("replacedDishes", replacedDishes);
            result.put("count", replacedDishes.size());
            result.put("message", "推荐菜品替换成功");

            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("替换推荐失败：userId={}", userId, e);
            return ResponseResult.fail("500", "替换推荐失败：" + e.getMessage());
        }
    }

    /**
     * 筛选推荐菜品
     */
    @PostMapping("/{userId}/filter")
    @ApiOperation(value = "筛选推荐", notes = "根据分类、卡路里、价格等条件筛选菜品")
    public ResponseResult<?> filterRecommendDishes(
            @ApiParam(value = "用户ID", required = true) @PathVariable String userId,
            @ApiParam(value = "筛选条件", required = true) @RequestBody Map<String, Object> params) {

        try {
            String category = (String) params.get("category");
            Integer minCalorie = params.get("minCalorie") != null ?
                Integer.valueOf(params.get("minCalorie").toString()) : null;
            Integer maxCalorie = params.get("maxCalorie") != null ?
                Integer.valueOf(params.get("maxCalorie").toString()) : null;
            BigDecimal minPrice = params.get("minPrice") != null ?
                new BigDecimal(params.get("minPrice").toString()) : null;
            BigDecimal maxPrice = params.get("maxPrice") != null ?
                new BigDecimal(params.get("maxPrice").toString()) : null;

            List<Dish> filteredDishes = recommendationService.filterRecommendDishes(
                userId, category, minCalorie, maxCalorie, minPrice, maxPrice);

            Map<String, Object> result = new HashMap<>();
            result.put("filteredDishes", filteredDishes);
            result.put("count", filteredDishes.size());
            result.put("filterParams", params);

            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("筛选推荐失败：userId={}", userId, e);
            return ResponseResult.fail("500", "筛选推荐失败：" + e.getMessage());
        }
    }

    /**
     * 获取推荐理由
     */
    @GetMapping("/{userId}/reason/{dishId}")
    @ApiOperation(value = "获取推荐理由", notes = "获取某个菜品被推荐的理由")
    public ResponseResult<?> getRecommendationReason(
            @ApiParam(value = "用户ID", required = true) @PathVariable String userId,
            @ApiParam(value = "菜品ID", required = true) @PathVariable String dishId) {

        try {
            String reason = recommendationService.getRecommendationReason(dishId, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("dishId", dishId);
            result.put("reason", reason);

            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("获取推荐理由失败：userId={}, dishId={}", userId, dishId, e);
            return ResponseResult.fail("500", "获取推荐理由失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户画像
     */
    @GetMapping("/profile/{userId}")
    @ApiOperation(value = "获取用户画像", notes = "获取用户的偏好画像信息")
    public ResponseResult<?> getUserProfile(
            @ApiParam(value = "用户ID", required = true) @PathVariable String userId) {

        try {
            UserProfile profile = userProfileService.getUserProfile(userId);
            return ResponseResult.success(profile);
        } catch (Exception e) {
            log.error("获取用户画像失败：userId={}", userId, e);
            return ResponseResult.fail("500", "获取用户画像失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户行为历史
     */
    @GetMapping("/behavior/{userId}")
    @ApiOperation(value = "获取用户行为历史", notes = "获取用户最近的行为记录")
    public ResponseResult<?> getUserBehaviors(
            @ApiParam(value = "用户ID", required = true) @PathVariable String userId,
            @ApiParam(value = "返回数量") @RequestParam(required = false, defaultValue = "50") Integer limit) {

        try {
            List<UserBehavior> behaviors = userBehaviorService.getRecentBehaviors(userId, limit);

            Map<String, Object> result = new HashMap<>();
            result.put("behaviors", behaviors);
            result.put("total", behaviors.size());

            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("获取用户行为历史失败：userId={}", userId, e);
            return ResponseResult.fail("500", "获取用户行为历史失败：" + e.getMessage());
        }
    }

    /**
     * 记录用户行为（通用接口）
     */
    @PostMapping("/behavior")
    @ApiOperation(value = "记录用户行为", notes = "记录用户的各种行为（浏览、点击、收藏等）")
    public ResponseResult<?> recordBehavior(
            @ApiParam(value = "行为信息", required = true) @RequestBody UserBehaviorDTO behaviorDTO) {

        try {
            if (behaviorDTO.getUserId() == null) {
                return ResponseResult.fail("400", "用户ID不能为空");
            }

            userBehaviorService.recordBehavior(behaviorDTO);
            return ResponseResult.success("行为记录成功");
        } catch (Exception e) {
            log.error("记录用户行为失败", e);
            return ResponseResult.fail("500", "记录用户行为失败：" + e.getMessage());
        }
    }

    /**
     * 设置推荐偏好（保留旧接口兼容性）
     */
    @PutMapping("/users/{userId}/prefer")
    @ApiOperation(value = "设置推荐偏好", notes = "设置用户的推荐偏好参数")
    public ResponseResult<?> setRecommendPreference(
            @ApiParam(value = "用户ID", required = true) @PathVariable String userId,
            @ApiParam(value = "偏好参数", required = true) @RequestBody Map<String, Object> params) {

        try {
            UserProfile profile = userProfileService.getUserProfile(userId);

            // 更新用户偏好
            if (params.get("dietGoal") != null) {
                profile.setDietGoal((String) params.get("dietGoal"));
            }

            boolean success = userProfileService.updateUserProfile(profile);
            if (success) {
                return ResponseResult.success("推荐偏好设置成功");
            }
            return ResponseResult.fail("500", "推荐偏好设置失败");
        } catch (Exception e) {
            log.error("设置推荐偏好失败：userId={}", userId, e);
            return ResponseResult.fail("500", "设置推荐偏好失败：" + e.getMessage());
        }
    }

    /**
     * 一键生成购物清单（保留旧接口）
     */
    @GetMapping("/recipe/{userId}/shopping-list")
    @ApiOperation(value = "生成购物清单", notes = "基于用户饮食目标生成购物清单")
    public ResponseResult<?> generateShoppingList(
            @ApiParam(value = "用户ID", required = true) @PathVariable String userId,
            @ApiParam(value = "日期") @RequestParam(required = false) String date) {

        try {
            UserProfile profile = userProfileService.getUserProfile(userId);

            // 基于用户营养目标生成购物清单
            Map<String, Object> result = new HashMap<>();
            result.put("shoppingList", generateShoppingListByProfile(profile));
            result.put("dietGoal", profile.getDietGoal() != null ? profile.getDietGoal() : "default");
            if (date != null) {
                result.put("date", date);
            }

            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("生成购物清单失败：userId={}", userId, e);
            return ResponseResult.fail("500", "生成购物清单失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户画像生成购物清单
     */
    private List<Map<String, String>> generateShoppingListByProfile(UserProfile profile) {
        List<Map<String, String>> shoppingList = new java.util.ArrayList<>();

        String dietGoal = profile != null && profile.getDietGoal() != null ? profile.getDietGoal() : "default";

        // 根据饮食目标生成不同的购物清单
        switch (dietGoal) {
            case "low_calorie":
                shoppingList.add(createItem("鸡胸肉", "150g"));
                shoppingList.add(createItem("燕麦", "80g"));
                shoppingList.add(createItem("西兰花", "200g"));
                shoppingList.add(createItem("菠菜", "150g"));
                break;
            case "high_protein":
                shoppingList.add(createItem("牛里脊", "250g"));
                shoppingList.add(createItem("鸡蛋", "4个"));
                shoppingList.add(createItem("希腊酸奶", "200ml"));
                shoppingList.add(createItem("三文鱼", "150g"));
                break;
            default:
                shoppingList.add(createItem("鸡胸肉", "200g"));
                shoppingList.add(createItem("大米", "1杯"));
                shoppingList.add(createItem("混合蔬菜", "250g"));
                shoppingList.add(createItem("豆腐", "200g"));
                break;
        }

        return shoppingList;
    }

    /**
     * 创建购物清单项
     */
    private Map<String, String> createItem(String ingredient, String quantity) {
        Map<String, String> item = new HashMap<>();
        item.put("ingredient", ingredient);
        item.put("quantity", quantity);
        return item;
    }
}
