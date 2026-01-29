package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.RejectRecommendation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 推荐拒绝行为Mapper
 */
@Mapper
public interface RejectRecommendationMapper extends BaseMapper<RejectRecommendation> {

    /**
     * 统计用户对某个菜品的拒绝次数
     * @param userId 用户ID
     * @param dishId 菜品ID
     * @return 拒绝次数
     */
    @Select("SELECT COUNT(*) FROM t_reject_recommendation WHERE user_id = #{userId} AND dish_id = #{dishId}")
    int countRejects(@Param("userId") String userId, @Param("dishId") String dishId);

    /**
     * 查询用户所有被拒绝的菜品ID列表
     * @param userId 用户ID
     * @return 菜品ID列表
     */
    @Select("SELECT DISTINCT dish_id FROM t_reject_recommendation WHERE user_id = #{userId}")
    List<String> getRejectedDishIds(@Param("userId") String userId);

    /**
     * 查询用户被拒绝超过指定次数的菜品ID列表
     * @param userId 用户ID
     * @param threshold 拒绝次数阈值
     * @return 菜品ID列表
     */
    @Select("SELECT dish_id FROM t_reject_recommendation " +
            "WHERE user_id = #{userId} " +
            "GROUP BY dish_id " +
            "HAVING COUNT(*) >= #{threshold}")
    List<String> getFrequentlyRejectedDishIds(@Param("userId") String userId, @Param("threshold") int threshold);
}
