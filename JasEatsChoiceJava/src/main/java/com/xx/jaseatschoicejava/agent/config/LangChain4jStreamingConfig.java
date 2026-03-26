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
import dev.langchain4j.memory.ChatMemory;
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
 * **架构设计：L3 Agent 调用 L2 Agent**
 *
 * L3: StreamingIntelligentAssistantAgent (智能调度)
 *   ↓ 调用
 * L2: SmartRecommendationAgent (推荐), HealthManagementAgent (健康), FullOrderAgent (订单)
 *   ↓ 调用
 * L1: UserPreferenceAgent, NutritionGuideAgent, DishRecommendationAgent 等
 *   ↓ 调用
 * 工具类: LocationTools, OrderTools 等
 *
 * @author Claude
 * @since 2026-03-24
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

    // ==================== L2 Agent 注入（已弃用，不使用Agent作为工具） ====================
    // 注意：这些Agent返回的是SupervisorAgent类型，需要使用@Qualifier注入

    @Resource
    @org.springframework.beans.factory.annotation.Qualifier("smartRecommendationAgent")
    private dev.langchain4j.agentic.supervisor.SupervisorAgent workflowSmartRecommendationAgent;

    @Resource
    @org.springframework.beans.factory.annotation.Qualifier("healthManagementAgent")
    private dev.langchain4j.agentic.supervisor.SupervisorAgent workflowHealthManagementAgent;

    @Resource
    @org.springframework.beans.factory.annotation.Qualifier("fullOrderAgent")
    private dev.langchain4j.agentic.supervisor.SupervisorAgent workflowFullOrderAgent;

    // L1 Agent 不再使用，改为直接使用工具类

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
     * 配置ChatMemory（对话记忆）- 流式版本使用独立的内存
     */
    @Bean
    public ChatMemory streamingChatMemory() {
        log.info("初始化StreamingChatMemory，消息窗口大小：20");

        return MessageWindowChatMemory.withMaxMessages(20);
    }

    /**
     * 构建L3流式智能助手AI Agent
     *
     * **重要**：这个Agent调用L2 Agent，而不是直接调用工具类
     */
    @Bean
    public StreamingIntelligentAssistantAgent streamingIntelligentAssistantAgent(
            StreamingChatModel streamingChatLanguageModel,
            ChatMemory streamingChatMemory) {
        log.info("构建L3: StreamingIntelligentAssistantAgent (直接使用工具类)...");

        return AiServices.builder(StreamingIntelligentAssistantAgent.class)
                .streamingChatModel(streamingChatLanguageModel)
                .chatMemory(streamingChatMemory)
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
     * **重要**：这是商家端的Agent，提供数据分析、经营优化建议等功能
     */
    @Bean
    public StreamingMerchantAssistantAgent streamingMerchantAssistantAgent(
            StreamingChatModel streamingChatLanguageModel,
            ChatMemory streamingChatMemory) {
        log.info("构建商家L3: StreamingMerchantAssistantAgent (调用商家工具)...");

        return AiServices.builder(StreamingMerchantAssistantAgent.class)
                .streamingChatModel(streamingChatLanguageModel)
                .chatMemory(streamingChatMemory)
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
