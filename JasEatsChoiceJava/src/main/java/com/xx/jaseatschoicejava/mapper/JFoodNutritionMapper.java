package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.JFoodNutrition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 食物营养成分Mapper
 *
 * @author Claude
 * @since 2026-03-14
 */
@Mapper
public interface JFoodNutritionMapper extends BaseMapper<JFoodNutrition> {

    /**
     * 根据食物名称模糊查询
     *
     * @param foodName 食物名称
     * @return 营养数据列表
     */
    @Select("SELECT * FROM j_food_nutrition WHERE food_name LIKE CONCAT('%', #{foodName}, '%') LIMIT 10")
    List<JFoodNutrition> searchByFoodName(@Param("foodName") String foodName);

    /**
     * 根据食物名称精确查询
     *
     * @param foodName 食物名称
     * @return 营养数据
     */
    @Select("SELECT * FROM j_food_nutrition WHERE food_name = #{foodName} LIMIT 1")
    JFoodNutrition getByFoodName(@Param("foodName") String foodName);

    /**
     * 根据别名查询
     *
     * @param aliasName 别名
     * @return 营养数据
     */
    @Select("SELECT * FROM j_food_nutrition WHERE alias_name LIKE CONCAT('%', #{aliasName}, '%') LIMIT 10")
    List<JFoodNutrition> searchByAliasName(@Param("aliasName") String aliasName);
}
