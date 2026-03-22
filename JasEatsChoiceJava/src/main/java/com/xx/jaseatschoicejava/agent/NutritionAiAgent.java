package com.xx.jaseatschoicejava.agent;

/**
 * 营养分析AI Agent接口
 *
 * LangChain4j会自动实现此接口
 * 只需定义方法签名，无需手动实现
 *
 * @author Claude
 * @since 2026-03-22
 */
public interface NutritionAiAgent {

    /**
     * 与Agent对话
     *
     * @param userMessage 用户消息
     * @return Agent回复
     */
    String chat(String userMessage);
}
