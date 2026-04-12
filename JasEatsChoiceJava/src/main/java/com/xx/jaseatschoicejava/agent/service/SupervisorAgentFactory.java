package com.xx.jaseatschoicejava.agent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.xx.jaseatschoicejava.agent.agents.CardRendererAgent;
import com.xx.jaseatschoicejava.agent.agents.DishRecommendationAgent;
import com.xx.jaseatschoicejava.agent.agents.LocationServiceAgent;
import com.xx.jaseatschoicejava.agent.agents.MerchantInfoAgent;
import com.xx.jaseatschoicejava.agent.agents.NutritionGuideAgent;
import com.xx.jaseatschoicejava.agent.agents.OrderHelperAgent;
import com.xx.jaseatschoicejava.agent.agents.TimeAwareAgent;
import com.xx.jaseatschoicejava.agent.agents.UserPreferenceAgent;
import com.xx.jaseatschoicejava.agent.config.ChatMemoryFactory;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.observability.AgentListener;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.model.chat.ChatModel;

/**
 * SupervisorAgent 工厂类（L2 → L1 架构）
 *
 * 动态创建带监听器的SupervisorAgent实例
 * 支持每个用户独立的ChatMemory（Redis + MySQL混合存储）
 *
 * **架构说明**：
 * - L2层：SupervisorAgent智能调度
 * - L1层：7个专家Agent（菜品推荐、用户偏好、营养指导、订单辅助、商家信息、时间感知、位置服务）
 *
 * 架构重构：
 * - 统一为L2→L1两层架构
 * - L2 SupervisorAgent直接协调L1专家Agent
 * - 实现智能任务规划和Agent路由
 * - 提升性能，减少调用层次
 *
 * @author Claude
 * @since 2026-03-27
 * @updated 2026-04-02 架构统一为L2→L1
 */
@Component
public class SupervisorAgentFactory {

    private static final Logger log = LoggerFactory.getLogger(SupervisorAgentFactory.class);

    private final ChatModel supervisorModel;
    private final ChatMemoryFactory chatMemoryFactory;
    private final CardRendererAgent cardRendererAgent;

    // L1专家Agent注入
    private final DishRecommendationAgent dishRecommendationAgent;
    private final UserPreferenceAgent userPreferenceAgent;
    private final NutritionGuideAgent nutritionGuideAgent;
    private final OrderHelperAgent orderHelperAgent;
    private final MerchantInfoAgent merchantInfoAgent;
    private final TimeAwareAgent timeAwareAgent;
    private final LocationServiceAgent locationServiceAgent;

    public SupervisorAgentFactory(
            @Qualifier("supervisorModel") ChatModel supervisorModel,
            ChatMemoryFactory chatMemoryFactory,
            CardRendererAgent cardRendererAgent,
            DishRecommendationAgent dishRecommendationAgent,
            UserPreferenceAgent userPreferenceAgent,
            NutritionGuideAgent nutritionGuideAgent,
            OrderHelperAgent orderHelperAgent,
            MerchantInfoAgent merchantInfoAgent,
            TimeAwareAgent timeAwareAgent,
            LocationServiceAgent locationServiceAgent) {
        this.supervisorModel = supervisorModel;
        this.chatMemoryFactory = chatMemoryFactory;
        this.cardRendererAgent = cardRendererAgent;
        this.dishRecommendationAgent = dishRecommendationAgent;
        this.userPreferenceAgent = userPreferenceAgent;
        this.nutritionGuideAgent = nutritionGuideAgent;
        this.orderHelperAgent = orderHelperAgent;
        this.merchantInfoAgent = merchantInfoAgent;
        this.timeAwareAgent = timeAwareAgent;
        this.locationServiceAgent = locationServiceAgent;

        log.info("SupervisorAgentFactory初始化完成（L2 → L1 架构）");
    }

