package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.DishFeature;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 菜品特征Mapper接口
 */
@Mapper
public interface DishFeatureMapper extends BaseMapper<DishFeature> {

    /**
     * 根据菜品ID查询特征
     */
    @Select("SELECT * FROM mch_dish_features WHERE dish_id = #{dishId}")
    DishFeature getByDishId(@Param("dishId") String dishId);

    /**
     * 根据菜品ID查询食材 JSON，避免 List 字段缺少 type handler 时返回 null。
     */
    @Select("SELECT COALESCE(CAST(ingredients AS CHAR), '[]') FROM mch_dish_features WHERE dish_id = #{dishId}")
    String getIngredientsJsonByDishId(@Param("dishId") String dishId);

    /**
     * 批量查询菜品营养表中的卡路里。
     */
    @Select("<script>" +
            "SELECT dish_id AS dishId, " +
            "CAST(JSON_UNQUOTE(JSON_EXTRACT(nutrition_info, '$.calories')) AS DECIMAL(10,2)) AS calories " +
            "FROM mch_dish_features " +
            "WHERE nutrition_info IS NOT NULL " +
            "AND JSON_EXTRACT(nutrition_info, '$.calories') IS NOT NULL " +
            "AND dish_id IN " +
            "<foreach collection='dishIds' item='dishId' open='(' separator=',' close=')'>#{dishId}</foreach>" +
            "</script>")
    List<Map<String, Object>> selectNutritionCaloriesByDishIds(@Param("dishIds") List<String> dishIds);

    /**
     * 根据分类查询菜品特征
     */
    @Select("SELECT * FROM mch_dish_features WHERE category = #{category} ORDER BY popularity_score DESC")
    List<DishFeature> getByCategory(@Param("category") String category);

    /**
     * 查询热门菜品特征（按热度分数排序）
     */
    @Select("SELECT * FROM mch_dish_features WHERE popularity_score >= #{threshold} ORDER BY popularity_score DESC LIMIT #{limit}")
    List<DishFeature> getHotDishes(@Param("threshold") double threshold, @Param("limit") int limit);

    /**
     * 根据标签查询菜品特征
     */
    @Select("SELECT * FROM mch_dish_features WHERE JSON_CONTAINS(tags, JSON_QUOTE(#{tag}))")
    List<DishFeature> getByTag(@Param("tag") String tag);

    /**
     * 查询适合特定时段的菜品
     */
    @Select("SELECT * FROM mch_dish_features WHERE JSON_CONTAINS(time_period_tags, JSON_QUOTE(#{timePeriod}))")
    List<DishFeature> getByTimePeriod(@Param("timePeriod") String timePeriod);

    /**
     * 获取所有菜品特征
     */
    @Select("SELECT * FROM mch_dish_features ORDER BY popularity_score DESC")
    List<DishFeature> getAllFeatures();

    /**
     * 批量插入菜品特征
     */
    int batchInsert(@Param("features") List<DishFeature> features);

    /**
     * 批量更新菜品特征
     */
    int batchUpdate(@Param("features") List<DishFeature> features);
}
