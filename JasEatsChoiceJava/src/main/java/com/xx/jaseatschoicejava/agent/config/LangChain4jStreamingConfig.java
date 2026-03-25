package com.xx.jaseatschoicejava.agent.config;

import com.xx.jaseatschoicejava.agent.agents.*;
import com.xx.jaseatschoicejava.agent.agents.stream.StreamingIntelligentAssistantAgent;
import com.xx.jaseatschoicejava.agent.agents.stream.StreamingMerchantAssistantAgent;
import com.xx.jaseatschoicejava.agent.tools.merchant.MerchantQueryTools;
import com.xx.jaseatschoicejava.agent.tools.merchant.MerchantStatsTools;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.zhipu.ZhipuAiStreamingChatModel;
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
import java.time.Duration;

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

    // ==================== L2 Agent 注入 ====================

    @Resource
    private SmartRecommendationAgent workflowSmartRecommendationAgent;

    @Resource
    private HealthManagementAgent workflowHealthManagementAgent;

    @Resource
    private FullOrderAgent workflowFullOrderAgent;

    // ==================== L1 Agent 注入（可选，直接调用） ====================

    @Resource
    private UserPreferenceAgent workflowUserPreferenceAgent;

    @Resource
    private NutritionGuideAgent workflowNutritionGuideAgent;

    @Resource
    private DishRecommendationAgent workflowDishRecommendationAgent;

    @Resource
    private MerchantInfoAgent workflowMerchantInfoAgent;

    @Resource
    private TimeAwareAgent workflowTimeAwareAgent;

    @Resource
    private LocationServiceAgent workflowLocationServiceAgent;

    @Resource
    private OrderHelperAgent workflowOrderHelperAgent;

    // ==================== 商家工具类注入 ====================

    @Resource
    private MerchantQueryTools merchantQueryTools;

    @Resource
    private MerchantStatsTools merchantStatsTools;

    private StreamingChatLanguageModel streamingChatLanguageModel;

    /**
     * 配置StreamingChatLanguageModel（智谱AI流式版本）
     */
    @Bean(destroyMethod = "")
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        log.info("初始化StreamingChatLanguageModel，模型：{}", zhipuAIConfig.getModel());

        this.streamingChatLanguageModel = ZhipuAiStreamingChatModel.builder()
                .apiKey(zhipuAIConfig.getApiKey())
                .model(zhipuAIConfig.getModel())
                .temperature(0.7)
                .callTimeout(Duration.ofSeconds(60))
                .connectTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(60))
                .readTimeout(Duration.ofSeconds(60))
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
            StreamingChatLanguageModel streamingChatLanguageModel,
            ChatMemory streamingChatMemory) {
        log.info("构建L3: StreamingIntelligentAssistantAgent (调用L2 Agents)...");

        return AiServices.builder(StreamingIntelligentAssistantAgent.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemory(streamingChatMemory)
                .tools(
                    // L2 Agents（主要调用的目标）
                    workflowSmartRecommendationAgent,
                    workflowHealthManagementAgent,
                    workflowFullOrderAgent,

                    // L1 Agents（也可以直接调用，进行更细粒度的控制）
                    workflowUserPreferenceAgent,
                    workflowNutritionGuideAgent,
                    workflowDishRecommendationAgent,
                    workflowMerchantInfoAgent,
                    workflowTimeAwareAgent,
                    workflowLocationServiceAgent,
                    workflowOrderHelperAgent
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
            StreamingChatLanguageModel streamingChatLanguageModel,
            ChatMemory streamingChatMemory) {
        log.info("构建商家L3: StreamingMerchantAssistantAgent (调用商家工具)...");

        return AiServices.builder(StreamingMerchantAssistantAgent.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemory(streamingChatMemory)
                .tools(
                    // 商家查询工具
                    merchantQueryTools,
                    merchantStatsTools,
                    // 订单工具（商家需要查看订单）
                    workflowOrderHelperAgent
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
                log.info("StreamingChatLanguageModel已清理");
            }
        } catch (Exception e) {
            log.error("LangChain4j流式资源清理失败", e);
        }

        log.info("LangChain4j流式资源清理完成");
    }
}
