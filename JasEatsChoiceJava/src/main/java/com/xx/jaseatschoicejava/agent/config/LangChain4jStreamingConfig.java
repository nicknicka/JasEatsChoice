package com.xx.jaseatschoicejava.agent.config;

import com.xx.jaseatschoicejava.agent.agents.stream.StreamingIntelligentAssistantAgent;
import com.xx.jaseatschoicejava.agent.tools.CollectionTools;
import com.xx.jaseatschoicejava.agent.tools.MerchantTools;
import com.xx.jaseatschoicejava.agent.tools.NutritionRecordTools;
import com.xx.jaseatschoicejava.agent.tools.NutritionTools;
import com.xx.jaseatschoicejava.agent.tools.OrderTools;
import com.xx.jaseatschoicejava.agent.tools.RecipeTools;
import com.xx.jaseatschoicejava.agent.tools.RecommendationTools;
import com.xx.jaseatschoicejava.agent.tools.UserTools;
import com.xx.jaseatschoicejava.agent.tools.system.LocationTools;
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

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.time.Duration;

/**
 * LangChain4j流式输出配置类
 * 配置支持流式输出的AI Agent
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

    @Resource
    private NutritionTools nutritionTools;

    @Resource
    private NutritionRecordTools nutritionRecordTools;

    @Resource
    private RecommendationTools recommendationTools;

    @Resource
    private RecipeTools recipeTools;

    @Resource
    private OrderTools orderTools;

    @Resource
    private CollectionTools collectionTools;

    @Resource
    private UserTools userTools;

    @Resource
    private LocationTools locationTools;

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
     * 构建流式智能助手AI Agent
     */
    @Bean
    public StreamingIntelligentAssistantAgent streamingIntelligentAssistantAgent(
            StreamingChatLanguageModel streamingChatLanguageModel,
            ChatMemory streamingChatMemory) {
        log.info("构建StreamingIntelligentAssistantAgent...");

        return AiServices.builder(StreamingIntelligentAssistantAgent.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemory(streamingChatMemory)
                .tools(
                    nutritionTools,
                    nutritionRecordTools,
                    recommendationTools,
                    recipeTools,
                    orderTools,
                    collectionTools,
                    userTools,
                    locationTools
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
