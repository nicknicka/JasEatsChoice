package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.jaseatschoicejava.entity.RejectRecommendation;
import com.xx.jaseatschoicejava.mapper.RejectRecommendationMapper;
import com.xx.jaseatschoicejava.service.RejectRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 推荐拒绝Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RejectRecommendationServiceImpl implements RejectRecommendationService {

    private final RejectRecommendationMapper rejectRecommendationMapper;

    @Override
    public RejectRecommendation addRejectRecord(String userId, String dishId, String reason) {
        RejectRecommendation record = new RejectRecommendation();
        record.setUserId(userId);
        record.setDishId(dishId);
        record.setReason(reason);
        record.setRejectTime(LocalDateTime.now());

        rejectRecommendationMapper.insert(record);

        log.debug("记录推荐拒绝：userId={}, dishId={}", userId, dishId);
        return record;
    }

    @Override
    public int countRejects(String userId, String dishId) {
        return rejectRecommendationMapper.countRejects(userId, dishId);
    }

    @Override
    public List<String> getRejectedDishIds(String userId) {
        return rejectRecommendationMapper.getRejectedDishIds(userId);
    }

    @Override
    public List<String> getFrequentlyRejectedDishIds(String userId, int threshold) {
        return rejectRecommendationMapper.getFrequentlyRejectedDishIds(userId, threshold);
    }

    @Override
    public boolean clearRejectRecord(String userId, String dishId) {
        LambdaQueryWrapper<RejectRecommendation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RejectRecommendation::getUserId, userId)
                .eq(RejectRecommendation::getDishId, dishId);

        int deleted = rejectRecommendationMapper.delete(queryWrapper);

        log.debug("清除拒绝记录：userId={}, dishId={}, deleted={}", userId, dishId, deleted);
        return deleted > 0;
    }
}
