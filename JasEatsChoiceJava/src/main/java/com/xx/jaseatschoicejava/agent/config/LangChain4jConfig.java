package com.xx.jaseatschoicejava.agent.config;

import com.xx.jaseatschoicejava.agent.MerchantAssistantAgent;
import com.xx.jaseatschoicejava.agent.NutritionAiAgent;
import com.xx.jaseatschoicejava.agent.OrderAiAgent;
import com.xx.jaseatschoicejava.agent.RecommendationAiAgent;
import com.xx.jaseatschoicejava.agent.agents.*;
import com.xx.jaseatschoicejava.agent.tools.merchant.MerchantQueryTools;
import com.xx.jaseatschoicejava.agent.tools.merchant.MerchantStatsTools;
import com.xx.jaseatschoicejava.agent.tools.nutrition.NutritionAnalysisTools;
import com.xx.jaseatschoicejava.agent.tools.nutrition.CalorieCalculatorTools;
import com.xx.jaseatschoicejava.agent.tools.order.OrderCreateTools;
import com.xx.jaseatschoicejava.agent.tools.order.OrderQueryTools;
import com.xx.jaseatschoicejava.agent.tools.recommendation.RecommendationFilterTools;
import com.xx.jaseatschoicejava.agent.tools.recommendation.RecommendationQueryTools;
import com.xx.jaseatschoicejava.agent.tools.recommendation.RecommendationRankTools;
import com.xx.jaseatschoicejava.agent.tools.system.LocationTools;
import com.xx.jaseatschoicejava.agent.tools.system.TimeTools;
import com.xx.jaseatschoicejava.agent.tools.user.UserProfileTools;
import com.xx.jaseatschoicejava.agent.tools.CollectionTools;
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

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

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

    private ChatLanguageModel chatLanguageModel;

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

    // Week 3 新增工具类
    @Resource
    private UserProfileTools userProfileTools;

    @Resource
    private MerchantQueryTools merchantQueryTools;

    @Resource
    private MerchantStatsTools merchantStatsTools;

    @Resource
    private TimeTools timeTools;

    @Resource
    private NutritionAnalysisTools nutritionAnalysisTools;

    @Resource
    private CalorieCalculatorTools calorieCalculatorTools;

    @Resource
    private RecommendationQueryTools recommendationQueryTools;

    @Resource
    private RecommendationFilterTools recommendationFilterTools;

    @Resource
    private RecommendationRankTools recommendationRankTools;

    @Resource
    private OrderQueryTools orderQueryTools;

    @Resource
    private OrderCreateTools orderCreateTools;

    /**
     * 配置ChatLanguageModel（智谱AI）
     */
    @Bean(destroyMethod = "") // 禁用Spring的默认destroy方法，避免异常
    public ChatLanguageModel chatLanguageModel() {
        log.info("初始化ChatLanguageModel，模型：{}", zhipuAIConfig.getModel());

        this.chatLanguageModel = ZhipuAiChatModel.builder()
                .apiKey(zhipuAIConfig.getApiKey())
                .model(zhipuAIConfig.getModel())
                .temperature(0.7)
                .maxRetries(2)
                .callTimeout(java.time.Duration.ofSeconds(60))
                .connectTimeout(java.time.Duration.ofSeconds(60))
                .writeTimeout(java.time.Duration.ofSeconds(60))
                .readTimeout(java.time.Duration.ofSeconds(60))
                .build();

        return this.chatLanguageModel;
    }

    /**
     * 应用关闭时清理资源
     * 尝试关闭底层的OkHttp客户端连接池
     */
    @PreDestroy
    public void cleanup() {
        log.info("LangChain4j资源清理开始...");

        try {
            if (chatLanguageModel instanceof ZhipuAiChatModel) {
                ZhipuAiChatModel zhipuModel = (ZhipuAiChatModel) chatLanguageModel;
                // LangChain4j的ZhipuAiChatModel内部使用OkHttpClient
                // 通过反射尝试关闭底层的OkHttpClient
                try {
                    java.lang.reflect.Field clientField = zhipuModel.getClass().getDeclaredField("client");
                    clientField.setAccessible(true);
                    Object client = clientField.get(zhipuModel);

                    if (client != null && client.getClass().getName().contains("okhttp3.OkHttpClient")) {
                        // 调用OkHttpClient的shutdown方法
                        try {
                            java.lang.reflect.Method shutdownMethod = client.getClass().getMethod("shutdown");
                            shutdownMethod.invoke(client);
                            log.info("OkHttpClient已成功关闭");
                        } catch (NoSuchMethodException e) {
                            // 如果没有shutdown方法，尝试使用dispatcher().executorService().shutdown()
                            try {
                                java.lang.reflect.Method dispatcherMethod = client.getClass().getMethod("dispatcher");
                                Object dispatcher = dispatcherMethod.invoke(client);
                                java.lang.reflect.Method executorServiceMethod = dispatcher.getClass().getMethod("executorService");
                                java.util.concurrent.ExecutorService executorService =
                                    (java.util.concurrent.ExecutorService) executorServiceMethod.invoke(dispatcher);
                                executorService.shutdown();
                                log.info("OkHttp ExecutorService已成功关闭");
                            } catch (Exception ex) {
                                log.warn("无法关闭OkHttp ExecutorService: {}", ex.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("反射关闭OkHttpClient失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("LangChain4j资源清理失败", e);
        }

        log.info("LangChain4j资源清理完成");
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

    // ==================== Week 4: L1 基础智能体 ====================

    /**
     * 构建L1用户偏好Agent
     */
    @Bean
    public UserPreferenceAgent userPreferenceAgent(ChatLanguageModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建UserPreferenceAgent...");

        return AiServices.builder(UserPreferenceAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    userProfileTools
                )
                .build();
    }

    /**
     * 构建L1营养指导Agent
     */
    @Bean
    public NutritionGuideAgent nutritionGuideAgent(ChatLanguageModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建NutritionGuideAgent...");

        return AiServices.builder(NutritionGuideAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    nutritionAnalysisTools,
                    calorieCalculatorTools
                )
                .build();
    }

    /**
     * 构建L1菜品推荐Agent
     */
    @Bean
    public DishRecommendationAgent dishRecommendationAgent(ChatLanguageModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建DishRecommendationAgent...");

        return AiServices.builder(DishRecommendationAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    recommendationQueryTools,
                    recommendationFilterTools,
                    recommendationRankTools
                )
                .build();
    }

    /**
     * 构建L1商家信息Agent
     */
    @Bean
    public MerchantInfoAgent merchantInfoAgent(ChatLanguageModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建MerchantInfoAgent...");

        return AiServices.builder(MerchantInfoAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    merchantQueryTools,
                    merchantStatsTools
                )
                .build();
    }

    /**
     * 构建L1时间感知Agent
     */
    @Bean
    public TimeAwareAgent timeAwareAgent(ChatLanguageModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建TimeAwareAgent...");

        return AiServices.builder(TimeAwareAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    timeTools
                )
                .build();
    }

    /**
     * 构建L1位置服务Agent
     */
    @Bean
    public LocationServiceAgent locationServiceAgent(ChatLanguageModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建LocationServiceAgent...");

        return AiServices.builder(LocationServiceAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    locationTools
                )
                .build();
    }

    /**
     * 构建L1订单辅助Agent
     */
    @Bean
    public OrderHelperAgent orderHelperAgent(ChatLanguageModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建OrderHelperAgent...");

        return AiServices.builder(OrderHelperAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    orderQueryTools,
                    orderCreateTools
                )
                .build();
    }

    // ==================== Week 5: L2 领域智能体 ====================

    /**
     * 构建L2智能推荐Agent
     * 综合多个维度的信息提供个性化推荐服务
     */
    @Bean
    public SmartRecommendationAgent smartRecommendationAgent(ChatLanguageModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建SmartRecommendationAgent...");

        return AiServices.builder(SmartRecommendationAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    userProfileTools,
                    recommendationQueryTools,
                    recommendationFilterTools,
                    recommendationRankTools,
                    merchantQueryTools,
                    merchantStatsTools,
                    nutritionAnalysisTools,
                    timeTools,
                    locationTools
                )
                .build();
    }

    /**
     * 构建L2健康管理Agent
     * 综合营养分析、健康目标和饮食记录提供全面的健康管理服务
     */
    @Bean
    public HealthManagementAgent healthManagementAgent(ChatLanguageModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建HealthManagementAgent...");

        return AiServices.builder(HealthManagementAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    userProfileTools,
                    nutritionAnalysisTools,
                    calorieCalculatorTools
                )
                .build();
    }

    /**
     * 构建L2全流程订单Agent
     * 处理从商家选择、菜品选择到订单提交的完整流程
     */
    @Bean
    public FullOrderAgent fullOrderAgent(ChatLanguageModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建FullOrderAgent...");

        return AiServices.builder(FullOrderAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    userProfileTools,
                    merchantQueryTools,
                    recommendationQueryTools,
                    orderQueryTools,
                    orderCreateTools,
                    timeTools,
                    locationTools
                )
                .build();
    }

    /**
     * 构建L2智能助手Agent
     * 综合性智能助手，能够处理各类用户问题并智能路由到相应的L1 Agent
     */
    @Bean
    public IntelligentAssistantAgent intelligentAssistantAgent(ChatLanguageModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建IntelligentAssistantAgent...");

        return AiServices.builder(IntelligentAssistantAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    userProfileTools,
                    nutritionAnalysisTools,
                    calorieCalculatorTools,
                    recommendationQueryTools,
                    recommendationFilterTools,
                    recommendationRankTools,
                    merchantQueryTools,
                    merchantStatsTools,
                    timeTools,
                    locationTools,
                    orderQueryTools,
                    orderCreateTools
                )
                .build();
    }

    // ==================== Week 6: L3 编排智能体 ====================

    /**
     * 构建L3生活服务编排Agent
     * 协调整个订餐、健康、推荐等多个服务流程，为用户提供一站式的生活服务
     */
    @Bean
    public LifeServiceAgent lifeServiceAgent(ChatLanguageModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建LifeServiceAgent...");

        return AiServices.builder(LifeServiceAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    userProfileTools,
                    recommendationQueryTools,
                    recommendationFilterTools,
                    recommendationRankTools,
                    merchantQueryTools,
                    merchantStatsTools,
                    nutritionAnalysisTools,
                    calorieCalculatorTools,
                    timeTools,
                    locationTools,
                    orderQueryTools,
                    orderCreateTools
                )
                .build();
    }

    /**
     * 构建L3每日规划Agent
     * 为用户制定每日饮食和生活规划，整合推荐、营养、时间等多方面信息
     */
    @Bean
    public DailyPlanningAgent dailyPlanningAgent(ChatLanguageModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建DailyPlanningAgent...");

        return AiServices.builder(DailyPlanningAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    userProfileTools,
                    recommendationQueryTools,
                    recommendationFilterTools,
                    recommendationRankTools,
                    nutritionAnalysisTools,
                    calorieCalculatorTools,
                    timeTools
                )
                .build();
    }

    /**
     * 构建L3目标达成Agent
     * 帮助用户达成长期健康目标，提供目标管理、进度跟踪、持续激励等服务
     */
    @Bean
    public GoalAchievementAgent goalAchievementAgent(ChatLanguageModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建GoalAchievementAgent...");

        return AiServices.builder(GoalAchievementAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    userProfileTools,
                    nutritionAnalysisTools,
                    calorieCalculatorTools,
                    recommendationQueryTools,
                    recommendationFilterTools,
                    recommendationRankTools,
                    timeTools
                )
                .build();
    }
}