    /**
     * 创建带监听器的SupervisorAgent（L2直接对接L1）
     *
     * 重构说明：
     * - L2层直接注入7个L1专家Agent
     * - 实现智能任务规划和路由逻辑
     * - 提升性能和响应速度
     *
     * @param listener Agent监听器
     * @param userId 用户ID（作为memoryId）
     * @return SupervisorAgent实例
     */
    public SupervisorAgent createWithListener(AgentListener listener, String userId) {
        log.debug("创建带监听器的L2 SupervisorAgent（直接对接L1），userId={}", userId);

        return AgenticServices
                .supervisorBuilder()
                .chatModel(supervisorModel)
                .chatMemoryProvider(memoryId -> chatMemoryFactory.createChatMemory(userId))
                .name("SupervisorAgent")
                .description("L2智能调度代理，直接协调L1专家Agent完成复杂任务")
                .subAgents(
                    // 直接注入所有L1专家Agent
                    dishRecommendationAgent,
                    userPreferenceAgent,
                    nutritionGuideAgent,
                    orderHelperAgent,
                    merchantInfoAgent,
                    timeAwareAgent,
                    locationServiceAgent
                )
                .outputKey("supervisorResult")
                .listener(listener)
                .supervisorContext(createSupervisorContext())
                .contextGenerationStrategy(dev.langchain4j.agentic.supervisor.SupervisorContextStrategy.CHAT_MEMORY_AND_SUMMARIZATION)
                .responseStrategy(dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy.LAST)
                .maxAgentsInvocations(3)
                .build();
    }

    /**
     * 创建简化版SupervisorAgent（用于重试降级）
     *
     * 使用更简短的supervisorContext，限制最多调用1个子Agent，
     * 降低LLM输出复杂度和JSON截断风险
     *
     * @param listener Agent监听器（可为null，降级场景不发送进度事件）
     * @param userId 用户ID
     * @return 简化版SupervisorAgent实例
     */
    public SupervisorAgent createRetryAgent(AgentListener listener, String userId) {
        log.debug("创建简化版SupervisorAgent（降级重试），userId={}", userId);

        return AgenticServices
                .supervisorBuilder()
                .chatModel(supervisorModel)
                .chatMemoryProvider(memoryId -> chatMemoryFactory.createChatMemory(userId))
                .name("SupervisorAgent-Retry")
                .description("降级重试调度代理")
                .subAgents(
                    dishRecommendationAgent,
                    userPreferenceAgent,
                    nutritionGuideAgent
                )
                .outputKey("supervisorResult")
                .listener(listener)
                .supervisorContext(createRetrySupervisorContext())
                .contextGenerationStrategy(dev.langchain4j.agentic.supervisor.SupervisorContextStrategy.CHAT_MEMORY_AND_SUMMARIZATION)
                .responseStrategy(dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy.LAST)
                .maxAgentsInvocations(1)
                .build();
    }

    /**
     * 简化版SupervisorContext（用于降级重试）
     *
     * 只保留核心路由规则，严格限制输出格式，
     * 最大限度降低JSON截断风险
     */
    private String createRetrySupervisorContext() {
        return """
            你是调度代理，只选择一个最合适的Agent处理用户请求。

            路由规则：
            - 推荐菜品 → DishRecommendationAgent
            - 偏好/忌口 → UserPreferenceAgent
            - 营养/健康 → NutritionGuideAgent

            严格约束：
            - 只调用1个Agent
            - arguments.userMessage不超过20个字，只传递用户意图，禁止生成任何推荐或回答内容
            - 禁止在userMessage中列举菜品名称
            """;
    }

