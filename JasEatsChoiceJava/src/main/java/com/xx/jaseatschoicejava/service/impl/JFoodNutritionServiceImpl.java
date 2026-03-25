package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.JFoodNutrition;
import com.xx.jaseatschoicejava.mapper.JFoodNutritionMapper;
import com.xx.jaseatschoicejava.service.JFoodNutritionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 食物营养成分服务实现
 *
 * @author Claude
 * @since 2026-03-14
 */
@Slf4j
@Service
public class JFoodNutritionServiceImpl extends ServiceImpl<JFoodNutritionMapper, JFoodNutrition> implements JFoodNutritionService {

    @Resource
    private JFoodNutritionMapper jFoodNutritionMapper;

    @Override
    public List<JFoodNutrition> searchByFoodName(String foodName) {
        log.debug("搜索食物营养数据（j_food_nutrition）：{}", foodName);
        return jFoodNutritionMapper.searchByFoodName(foodName);
    }

    @Override
    public JFoodNutrition getByFoodName(String foodName) {
        log.debug("精确查询食物营养数据（j_food_nutrition）：{}", foodName);
        return jFoodNutritionMapper.getByFoodName(foodName);
    }

    @Override
    public List<JFoodNutrition> searchByAliasName(String aliasName) {
        log.debug("根据别名搜索食物（j_food_nutrition）：{}", aliasName);
        return jFoodNutritionMapper.searchByAliasName(aliasName);
    }
}
