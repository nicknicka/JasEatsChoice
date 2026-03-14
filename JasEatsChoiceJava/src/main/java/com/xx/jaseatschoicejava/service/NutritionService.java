package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.Nutrition;

import java.util.List;

/**
 * 营养数据库服务接口
 *
 * @author Claude
 * @since 2026-03-14
 */
public interface NutritionService extends IService<Nutrition> {

    /**
     * 根据食物名称搜索（模糊匹配）
     *
     * @param foodName 食物名称
     * @return 营养数据列表
     */
    List<Nutrition> searchByFoodName(String foodName);

    /**
     * 根据食物名称精确查询
     *
     * @param foodName 食物名称
     * @return 营养数据
     */
    Nutrition getByFoodName(String foodName);

    /**
     * 根据食物代码查询
     *
     * @param foodCode 食物代码
     * @return 营养数据
     */
    Nutrition getByFoodCode(String foodCode);
}
