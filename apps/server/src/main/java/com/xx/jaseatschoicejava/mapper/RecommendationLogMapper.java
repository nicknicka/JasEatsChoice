package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.RecommendationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 推荐记录Mapper接口
 */
@Mapper
public interface RecommendationLogMapper extends BaseMapper<RecommendationLog> {

    /**
     * 获取用户指定时间内的推荐记录
     */
    @Select("SELECT * FROM recommendation_log WHERE user_id = #{userId} AND created_time >= #{startTime} ORDER BY created_time DESC")
    List<RecommendationLog> getRecommendationsSince(@Param("userId") String userId, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取推荐批次的所有记录
     */
    @Select("SELECT * FROM recommendation_log WHERE recommendation_id = #{recommendationId} ORDER BY rank")
    List<RecommendationLog> getByRecommendationId(@Param("recommendationId") String recommendationId);

    /**
     * 统计推荐效果（点击率、转化率等）
     */
    @Select("SELECT " +
            "COUNT(*) as total_recommendations, " +
            "SUM(CASE WHEN is_clicked = TRUE THEN 1 ELSE 0 END) as total_clicks, " +
            "SUM(CASE WHEN is_ordered = TRUE THEN 1 ELSE 0 END) as total_orders, " +
            "AVG(score) as avg_score " +
            "FROM recommendation_log " +
            "WHERE user_id = #{userId} AND created_time >= #{startTime}")
    Map<String, Object> getRecommendationStats(@Param("userId") String userId, @Param("startTime") LocalDateTime startTime);

    /**
     * 更新推荐反馈状态
     */
    void updateFeedback(@Param("userId") String userId, @Param("dishId") String dishId, @Param("recommendationId") String recommendationId, @Param("isClicked") Boolean isClicked, @Param("isOrdered") Boolean isOrdered);
}
