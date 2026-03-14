package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xx.jaseatschoicejava.dto.NutritionInfo;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.NutritionAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 营养分析服务实现类
 * 优先使用中国食物成分表数据，其次使用t_dish表数据
 *
 * @author Claude
 * @since 2026-03-14
 */
@Slf4j
@Service
public class NutritionAnalysisServiceImpl implements NutritionAnalysisService {

    @Resource
    private DishService dishService;

    @Resource
    private com.xx.jaseatschoicejava.service.NutritionService nutritionService;

    /**
     * 默认份量（克）
     */
    private static final int DEFAULT_PORTION = 100;

    @Override
    public NutritionInfo analyzeNutrition(String foodName) {
        return analyzeNutrition(foodName, DEFAULT_PORTION);
    }

    @Override
    public NutritionInfo analyzeNutrition(String foodName, int portion) {
        log.info("分析食物营养：{}，份量：{}克", foodName, portion);

        try {
            // 1. 优先查询中国食物成分表（最准确）
            com.xx.jaseatschoicejava.entity.Nutrition nutritionData = nutritionService.getByFoodName(foodName);
            if (nutritionData != null) {
                log.info("从中国食物成分表找到食物：{}", foodName);
                return buildNutritionInfoFromDatabase(nutritionData, portion);
            }

            // 2. 尝试模糊匹配
            List<com.xx.jaseatschoicejava.entity.Nutrition> nutritionList = nutritionService.searchByFoodName(foodName);
            if (!nutritionList.isEmpty()) {
                log.info("通过模糊匹配找到食物：{}，匹配结果：{}", foodName, nutritionList.get(0).getFoodName());
                return buildNutritionInfoFromDatabase(nutritionList.get(0), portion);
            }

            // 3. 从t_dish表查询菜品信息（次优）
            QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();
            queryWrapper.like("name", foodName)
                    .eq("is_online", true)
                    .last("LIMIT 1");

            Dish dish = dishService.getOne(queryWrapper);
            if (dish != null) {
                log.info("从t_dish表找到菜品：{}，使用卡路里数据估算其他营养素", foodName);
                return buildNutritionInfoFromDish(dish, portion);
            }

            // 4. 如果数据库中没有，使用默认估算
            log.warn("数据库中未找到食物：{}，使用默认估算", foodName);
            return createDefaultNutritionInfo(foodName, portion);

        } catch (Exception e) {
            log.error("营养分析失败：{}", foodName, e);
            return createDefaultNutritionInfo(foodName, portion);
        }
    }

    /**
     * 从中国食物成分表数据构建营养信息
     */
    private NutritionInfo buildNutritionInfoFromDatabase(com.xx.jaseatschoicejava.entity.Nutrition nutrition, int portion) {
        log.info("使用中国食物成分表数据：{}, 份量：{}克", nutrition.getFoodName(), portion);

        // 按份量比例计算营养值
        BigDecimal ratio = new BigDecimal(portion).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);

