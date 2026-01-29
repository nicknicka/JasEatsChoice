package com.xx.jaseatschoicejava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.jaseatschoicejava.entity.UserBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户行为Mapper接口
 */
@Mapper
public interface UserBehaviorMapper extends BaseMapper<UserBehavior> {

    /**
     * 获取用户指定时间之后的行为列表
     */
    @Select("SELECT * FROM user_behavior WHERE user_id = #{userId} AND created_time >= #{startTime} ORDER BY created_time DESC")
    List<UserBehavior> getBehaviorsSince(@Param("userId") String userId, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取用户最近的行为记录
     */
    @Select("SELECT * FROM user_behavior WHERE user_id = #{userId} ORDER BY created_time DESC LIMIT #{limit}")
    List<UserBehavior> getRecentBehaviors(@Param("userId") String userId, @Param("limit") int limit);

    /**
     * 统计用户对不同物品的行为次数
     */
    @Select("SELECT item_id, item_type, COUNT(*) as count FROM user_behavior " +
            "WHERE user_id = #{userId} AND behavior_type = #{behaviorType} " +
            "GROUP BY item_id, item_type ORDER BY count DESC")
    List<Map<String, Object>> countBehaviorsByItem(@Param("userId") String userId, @Param("behaviorType") String behaviorType);

    /**
     * 获取用户交互过的物品ID列表
     */
    @Select("SELECT DISTINCT item_id FROM user_behavior WHERE user_id = #{userId} AND item_type = #{itemType}")
    List<String> getInteractedItems(@Param("userId") String userId, @Param("itemType") String itemType);

    /**
     * 统计用户行为数量（按类型）
     */
    @Select("SELECT behavior_type, COUNT(*) as count FROM user_behavior " +
            "WHERE user_id = #{userId} AND created_time >= #{startTime} " +
            "GROUP BY behavior_type")
    List<Map<String, Object>> countBehaviorsByType(@Param("userId") String userId, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取用户最近下单的菜品
     */
    @Select("SELECT item_id FROM user_behavior " +
            "WHERE user_id = #{userId} AND behavior_type = 'order' AND item_type = 'dish' " +
            "ORDER BY created_time DESC LIMIT #{limit}")
    List<String> getRecentOrderedDishes(@Param("userId") String userId, @Param("limit") int limit);

    /**
     * 检查用户是否对某个物品有过特定行为
     */
    @Select("SELECT COUNT(*) FROM user_behavior " +
            "WHERE user_id = #{userId} AND item_id = #{itemId} AND behavior_type = #{behaviorType}")
    int checkBehaviorExists(@Param("userId") String userId, @Param("itemId") String itemId, @Param("behaviorType") String behaviorType);

    /**
     * 获取用户喜欢的菜品（下单或收藏），按最近行为时间排序
     */
    @Select("SELECT item_id FROM user_behavior " +
            "WHERE user_id = #{userId} AND item_type = 'dish' " +
            "AND behavior_type IN ('order', 'favorite') " +
            "GROUP BY item_id " +
            "ORDER BY MAX(created_time) DESC")
    List<String> getUserLikedDishes(@Param("userId") String userId);
}
