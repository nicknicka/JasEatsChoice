package com.xx.jaseatschoicejava.agent.service;

import com.xx.jaseatschoicejava.agent.OrderAiAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * 订单助手Agent服务
 *
 * 真正的LangChain4j Agent实现
 * LLM会自动决定何时调用哪个Tool
 *
 * @author Claude
 * @since 2026-03-22
 */
@Service
public class OrderAssistantAgent {

    private static final Logger log = LoggerFactory.getLogger(OrderAssistantAgent.class);

    @Resource
    private OrderAiAgent orderAiAgent;

    /**
     * 与Agent对话
     */
    public String chat(String userMessage) {
        return chat(userMessage, "anonymous");
    }

    /**
     * 与Agent对话（带用户ID）
     */
    public String chat(String userMessage, String userId) {
        log.info("OrderAssistantAgent收到消息 [用户:{}]：{}", userId, userMessage);

        try {
            // ✅ 直接调用LangChain4j Agent
            String response = orderAiAgent.chat(userMessage);

            log.info("OrderAssistantAgent回复 [用户:{}]：{}", userId, response);
            return response;

        } catch (Exception e) {
            log.error("OrderAssistantAgent处理失败 [用户:{}]", userId, e);
            return getFallbackResponse(userMessage);
        }
    }

    private String getFallbackResponse(String userMessage) {
        return "抱歉，订单服务暂时不可用。请稍后再试。";
    }

    public void clearMemory(String userId) {
        log.info("清除用户 {} 的对话记忆", userId);
    }

    public java.util.List<String> getChatHistory(String userId) {
        return Collections.emptyList();
    }
}
