package com.xx.jaseatschoicejava.agent.config;

import com.xx.jaseatschoicejava.agent.MerchantAssistantAgent;
import com.xx.jaseatschoicejava.agent.NutritionAiAgent;
import com.xx.jaseatschoicejava.agent.OrderAiAgent;
import com.xx.jaseatschoicejava.agent.RecommendationAiAgent;
import com.xx.jaseatschoicejava.agent.agents.CardRendererAgent;
import com.xx.jaseatschoicejava.agent.agents.CustomerServiceAgent;
import com.xx.jaseatschoicejava.agent.agents.DishRecommendationAgent;
import com.xx.jaseatschoicejava.agent.agents.LocationServiceAgent;
import com.xx.jaseatschoicejava.agent.agents.MerchantInfoAgent;
import com.xx.jaseatschoicejava.agent.agents.NutritionGuideAgent;
import com.xx.jaseatschoicejava.agent.agents.OrderHelperAgent;
import com.xx.jaseatschoicejava.agent.agents.TimeAwareAgent;
import com.xx.jaseatschoicejava.agent.agents.UserPreferenceAgent;
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
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

/**
 * 统一Agent配置类
 * 使用AgenticServices构建AI Agent，支持监督代理模式
 *
 * 整合了原LangChain4jConfig和AgenticWorkflowConfig的功能
 *
 * @author Claude
 * @since 2026-03-25
 */
@Configuration
@EnableConfigurationProperties(ZhipuAIConfig.class)
public class LangChain4jConfig {

    private static final Logger log = LoggerFactory.getLogger(LangChain4jConfig.class);

    @Resource
    private ZhipuAIConfig zhipuAIConfig;

    private ChatModel chatLanguageModel;

    @Resource
    private NutritionTools nutritionTools;

    @Resource
    private NutritionRecordTools nutritionRecordTools;

    @Resource
    private RecommendationTools recommendationTools;

    @Resource
    private RecipeTools recipeTools;

    // 注意：orderTools 通过私有辅助方法创建，避免Spring AOP代理导致LangChain4j无法扫描@Tool注解

    @Resource
    private CollectionTools collectionTools;

    @Resource
    private UserTools userTools;

    // 注意：locationTools 通过 @Bean 方法创建，避免Spring AOP代理导致LangChain4j无法扫描@Tool注解
    // 所有使用 locationTools 的 Bean 方法都通过方法参数注入

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

    // 新增工具类（2026-03-27）
    @Resource
    private com.xx.jaseatschoicejava.agent.tools.user.HealthGoalTrackerTools healthGoalTrackerTools;

    @Resource
    private com.xx.jaseatschoicejava.agent.tools.nutrition.DietRecordAnalysisTools dietRecordAnalysisTools;

    @Resource
    private com.xx.jaseatschoicejava.agent.tools.system.TimeRecommendationTools timeRecommendationTools;

    @Resource
    private com.xx.jaseatschoicejava.agent.tools.system.LocationRecommendationTools locationRecommendationTools;

    @Resource
    private com.xx.jaseatschoicejava.service.MerchantService merchantService;

    @Resource
    private com.xx.jaseatschoicejava.service.DishService dishService;

    @Resource
    private com.xx.jaseatschoicejava.service.OrderService orderService;

    @Resource
    private com.xx.jaseatschoicejava.service.UserCouponService userCouponService;

    /**
     * 配置Supervisor专用模型（更强推理能力）
     * 使用glm-4-plus提供更好的规划和决策能力
     */
    @Bean("supervisorModel")
    @SuppressWarnings("unused")
    public ChatModel supervisorModel() {
        log.info("初始化Supervisor专用模型，模型：glm-4-plus");

        return ZhipuAiChatModel.builder()
                .apiKey(zhipuAIConfig.getApiKey())
                .model("glm-4-plus")  // 使用更强的模型
                .temperature(0.3)     // 降低温度，更确定的规划
                .maxRetries(1)
                // 注意：callTimeout, writeTimeout 已弃用，移除使用默认值
                .build();
    }

