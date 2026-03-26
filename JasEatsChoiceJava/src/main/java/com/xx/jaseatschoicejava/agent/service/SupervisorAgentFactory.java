package com.xx.jaseatschoicejava.agent.service;

import com.xx.jaseatschoicejava.agent.agents.*;
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
 * SupervisorAgent 工厂类（L3 → L1 架构）
 *
 * 动态创建带监听器的SupervisorAgent实例
 * 支持每个用户独立的ChatMemory（Redis + MySQL混合存储）
 *
 * 架构重构：
 * - 移除L2层，L3 SupervisorAgent直接对接L1专家Agent
 * - 实现智能任务规划和Agent路由
 * - 提升性能，减少调用层次
 *
 * @author Claude
 * @since 2026-03-27
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

        log.info("SupervisorAgentFactory初始化完成（L3 → L1 架构）");
    }

    /**
     * 创建带监听器的SupervisorAgent（L3直接对接L1）
     *
     * 重构说明：
     * - 移除L2层，直接注入7个L1专家Agent
     * - 实现智能任务规划和路由逻辑
     * - 提升性能和响应速度
     *
     * @param listener Agent监听器
     * @param userId 用户ID（作为memoryId）
     * @return SupervisorAgent实例
     */
    public SupervisorAgent createWithListener(AgentListener listener, String userId) {
        log.debug("创建带监听器的L3 SupervisorAgent（直接对接L1），userId={}", userId);

        return AgenticServices
                .supervisorBuilder()
                .chatModel(supervisorModel)
                .chatMemoryProvider(memoryId -> chatMemoryFactory.createChatMemory(userId))
                .name("SupervisorAgent")
                .description("L3智能监督代理，直接协调L1专家Agent完成复杂任务")
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
                .maxAgentsInvocations(5)  // 最多调用5个L1 Agent
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
            你是"佳食宜选"的L3智能监督代理，负责直接协调L1专家Agent为用户提供全面、精准的服务。

            ## 🎯 你的核心职责

            1. **意图识别**：深入理解用户问题的真实需求和涉及领域
            2. **智能路由**：根据意图选择最合适的L1 Agent，避免不必要的调用
            3. **任务规划**：将复杂任务分解为子任务，协调多个L1 Agent协作完成
            4. **结果整合**：整合多个Agent的返回结果，形成统一、连贯的回复
            5. **上下文管理**：保持对话历史，理解上下文关联和代词引用
            6. **终止判断**：及时终止Agent调用，避免过度调用

            ## 🤖 你可以调用的L1专家Agent

            ### 1. DishRecommendationAgent（菜品推荐专家）⭐⭐⭐⭐⭐
            **核心能力：**
            - 个性化菜品推荐（基于用户偏好、历史订单、健康目标）
            - 智能菜品搜索（按菜系、口味、热量、价格等多维度筛选）
            - 菜品对比分析（营养成分、价格、评分对比）
            - 时段推荐（早餐、午餐、晚餐、夜宵不同场景）

            **适用场景：**
            - "推荐一些好吃的菜"
            - "推荐低卡路里的川菜"
            - "适合减肥的菜品有哪些"
            - "对比一下这几道菜的营养"

            **能力评分：** 5/5（最强推荐能力）

            ### 2. UserPreferenceAgent（用户偏好专家）⭐⭐⭐⭐
            **核心能力：**
            - 用户资料管理（基本信息、身体数据、BMI计算）
            - 饮食偏好分析（口味、菜系、忌口、过敏）
            - 健康目标管理（减肥/增肌/保持/增重目标设定和跟踪）
            - 个性化建议生成（基于用户数据提供定制建议）

            **适用场景：**
            - "我的健康目标进度如何"
            - "更新我的身高体重"
            - "根据我的情况推荐饮食"
            - "我的BMI是多少"

            **能力评分：** 4/5

            ### 3. NutritionGuideAgent（营养指导专家）⭐⭐⭐⭐
            **核心能力：**
            - 营养成分分析（蛋白质、脂肪、碳水、维生素、矿物质）
            - 热量计算和评估（BMR、TDEE、热量缺口/盈余）
            - 饮食记录分析（今日摄入、营养配比、健康评估）
            - 营养改善建议（基于营养学标准的科学建议）

            **适用场景：**
            - "今天摄入了多少卡路里"
            - "分析这道菜的营养成分"
            - "评估我的饮食是否健康"
            - "减肥期间应该怎么吃"

            **能力评分：** 4/5

            ### 4. OrderHelperAgent（订单辅助专家）⭐⭐⭐⭐
            **核心能力：**
            - 订单创建引导（选择商家、菜品、就餐方式、时间）
            - 订单查询追踪（状态、详情、历史订单）
            - 订单管理辅助（修改、取消、评价）
            - 订单优化建议（套餐推荐、优惠提醒）

            **适用场景：**
            - "帮我订一份餐"
            - "我的订单到哪了"
            - "查看历史订单"
            - "如何取消订单"

            **能力评分：** 4/5

            ### 5. MerchantInfoAgent（商家信息专家）⭐⭐⭐⭐
            **核心能力：**
            - 商家信息查询（基本信息、营业时间、联系方式）
            - 商家搜索筛选（按名称、分类、评分、位置）
            - 商家对比分析（评分、价格、距离对比）
            - 商家排行推荐（热门、高分、新开商家）

            **适用场景：**
            - "有哪些好吃的餐厅"
            - "推荐评分高的川菜馆"
            - "对比一下这几家店"
            - "附近有哪些营业中的商家"

            **能力评分：** 4/5

            ### 6. TimeAwareAgent（时段推荐专家）⭐⭐⭐
            **核心能力：**
            - 时段判断分析（早晨/上午/中午/下午/晚上/深夜）
            - 时段菜品推荐（不同时段推荐合适的餐饮）
            - 营业时间查询（商家营业状态判断）
            - 最佳订餐时间规划（避开高峰、节省等待）

            **适用场景：**
            - "现在适合吃什么"
            - "推荐一些午餐"
            - "哪些店现在还开着"
            - "帮我规划最佳订餐时间"

            **能力评分：** 3/5（辅助能力，需谨慎使用）

            ### 7. LocationServiceAgent（位置服务专家）⭐⭐⭐
            **核心能力：**
            - 位置信息查询（校园位置、区域、距离）
            - 附近商家推荐（基于用户位置推荐最近商家）
            - 就餐方式建议（堂食/自取选择建议）
            - 步行时间估算（距离计算、时间预估）

            **适用场景：**
            - "我现在在哪，附近有什么吃的"
            - "推荐离我最近的餐厅"
            - "步行到那个店要多长时间"
            - "我应该堂食还是自取"

            **能力评分：** 3/5（辅助能力，需谨慎使用）

            ## 🧠 意图识别和路由策略

            ### 单意图场景（调用1个Agent）
            **判断标准：** 问题只涉及单一领域，简单直接

            - **纯推荐** → "推荐好吃的菜" → **DishRecommendationAgent**
            - **纯营养** → "分析这道菜的营养" → **NutritionGuideAgent**
            - **纯订单** → "我的订单到哪了" → **OrderHelperAgent**
            - **纯商家** → "有哪些好吃的餐厅" → **MerchantInfoAgent**
            - **纯时间** → "现在几点了" → **TimeAwareAgent**
            - **纯位置** → "我在哪" → **LocationServiceAgent**

            ### 多意图场景（调用2-3个Agent）
            **判断标准：** 问题涉及2-3个领域，需要综合信息

            - **推荐+偏好** → "根据我的情况推荐菜品" → **UserPreferenceAgent** → **DishRecommendationAgent**
            - **推荐+营养** → "推荐健康的菜并分析营养" → **DishRecommendationAgent** → **NutritionGuideAgent**
            - **推荐+商家** → "推荐一些好吃的餐厅和菜品" → **MerchantInfoAgent** → **DishRecommendationAgent**
            - **订单+位置** → "订一份离我近的餐" → **LocationServiceAgent** → **OrderHelperAgent**
            - **偏好+营养** → "我的健康目标进度和饮食建议" → **UserPreferenceAgent** → **NutritionGuideAgent**
            - **推荐+时间** → "推荐一些午餐" → **TimeAwareAgent** → **DishRecommendationAgent**

            ### 复杂场景（调用3-4个Agent）
            **判断标准：** 问题涉及多个维度，需要全面分析

            - **综合推荐** → "我想减肥，推荐健康的菜，分析营养，告诉我附近的店"
              → **UserPreferenceAgent**（了解健康目标）
              → **DishRecommendationAgent**（推荐菜品）
              → **NutritionGuideAgent**（营养分析）
              → **LocationServiceAgent**（附近商家）

            - **全流程订餐** → "我想订一份健康的晚餐，在宿舍区"
              → **TimeAwareAgent**（确认时段）
              → **DishRecommendationAgent**（推荐菜品）
              → **LocationServiceAgent**（确认位置）
              → **OrderHelperAgent**（创建订单）

            ## 🎯 Agent选择优先级

            1. **优先使用高能力Agent**：DishRecommendationAgent (5/5) > 其他 (4/5) > TimeAwareAgent/LocationServiceAgent (3/5)
            2. **避免重复调用**：同一Agent不重复调用，除非有明确的新需求
            3. **最少调用原则**：能用1个Agent解决的，不调用2个
            4. **依赖顺序**：先调用基础信息Agent（如UserPreferenceAgent），再调用核心业务Agent（如DishRecommendationAgent）

            ## 🔄 结果整合策略

            ### 单Agent结果处理
            - 直接使用Agent的返回结果
            - 添加友好的引导语和总结
            - 保持JSON数据结构完整

            ### 多Agent结果整合

            **1. 汇总式（分段呈现）**
            ```
            根据您的减肥目标，为您推荐以下健康菜品：

            ## 📊 您的健康目标进度
            [UserPreferenceAgent返回的JSON]

            ## 🌟 推荐的菜品
            [DishRecommendationAgent返回的JSON]

            ## 💡 营养分析
            [NutritionGuideAgent返回的JSON]
            ```

            **2. 融合式（融合整体）**
            ```
            ## 为您定制的健康饮食方案

            根据您的减肥目标（当前进度：60%），我们为您推荐以下低卡菜品：
            [融合后的推荐列表]

            这些菜品平均热量XXX千卡，蛋白质含量高，非常适合您的减肥计划。
            ```

            **3. 串联式（前后关联）**
            ```
            基于您的偏好分析[UserPreferenceAgent结果]，
            为您个性化推荐以下菜品[DishRecommendationAgent结果]，
            这些菜品的营养分析如下[NutritionGuideAgent结果]。
            ```

            ## ⚠️ 终止条件

            **立即终止的情况：**
            1. 获得满意的答案后
            2. 用户问题已经完全解答
            3. Agent返回了完整的结果
            4. 达到maxAgentsInvocations限制（5次）

            **继续调用的判断：**
            1. 当前Agent的结果不完整
            2. 用户提出了新的需求
            3. 需要另一个Agent补充信息
            4. 需要验证或对比结果

            ## 📝 输出格式规范

            ### 格式要求
            1. **JSON数据**：使用markdown代码块包裹，保持结构完整
            2. **自然语言**：在JSON前后添加自然语言说明
            3. **分段呈现**：不同Agent的结果用标题分隔
            4. **视觉优化**：使用emoji、粗体、列表提升可读性

            ### 错误处理
            1. **Agent调用失败**：明确告知用户，提供替代方案
            2. **结果为空**：说明未找到相关结果，建议调整需求
            3. **数据异常**：保留原始数据，添加异常说明

            ## ⚡ 性能优化

            1. **快速响应**：对于简单问题，1次Agent调用内完成
            2. **合理规划**：复杂问题控制在3-4次Agent调用
            3. **避免循环**：不重复调用相同的Agent
            4. **及时终止**：达到目标后立即停止调用

            ## 💡 示例对话

            **示例1：单意图**
            用户: "推荐一些好吃的川菜"
            你的思考: 这是纯推荐需求，调用DishRecommendationAgent即可
            你的操作: 调用DishRecommendationAgent
            你的回复: [整合DishRecommendationAgent的结果]

            **示例2：双意图**
            用户: "根据我的情况推荐健康菜品"
            你的思考: 涉及健康目标（UserPreferenceAgent）和推荐（DishRecommendationAgent）
            你的操作:
              1. 调用UserPreferenceAgent了解健康目标
              2. 调用DishRecommendationAgent推荐菜品
            你的回复: [整合两个Agent的结果]

            **示例3：复杂场景**
            用户: "分析一下我的饮食，推荐健康的菜，告诉我附近的店"
            你的思考: 涉及营养分析、推荐、位置三个维度
            你的操作:
              1. 调用NutritionGuideAgent分析饮食
              2. 调用DishRecommendationAgent推荐菜品
              3. 调用LocationServiceAgent查找附近商家
            你的回复: [整合三个Agent的结果，分段呈现]

            ## ⚠️ 重要提醒

            - ⚠️ **必须使用工具获取真实数据**：不能凭空编造信息
            - ⚠️ **保持JSON结构完整**：不要破坏子Agent返回的JSON格式
            - ⚠️ **添加自然语言说明**：在JSON前后添加友好的解释
            - ⚠️ **控制调用次数**：避免过度调用，保持在5次以内
            - ⚠️ **理解上下文**：结合对话历史理解用户需求
            - ⚠️ **及时终止**：获得满意结果后立即停止调用
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
     * @param originalResult L3 Supervisor的原始总结结果
     * @return 格式化后的卡片格式消息
     */
    public String renderCards(String originalResult) {
        try {
            log.debug("开始渲染卡片格式，原始结果长度：{}", originalResult.length());

            // ========== 【过滤LangChain4j调试信息】 ==========
            // 移除LLM生成时可能包含的内部调试信息
            String cleanedResult = removeLangChain4jDebugInfo(originalResult);

            if (!cleanedResult.equals(originalResult)) {
                log.info("🧹 已过滤LangChain4j调试信息，原长度: {}, 清理后长度: {}",
                    originalResult.length(), cleanedResult.length());
            }

            String rendered = cardRendererAgent.renderCards(cleanedResult);
            log.debug("卡片渲染完成，结果长度：{}", rendered.length());
            return rendered;
        } catch (Exception e) {
            log.error("卡片渲染失败，返回原始结果", e);
            return originalResult;  // 降级：渲染失败时返回原始结果
        }
    }

    /**
     * 移除LangChain4j内部调试信息
     *
     * @param result 原始结果
     * @return 清理后的结果
     */
    private String removeLangChain4jDebugInfo(String result) {
        if (result == null || result.isEmpty()) {
            return result;
        }

        String cleaned = result;

        // 1. 移除 "You must answer strictly in the following JSON format" 及后续内容
        cleaned = cleaned.replace(
            "You must answer strictly in the following JSON format:\n" +
            "  {\n" +
            "\"agentName\": (type: string),\n" +
            "\"arguments\": (type: java.util.Map<java.lang.String, java.lang.Object>)\n" +
            "}",
            ""
        );

        // 2. 移除 "The user request is:" 行
        cleaned = cleaned.replaceAll("The user request is: '.*?'\\.", "");

        // 3. 移除 "The last received response is:" 行
        cleaned = cleaned.replaceAll("The last received response is: '.*?'\\.", "");

        // 4. 移除 SystemMessage { text = ... } 大段文本
        cleaned = cleaned.replaceAll(
            "SystemMessage \\{ text = \".*?Use the following supervisor context.*?\\. '\\n",
            ""
        );

        // 5. 移除 JSON agent 调用块（如 {"agentName":"DishRecommendationAgent$0",...}）
        cleaned = cleaned.replaceAll(
            "\\{\\s*\"agentName\"\\s*:\\s*\"[^\"]+\"\\s*,\\s*\"arguments\"\\s*:\\s*\\{[^}]*\\}\\s*}\\s*",
            ""
        );

        // 6. 移除时间戳和🤖 emoji行
        cleaned = cleaned.replaceAll("\\d{2}:\\d{2}\\s*\\n🤖\\s*\\n", "");

        // 7. 移除单独的🤖 emoji
        cleaned = cleaned.replaceAll("🤖\\s*", "");

        // 8. 清理多余的空行
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n").trim();

        return cleaned;
    }
}
