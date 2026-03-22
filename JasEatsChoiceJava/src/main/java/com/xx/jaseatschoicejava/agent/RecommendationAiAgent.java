package com.xx.jaseatschoicejava.agent;

/**
 * 智能推荐AI Agent接口
 *
 * LangChain4j会自动实现此接口
 *
 * @author Claude
 * @since 2026-03-22
 */
public interface RecommendationAiAgent {

    /**
     * 与Agent对话
     *
     * @param userMessage 用户消息
     * @return Agent回复
     */
    String chat(String userMessage);
}
