package com.xx.jaseatschoicejava.agent.config;

import com.xx.jaseatschoicejava.agent.agents.*;
import com.xx.jaseatschoicejava.agent.agents.stream.StreamingIntelligentAssistantAgent;
import com.xx.jaseatschoicejava.agent.agents.stream.StreamingMerchantAssistantAgent;
import com.xx.jaseatschoicejava.agent.tools.merchant.MerchantQueryTools;
import com.xx.jaseatschoicejava.agent.tools.merchant.MerchantStatsTools;
import com.xx.jaseatschoicejava.agent.tools.nutrition.CalorieCalculatorTools;
import com.xx.jaseatschoicejava.agent.tools.nutrition.NutritionAnalysisTools;
import com.xx.jaseatschoicejava.agent.tools.order.OrderCreateTools;
import com.xx.jaseatschoicejava.agent.tools.order.OrderQueryTools;
import com.xx.jaseatschoicejava.agent.tools.recommendation.RecommendationFilterTools;
import com.xx.jaseatschoicejava.agent.tools.recommendation.RecommendationQueryTools;
import com.xx.jaseatschoicejava.agent.tools.recommendation.RecommendationRankTools;
import com.xx.jaseatschoicejava.agent.tools.system.TimeTools;
import com.xx.jaseatschoicejava.agent.tools.user.UserProfileTools;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

/**
 * LangChain4j流式输出配置类
 *
 * **架构设计：L2 Agent 直接调用工具类或L1专家Agent**
 *
 * 流式Agent定位：
 * - StreamingIntelligentAssistantAgent: L2智能调度（用户端）
 * - StreamingMerchantAssistantAgent: L2智能调度（商家端）
 *
 * @author Claude
 * @since 2026-03-24
 * @updated 2026-04-02 架构统一为L2→L1
 */
@Configuration
@EnableConfigurationProperties(ZhipuAIConfig.class)
public class LangChain4jStreamingConfig {

    private static final Logger log = LoggerFactory.getLogger(LangChain4jStreamingConfig.class);

    @Resource
    private ZhipuAIConfig zhipuAIConfig;

    // ==================== 工具类注入 ====================

    @Resource
    private UserProfileTools userProfileTools;

    @Resource
    private RecommendationQueryTools recommendationQueryTools;

    @Resource
    private RecommendationFilterTools recommendationFilterTools;

    @Resource
    private RecommendationRankTools recommendationRankTools;

    @Resource
    private NutritionAnalysisTools nutritionAnalysisTools;

    @Resource
    private CalorieCalculatorTools calorieCalculatorTools;

    @Resource
    private OrderQueryTools orderQueryTools;

    @Resource
    private OrderCreateTools orderCreateTools;

    @Resource
    private TimeTools timeTools;

    // ==================== 架构重构说明 ====================
    // 2026-03-27: 移除L2层，L3 SupervisorAgent直接对接L1专家Agent
    // 流式Agent使用工具类提供实时响应，不使用Agent作为工具

    @Resource
    private MerchantQueryTools merchantQueryTools;

    @Resource
    private MerchantStatsTools merchantStatsTools;

    private StreamingChatModel streamingChatLanguageModel;

    /**
     * 配置StreamingChatModel（智谱AI流式版本）
     */
    @Bean(destroyMethod = "")
    public StreamingChatModel streamingChatLanguageModel() {
        log.info("初始化StreamingChatModel，模型：{}", zhipuAIConfig.getModel());

        this.streamingChatLanguageModel = ZhipuAiStreamingChatModel.builder()
                .apiKey(zhipuAIConfig.getApiKey())
                .model(zhipuAIConfig.getModel())
                .temperature(0.7)
                // 注意：callTimeout, writeTimeout 已弃用，移除使用默认值
                .build();

        return this.streamingChatLanguageModel;
    }

    /**
     * 构建流式智能助手AI Agent
     *
     * **架构说明**：直接使用工具类提供流式响应
     * - 用于用户端的实时对话场景
     * - 不使用Agent作为工具（Agent不能作为工具传递）
     * - 工具类提供数据查询和业务逻辑
     *
     * **用户隔离**：
     * - 使用 @MemoryId 注解实现用户级别的对话隔离
     * - 每个不同的 memoryId 会自动获得独立的 ChatMemory
     * - 调用方需要传递 memoryId 参数（通常使用 userId）
     */
    @Bean
    public StreamingIntelligentAssistantAgent streamingIntelligentAssistantAgent(
            StreamingChatModel streamingChatLanguageModel) {
        log.info("构建流式: StreamingIntelligentAssistantAgent (支持用户隔离)...");

        return AiServices.builder(StreamingIntelligentAssistantAgent.class)
                .streamingChatModel(streamingChatLanguageModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .tools(
                    // 直接使用工具类，而不是Agent（Agent不能作为工具传递）
                    userProfileTools,
                    recommendationQueryTools,
                    recommendationFilterTools,
                    recommendationRankTools,
                    nutritionAnalysisTools,
                    calorieCalculatorTools,
                    orderQueryTools,
                    orderCreateTools,
                    merchantQueryTools,
                    merchantStatsTools,
                    timeTools
                )
                .build();
    }

    /**
     * 构建商家流式经营助手AI Agent
     *
     * **架构说明**：商家端专用Agent，提供数据分析和经营优化建议
     * - 商家查询：商家信息、统计数据
     * - 经营建议：菜品优化、营销策略
     */
    @Bean
    public StreamingMerchantAssistantAgent streamingMerchantAssistantAgent(
            StreamingChatModel streamingChatLanguageModel) {
        log.info("构建商家流式: StreamingMerchantAssistantAgent (调用商家工具)...");

        return AiServices.builder(StreamingMerchantAssistantAgent.class)
                .streamingChatModel(streamingChatLanguageModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .tools(
                    // 商家查询工具
                    merchantQueryTools,
                    merchantStatsTools
                    // 订单工具已移除，商家Agent不应直接访问订单工具
                )
                .build();
    }

    /**
     * 应用关闭时清理资源
     */
    @PreDestroy
    public void cleanup() {
        log.info("LangChain4j流式资源清理开始...");

        try {
            if (streamingChatLanguageModel != null) {
                // 流式模型的清理
                log.info("StreamingChatModel已清理");
            }
        } catch (Exception e) {
            log.error("LangChain4j流式资源清理失败", e);
        }

        log.info("LangChain4j流式资源清理完成");
    }
}