    /**
     * 创建详细的SupervisorContext
     *
     * 包含：
     * 1. 意图识别和路由策略
     * 2. L1专家Agent能力清单
     * 3. 任务分解和协调逻辑
     * 4. 结果整合策略
     * 5. 终止条件和优化策略
     */
    private String createSupervisorContext() {
        return """
            你是"佳食宜选"智能调度代理。你的唯一职责是：选择最合适的子Agent，并把用户意图转发给它。

            ## 你的角色边界（严格遵守）
            - 你只做"路由决策"，不做"内容生成"
            - 你绝不能自己生成推荐、建议、菜品列表或任何回答内容
            - 你只输出一个JSON：选择哪个Agent、传什么意图给它

            ## 路由规则（按优先级匹配）

            ### 复合意图（必须按顺序调用多个Agent）
            1. 涉及"口味推荐"、"根据我的偏好推荐"、"适合我的菜"等推荐+偏好请求：
               → 先调用 UserPreferenceAgent 获取偏好，再调用 DishRecommendationAgent 推荐菜品
            2. 涉及"营养推荐"、"健康饮食"等营养+推荐请求：
               → 先调用 NutritionGuideAgent 获取营养建议，再调用 DishRecommendationAgent 推荐菜品

            ### 单一意图（只调用1个Agent）
            - 纯菜品/食物推荐（上下文已有偏好数据）→ DishRecommendationAgent
            - 纯偏好/忌口/过敏设置 → UserPreferenceAgent
            - 纯营养/热量/健康咨询 → NutritionGuideAgent
            - 订单/催单/配送 → OrderHelperAgent
            - 商家/餐厅信息 → MerchantInfoAgent
            - 时段/三餐搭配 → TimeAwareAgent
            - 附近/距离/位置 → LocationServiceAgent

            ## 禁止行为
            1. 禁止把子Agent的追问当成新的主任务继续分发
            2. 禁止重复调用同一个Agent
            3. 禁止编造数据，所有数据必须来自工具调用结果
            4. 单轮请求最多调用3个Agent
            5. 推荐完成后必须立即结束，不得在推荐之后继续调用偏好修改、资料优化或其他Agent
            6. 禁止在arguments.userMessage中生成推荐内容、菜品列表、建议或回答

            ## JSON格式要求
            1. response字段中禁止未转义双引号，用《》替代，如《宫保鸡丁》
            2. arguments.userMessage只能是用户意图的简短转述，不超过30个字，禁止包含具体菜品名称

            ## 输出正反示例
            正确: {"agentName":"DishRecommendationAgent","arguments":{"userMessage":"根据用户口味推荐菜品"}}
            正确: {"agentName":"UserPreferenceAgent","arguments":{"userMessage":"查询用户饮食偏好"}}
            错误: {"agentName":"DishRecommendationAgent","arguments":{"userMessage":"推荐：蒜蓉西兰花、清炒时蔬、蒜香排骨...这些菜品口感清淡，营养丰富"}}（禁止生成推荐内容）
            """;
    }

    /**
     * 渲染卡片格式
     *
     * @param originalResult 原始结果
     * @return 格式化后的结果
     */
    /**
     * 将原始结果渲染为卡片格式
     *
     * @param originalResult L2 Supervisor的原始总结结果
     * @return 格式化后的卡片格式消息
     */
    public String renderCards(String originalResult) {
        try {
            log.info("==================== 卡片渲染开始 ====================");
            log.info("📥 原始结果长度: {} 字符", originalResult.length());
            log.info("📥 原始结果内容:");
            log.info("─ 开始 ({} 字符) ─", originalResult.length());
            log.info(originalResult);
            log.info("─ 结束 ─");

            // ========== 【过滤LangChain4j调试信息】 ==========
            // 移除LLM生成时可能包含的内部调试信息
            String cleanedResult = removeLangChain4jDebugInfo(originalResult);

            if (!cleanedResult.equals(originalResult)) {
                log.info("🧹 已过滤LangChain4j调试信息");
                log.info("📊 过滤前长度: {} 字符", originalResult.length());
                log.info("📊 过滤后长度: {} 字符", cleanedResult.length());
                log.info("📊 过滤掉字符数: {} 字符", originalResult.length() - cleanedResult.length());
                log.info("📥 清理后结果内容:");
                log.info("─ 开始 ({} 字符) ─", cleanedResult.length());
                log.info(cleanedResult);
                log.info("─ 结束 ─");
            } else {
                log.info("✅ 无需过滤，内容无变化");
            }

            String rendered = cardRendererAgent.renderCards(cleanedResult);
            log.info("📤 卡片渲染完成，最终结果长度: {} 字符", rendered.length());
            log.info("=====================================================");
            return rendered;
        } catch (Exception e) {
            log.error("卡片渲染失败，返回原始结果", e);
            return originalResult;  // 降级：渲染失败时返回原始结果
        }
    }

