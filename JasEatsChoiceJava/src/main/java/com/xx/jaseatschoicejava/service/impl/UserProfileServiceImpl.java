package com.xx.jaseatschoicejava.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.UserBehavior;
import com.xx.jaseatschoicejava.entity.UserProfile;
import com.xx.jaseatschoicejava.mapper.DishMapper;
import com.xx.jaseatschoicejava.mapper.UserBehaviorMapper;
import com.xx.jaseatschoicejava.mapper.UserProfileMapper;
import com.xx.jaseatschoicejava.service.UserBehaviorService;
import com.xx.jaseatschoicejava.service.UserProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户画像服务实现
 */
@Slf4j
@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Autowired
    private DishMapper dishMapper;

    @Override
    public UserProfile getUserProfile(String userId) {
        UserProfile profile = userProfileMapper.getByUserId(userId);

        // 如果不存在，创建默认画像
        if (profile == null) {
            profile = createDefaultProfile(userId);
            userProfileMapper.insert(profile);
        }

        return profile;
    }

    @Override
    public boolean createUserProfile(UserProfile profile) {
        try {
            profile.setLastUpdated(LocalDateTime.now());
            return userProfileMapper.insert(profile) > 0;
        } catch (Exception e) {
            log.error("创建用户画像失败：userId={}", profile.getUserId(), e);
            return false;
        }
    }

    @Override
    public boolean updateUserProfile(UserProfile profile) {
        try {
            profile.setLastUpdated(LocalDateTime.now());
            return userProfileMapper.updateById(profile) > 0;
        } catch (Exception e) {
            log.error("更新用户画像失败：userId={}", profile.getUserId(), e);
            return false;
        }
    }

    @Override
    public boolean deleteUserProfile(String userId) {
        try {
            return userProfileMapper.deleteByMap(Map.of("user_id", userId)) > 0;
        } catch (Exception e) {
            log.error("删除用户画像失败：userId={}", userId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public void updateUserProfileOnBehavior(String userId, String behaviorType, String itemId) {
        try {
            UserProfile profile = getUserProfile(userId);

            // 获取菜品分类作为标签
            String category = getDishCategory(itemId);
            if (category == null) {
                return;
            }

            // 增量更新偏好标签
            List<UserProfile.PreferenceTag> prefTags = profile.getPreferenceTags();
            if (prefTags == null) {
                prefTags = new ArrayList<>();
                profile.setPreferenceTags(prefTags);
            }

            // 查找或创建标签
            final List<UserProfile.PreferenceTag> finalPrefTags = prefTags;
            UserProfile.PreferenceTag targetTag = prefTags.stream()
                    .filter(tag -> category.equals(tag.getTag()))
                    .findFirst()
                    .orElseGet(() -> {
                        UserProfile.PreferenceTag newTag = new UserProfile.PreferenceTag();
                        newTag.setTag(category);
                        newTag.setScore(0.5);
                        finalPrefTags.add(newTag);
                        return newTag;
                    });

            // 根据行为类型调整分数
            int weight = getBehaviorWeight(behaviorType);
            double increment = weight * 0.01; // 小幅度增量

            double currentScore = targetTag.getScore();
            double newScore = currentScore + increment;

            // 限制在0-1范围
            newScore = Math.max(0.0, Math.min(1.0, newScore));
            targetTag.setScore(newScore);

            profile.setPreferenceTags(prefTags);
            profile.setLastUpdated(LocalDateTime.now());

            userProfileMapper.updateById(profile);

            log.debug("实时更新用户画像：userId={}, tag={}, score={}",
                    userId, category, newScore);

        } catch (Exception e) {
            log.error("实时更新用户画像失败：userId={}, itemId={}", userId, itemId, e);
        }
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void batchUpdateAllProfiles() {
        log.info("开始批量更新用户画像");

        try {
            // 获取所有有行为记录的用户ID
            List<String> userIds = getUserIdsWithBehaviors();

            int successCount = 0;
            int failCount = 0;

            for (String userId : userIds) {
                try {
                    UserProfile profile = calculateUserProfile(userId);
                    userProfileMapper.updateById(profile);
                    successCount++;
                } catch (Exception e) {
                    log.error("更新用户画像失败：userId={}", userId, e);
                    failCount++;
                }
            }

            log.info("批量更新用户画像完成：成功={}, 失败={}", successCount, failCount);

        } catch (Exception e) {
            log.error("批量更新用户画像失败", e);
        }
    }

    @Override
    public UserProfile calculateUserProfile(String userId) {
        // 获取用户30天内的所有行为
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        List<UserBehavior> behaviors = userBehaviorMapper.getBehaviorsSince(userId, startTime);

        UserProfile profile = new UserProfile();
        profile.setUserId(userId);

        if (behaviors.isEmpty()) {
            // 新用户，返回默认画像
            return createDefaultProfile(userId);
        }

        // 1. 统计偏好标签
        Map<String, Integer> tagCounts = new HashMap<>();
        Map<String, Integer> categoryOrders = new HashMap<>();

        for (UserBehavior behavior : behaviors) {
            String category = getDishCategory(behavior.getItemId());
            if (category == null) continue;

            int weight = getBehaviorWeight(behavior.getBehaviorType());

            // 累加权重
            tagCounts.merge(category, weight, Integer::sum);

            // 统计下单次数
            if ("order".equals(behavior.getBehaviorType())) {
                categoryOrders.merge(category, 1, Integer::sum);
            }
        }

        // 归一化为0-1分数
        List<UserProfile.PreferenceTag> prefTags = new ArrayList<>();
        int maxCount = tagCounts.values().stream().max(Integer::compare).orElse(1);

        for (Map.Entry<String, Integer> entry : tagCounts.entrySet()) {
            UserProfile.PreferenceTag tag = new UserProfile.PreferenceTag();
            tag.setTag(entry.getKey());
            tag.setScore(entry.getValue() / (double) maxCount);
            prefTags.add(tag);
        }

        // 按分数降序排序
        prefTags.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        profile.setPreferenceTags(prefTags);

        // 2. 计算口味偏好
        profile.setFlavorPreference(calculateFlavorPreference(behaviors));

        // 3. 计算价格偏好
        profile.setPricePreference(calculatePricePreference(userId, behaviors));

        // 4. 计算营养目标
        profile.setNutritionGoals(calculateNutritionGoals(userId));

        // 5. 统计数据
        UserProfile.UserStatistics stats = new UserProfile.UserStatistics();
        stats.setTotalOrders((int) behaviors.stream()
                .filter(b -> "order".equals(b.getBehaviorType()))
                .count());

        // 平均订单金额（这里简化为0，实际应从订单表统计）
        stats.setAvgOrderAmount(0.0);

        // 喜爱类别（按下单次数排序）
        List<String> favCategories = categoryOrders.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        stats.setFavCategories(favCategories);

        // 最常下单时段
        stats.setMostFrequentTimePeriod(calculateMostFrequentTimePeriod(behaviors));

        profile.setStatistics(stats);
        profile.setLastUpdated(LocalDateTime.now());

        return profile;
    }

    @Override
    public List<UserProfile> getProfilesNeedUpdate() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);
        return userProfileMapper.getProfilesNeedUpdate(threshold);
    }

    /**
     * 创建默认用户画像
     */
    private UserProfile createDefaultProfile(String userId) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);

        // 默认偏好标签
        List<UserProfile.PreferenceTag> defaultTags = new ArrayList<>();
        profile.setPreferenceTags(defaultTags);

        // 默认口味偏好（中等）
        Map<String, Double> defaultFlavor = new HashMap<>();
        defaultFlavor.put("spicy", 0.5);
        defaultFlavor.put("sweet", 0.5);
        defaultFlavor.put("salty", 0.5);
        profile.setFlavorPreference(defaultFlavor);

        // 默认价格偏好
        UserProfile.PricePreference defaultPrice = new UserProfile.PricePreference();
        defaultPrice.setMin(10.0);
        defaultPrice.setMax(50.0);
        defaultPrice.setOptimal(25.0);
        profile.setPricePreference(defaultPrice);

        // 默认营养目标
        UserProfile.NutritionGoals defaultNutrition = new UserProfile.NutritionGoals();
        defaultNutrition.setCalories(2000.0);
        defaultNutrition.setProtein(60.0);
        profile.setNutritionGoals(defaultNutrition);

        // 默认统计数据
        UserProfile.UserStatistics stats = new UserProfile.UserStatistics();
        stats.setTotalOrders(0);
        stats.setAvgOrderAmount(0.0);
        stats.setFavCategories(new ArrayList<>());
        profile.setStatistics(stats);

        profile.setLastUpdated(LocalDateTime.now());

        return profile;
    }

    /**
     * 获取菜品分类
     */
    private String getDishCategory(String dishId) {
        try {
            Dish dish = dishMapper.selectById(dishId);
            return dish != null ? dish.getCategory() : null;
        } catch (Exception e) {
            log.warn("获取菜品分类失败：dishId={}", dishId);
            return null;
        }
    }

    /**
     * 计算口味偏好
     */
    private Map<String, Double> calculateFlavorPreference(List<UserBehavior> behaviors) {
        // 简化实现：基于菜品标签统计
        Map<String, Double> flavorPref = new HashMap<>();
        flavorPref.put("spicy", 0.5);
        flavorPref.put("sweet", 0.5);
        flavorPref.put("salty", 0.5);

        // 未来可以基于菜品标签和用户行为计算真实口味偏好

        return flavorPref;
    }

    /**
     * 计算价格偏好
     */
    private UserProfile.PricePreference calculatePricePreference(String userId, List<UserBehavior> behaviors) {
        UserProfile.PricePreference pricePref = new UserProfile.PricePreference();

        // 获取用户下单过的菜品价格
        List<BigDecimal> prices = behaviors.stream()
                .filter(b -> "order".equals(b.getBehaviorType()))
                .map(b -> {
                    Dish dish = dishMapper.selectById(b.getItemId());
                    return dish != null ? dish.getPrice() : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (prices.isEmpty()) {
            // 默认价格范围
            pricePref.setMin(10.0);
            pricePref.setMax(50.0);
            pricePref.setOptimal(25.0);
        } else {
            // 基于历史订单计算
            double minPrice = prices.stream().min(BigDecimal::compareTo).orElse(BigDecimal.valueOf(10)).doubleValue();
            double maxPrice = prices.stream().max(BigDecimal::compareTo).orElse(BigDecimal.valueOf(50)).doubleValue();
            double avgPrice = prices.stream()
                    .mapToDouble(BigDecimal::doubleValue)
                    .average()
                    .orElse(25.0);

            pricePref.setMin(minPrice);
            pricePref.setMax(maxPrice);
            pricePref.setOptimal(avgPrice);
        }

        return pricePref;
    }

    /**
     * 计算营养目标
     */
    private UserProfile.NutritionGoals calculateNutritionGoals(String userId) {
        // 简化实现：返回默认营养目标
        UserProfile.NutritionGoals goals = new UserProfile.NutritionGoals();
        goals.setCalories(2000.0);
        goals.setProtein(60.0);
        goals.setFat(60.0);
        goals.setCarbs(250.0);

        // 未来可以基于用户信息和健康目标计算

        return goals;
    }

    /**
     * 计算最常下单时段
     */
    private String calculateMostFrequentTimePeriod(List<UserBehavior> behaviors) {
        Map<String, Long> periodCounts = behaviors.stream()
                .filter(b -> "order".equals(b.getBehaviorType()))
                .collect(Collectors.groupingBy(
                        b -> getTimePeriod(b.getCreatedTime().toLocalTime()),
                        Collectors.counting()
                ));

        return periodCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("午餐");
    }

    /**
     * 根据时间获取时段
     */
    private String getTimePeriod(java.time.LocalTime time) {
        int hour = time.getHour();
        if (hour >= 6 && hour < 9) return "早餐";
        if (hour >= 11 && hour < 14) return "午餐";
        if (hour >= 17 && hour < 20) return "晚餐";
        return "宵夜";
    }

    /**
     * 获取有行为记录的用户ID列表
     */
    private List<String> getUserIdsWithBehaviors() {
        // TODO: 实现获取所有有行为的用户ID
        return userBehaviorMapper.selectList(null).stream()
                .map(UserBehavior::getUserId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 根据行为类型获取权重
     */
    private int getBehaviorWeight(String behaviorType) {
        if (behaviorType == null) {
            return 1;
        }
        switch (behaviorType) {
            case "order":
                return 5;  // 下单权重最高
            case "cart":
                return 3;  // 加入购物车次之
            case "view":
                return 1;  // 浏览权重最低
            case "click":
                return 2;  // 点击中等权重
            default:
                return 1;
        }
    }
}
