package com.xx.jaseatschoicejava.agent.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * L3 监督代理 (SupervisorAgent)
 *
 * 智能调度L2领域Agent，协调多个Agent完成复杂任务
 *
 * 职责：
 * - 理解用户意图，自动路由到合适的L2 Agent
 * - 协调多个L2 Agent协作完成复杂任务
 * - 综合多个Agent的结果，生成最终回复
 * - 处理Agent调用失败等异常情况
 *
 * @author Claude
 * @since 2026-03-25
 */
public interface SupervisorAgent {

    /**
     * 主对话接口
     *
     * @param userMessage 用户消息
     * @return Agent响应
     */
    String chat(@UserMessage String userMessage);

    /**
     * 带用户ID的对话接口
     *
     * @param userMessage 用户消息
     * @param userId 用户ID
     * @return Agent响应
     */
    String chatWithContext(
            @UserMessage String userMessage,
            @UserMessage String userId
    );
}