        return NutritionInfo.builder()
                .foodName(nutrition.getFoodName())
                .portion(new BigDecimal(portion))
                .calories(nutrition.getEnergyKcal().multiply(ratio))
                .protein(nutrition.getProtein() != null ? nutrition.getProtein().multiply(ratio) : BigDecimal.ZERO)
                .fat(nutrition.getFat() != null ? nutrition.getFat().multiply(ratio) : BigDecimal.ZERO)
                .carbohydrates(nutrition.getCho() != null ? nutrition.getCho().multiply(ratio) : BigDecimal.ZERO)
                .dietaryFiber(nutrition.getDietaryFiber() != null ? nutrition.getDietaryFiber().multiply(ratio) : BigDecimal.ZERO)
                .sodium(nutrition.getNa() != null ? nutrition.getNa().multiply(ratio) : BigDecimal.ZERO)
                .dataSource("中国食物成分表第6版")
                .nutritionGrade(calculateNutritionGradeFromKcal(nutrition.getEnergyKcal()))
                .healthAdvice(generateHealthAdviceFromKcal(nutrition.getEnergyKcal()))
                .build();
    }

    /**
     * 从Dish表数据构建营养信息（使用卡路里估算其他营养素）
     */
    private NutritionInfo buildNutritionInfoFromDish(Dish dish, int portion) {
        log.info("使用t_dish表数据：{}, 份量：{}克", dish.getName(), portion);

        // 按份量比例计算
        BigDecimal ratio = new BigDecimal(portion).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal calories = dish.getCalorie() != null
            ? new BigDecimal(dish.getCalorie()).multiply(ratio)
            : new BigDecimal("250");

        return NutritionInfo.builder()
                .foodName(dish.getName())
                .portion(new BigDecimal(portion))
                .calories(calories)
                .protein(estimateProtein(calories, ratio))
                .fat(estimateFat(calories, ratio))
                .carbohydrates(estimateCarbohydrates(calories, ratio))
                .dataSource("t_dish表+估算")
                .nutritionGrade(calculateNutritionGradeFromKcal(calories))
                .healthAdvice(generateHealthAdviceFromKcal(calories))
                .build();
    }

    /**
     * 根据卡路里计算营养评级
     */
    private String calculateNutritionGradeFromKcal(BigDecimal calories) {
        if (calories.compareTo(new BigDecimal("150")) <= 0) {
            return "A";
        } else if (calories.compareTo(new BigDecimal("250")) <= 0) {
            return "B";
        } else if (calories.compareTo(new BigDecimal("400")) <= 0) {
            return "C";
        } else if (calories.compareTo(new BigDecimal("500")) <= 0) {
            return "D";
        } else {
            return "E";
        }
    }

    /**
     * 根据卡路里生成健康建议
     */
    private String generateHealthAdviceFromKcal(BigDecimal calories) {
        if (calories.compareTo(new BigDecimal("200")) <= 0) {
            return "这是一道低热量菜品，适合减脂期食用。";
        } else if (calories.compareTo(new BigDecimal("400")) >= 0) {
            return "这道菜热量较高，建议适量食用或搭配蔬菜。";
        } else {
            return "适量食用，保持营养均衡。";
        }
    }

    @Override
    public List<NutritionInfo> batchAnalyzeNutrition(List<String> foodNames) {
        if (foodNames == null || foodNames.isEmpty()) {
            return Collections.emptyList();
        }

        return foodNames.stream()
                .map(name -> analyzeNutrition(name, DEFAULT_PORTION))
                .collect(Collectors.toList());
    }

    @Override
    public String getNutritionGrade(String foodName) {
        NutritionInfo info = analyzeNutrition(foodName);
        return info.getNutritionGrade();
    }

    @Override
    public List<String> recommendFoodByNutrition(Integer maxCalories, Integer minProtein, String dietaryRestrictions) {
        log.info("根据营养需求推荐食物：最大卡路里={}，最小蛋白质={}，饮食限制={}",
                maxCalories, minProtein, dietaryRestrictions);

        try {
            QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();

            // 构建查询条件
            if (maxCalories != null && maxCalories > 0) {
                queryWrapper.le("calorie", maxCalories);
            }

            queryWrapper.eq("is_online", true)
                    .orderByDesc("avg_rating")
                    .last("LIMIT 10");

            List<Dish> dishes = dishService.list(queryWrapper);

            return dishes.stream()
                    .map(Dish::getName)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("推荐食物失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 创建默认营养信息（当数据库中没有找到时）
     */
    private NutritionInfo createDefaultNutritionInfo(String foodName, int portion) {
        // 使用基础估算值（基于常见食物的平均值）
        BigDecimal baseCalories = new BigDecimal("250");

        return NutritionInfo.builder()
                .foodName(foodName)
                .portion(new BigDecimal(portion))
                .calories(baseCalories)
                .protein(new BigDecimal("15"))
                .fat(new BigDecimal("10"))
                .carbohydrates(new BigDecimal("30"))
                .dataSource("estimate")
                .nutritionGrade("C")
                .healthAdvice("建议查看具体产品的营养成分表以获取准确信息。")
                .build();
    }

    /**
     * 估算蛋白质含量
     * 基于热量的简单估算（实际应该从数据库获取）
     */
    private BigDecimal estimateProtein(BigDecimal calories, BigDecimal ratio) {
        // 假设蛋白质占总热量的15%，1g蛋白质=4kcal
        return calories.multiply(new BigDecimal("0.15"))
                .divide(new BigDecimal("4"), 1, BigDecimal.ROUND_HALF_UP)
                .multiply(ratio);
    }

    /**
     * 估算脂肪含量
     */
    private BigDecimal estimateFat(BigDecimal calories, BigDecimal ratio) {
        // 假设脂肪占总热量的30%，1g脂肪=9kcal
        return calories.multiply(new BigDecimal("0.30"))
                .divide(new BigDecimal("9"), 1, BigDecimal.ROUND_HALF_UP)
                .multiply(ratio);
    }

    /**
     * 估算碳水化合物含量
     */
    private BigDecimal estimateCarbohydrates(BigDecimal calories, BigDecimal ratio) {
        // 假设碳水化合物占总热量的55%，1g碳水=4kcal
        return calories.multiply(new BigDecimal("0.55"))
                .divide(new BigDecimal("4"), 1, BigDecimal.ROUND_HALF_UP)
                .multiply(ratio);
    }
}