    /**
     * 配置子Agent通用模型（快速响应）
     * 使用glm-4-flash提供快速响应
     */
    @Bean("agentModel")
    @SuppressWarnings("unused")
    public ChatModel agentModel() {
        log.info("初始化子Agent通用模型，模型：glm-4-flash");

        return ZhipuAiChatModel.builder()
                .apiKey(zhipuAIConfig.getApiKey())
                .model("glm-4-flash")  // 使用快速模型
                .temperature(0.7)
                .maxRetries(1)
                // 注意：callTimeout, writeTimeout 已弃用，移除使用默认值
                .build();
    }

    /**
     * 配置ChatModel（智谱AI）
     * @deprecated 优先使用 supervisorModel 或 agentModel，保留此方法仅为兼容性
     */
    @Bean(destroyMethod = "") // 禁用Spring的默认destroy方法，避免异常
    @Deprecated
    public ChatModel chatLanguageModel() {
        log.info("初始化ChatModel，模型：{}", zhipuAIConfig.getModel());

        this.chatLanguageModel = ZhipuAiChatModel.builder()
                .apiKey(zhipuAIConfig.getApiKey())
                .model(zhipuAIConfig.getModel())
                .temperature(0.7)
                .maxRetries(1)
                // 注意：callTimeout, writeTimeout 已弃用，移除使用默认值
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
     * 创建非代理的LocationTools实例
     *
     * 重要：必须手动创建LocationTools实例并注入依赖，避免Spring AOP代理
     * LangChain4j需要扫描原始类的@Tool注解，如果被代理则无法扫描
     *
     * 注意：这不是 @Bean 方法，避免 Spring 代理！
     */
    private LocationTools createLocationTools() {
        LocationTools tools = new LocationTools();
        // 手动注入依赖（通过反射设置字段）
        setField(tools, "merchantService", merchantService);
        setField(tools, "dishService", dishService);
        return tools;
    }

    /**
     * 创建非代理的OrderTools实例
     *
     * 重要：必须手动创建OrderTools实例并注入依赖，避免Spring AOP代理
     * LangChain4j需要扫描原始类的@Tool注解，如果被代理则无法扫描
     *
     * 注意：这不是 @Bean 方法，避免 Spring 代理！
     */
    private OrderTools createOrderTools() {
        OrderTools tools = new OrderTools();
        // 手动注入依赖（通过反射设置字段）
        setField(tools, "orderService", orderService);
        setField(tools, "dishService", dishService);
        setField(tools, "userCouponService", userCouponService);
        return tools;
    }

    /**
     * 反射设置字段值的辅助方法
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            log.error("设置字段失败: {} = {}", fieldName, value, e);
        }
    }

    /**
     * 构建营养分析AI Agent
     *
     * 使用LangChain4j的AiServices构建真正的Agent
     * LLM会自动决定何时调用哪个Tool
     */
    @Bean
    public NutritionAiAgent nutritionAiAgent(ChatModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建NutritionAiAgent...");

        return AiServices.builder(NutritionAiAgent.class)
                .chatModel(chatLanguageModel)
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
    public RecommendationAiAgent recommendationAiAgent(
            ChatModel chatLanguageModel,
            ChatMemory chatMemory) {
        log.info("构建RecommendationAiAgent...");

        return AiServices.builder(RecommendationAiAgent.class)
                .chatModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    recommendationTools,
                    recipeTools,
                    collectionTools,
                    nutritionTools,
                    createLocationTools()
                )
                .build();
    }

    /**
     * 构建订单助手AI Agent
     */
    @Bean
    public OrderAiAgent orderAiAgent(ChatModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建OrderAiAgent...");

        return AiServices.builder(OrderAiAgent.class)
                .chatModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    createOrderTools(),
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
    @Scope("prototype")
    public MerchantAssistantAgent merchantAssistantAgent(ChatModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建MerchantAssistantAgent...");

        return AiServices.builder(MerchantAssistantAgent.class)
                .chatModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    merchantTools
                )
                .build();
    }

    // ==================== Week 4: L1 基础智能体 ====================

    /**
     * 构建客服助手Agent（无个性化服务）
     *
     * 用于未开启个性化服务时的基础对话和引导
     * 不需要ChatMemory，每次对话都是独立的
     */
    @Bean
    @Scope("prototype")
    public CustomerServiceAgent customerServiceAgent(ChatModel chatLanguageModel) {
        log.info("构建CustomerServiceAgent（客服助手）...");

        return AiServices.builder(CustomerServiceAgent.class)
                .chatModel(chatLanguageModel)
                // 不使用ChatMemory，每次对话都是独立的
                .build();
    }

    /**
     * 构建L1用户偏好Agent
     */
    @Bean
    @Scope("prototype")
    public UserPreferenceAgent userPreferenceAgent(@Qualifier("agentModel") ChatModel agentModel) {
        log.info("构建UserPreferenceAgent...");

        return AgenticServices.agentBuilder(UserPreferenceAgent.class)
                .chatModel(agentModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .name("UserPreferenceAgent")
                .tools(
                    userProfileTools,
                    healthGoalTrackerTools
                )
                .build();
    }

    /**
     * 构建L1营养指导Agent
     */
    @Bean
    @Scope("prototype")
    public NutritionGuideAgent nutritionGuideAgent(@Qualifier("agentModel") ChatModel agentModel) {
        log.info("构建NutritionGuideAgent...");

        return AgenticServices.agentBuilder(NutritionGuideAgent.class)
                .chatModel(agentModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .name("NutritionGuideAgent")
                .tools(
                    nutritionAnalysisTools,
                    calorieCalculatorTools,
                    dietRecordAnalysisTools
                )
                .build();
    }

    /**
     * 构建L1菜品推荐Agent
     */
    @Bean
    @Scope("prototype")
    public DishRecommendationAgent dishRecommendationAgent(
            @Qualifier("agentModel") ChatModel agentModel) {
        log.info("构建DishRecommendationAgent...");

        return AgenticServices.agentBuilder(DishRecommendationAgent.class)
                .chatModel(agentModel)  // ✅ 使用agentModel而不是chatLanguageModel
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .name("DishRecommendationAgent")
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
    @Scope("prototype")
    public MerchantInfoAgent merchantInfoAgent(@Qualifier("agentModel") ChatModel agentModel) {
        log.info("构建MerchantInfoAgent...");

        return AgenticServices.agentBuilder(MerchantInfoAgent.class)
                .chatModel(agentModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .name("MerchantInfoAgent")
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
    @Scope("prototype")
    public TimeAwareAgent timeAwareAgent(@Qualifier("agentModel") ChatModel agentModel) {
        log.info("构建TimeAwareAgent...");

        return AgenticServices.agentBuilder(TimeAwareAgent.class)
                .chatModel(agentModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .name("TimeAwareAgent")
                .tools(
                    timeTools,
                    timeRecommendationTools
                )
                .build();
    }

    /**
     * 构建L1位置服务Agent
     */
    @Bean
    @Scope("prototype")
    public LocationServiceAgent locationServiceAgent(
            ChatModel chatLanguageModel) {
        log.info("构建LocationServiceAgent...");

        return AgenticServices.agentBuilder(LocationServiceAgent.class)
                .chatModel(chatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .name("LocationServiceAgent")
                .tools(
                    createLocationTools(),
                    locationRecommendationTools
                )
                .build();
    }

    /**
     * 构建L1订单辅助Agent
     */
    @Bean
    @Scope("prototype")
    public OrderHelperAgent orderHelperAgent(@Qualifier("agentModel") ChatModel agentModel) {
        log.info("构建OrderHelperAgent...");

        return AgenticServices.agentBuilder(OrderHelperAgent.class)
                .chatModel(agentModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .name("OrderHelperAgent")
                .tools(
                    orderQueryTools,
                    orderCreateTools
                )
                .build();
    }

    /**
     * 构建L1卡片渲染Agent
     * 负责将SupervisorAgent的结果格式化为卡片格式
     */
    @Bean
    @Scope("prototype")
    public CardRendererAgent cardRendererAgent(ChatModel chatLanguageModel) {
        log.info("构建CardRendererAgent（L1 Agent）...");

        return AgenticServices.agentBuilder(CardRendererAgent.class)
                .chatModel(chatLanguageModel)
                .name("CardRendererAgent")
                .description("消息格式化专家，将结果渲染为卡片格式")
                .build();
    }

}

