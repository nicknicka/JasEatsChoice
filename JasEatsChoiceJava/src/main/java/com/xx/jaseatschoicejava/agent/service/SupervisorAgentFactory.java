package com.xx.jaseatschoicejava.agent.service;

import com.xx.jaseatschoicejava.agent.agents.CardRendererAgent;
import com.xx.jaseatschoicejava.agent.config.ChatMemoryFactory;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.observability.AgentListener;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * SupervisorAgent 工厂类
 *
 * 动态创建带监听器的SupervisorAgent实例
 * 支持每个用户独立的ChatMemory（Redis + MySQL混合存储）
 *
 * @author Claude
 * @since 2026-03-26
 */
@Component
public class SupervisorAgentFactory {

    private static final Logger log = LoggerFactory.getLogger(SupervisorAgentFactory.class);

    private final ChatModel supervisorModel;
    private final ChatMemoryFactory chatMemoryFactory;
    private final CardRendererAgent cardRendererAgent;
    private final SupervisorAgent smartRecommendationAgent;
    private final SupervisorAgent healthManagementAgent;
    private final SupervisorAgent fullOrderAgent;
    private final SupervisorAgent intelligentAssistantAgent;

    public SupervisorAgentFactory(
            ChatModel supervisorModel,
            ChatMemoryFactory chatMemoryFactory,
            CardRendererAgent cardRendererAgent,
            @Qualifier("smartRecommendationAgent") SupervisorAgent smartRecommendationAgent,
            @Qualifier("healthManagementAgent") SupervisorAgent healthManagementAgent,
            @Qualifier("fullOrderAgent") SupervisorAgent fullOrderAgent,
            @Qualifier("intelligentAssistantAgent") SupervisorAgent intelligentAssistantAgent) {
        this.supervisorModel = supervisorModel;
        this.chatMemoryFactory = chatMemoryFactory;
        this.cardRendererAgent = cardRendererAgent;
        this.smartRecommendationAgent = smartRecommendationAgent;
        this.healthManagementAgent = healthManagementAgent;
        this.fullOrderAgent = fullOrderAgent;
        this.intelligentAssistantAgent = intelligentAssistantAgent;

        log.info("SupervisorAgentFactory初始化完成");
    }

    /**
     * 创建带监听器的SupervisorAgent
     *
     * @param listener Agent监听器
     * @param userId 用户ID（作为memoryId）
     * @return SupervisorAgent实例
     */
    public SupervisorAgent createWithListener(AgentListener listener, String userId) {
        log.debug("创建带监听器的SupervisorAgent，userId={}", userId);

        return AgenticServices
                .supervisorBuilder()
                .chatModel(supervisorModel)
                // ✅ 使用chatMemoryFactory为每个用户创建独立的ChatMemory
                .chatMemoryProvider(memoryId -> chatMemoryFactory.createChatMemory(userId))
                .name("SupervisorAgent")
                .description("智能调度Agent，协调多个L2领域Agent完成复杂任务")
                .subAgents(
                    smartRecommendationAgent,
                    healthManagementAgent,
                    fullOrderAgent,
                    intelligentAssistantAgent
                )
                .outputKey("supervisorResult")
                .listener(listener)  // ✅ 动态注册监听器
                .supervisorContext("""
                    你是"佳食宜选"的智能监督代理，协调各个领域专家Agent为用户提供全面的服务。

                    你可以调用以下领域专家Agent：
                    - **SmartRecommendationAgent** - 个性化菜品和商家推荐
                    - **HealthManagementAgent** - 营养分析、健康目标和饮食建议
                    - **FullOrderAgent** - 订餐流程和订单管理
                    - **IntelligentAssistantAgent** - 综合问题解答

                    根据用户需求灵活选择最合适的Agent，或协调多个Agent协作完成复杂任务。
                    理解用户的完整需求，提供个性化的解决方案。

                    ⚠️ 输出格式要求：
                    - 子Agent返回的JSON数据，保持原样，不要修改
                    - 使用markdown代码块包裹JSON
                    - 在JSON前后添加自然语言的总结和建议
                    - 不要破坏JSON的结构
                    """)
                .contextGenerationStrategy(dev.langchain4j.agentic.supervisor.SupervisorContextStrategy.CHAT_MEMORY_AND_SUMMARIZATION)
                .responseStrategy(dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy.SCORED)
                .maxAgentsInvocations(10)
                .build();
    }

    /**
     * 渲染卡片格式
     *
     * @param originalResult 原始结果
     * @return 格式化后的结果
     */
    public String renderCards(String originalResult) {
        try {
            log.debug("开始渲染卡片格式，原始结果长度：{}", originalResult.length());
            String rendered = cardRendererAgent.renderCards(originalResult);
            log.debug("卡片渲染完成，结果长度：{}", rendered.length());
            return rendered;
        } catch (Exception e) {
            log.error("卡片渲染失败，返回原始结果", e);
            return originalResult;  // 降级：渲染失败时返回原始结果
        }
    }
}
