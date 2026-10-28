package com.xx.jaseatschoicejava.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.dto.RecommendationRequestDTO;
import com.xx.jaseatschoicejava.dto.RecommendationResultDTO;
import com.xx.jaseatschoicejava.entity.Dish;
import com.xx.jaseatschoicejava.enums.MsgType;
import com.xx.jaseatschoicejava.mapper.DishMapper;
import com.xx.jaseatschoicejava.websocket.WebSocketMessageService;
import com.xx.jaseatschoicejava.service.RealtimeRecommendationService;
import com.xx.jaseatschoicejava.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 实时推荐推送服务实现
 */
@Slf4j
@Service
public class RealtimeRecommendationServiceImpl implements RealtimeRecommendationService {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private DishMapper dishMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void pushPersonalizedRecommendations(String userId, List<RecommendationResultDTO> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            log.warn("推荐列表为空，跳过推送：userId={}", userId);
            return;
        }

        try {
            // 构建推送消息
            Map<String, Object> message = new HashMap<>();
            message.put("msgType", MsgType.RECOMMENDATION.getValue());
            message.put("userId", userId);
            message.put("timestamp", System.currentTimeMillis());
            message.put("data", recommendations);

            String messageJson = objectMapper.writeValueAsString(message);

            // 通过WebSocket推送给用户
            sendToUser(userId, messageJson);

            log.info("推送个性化推荐成功：userId={}, 推荐数={}", userId, recommendations.size());

        } catch (Exception e) {
            log.error("推送个性化推荐失败：userId={}", userId, e);
        }
    }

    @Override
    public void pushNewDishRecommendations(String userId, List<Dish> newDishes) {
        if (newDishes == null || newDishes.isEmpty()) {
            return;
        }

        try {
            // 构建推送消息
            Map<String, Object> message = new HashMap<>();
            message.put("msgType", MsgType.NEW_DISH_RECOMMEND.getValue());
            message.put("userId", userId);
            message.put("timestamp", System.currentTimeMillis());
            message.put("data", newDishes);

            String messageJson = objectMapper.writeValueAsString(message);

            // 通过WebSocket推送给用户
            sendToUser(userId, messageJson);

            log.info("推送新菜品推荐成功：userId={}, 新菜品数={}", userId, newDishes.size());

        } catch (Exception e) {
            log.error("推送新菜品推荐失败：userId={}", userId, e);
        }
    }

    @Override
    public void broadcastRecommendationUpdate(String message) {
        try {
            // 构建推送消息
            Map<String, Object> pushMessage = new HashMap<>();
            pushMessage.put("msgType", MsgType.RECOMMENDATION_UPDATE.getValue());
            pushMessage.put("timestamp", System.currentTimeMillis());
            pushMessage.put("message", message);

            String messageJson = objectMapper.writeValueAsString(pushMessage);

            // 广播给所有在线用户
            broadcastToAll(messageJson);

            log.info("广播推荐更新通知成功：message={}", message);

        } catch (Exception e) {
            log.error("广播推荐更新通知失败", e);
        }
    }

    @Override
    public void pushRealtimeRecommendation(String userId, String context) {
        deliverRealtimeRecommendation(userId, context);
    }

    /**
     * 生成并推送实时推荐，返回实际送达的连接数。
     *
     * 0 表示用户不在线，或生成/推送过程中失败（失败已记录日志，不向上抛）。
     */
    public int deliverRealtimeRecommendation(String userId, String context) {
        try {
            // 获取实时推荐
            RecommendationRequestDTO request = new RecommendationRequestDTO();
            request.setUserId(userId);
            request.setScene("realtime");
            request.setLimit(5);

            // 添加上下文信息
            Map<String, Object> contextMap = new HashMap<>();
            contextMap.put("context", context);
            contextMap.put("timePeriod", getCurrentTimePeriod());
            request.setContext(contextMap);

            List<RecommendationResultDTO> recommendations = recommendationService.getRecommendations(request);

            // 推送实时推荐
            Map<String, Object> message = new HashMap<>();
            message.put("msgType", MsgType.REALTIME_RECOMMEND.getValue());
            message.put("userId", userId);
            message.put("timestamp", System.currentTimeMillis());
            message.put("context", context);
            message.put("data", recommendations);

            String messageJson = objectMapper.writeValueAsString(message);

            int delivered = WebSocketMessageService.pushToUser(userId, messageJson);
            if (delivered > 0) {
                log.info("推送实时推荐成功：userId={}, context={}, 送达连接数={}", userId, context, delivered);
            } else {
                // 用户不在线是正常情况，不是错误
                log.debug("用户不在线，跳过实时推荐：userId={}", userId);
            }
            return delivered;

        } catch (Exception e) {
            log.error("推送实时推荐失败：userId={}", userId, e);
            return 0;
        }
    }

    @Override
    @Scheduled(cron = "0 0/30 * * * ?") // 每30分钟执行一次
    public void scheduleRecommendationPush() {
        log.info("开始定时推送推荐任务");

        try {
            Set<String> onlineUsers = getOnlineUsers();

            int attempted = 0;
            int delivered = 0;
            for (String userId : onlineUsers) {
                // 单个用户失败在方法内部已兜住，不会中断整批推送
                String context = "定时推荐 - " + getCurrentTimePeriod();
                attempted++;
                delivered += deliverRealtimeRecommendation(userId, context);
            }

            log.info("定时推荐推送完成：在线用户数={}, 尝试推送={}, 实际送达连接数={}",
                    onlineUsers.size(), attempted, delivered);

        } catch (Exception e) {
            log.error("定时推荐推送任务失败", e);
        }
    }

    /**
     * 向指定用户发送消息
     */
    private void sendToUser(String userId, String message) {
        int delivered = WebSocketMessageService.pushToUser(userId, message);
        if (delivered > 0) {
            log.info("已推送消息给用户：userId={}, 送达连接数={}", userId, delivered);
        } else {
            // 用户不在线是正常情况，不是错误
            log.debug("用户不在线，跳过推送：userId={}", userId);
        }
    }

    /**
     * 向所有在线用户广播消息
     */
    private void broadcastToAll(String message) {
        int delivered = WebSocketMessageService.broadcastToAll(message);
        log.info("已广播消息，送达连接数={}", delivered);
    }

    /**
     * 获取所有在线用户
     */
    private Set<String> getOnlineUsers() {
        return WebSocketMessageService.getOnlineUsers();
    }

    /**
     * 获取当前时间段
     */
    private String getCurrentTimePeriod() {
        int hour = LocalDateTime.now().getHour();
        if (hour >= 6 && hour < 10) return "早餐";
        if (hour >= 10 && hour < 14) return "午餐";
        if (hour >= 14 && hour < 18) return "下午茶";
        if (hour >= 18 && hour < 22) return "晚餐";
        return "夜宵";
    }
}
