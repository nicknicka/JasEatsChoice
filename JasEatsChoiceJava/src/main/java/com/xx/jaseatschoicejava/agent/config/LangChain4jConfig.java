package com.xx.jaseatschoicejava.agent.config;

import com.xx.jaseatschoicejava.agent.MerchantAssistantAgent;
import com.xx.jaseatschoicejava.agent.NutritionAiAgent;
import com.xx.jaseatschoicejava.agent.OrderAiAgent;
import com.xx.jaseatschoicejava.agent.RecommendationAiAgent;
import com.xx.jaseatschoicejava.agent.tools.CollectionTools;
import com.xx.jaseatschoicejava.agent.tools.LocationTools;
import com.xx.jaseatschoicejava.agent.tools.MerchantTools;
import com.xx.jaseatschoicejava.agent.tools.NutritionRecordTools;
import com.xx.jaseatschoicejava.agent.tools.NutritionTools;
import com.xx.jaseatschoicejava.agent.tools.OrderTools;
import com.xx.jaseatschoicejava.agent.tools.RecipeTools;
import com.xx.jaseatschoicejava.agent.tools.RecommendationTools;
import com.xx.jaseatschoicejava.agent.tools.UserTools;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * LangChain4j配置类
 * 使用AiServices构建真正的AI Agent
 *
 * @author Claude
 * @since 2026-03-22
 */
@Configuration
@EnableConfigurationProperties(ZhipuAIConfig.class)
public class LangChain4jConfig {

    private static final Logger log = LoggerFactory.getLogger(LangChain4jConfig.class);

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

    @Resource
    private MerchantTools merchantTools;

    /**
     * 配置ChatLanguageModel（智谱AI）
     */
    @Bean(destroyMethod = "") // 禁用Spring的默认destroy方法，避免异常
    public ChatLanguageModel chatLanguageModel() {
        log.info("初始化ChatLanguageModel，模型：{}", zhipuAIConfig.getModel());

        return ZhipuAiChatModel.builder()
                .apiKey(zhipuAIConfig.getApiKey())
                .model(zhipuAIConfig.getModel())
                .temperature(0.7)
                .maxRetries(2)
                .build();
    }

    /**
     * 应用关闭时清理资源
     * 注意：OkHttp的守护线程会在JVM退出时自动清理，这个警告可以忽略
     */
    @PreDestroy
    public void cleanup() {
        log.info("LangChain4j资源清理：OkHttp守护线程将在JVM退出时自动清理");
    }

    /**
     * 配置ChatMemory（对话记忆）
     */
    @Bean
    public ChatMemory chatMemory() {
        log.info("初始化ChatMemory，消息窗口大小：20");

        return MessageWindowChatMemory.withMaxMessages(20);
    }

    /**
     * 构建营养分析AI Agent
     *
     * 使用LangChain4j的AiServices构建真正的Agent
     * LLM会自动决定何时调用哪个Tool
     */
    @Bean
    public NutritionAiAgent nutritionAiAgent(ChatLanguageModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建NutritionAiAgent...");

        return AiServices.builder(NutritionAiAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    nutritionTools,
                    nutritionRecordTools,
                    userTools
                )
                .build();
    }

    /**
     * 构建智能推荐AI Agent
     */
    @Bean
    public RecommendationAiAgent recommendationAiAgent(ChatLanguageModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建RecommendationAiAgent...");

        return AiServices.builder(RecommendationAiAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    recommendationTools,
                    recipeTools,
                    collectionTools,
                    nutritionTools,
                    locationTools
                )
                .build();
    }

    /**
     * 构建订单助手AI Agent
     */
    @Bean
    public OrderAiAgent orderAiAgent(ChatLanguageModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建OrderAiAgent...");

        return AiServices.builder(OrderAiAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    orderTools,
                    recommendationTools,
                    collectionTools,
                    userTools
                )
                .build();
    }

    /**
     * 构建商家经营助手AI Agent
     */
    @Bean
    public MerchantAssistantAgent merchantAssistantAgent(ChatLanguageModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建MerchantAssistantAgent...");

        return AiServices.builder(MerchantAssistantAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    merchantTools
                )
                .build();
    }
}

