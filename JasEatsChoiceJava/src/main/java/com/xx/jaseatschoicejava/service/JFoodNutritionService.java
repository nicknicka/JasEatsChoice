package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.JFoodNutrition;

import java.util.List;

/**
 * 食物营养成分服务接口
 *
 * @author Claude
 * @since 2026-03-14
 */
public interface JFoodNutritionService extends IService<JFoodNutrition> {

    /**
     * 根据食物名称搜索（模糊匹配）
     *
     * @param foodName 食物名称
     * @return 营养数据列表
     */
    List<JFoodNutrition> searchByFoodName(String foodName);

    /**
     * 根据食物名称精确查询
     *
     * @param foodName 食物名称
     * @return 营养数据
     */
    JFoodNutrition getByFoodName(String foodName);

    /**
     * 根据别名搜索
     *
     * @param aliasName 别名
     * @return 营养数据列表
     */
    List<JFoodNutrition> searchByAliasName(String aliasName);
}
