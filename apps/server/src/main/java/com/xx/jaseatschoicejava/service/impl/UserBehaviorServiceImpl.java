package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.dto.UserBehaviorDTO;
import com.xx.jaseatschoicejava.entity.UserBehavior;
import com.xx.jaseatschoicejava.mapper.UserBehaviorMapper;
import com.xx.jaseatschoicejava.service.UserBehaviorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户行为服务实现
 */
@Slf4j
@Service
public class UserBehaviorServiceImpl implements UserBehaviorService {

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Override
    public void recordBehavior(UserBehaviorDTO behaviorDTO) {
        if (behaviorDTO == null || behaviorDTO.getUserId() == null) {
            log.warn("用户行为记录失败：参数为空");
            return;
        }

        try {
            UserBehavior behavior = new UserBehavior();
            behavior.setUserId(behaviorDTO.getUserId());
            behavior.setBehaviorType(behaviorDTO.getBehaviorType());
            behavior.setItemType(behaviorDTO.getItemType());
            behavior.setItemId(behaviorDTO.getItemId());
            behavior.setContext(behaviorDTO.getContext());
            behavior.setDuration(behaviorDTO.getDuration());
            behavior.setCreatedTime(LocalDateTime.now());

            userBehaviorMapper.insert(behavior);

            log.debug("用户行为记录成功：userId={}, type={}, item={}",
                    behaviorDTO.getUserId(),
                    behaviorDTO.getBehaviorType(),
                    behaviorDTO.getItemId());

        } catch (Exception e) {
            log.error("用户行为记录失败：{}", behaviorDTO, e);
        }
    }

    @Override
    @Async
    public void recordBehaviorAsync(UserBehaviorDTO behaviorDTO) {
        recordBehavior(behaviorDTO);
    }

    @Override
    public List<UserBehavior> getBehaviorsSince(String userId, LocalDateTime startTime) {
        return userBehaviorMapper.getBehaviorsSince(userId, startTime);
    }

    @Override
    public List<UserBehavior> getRecentBehaviors(String userId, int limit) {
        return userBehaviorMapper.getRecentBehaviors(userId, limit);
    }

    @Override
    public List<Map<String, Object>> countBehaviorsByItem(String userId, String behaviorType) {
        return userBehaviorMapper.countBehaviorsByItem(userId, behaviorType);
    }

    @Override
    public List<String> getInteractedItems(String userId, String itemType) {
        return userBehaviorMapper.getInteractedItems(userId, itemType);
    }

    @Override
    public List<Map<String, Object>> countBehaviorsByType(String userId, LocalDateTime startTime) {
        return userBehaviorMapper.countBehaviorsByType(userId, startTime);
    }

    @Override
    public List<String> getRecentOrderedDishes(String userId, int limit) {
        return userBehaviorMapper.getRecentOrderedDishes(userId, limit);
    }

    @Override
    public boolean checkBehaviorExists(String userId, String itemId, String behaviorType) {
        return userBehaviorMapper.checkBehaviorExists(userId, itemId, behaviorType) > 0;
    }

    @Override
    public List<String> getUserLikedDishes(String userId) {
        return userBehaviorMapper.getUserLikedDishes(userId);
    }

    /**
     * 获取行为权重
     * 不同行为类型对用户画像的贡献度不同
     */
    public int getBehaviorWeight(String behaviorType) {
        return switch (behaviorType) {
            case "order" -> 5;      // 下单权重最高
            case "favorite" -> 3;   // 收藏
            case "click" -> 2;      // 点击
            case "view" -> 1;       // 浏览权重最低
            case "reject" -> -3;    // 拒绝（负权重）
            case "share" -> 4;      // 分享
            default -> 1;
        };
    }
}
