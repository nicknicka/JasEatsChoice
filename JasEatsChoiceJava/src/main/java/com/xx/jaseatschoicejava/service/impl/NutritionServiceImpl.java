package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.Nutrition;
import com.xx.jaseatschoicejava.mapper.NutritionMapper;
import com.xx.jaseatschoicejava.service.NutritionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 营养数据库服务实现
 *
 * @author Claude
 * @since 2026-03-14
 */
@Slf4j
@Service
public class NutritionServiceImpl extends ServiceImpl<NutritionMapper, Nutrition> implements NutritionService {

    @Resource
    private NutritionMapper nutritionMapper;

    @Override
    public List<Nutrition> searchByFoodName(String foodName) {
        log.debug("搜索营养数据：{}", foodName);
        return nutritionMapper.searchByFoodName(foodName);
    }

    @Override
    public Nutrition getByFoodName(String foodName) {
        log.debug("精确查询营养数据：{}", foodName);
        return nutritionMapper.getByFoodName(foodName);
    }

    @Override
    public Nutrition getByFoodCode(String foodCode) {
        log.debug("根据食物代码查询：{}", foodCode);
        QueryWrapper<Nutrition> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("food_code", foodCode);
        return nutritionMapper.selectOne(queryWrapper);
    }
}
