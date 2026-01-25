package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.jaseatschoicejava.dto.RecommendationRequestDTO;
import com.xx.jaseatschoicejava.dto.RecommendationResultDTO;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.entity.DishFeature;
import com.xx.jaseatschoicejava.entity.RecommendationLog;
import com.xx.jaseatschoicejava.entity.UserBehavior;
import com.xx.jaseatschoicejava.entity.UserProfile;
import com.xx.jaseatschoicejava.mapper.DishMapper;
import com.xx.jaseatschoicejava.mapper.DishFeatureMapper;
import com.xx.jaseatschoicejava.mapper.RecommendationLogMapper;
import com.xx.jaseatschoicejava.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐服务实现
 * 整合多种召回策略和排序策略
 */
@Slf4j
@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Autowired
    private UserBehaviorService userBehaviorService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private DishFeatureService dishFeatureService;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFeatureMapper dishFeatureMapper;

    @Autowired
    private RecommendationLogMapper recommendationLogMapper;

    @Autowired
    private ZhipuAIService zhipuAIService;

    @Override
    public List<RecommendationResultDTO> getRecommendations(RecommendationRequestDTO request) {
        log.info("开始生成推荐：userId={}, scene={}, limit={}",
                request.getUserId(), request.getScene(), request.getLimit());

        String userId = request.getUserId();
        int limit = request.getLimit() != null ? request.getLimit() : 20;
        Map<String, Object> context = request.getContext() != null ? request.getContext() : new HashMap<>();

        // 1. 获取用户画像
        UserProfile profile = userProfileService.getUserProfile(userId);

        // 2. 执行多种召回策略
        List<Dish> recalledDishes = executeRecallStrategies(profile, context, limit);

        // 3. 排序
        List<Dish> rankedDishes = rankDishes(recalledDishes, profile, context);

        // 4. 多样性处理
        List<Dish> diversifiedDishes = ensureDiversity(rankedDishes, limit);

        // 5. 转换为DTO并生成推荐理由
        List<RecommendationResultDTO> results = convertToDTO(diversifiedDishes, profile, context);

        // 6. 记录推荐日志
        saveRecommendationLog(userId, results);

        log.info("推荐生成完成：返回{}个推荐", results.size());
        return results;
    }

    @Override
    @Transactional
    public void recordFeedback(String userId, String dishId, String recommendationId,
                             Boolean isClicked, Boolean isOrdered) {
        try {
            // 更新推荐记录
            QueryWrapper<RecommendationLog> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("recommendation_id", recommendationId)
                    .eq("dish_id", dishId)
                    .eq("user_id", userId)
                    .orderByDesc("created_time")
                    .last("LIMIT 1");

            RecommendationLog recommendationLog = recommendationLogMapper.selectOne(queryWrapper);

            if (recommendationLog != null) {
                recommendationLog.setIsClicked(isClicked);
                recommendationLog.setIsOrdered(isOrdered);
                if (isClicked || isOrdered) {
                    recommendationLog.setFeedbackTime(LocalDateTime.now());
                }
                recommendationLogMapper.updateById(recommendationLog);
            }

            // 如果用户下单了，触发实时画像更新
            if (Boolean.TRUE.equals(isOrdered)) {
                userProfileService.updateUserProfileOnBehavior(userId, "order", dishId);
            }

            log.debug("推荐反馈记录成功：userId={}, dishId={}, clicked={}, ordered={}",
                    userId, dishId, isClicked, isOrdered);
        } catch (Exception e) {
            log.error("记录推荐反馈失败：userId={}, dishId={}", userId, dishId, e);
        }
    }

    @Override
    public List<RecommendationResultDTO> refreshRecommendations(String userId) {
        // 重新调用推荐接口
        RecommendationRequestDTO request = new RecommendationRequestDTO();
        request.setUserId(userId);
        request.setScene("refresh");
        request.setLimit(20);

        return getRecommendations(request);
    }

    @Override
    public List<Dish> replaceRecommendDishes(String userId, List<String> dishIds) {
        // 获取用户画像
        UserProfile profile = userProfileService.getUserProfile(userId);

        // 获取候选菜品（排除用户要替换的）
        List<Dish> allDishes = dishMapper.selectList(null);
        List<Dish> candidates = allDishes.stream()
                .filter(dish -> !dishIds.contains(String.valueOf(dish.getId())))
                .filter(Dish::getStatus)
                .collect(Collectors.toList());

        // 应用用户偏好筛选
        if (profile != null && profile.getDietGoal() != null) {
            String goal = profile.getDietGoal();
            if ("low_calorie".equals(goal)) {
                candidates = candidates.stream()
                        .filter(dish -> dish.getCalorie() != null && dish.getCalorie() < 400)
                        .collect(Collectors.toList());
            }
        }

        // 随机选择指定数量的菜品
        Collections.shuffle(candidates);
        return candidates.stream()
                .limit(dishIds.size())
                .collect(Collectors.toList());
    }

    @Override
    public List<Dish> filterRecommendDishes(String userId, String category,
                                                   Integer minCalorie, Integer maxCalorie,
                                                   BigDecimal minPrice, BigDecimal maxPrice) {
        List<Dish> allDishes = dishMapper.selectList(null);

        // 应用筛选条件
        List<Dish> filtered = allDishes.stream()
                .filter(Dish::getStatus)
                .filter(dish -> category == null || category.equals(dish.getCategory()))
                .filter(dish -> minCalorie == null || dish.getCalorie() == null || dish.getCalorie() >= minCalorie)
                .filter(dish -> maxCalorie == null || dish.getCalorie() == null || dish.getCalorie() <= maxCalorie)
                .filter(dish -> minPrice == null || dish.getPrice() == null || dish.getPrice().compareTo(minPrice) >= 0)
                .filter(dish -> maxPrice == null || dish.getPrice() == null || dish.getPrice().compareTo(maxPrice) <= 0)
                .collect(Collectors.toList());

        return filtered;
    }

    @Override
    public String getRecommendationReason(String dishId, String userId) {
        try {
            // 获取菜品信息
            Dish dish = dishMapper.selectById(dishId);
            if (dish == null) {
                return "暂无推荐理由";
            }

            // 获取用户画像
            UserProfile profile = userProfileService.getUserProfile(userId);

            // 构建用户画像信息
            Map<String, Object> userProfileInfo = new HashMap<>();
            if (profile != null) {
                if (profile.getDietGoal() != null) {
                    userProfileInfo.put("dietGoal", profile.getDietGoal());
                }
                if (profile.getFlavorPreference() != null) {
                    userProfileInfo.put("flavorPreference", profile.getFlavorPreference());
                }
            }

            // 构建上下文信息
            Map<String, Object> context = new HashMap<>();
            context.put("timePeriod", getCurrentTimePeriod());

            // 调用AI生成推荐理由
            return zhipuAIService.generateRecommendationReason(
                    dish.getName() != null ? dish.getName() : "该菜品",
                    userProfileInfo,
                    context
            );
        } catch (Exception e) {
            log.error("生成推荐理由失败", e);
            return "根据您的口味偏好推荐";
        }
    }

    /**
     * 获取当前时间段
     */
    private String getCurrentTimePeriod() {
        int hour = java.time.LocalDateTime.now().getHour();
        if (hour >= 6 && hour < 10) return "早餐";
        if (hour >= 10 && hour < 14) return "午餐";
        if (hour >= 14 && hour < 18) return "下午茶";
        if (hour >= 18 && hour < 22) return "晚餐";
        return "夜宵";
    }

    /**
     * 执行多种召回策略
     */
    private List<Dish> executeRecallStrategies(UserProfile profile, Map<String, Object> context, int totalCount) {
        List<Dish> allCandidates = new ArrayList<>();

        // 1. 用户画像召回（权重40%）
        List<Dish> profileRecall = userProfileRecall(profile, (int)(totalCount * 0.4));
        allCandidates.addAll(profileRecall);

        // 2. 协同过滤召回（权重30%）
        List<Dish> collaborativeRecall = collaborativeFilteringRecall(profile, (int)(totalCount * 0.3));
        allCandidates.addAll(collaborativeRecall);

        // 3. 热门菜品召回（权重20%）
        List<Dish> hotRecall = hotDishRecall((int)(totalCount * 0.2));
        allCandidates.addAll(hotRecall);

        // 4. 上下文召回（权重10%）
        List<Dish> contextRecall = contextRecall(context, (int)(totalCount * 0.1));
        allCandidates.addAll(contextRecall);

        // 去重
        Map<String, Dish> uniqueDishes = new HashMap<>();
        for (Dish dish : allCandidates) {
            // 保留每个菜品的最高分版本
            uniqueDishes.computeIfAbsent(dish.getId(), k -> dish);
        }

        return new ArrayList<>(uniqueDishes.values());
    }

    /**
     * 用户画像召回
     */
    private List<Dish> userProfileRecall(UserProfile profile, int count) {
        if (profile == null || profile.getPreferenceTags() == null || profile.getPreferenceTags().isEmpty()) {
            return new ArrayList<>();
        }

        List<Dish> allDishes = dishMapper.selectList(null);

        // 基于偏好标签匹配
        List<Dish> matchedDishes = allDishes.stream()
                .filter(dish -> profile.getPreferenceTags().stream()
                        .anyMatch(tag -> tag.getTag().equals(dish.getCategory())))
                .limit(count * 2) // 获取更多候选
                .collect(Collectors.toList());

        // 按偏好分数排序
        matchedDishes.sort((a, b) -> {
            double scoreA = getTagScore(a.getCategory(), profile.getPreferenceTags());
            double scoreB = getTagScore(b.getCategory(), profile.getPreferenceTags());
            return Double.compare(scoreB, scoreA);
        });

        return matchedDishes.stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * 协同过滤召回
     */
    private List<Dish> collaborativeFilteringRecall(UserProfile profile, int count) {
        // 获取用户喜欢的菜品
        List<String> likedDishes = userBehaviorService.getUserLikedDishes(profile.getUserId());
        if (likedDishes.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取这些菜品的相似菜品
        Set<String> similarDishIds = new HashSet<>();
        for (String dishId : likedDishes) {
            List<com.xx.jaseatschoicejava.entity.DishSimilarity> similarities =
                    dishFeatureService.getSimilarDishes(dishId,
                            com.xx.jaseatschoicejava.entity.DishSimilarity.SimilarityType.CONTENT.getCode(),
                            5);

            for (com.xx.jaseatschoicejava.entity.DishSimilarity similarity : similarities) {
                if (similarity.isHighSimilarity()) {
                    similarDishIds.add(similarity.getDishIdB());
                }
            }
        }

        // 转换为菜品对象
        List<Dish> result = new ArrayList<>();
        for (String dishId : similarDishIds) {
            Dish dish = dishMapper.selectById(dishId);
            if (dish != null && dish.getStatus()) {
                result.add(dish);
            }
        }

        // 按与用户喜欢菜品的平均相似度排序
        result.sort((a, b) -> {
            double avgSimA = likedDishes.stream()
                    .mapToDouble(liked -> dishFeatureService.calculateContentSimilarity(
                            String.valueOf(a.getId()), liked))
                    .average()
                    .orElse(0.0);
            double avgSimB = likedDishes.stream()
                    .mapToDouble(liked -> dishFeatureService.calculateContentSimilarity(
                            String.valueOf(b.getId()), liked))
                    .average()
                    .orElse(0.0);
            return Double.compare(avgSimB, avgSimA);
        });

        return result.stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * 热门菜品召回
     */
    private List<Dish> hotDishRecall(int count) {
        // 获取热门菜品特征列表
        List<DishFeature> hotFeatures = dishFeatureMapper.getHotDishes(0.3, count);

        // 转换为Dish对象
        List<Dish> hotDishes = new ArrayList<>();
        for (DishFeature feature : hotFeatures) {
            Dish dish = dishMapper.selectById(feature.getDishId());
            if (dish != null && dish.getStatus()) {
                hotDishes.add(dish);
            }
        }

        return hotDishes;
    }

    /**
     * 上下文召回
     */
    private List<Dish> contextRecall(Map<String, Object> context, int count) {
        List<Dish> allDishes = dishMapper.selectList(null);

        // 基于时间
        String timePeriod = (String) context.get("timePeriod");
        // 基于天气
        String weather = (String) context.get("weather");

        return allDishes.stream()
                .filter(dish -> {
                    DishFeature feature = dishFeatureService.getDishFeature(String.valueOf(dish.getId()));
                    if (feature == null) return false;

                    // 时间匹配
                    if (timePeriod != null && !feature.getTimePeriodTags().contains(timePeriod)) {
                        return false;
                    }

                    // 天气匹配
                    if (weather != null) {
                        if ("hot".equals(weather) && !feature.getTags().contains("清爽")) {
                            // 热天推荐清爽的
                        }
                    }

                    return true;
                })
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * 排序菜品
     */
    private List<Dish> rankDishes(List<Dish> dishes, UserProfile profile, Map<String, Object> context) {
        // 为每道菜计算最终得分
        for (Dish dish : dishes) {
            double finalScore = calculateFinalScore(dish, profile, context);
            dish.setScore(BigDecimal.valueOf(finalScore).setScale(4, RoundingMode.HALF_UP));
        }

        // 按分数降序排序
        return dishes.stream()
                .sorted((a, b) -> b.getScore().compareTo(a.getScore()))
                .collect(Collectors.toList());
    }

    /**
     * 计算最终得分
     */
    private double calculateFinalScore(Dish dish, UserProfile profile, Map<String, Object> context) {
        double score = 0.0;

        // 1. 用户偏好匹配得分（权重40%）
        if (profile != null && profile.getPreferenceTags() != null) {
            double tagScore = getTagScore(dish.getCategory(), profile.getPreferenceTags());
            score += tagScore * 0.4;
        }

        // 2. 菜品热度得分（权重20%）
        DishFeature feature = dishFeatureService.getDishFeature(String.valueOf(dish.getId()));
        if (feature != null && feature.getPopularityScore() != null) {
            score += feature.getPopularityScore().doubleValue() * 0.2;
        }

        // 3. 时间上下文得分（权重15%）
        String timePeriod = (String) context.get("timePeriod");
        if (timePeriod != null && feature != null &&
            feature.getTimePeriodTags().contains(timePeriod)) {
            score += 0.15;
        }

        // 4. 新鲜度得分（权重15%）
        // TODO: 基于菜品创建时间或上架时间
        score += 0.15;

        // 5. 多样性得分（权重10%）
        // TODO: 与已推荐菜品的多样性计算
        score += 0.1;

        return score;
    }

    /**
     * 保证多样性
     */
    private List<Dish> ensureDiversity(List<Dish> dishes, int limit) {
        // 简化实现：按类别分组，确保多样性
        Map<String, List<Dish>> categoryGroups = new HashMap<>();
        for (Dish dish : dishes) {
            String category = dish.getCategory();
            categoryGroups.computeIfAbsent(category, k -> new ArrayList<>()).add(dish);
        }

        List<Dish> result = new ArrayList<>();
        int maxPerCategory = (int) Math.ceil(limit * 0.4); // 每个类别最多40%

        // 轮流从不同类别选取
        List<String> categories = new ArrayList<>(categoryGroups.keySet());
        Collections.shuffle(categories);

        int[] categoryIndex = {0};
        int[] totalAdded = {0};

        for (int i = 0; i < categories.size() && totalAdded[0] < limit; i++) {
            String category = categories.get(i);
            List<Dish> categoryDishes = categoryGroups.get(category);

            for (int j = 0; j < Math.min(maxPerCategory, categoryDishes.size()); j++) {
                if (totalAdded[0] >= limit) break;
                result.add(categoryDishes.get(j));
                totalAdded[0]++;
            }
        }

        // 如果还没达到limit，从剩余菜品中补充
        if (result.size() < limit) {
            Set<String> addedDishIds = result.stream()
                    .map(d -> String.valueOf(d.getId()))
                    .collect(Collectors.toSet());

            for (Dish dish : dishes) {
                if (result.size() >= limit) break;
                if (!addedDishIds.contains(String.valueOf(dish.getId()))) {
                    result.add(dish);
                }
            }
        }

        return result.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 转换为DTO并生成推荐理由
     */
    private List<RecommendationResultDTO> convertToDTO(List<Dish> dishes, UserProfile profile,
                                                              Map<String, Object> context) {
        List<RecommendationResultDTO> results = new ArrayList<>();

        for (int i = 0; i < dishes.size(); i++) {
            Dish dish = dishes.get(i);
            RecommendationResultDTO dto = new RecommendationResultDTO();

            dto.setDishId(String.valueOf(dish.getId()));
            dto.setDishName(dish.getName());
            dto.setDishImage(dish.getImage());
            dto.setCategory(dish.getCategory());
            dto.setScore(dish.getScore());
            dto.setRank(i + 1);
            dto.setCalories(dish.getCalorie());
            dto.setPrice(dish.getPrice());
            dto.setRating(dish.getAvgRating() != null ? dish.getAvgRating() : BigDecimal.valueOf(4.5));

            // 生成推荐理由
            RecommendationResultDTO.RecommendationReason reason = new RecommendationResultDTO.RecommendationReason();
            reason.setPrimary(generateReasonPrimary(dish, profile, context));

            List<RecommendationResultDTO.ReasonFactor> factors = generateReasonFactors(dish, profile, context);
            reason.setFactors(factors);

            dto.setReason(reason);

            results.add(dto);
        }

        return results;
    }

    /**
     * 生成主要推荐理由
     */
    private String generateReasonPrimary(Dish dish, UserProfile profile, Map<String, Object> context) {
        List<String> reasons = new ArrayList<>();

        // 用户偏好匹配
        if (profile != null && profile.getPreferenceTags() != null) {
            for (UserProfile.PreferenceTag tag : profile.getPreferenceTags()) {
                if (tag.getTag().equals(dish.getCategory()) && tag.getScore() > 0.6) {
                    reasons.add("您喜欢" + tag.getTag());
                    break;
                }
            }
        }

        // 热门推荐
        DishFeature feature = dishFeatureService.getDishFeature(String.valueOf(dish.getId()));
        if (feature != null && feature.getPopularityScore() != null &&
            feature.getPopularityScore().compareTo(BigDecimal.valueOf(0.7)) >= 0) {
            reasons.add("热门推荐");
        }

        // 上下文匹配
        String timePeriod = (String) context.get("timePeriod");
        if (timePeriod != null && feature != null &&
            feature.getTimePeriodTags().contains(timePeriod)) {
            reasons.add("适合" + timePeriod);
        }

        if (reasons.isEmpty()) {
            return "系统推荐";
        }

        return String.join("，", reasons);
    }

    /**
     * 生成推荐理由因素
     */
    private List<RecommendationResultDTO.ReasonFactor> generateReasonFactors(Dish dish, UserProfile profile,
                                                               Map<String, Object> context) {
        List<RecommendationResultDTO.ReasonFactor> factors = new ArrayList<>();

        // 用户偏好因素
        if (profile != null && profile.getPreferenceTags() != null) {
            for (UserProfile.PreferenceTag tag : profile.getPreferenceTags()) {
                if (tag.getTag().equals(dish.getCategory())) {
                    RecommendationResultDTO.ReasonFactor factor = new RecommendationResultDTO.ReasonFactor();
                    factor.setType("user_preference");
                    factor.setName("您喜欢" + tag.getTag());
                    factor.setScore(tag.getScore());
                    factors.add(factor);
                    break;
                }
            }
        }

        // 热度因素
        DishFeature feature = dishFeatureService.getDishFeature(dish.getId());
        if (feature != null && feature.getPopularityScore() != null) {
            RecommendationResultDTO.ReasonFactor factor = new RecommendationResultDTO.ReasonFactor();
            factor.setType("popularity");
            factor.setName("热度" + Math.round(feature.getPopularityScore().doubleValue() * 100) + "%");
            factor.setScore(feature.getPopularityScore().doubleValue());
            factors.add(factor);
        }

        // 上下文因素
        String timePeriod = (String) context.get("timePeriod");
        if (timePeriod != null && feature != null &&
            feature.getTimePeriodTags().contains(timePeriod)) {
            RecommendationResultDTO.ReasonFactor factor = new RecommendationResultDTO.ReasonFactor();
            factor.setType("context");
            factor.setName("适合" + timePeriod);
            factor.setScore(0.8);
            factors.add(factor);
        }

        return factors;
    }

    /**
     * 保存推荐日志
     */
    private void saveRecommendationLog(String userId, List<RecommendationResultDTO> results) {
        String recommendationId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        for (RecommendationResultDTO result : results) {
            RecommendationLog log = new RecommendationLog();
            log.setUserId(userId);
            log.setRecommendationId(recommendationId);
            log.setDishId(result.getDishId());
            log.setRank(result.getRank());
            log.setScore(result.getScore());
            log.setAlgorithm("hybrid");
            log.setCreatedTime(now);

            // 设置推荐理由
            if (result.getReason() != null) {
                RecommendationLog.RecommendationReason reason = new RecommendationLog.RecommendationReason();
                reason.setPrimary(result.getReason().getPrimary());

                List<RecommendationLog.ReasonFactor> factorList = new ArrayList<>();
                for (RecommendationResultDTO.ReasonFactor dtoFactor : result.getReason().getFactors()) {
                    RecommendationLog.ReasonFactor factor = new RecommendationLog.ReasonFactor();
                    factor.setType(dtoFactor.getType());
                    factor.setName(dtoFactor.getName());
                    factor.setScore(dtoFactor.getScore());
                    factorList.add(factor);
                }
                reason.setFactors(factorList);
                log.setReason(reason);
            }

            recommendationLogMapper.insert(log);
        }
    }

    /**
     * 获取标签匹配分数
     */
    private double getTagScore(String category, List<UserProfile.PreferenceTag> preferenceTags) {
        return preferenceTags.stream()
                .filter(tag -> tag.getTag().equals(category))
                .findFirst()
                .map(UserProfile.PreferenceTag::getScore)
                .orElse(0.0);
    }
}