    /**
     * 清理 LangChain4j 调试信息（公开方法，供 Controller 调用）
     *
     * @param result 原始结果
     * @return 清理后的结果
     */
    public String cleanDebugInfo(String result) {
        return removeLangChain4jDebugInfo(result);
    }

    /**
     * 移除LangChain4j内部调试信息和SystemMessage
     *
     * @param result 原始结果
     * @return 清理后的结果
     */
    private String removeLangChain4jDebugInfo(String result) {
        if (result == null || result.isEmpty()) {
            return result;
        }

        String cleaned = result;

        // ========== 【移除 SystemMessage 大段文本】 ==========

        // 1. 移除包含 "性能优化" 的整个段落
        cleaned = cleaned.replaceAll(
            "性能优化[\\s\\S]*?及时终止：获得满意结果后立即停止调用\\s*",
            ""
        );

        // 2. 移除包含 "💡 示例对话" 的整个段落
        cleaned = cleaned.replaceAll(
            "💡 示例对话[\\s\\S]*?⚠️ 及时终止：获得满意结果后立即停止调用\\s*",
            ""
        );

        // 3. 移除包含 "示例1：单意图" 到 "示例3：复杂场景" 的大段文本
        cleaned = cleaned.replaceAll(
            "示例\\d+：[\\s\\S]*?\\n\\n",
            ""
        );

        // 4. 移除 "⚠️ 重要提醒" 及其后的多个要点
        cleaned = cleaned.replaceAll(
            "⚠️ 重要提醒[\\s\\S]*?及时终止：获得满意结果后立即停止调用\\s*",
            ""
        );

        // 5. 移除包含 "你的思考"、"你的操作" 的行
        cleaned = cleaned.replaceAll("你的思考：.*\\n", "");
        cleaned = cleaned.replaceAll("你的操作：[\\s\\S]*?你的回复：", "你的回复：");

        // ========== 【移除 JSON 格式要求】 ==========

        // 6. 移除 "You must answer strictly in the following JSON format" 及后续内容
                cleaned = cleaned.replace(
                        """
                        You must answer strictly in the following JSON format:
                            {
                        "agentName": (type: string),
                        "arguments": (type: java.util.Map<java.lang.String, java.lang.Object>)
                        }
                        """,
                        ""
                );

        // 7. 移除 "The user request is:" 行
        cleaned = cleaned.replaceAll("The user request is: '.*?'\\.\n", "");

        // 8. 移除 "The last received response is:" 行
        cleaned = cleaned.replaceAll("The last received response is: '.*?'\\.\n", "");

        // 9. 移除 SystemMessage { text = ... } 开头的行
        cleaned = cleaned.replaceAll("SystemMessage \\{ text = \".*?\\n", "");

        // ========== 【移除 Agent 调用 JSON】 ==========

        // 10. 移除 JSON agent 调用块（如 {"agentName":"DishRecommendationAgent$0",...}）
        cleaned = cleaned.replaceAll(
            "\\{\\s*\"agentName\"\\s*:\\s*\"[^\"]+\\$\\d+\"\\s*,\\s*\"arguments\"\\s*:\\s*\\{[^}]*\\}\\s*}\\s*\\n",
            ""
        );

        // 11. 移除 {"agentName":"done",...}
        cleaned = cleaned.replaceAll(
            "\\{\\s*\"agentName\"\\s*:\\s*\"done\"\\s*,\\s*\"arguments\"\\s*:\\s*\\{[^}]*\\}\\s*}\\s*\\n",
            ""
        );

        // ========== 【移除其他技术标记】 ==========

        // 12. 移除时间戳和🤖 emoji行
        cleaned = cleaned.replaceAll("\\d{2}:\\d{2}\\s*\\n🤖\\s*\\n", "");

        // 13. 移除单独的🤖 emoji
        cleaned = cleaned.replaceAll("🤖\\s*", "");

        // 14. 清理多余的空行和空格
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n").trim();

        // 15. 移除开头的单引号（如果有）
        if (cleaned.startsWith("'")) {
            cleaned = cleaned.substring(1);
        }

        // 16. 移除结尾的单引号（如果有）
        if (cleaned.endsWith("'")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        return cleaned;
    }
}
