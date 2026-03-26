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
 * **架构设计：L3 Agent 直接调用工具类**
 *
 * 架构重构（2026-03-27）：
 * - 移除L2层，L3直接对接L1专家Agent和工具类
 * - L3 SupervisorAgent负责智能任务规划和Agent路由
 * - 流式Agent使用工具类提供实时响应
 *
 * 流式Agent定位：
 * - StreamingIntelligentAssistantAgent: 用户端智能助手（流式响应）
 * - StreamingMerchantAssistantAgent: 商家端经营助手（流式响应）
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
     * 配置ChatMemory（对话记忆）- 流式版本使用独立的内存
     */
    @Bean
    public ChatMemory streamingChatMemory() {
        log.info("初始化StreamingChatMemory，消息窗口大小：20");

        return MessageWindowChatMemory.withMaxMessages(20);
    }

    /**
     * 构建流式智能助手AI Agent
     *
     * **架构说明**：直接使用工具类提供流式响应
     * - 用于用户端的实时对话场景
     * - 不使用Agent作为工具（Agent不能作为工具传递）
     * - 工具类提供数据查询和业务逻辑
     */
    @Bean
    public StreamingIntelligentAssistantAgent streamingIntelligentAssistantAgent(
            StreamingChatModel streamingChatLanguageModel,
            ChatMemory streamingChatMemory) {
        log.info("构建流式: StreamingIntelligentAssistantAgent (直接使用工具类)...");

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
     * **架构说明**：商家端专用Agent，提供数据分析和经营优化建议
     * - 商家查询：商家信息、统计数据
     * - 经营建议：菜品优化、营销策略
     */
    @Bean
    public StreamingMerchantAssistantAgent streamingMerchantAssistantAgent(
            StreamingChatModel streamingChatLanguageModel,
            ChatMemory streamingChatMemory) {
        log.info("构建商家流式: StreamingMerchantAssistantAgent (调用商家工具)...");

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
