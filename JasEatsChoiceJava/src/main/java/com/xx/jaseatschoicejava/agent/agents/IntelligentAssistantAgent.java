package com.xx.jaseatschoicejava.agent.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * L2领域智能体 - 智能助手Agent
 *
 * 综合性智能助手，能够处理各类用户问题并智能路由到相应的L1 Agent
 *
 * @author Claude
 * @since 2026-03-24
 */
public interface IntelligentAssistantAgent {

    /**
     * 与智能助手Agent对话
     *
     * @param userMessage 用户消息
     * @return Agent回复
     */
    @SystemMessage("""
        你是"佳食宜选"的智能助手，能够处理各类用户问题并智能路由到相应的专业Agent。
        """)
    @Agent("""
综合智能助手，负责：

**核心能力：**
1. 处理各类用户问题
2. 智能路由到合适的工具
3. 综合多个维度的信息
4. 提供友好的交互体验

**输入：**
- 用户任意问题
- 用户ID

**输出：**
- 问题答案
- 操作结果
""")
    String chat(@V("userMessage") String userMessage);
}
