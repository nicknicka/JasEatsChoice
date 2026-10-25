package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.dto.UserBehaviorDTO;
import com.xx.jaseatschoicejava.entity.UserBehavior;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户行为服务接口
 */
public interface UserBehaviorService {

    /**
     * 记录用户行为
     */
    void recordBehavior(UserBehaviorDTO behaviorDTO);

    /**
     * 异步记录用户行为
     */
    void recordBehaviorAsync(UserBehaviorDTO behaviorDTO);

    /**
     * 获取用户指定时间之后的行为列表
     */
    List<UserBehavior> getBehaviorsSince(String userId, LocalDateTime startTime);

    /**
     * 获取用户最近的行为记录
     */
    List<UserBehavior> getRecentBehaviors(String userId, int limit);

    /**
     * 统计用户对不同物品的行为次数
     */
    List<Map<String, Object>> countBehaviorsByItem(String userId, String behaviorType);

    /**
     * 获取用户交互过的物品ID列表
     */
    List<String> getInteractedItems(String userId, String itemType);

    /**
     * 统计用户行为数量（按类型）
     */
    List<Map<String, Object>> countBehaviorsByType(String userId, LocalDateTime startTime);

    /**
     * 获取用户最近下单的菜品
     */
    List<String> getRecentOrderedDishes(String userId, int limit);

    /**
     * 检查用户是否对某个物品有过特定行为
     */
    boolean checkBehaviorExists(String userId, String itemId, String behaviorType);

    /**
     * 获取用户喜欢的菜品（下单或收藏）
     */
    List<String> getUserLikedDishes(String userId);
}
