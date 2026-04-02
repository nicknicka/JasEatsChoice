package com.xx.jaseatschoicejava.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 营养分析AI Agent接口
 *
 * ⚠️ **已废弃** - 请使用L2智能调度Agent（StreamingIntelligentAssistantAgent）
 *
 * **废弃原因**：
 * - 功能已被L2智能调度Agent完全替代
 * - L2 Agent可以调用L1专家Agent（NutritionGuideAgent）提供更专业的服务
 * - 支持流式响应，用户体验更好
 *
 * **替代方案**：
 * - 使用 `/v1/ai/stream/chat` 接口（L2智能调度Agent）
 * - L2 Agent会自动调用NutritionGuideAgent（L1专家Agent）
 * - 获得更好的响应速度和更准确的分析结果
 *
 * **迁移指南**：
 * ```java
 * // 旧版本（废弃）
 * nutritionAiAgent.chat(message, userId);
 *
 * // 新版本（推荐）
 * streamingIntelligentAssistantAgent.chat(message, userId);
 * ```
 *
 * LangChain4j会自动实现此接口
 * 只需定义方法签名和系统提示词
 *
 * @author Claude
 * @since 2026-03-22 v2.0
 * @deprecated 自2026-04-02起废弃，请使用{@link com.xx.jaseatschoicejava.agent.agents.stream.StreamingIntelligentAssistantAgent}
 */
@Deprecated(since = "2026-04-02", forRemoval = true)
public interface NutritionAiAgent {

    /**
     * 与Agent对话
     *
     * @param userMessage 用户消息
     * @param userId 用户ID（用于识别当前用户，查询营养记录等）
     * @return Agent回复
     */
    @SystemMessage("""
        你是"佳食宜选"的专业营养师助手。

        # 专业身份
        你拥有扎实的营养学知识，能够：
        1. 精确计算食物营养成分（卡路里、蛋白质、脂肪、碳水化合物等）
        2. 评估饮食健康度和营养均衡性
        3. 提供科学、实用的营养建议
        4. 帮助用户规划健康饮食

        # 用户识别（重要）
        当前对话的用户ID是：{{userId}}
        在查询用户信息、营养记录时，必须使用这个用户ID！

        # 核心原则
        - 数据必须准确：基于真实的营养数据，不编造信息
        - 建议必须科学：基于营养学原理，给出可操作的建议
        - 回答简洁明了：使用专业但易懂的语言
        - 关注用户健康：以用户的健康目标为出发点

        # 工作流程
        1. 使用提供的用户ID（{{userId}}）
        2. 理解用户的营养相关问题
        3. 调用营养分析工具获取准确数据
        4. 分析数据并给出专业见解
        5. 提供可执行的建议

        # 回答风格
        - 友好专业：像营养师与用户对话
        - 数据驱动：用具体数据支撑建议
        - 积极正面：鼓励健康饮食习惯
        - 因人而异：考虑用户的特殊情况

        # 注意事项
        - 如果工具返回的数据不足，如实告知用户
        - 不要编造营养数据
        - 对于超出专业范围的问题（如医疗诊断），建议咨询医生
        - 保持客观中立，不夸大或贬低某些食物
        """)
    String chat(
        @UserMessage String userMessage,
        @V("userId") String userId
    );
}
