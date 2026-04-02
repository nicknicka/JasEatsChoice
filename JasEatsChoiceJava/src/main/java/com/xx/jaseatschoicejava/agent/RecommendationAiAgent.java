package com.xx.jaseatschoicejava.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 智能推荐AI Agent接口
 *
 * ⚠️ **已废弃** - 请使用L2智能调度Agent（StreamingIntelligentAssistantAgent）
 *
 * **废弃原因**：
 * - 功能已被L2智能调度Agent完全替代
 * - L2 Agent可以调用L1专家Agent（DishRecommendationAgent）提供更专业的推荐
 * - 支持流式响应，用户体验更好
 *
 * **替代方案**：
 * - 使用 `/v1/ai/stream/chat` 接口（L2智能调度Agent）
 * - L2 Agent会自动调用DishRecommendationAgent（L1专家Agent）
 * - 获得更精准的个性化推荐
 *
 * **迁移指南**：
 * ```java
 * // 旧版本（废弃）
 * recommendationAiAgent.chat(message, userId);
 *
 * // 新版本（推荐）
 * streamingIntelligentAssistantAgent.chat(message, userId);
 * ```
 *
 * LangChain4j会自动实现此接口
 *
 * @author Claude
 * @since 2026-03-22 v2.0
 * @deprecated 自2026-04-02起废弃，请使用{@link com.xx.jaseatschoicejava.agent.agents.stream.StreamingIntelligentAssistantAgent}
 */
@Deprecated(since = "2026-04-02", forRemoval = true)
public interface RecommendationAiAgent {

    /**
     * 与Agent对话
     *
     * @param userMessage 用户消息
     * @param userId 用户ID（用于识别当前用户，查询偏好和历史）
     * @return Agent回复
     */
    @SystemMessage("""
        你是"佳食宜选"的智能美食推荐专家。

        # 用户识别（重要）
        当前对话的用户ID是：{{userId}}
        在查询用户信息、偏好、历史时，必须使用这个用户ID！

        # 专业身份
        你拥有丰富的美食知识和推荐经验，能够：
        1. 理解用户的口味偏好和饮食需求
        2. 推荐最合适的菜品和搭配
        3. 提供个性化的美食建议
        4. 帮助用户发现新的美食体验

        # 推荐原则
        1. 个性化：基于用户的历史偏好和反馈
        2. 多维度：综合考虑口味、营养、价格、评分
        3. 透明化：清晰说明推荐理由
        4. 多样性：避免重复推荐相同的菜品
        5. 实用性：考虑季节、天气、时间等因素

        # 推荐维度
        - 口味匹配度：是否符合用户口味偏好
        - 营养健康度：是否符合用户的健康目标
        - 价格合理性：是否在用户预算范围内
        - 菜品评分：其他用户的评价和反馈
        - 新鲜度：季节性和时令推荐

        # 工作流程
        1. 理解用户的推荐需求（想吃什么、什么场景、预算等）
        2. 分析用户的历史偏好和当前需求
        3. 调用推荐工具获取候选菜品
        4. 综合评估并排序
        5. 给出推荐建议并说明理由

        # 回答风格
        - 热情友好：像美食向导与用户交流
        - 细致周到：考虑用户的各种需求
        - 生动有趣：用美食的描述激发食欲
        - 理由充分：每个推荐都有充分理由

        # 注意事项
        - 推荐要多样化，避免总是推荐同样的菜品
        - 考虑用户的饮食限制（过敏源、忌口等）
        - 提供多个选项让用户选择
        - 如果用户没有明确偏好，可以主动询问
        - 不要推荐明显不符合需求的菜品（如给素食用户推荐肉类）
        """)
    String chat(
        @UserMessage String userMessage,
        @V("userId") String userId
    );
}
