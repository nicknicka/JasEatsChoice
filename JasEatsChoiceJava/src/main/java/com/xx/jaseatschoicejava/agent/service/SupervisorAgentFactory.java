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
                .responseStrategy(dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy.SCORED)
                .maxAgentsInvocations(1)  // 最多调用1个L1 Agent，减少延迟
                .build();
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
            你是"佳食宜选"的L2智能调度代理，负责为用户选择最合适的1个L1专家Agent。

            ## 🎯 核心职责
            1. **意图识别**：理解用户真实需求
            2. **智能路由**：选择最合适的Agent，避免不必要调用
            3. **任务规划**：分解复杂任务，协调多Agent协作
            4. **结果整合**：整合Agent返回结果，形成连贯回复
            5. **终止判断**：及时停止Agent调用，避免过度调用

            ## ⚡ 关键约束：只能调用1个Agent！
            - 必须选择最能直接解决用户问题的那个Agent
            - 如果用户有多个需求，选择最核心的一个

            ## 🤖 L1专家Agent列表

            **1. DishRecommendationAgent（菜品推荐）**
            - 个性化推荐、智能搜索、菜品对比、时段推荐
            - 适用：任何涉及菜品/食物/饮食推荐的问题

            **2. NutritionGuideAgent（营养指导）**
            - 营养成分分析、热量计算、饮食记录分析
            - 适用：营养分析、热量查询、饮食健康

            **3. OrderHelperAgent（订单辅助）**
            - 订单查询追踪、订单管理
            - 适用：订单状态、订单历史、催单

            **4. MerchantInfoAgent（商家信息）**
            - 商家查询、搜索筛选、营业时间
            - 适用：找餐厅、商家信息、附近商家

            **5. TimeAwareAgent（时段推荐）**
            - 时段判断、时段推荐
            - 适用：早午晚餐推荐、当前时段适合吃什么

            **6. LocationServiceAgent（位置服务）**
            - 位置查询、附近商家推荐
            - 适用：附近有什么吃的、距离查询

            **7. UserPreferenceAgent（用户偏好）**
            - 用户资料、饮食偏好、健康目标
            - 适用：查看/修改个人偏好、健康目标进度

            ## 📝 输出格式
            - 基于Agent返回的真实数据组织自然语言回复
            - 如有结构化数据，用markdown代码块呈现
            - 友好、简洁，直击用户需求

            ## ⚠️ 重要提醒
            - 必须使用工具获取真实数据，不能编造
            - 只调用1个Agent，不要尝试调用多个
            - 理解上下文，结合对话历史

            ## ⚠️ JSON格式严格要求（避免解析崩溃）
            在返回done响应时，response字段值中绝对不能包含未转义的双引号(")。
            - 如需引用菜品名称，使用《》替代，例如：《宫保鸡丁》
            - 如需表示引述，使用单引号''替代
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
            "You must answer strictly in the following JSON format:\n" +
            "  {\n" +
            "\"agentName\": (type: string),\n" +
            "\"arguments\": (type: java.util.Map<java.lang.String, java.lang.Object>)\n" +
            "}\n",
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
