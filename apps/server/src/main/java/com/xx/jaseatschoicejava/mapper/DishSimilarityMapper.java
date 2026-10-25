package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.DishSimilarity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 菜品相似度Mapper接口
 */
@Mapper
public interface DishSimilarityMapper extends BaseMapper<DishSimilarity> {

    /**
     * 获取菜品的相似菜品列表（按相似度降序）
     */
    @Select("SELECT * FROM dish_similarity WHERE dish_id_a = #{dishId} AND similarity_type = #{similarityType} ORDER BY similarity_score DESC LIMIT #{limit}")
    List<DishSimilarity> getSimilarDishes(@Param("dishId") String dishId, @Param("similarityType") String similarityType, @Param("limit") int limit);

    /**
     * 获取菜品的高相似度菜品（相似度 > 阈值）
     */
    @Select("SELECT * FROM dish_similarity WHERE dish_id_a = #{dishId} AND similarity_type = #{similarityType} AND similarity_score >= #{threshold} ORDER BY similarity_score DESC")
    List<DishSimilarity> getHighSimilarityDishes(@Param("dishId") String dishId, @Param("similarityType") String similarityType, @Param("threshold") BigDecimal threshold);

    /**
     * 获取两个菜品之间的相似度
     */
    @Select("SELECT * FROM dish_similarity WHERE dish_id_a = #{dishIdA} AND dish_id_b = #{dishIdB} AND similarity_type = #{similarityType}")
    DishSimilarity getSimilarity(@Param("dishIdA") String dishIdA, @Param("dishIdB") String dishIdB, @Param("similarityType") String similarityType);

    /**
     * 批量插入相似度数据
     */
    int batchInsert(@Param("similarities") List<DishSimilarity> similarities);

    /**
     * 删除菜品的旧相似度数据（用于重新计算）
     */
    int deleteByDishIdAndType(@Param("dishId") String dishId, @Param("similarityType") String similarityType);
}
