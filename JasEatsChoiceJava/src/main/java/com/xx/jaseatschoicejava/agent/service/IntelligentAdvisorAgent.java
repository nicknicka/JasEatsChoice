package com.xx.jaseatschoicejava.agent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 智能顾问Agent（总协调器）
 *
 * 整合所有子Agent，实现Agent路由和协作
 *
 * @author Claude
 * @since 2026-03-22
 */
@Service
public class IntelligentAdvisorAgent {

    private static final Logger log = LoggerFactory.getLogger(IntelligentAdvisorAgent.class);

    @Resource
    private NutritionAgent nutritionAgent;

    @Resource
    private RecommendationAgent recommendationAgent;

    @Resource
    private OrderAssistantAgent orderAssistantAgent;

    @Resource
    private IntentClassifierService intentClassifierService;

    /**
     * 用户对话记忆
     */
    private final Map<String, List<String>> conversationMemories = new ConcurrentHashMap<>();

    /**
     * 处理用户消息（主入口）
     */
    public String chat(String userMessage) {
        return chat(userMessage, "anonymous");
    }

    /**
     * 处理用户消息（带用户ID）
     */
    public String chat(String userMessage, String userId) {
        log.info("IntelligentAdvisorAgent收到消息 [用户:{}]：{}", userId, userMessage);

        try {
            List<String> history = conversationMemories.computeIfAbsent(userId, k -> new ArrayList<>());
            history.add("用户: " + userMessage);

            // 意图识别与Agent路由（使用AI驱动）
            String intent = intentClassifierService.classifyIntent(userMessage);
            log.info("AI识别意图：{}", intent);

            String response;
            switch (intent) {
                case "NUTRITION":
                    response = nutritionAgent.chat(userMessage, userId);
                    break;
                case "RECOMMENDATION":
                    response = recommendationAgent.chat(userMessage, userId);
                    break;
                case "ORDER":
                    response = orderAssistantAgent.chat(userMessage, userId);
                    break;
                case "GREETING":
                case "GENERAL":
                default:
                    response = getWelcomeMessage();
                    break;
            }

            history.add("AI: " + response);

            if (history.size() > 30) {
                history.subList(0, history.size() - 30).clear();
            }

            return response;

        } catch (Exception e) {
            log.error("IntelligentAdvisorAgent处理失败", e);
            return chatWithFallback(userMessage);
        }
    }

    /**
     * 降级模式（直接路由）
     */
    private String chatWithFallback(String userMessage) {
        String intent = intentClassifierService.classifyIntent(userMessage);

        switch (intent) {
            case "NUTRITION":
                return nutritionAgent.chat(userMessage);
            case "RECOMMENDATION":
                return recommendationAgent.chat(userMessage);
            case "ORDER":
                return orderAssistantAgent.chat(userMessage);
            case "GREETING":
                return getWelcomeMessage();
            default:
                return getWelcomeMessage();
        }
    }

    /**
     * 获取欢迎消息
     */
    private String getWelcomeMessage() {
        return """
                👋 您好！我是"佳食宜选"的智能助手，很高兴为您服务！

                我是您的全能AI助手，可以帮您：

                🥗 **营养咨询**
                - 分析食物营养成分
                - 计算每日热量需求
                - 提供健康饮食建议

                🍽️ **美食推荐**
                - 个性化菜品推荐
                - 搜索您想吃的菜
                - 发现热门美食

                📱 **订餐服务**
                - 智能下单
                - 查询订单状态
                - 管理订单

                请告诉我您需要什么帮助？

                您可以说：
                - "苹果有多少卡路里？"（营养咨询）
                - "今天推荐什么菜？"（美食推荐）
                - "我要宫保鸡丁和米饭"（智能下单）
                """;
    }

    /**
     * 清除用户对话记忆
     */
    public void clearMemory(String userId) {
        conversationMemories.remove(userId);

        // 同时清除所有子Agent的记忆
        nutritionAgent.clearMemory(userId);
        recommendationAgent.clearMemory(userId);
        orderAssistantAgent.clearMemory(userId);

        log.info("已清除用户 {} 的所有对话记忆", userId);
    }

    /**
     * 获取对话历史
     */
    public List<String> getChatHistory(String userId) {
        return conversationMemories.getOrDefault(userId, List.of());
    }

    /**
     * 获取所有Agent的对话历史
     */
    public Map<String, List<String>> getAllAgentHistory(String userId) {
        return Map.of(
                "Advisor", getChatHistory(userId),
                "Nutrition", nutritionAgent.getChatHistory(userId),
                "Recommendation", recommendationAgent.getChatHistory(userId),
                "Order", orderAssistantAgent.getChatHistory(userId)
        );
    }
}
