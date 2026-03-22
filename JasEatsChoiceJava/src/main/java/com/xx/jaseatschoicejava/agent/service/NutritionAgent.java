package com.xx.jaseatschoicejava.agent.service;

import com.xx.jaseatschoicejava.agent.NutritionAiAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 营养分析Agent服务
 *
 * 真正的LangChain4j Agent实现
 * LLM会自动决定何时调用哪个Tool
 *
 * @author Claude
 * @since 2026-03-22
 */
@Service
public class NutritionAgent {

    private static final Logger log = LoggerFactory.getLogger(NutritionAgent.class);

    @Resource
    private NutritionAiAgent nutritionAiAgent;

    /**
     * 与Agent对话
     *
     * @param userMessage 用户消息
     * @return Agent回复
     */
    public String chat(String userMessage) {
        return chat(userMessage, "anonymous");
    }

    /**
     * 与Agent对话（带用户ID）
     *
     * @param userMessage 用户消息
     * @param userId 用户ID
     * @return Agent回复
     */
    public String chat(String userMessage, String userId) {
        log.info("NutritionAgent收到消息 [用户:{}]：{}", userId, userMessage);

        try {
            // ✅ 直接调用LangChain4j Agent
            // LLM会自动决定调用哪个Tool
            String response = nutritionAiAgent.chat(userMessage);

            log.info("NutritionAgent回复 [用户:{}]：{}", userId, response);
            return response;

        } catch (Exception e) {
            log.error("NutritionAgent处理失败 [用户:{}]", userId, e);
            return getFallbackResponse(userMessage);
        }
    }

    /**
     * 降级响应
     */
    private String getFallbackResponse(String userMessage) {
        return "抱歉，营养分析服务暂时不可用。请稍后再试。";
    }

    /**
     * 清除用户对话记忆（通过LangChain4j自动管理）
     * 注意：LangChain4j的ChatMemory会自动管理对话历史
     * 此方法保留用于兼容性
     */
    public void clearMemory(String userId) {
        log.info("清除用户 {} 的对话记忆（注意：LangChain4j自动管理对话历史）", userId);
        // LangChain4j的ChatMemory是全局共享的，暂不支持按用户清除
        // 如需按用户隔离，需要使用ChatMemoryProvider
    }

    /**
     * 获取对话历史
     * 注意：当前实现使用共享ChatMemory，返回的是全局历史
     */
    public java.util.List<String> getChatHistory(String userId) {
        log.info("获取用户 {} 的对话历史", userId);
        // 暂时返回空列表，因为ChatMemory是内部管理
        return java.util.Collections.emptyList();
    }
}
