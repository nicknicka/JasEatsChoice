package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.Nutrition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 营养数据库Mapper
 *
 * @author Claude
 * @since 2026-03-14
 */
@Mapper
public interface NutritionMapper extends BaseMapper<Nutrition> {

    /**
     * 根据食物名称模糊查询
     *
     * @param foodName 食物名称
     * @return 营养数据列表
     */
    @Select("SELECT * FROM t_nutrition WHERE food_name LIKE CONCAT('%', #{foodName}, '%') LIMIT 10")
    List<Nutrition> searchByFoodName(@Param("foodName") String foodName);

    /**
     * 根据食物名称精确查询
     *
     * @param foodName 食物名称
     * @return 营养数据
     */
    @Select("SELECT * FROM t_nutrition WHERE food_name = #{foodName} LIMIT 1")
    Nutrition getByFoodName(@Param("foodName") String foodName);
}
