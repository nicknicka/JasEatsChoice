package com.xx.jaseatschoicejava.agent;

/**
 * 订单助手AI Agent接口
 *
 * LangChain4j会自动实现此接口
 *
 * @author Claude
 * @since 2026-03-22
 */
public interface OrderAiAgent {

    /**
     * 与Agent对话
     *
     * @param userMessage 用户消息
     * @return Agent回复
     */
    String chat(String userMessage);
}
