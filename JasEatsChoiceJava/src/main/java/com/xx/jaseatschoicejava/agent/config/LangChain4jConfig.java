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
import dev.langchain4j.agentic.AgenticServices;
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
                .maxRetries(2)
                .callTimeout(java.time.Duration.ofSeconds(60))
                .connectTimeout(java.time.Duration.ofSeconds(60))
                .writeTimeout(java.time.Duration.ofSeconds(60))
                .readTimeout(java.time.Duration.ofSeconds(60))
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
                .maxRetries(2)
                .callTimeout(java.time.Duration.ofSeconds(60))
                .connectTimeout(java.time.Duration.ofSeconds(60))
                .writeTimeout(java.time.Duration.ofSeconds(60))
                .readTimeout(java.time.Duration.ofSeconds(60))
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
     * 构建L1用户偏好Agent
     * 配置输出键名为 "userPreferences"，用于Agent间状态共享
     */
    @Bean
    public UserPreferenceAgent userPreferenceAgent(ChatModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建UserPreferenceAgent...");

        return AiServices.builder(UserPreferenceAgent.class)
                .chatModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    userProfileTools
                )
                .build();
    }

    /**
     * 构建L1营养指导Agent
     * 配置输出键名为 "nutritionInfo"，用于Agent间状态共享
     */
    @Bean
    public NutritionGuideAgent nutritionGuideAgent(ChatModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建NutritionGuideAgent...");

        return AiServices.builder(NutritionGuideAgent.class)
                .chatModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    nutritionAnalysisTools,
                    calorieCalculatorTools
                )
                .build();
    }

    /**
     * 构建L1菜品推荐Agent
     * 配置输出键名为 "recommendations"，用于Agent间状态共享
     */
    @Bean
    public DishRecommendationAgent dishRecommendationAgent(ChatModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建DishRecommendationAgent...");

        return AiServices.builder(DishRecommendationAgent.class)
                .chatModel(chatLanguageModel)
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
     * 配置输出键名为 "merchantInfo"，用于Agent间状态共享
     */
    @Bean
    public MerchantInfoAgent merchantInfoAgent(ChatModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建MerchantInfoAgent...");

        return AiServices.builder(MerchantInfoAgent.class)
                .chatModel(chatLanguageModel)
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
    public TimeAwareAgent timeAwareAgent(ChatModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建TimeAwareAgent...");

        return AiServices.builder(TimeAwareAgent.class)
                .chatModel(chatLanguageModel)
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
    public LocationServiceAgent locationServiceAgent(
            ChatModel chatLanguageModel,
            ChatMemory chatMemory) {
        log.info("构建LocationServiceAgent...");

        return AiServices.builder(LocationServiceAgent.class)
                .chatModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(
                    createLocationTools()
                )
                .build();
    }

    /**
     * 构建L1订单辅助Agent
     * 配置输出键名为 "orderInfo"，用于Agent间状态共享
     */
    @Bean
    public OrderHelperAgent orderHelperAgent(ChatModel chatLanguageModel, ChatMemory chatMemory) {
        log.info("构建OrderHelperAgent...");

        return AiServices.builder(OrderHelperAgent.class)
                .chatModel(chatLanguageModel)
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
    public SmartRecommendationAgent smartRecommendationAgent(
            @Qualifier("agentModel") ChatModel agentModel,
            ChatMemory chatMemory) {
        log.info("构建SmartRecommendationAgent...");

        return AgenticServices.agentBuilder(SmartRecommendationAgent.class)
                .chatModel(agentModel)
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
                    createLocationTools()
                )
                .build();
    }

    /**
     * 构建L2健康管理Agent
     * 综合营养分析、健康目标和饮食记录提供全面的健康管理服务
     */
    @Bean
    public HealthManagementAgent healthManagementAgent(
            @Qualifier("agentModel") ChatModel agentModel,
            ChatMemory chatMemory) {
        log.info("构建HealthManagementAgent...");

        return AgenticServices.agentBuilder(HealthManagementAgent.class)
                .chatModel(agentModel)
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
    public FullOrderAgent fullOrderAgent(
            @Qualifier("agentModel") ChatModel agentModel,
            ChatMemory chatMemory) {
        log.info("构建FullOrderAgent...");

        return AgenticServices.agentBuilder(FullOrderAgent.class)
                .chatModel(agentModel)
                .chatMemory(chatMemory)
                .tools(
                    userProfileTools,
                    merchantQueryTools,
                    recommendationQueryTools,
                    orderQueryTools,
                    orderCreateTools,
                    timeTools,
                    createLocationTools()
                )
                .build();
    }

    /**
     * 构建L2智能助手Agent
     * 综合性智能助手，能够处理各类用户问题并智能路由到相应的L1 Agent
     */
    @Bean
    public IntelligentAssistantAgent intelligentAssistantAgent(
            @Qualifier("agentModel") ChatModel agentModel,
            ChatMemory chatMemory) {
        log.info("构建IntelligentAssistantAgent...");

        return AgenticServices.agentBuilder(IntelligentAssistantAgent.class)
                .chatModel(agentModel)
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
                    createLocationTools(),
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
    public LifeServiceAgent lifeServiceAgent(
            @Qualifier("agentModel") ChatModel agentModel,
            ChatMemory chatMemory) {
        log.info("构建LifeServiceAgent...");

        return AiServices.builder(LifeServiceAgent.class)
                .chatModel(agentModel)
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
                    createLocationTools(),
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
    public DailyPlanningAgent dailyPlanningAgent(
            @Qualifier("agentModel") ChatModel agentModel,
            ChatMemory chatMemory) {
        log.info("构建DailyPlanningAgent...");

        return AiServices.builder(DailyPlanningAgent.class)
                .chatModel(agentModel)
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
    public GoalAchievementAgent goalAchievementAgent(
            @Qualifier("agentModel") ChatModel agentModel,
            ChatMemory chatMemory) {
        log.info("构建GoalAchievementAgent...");

        return AiServices.builder(GoalAchievementAgent.class)
                .chatModel(agentModel)
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

    // ==================== Week 7: L3 监督代理 ====================

    /**
     * 构建L3监督代理 (SupervisorAgent)
     * 智能调度L2领域Agent，协调多个Agent完成复杂任务
     *
     * 使用AgenticServices.supervisorBuilder()构建
     * 配置所有L2领域Agent作为子Agent
     */
    @Bean
    public SupervisorAgent supervisorAgent(
            @Qualifier("supervisorModel") ChatModel supervisorModel,
            ChatMemory chatMemory,
            // 注入L2 Agent作为子Agent
            SmartRecommendationAgent smartRecommendationAgent,
            HealthManagementAgent healthManagementAgent,
            FullOrderAgent fullOrderAgent,
            IntelligentAssistantAgent intelligentAssistantAgent) {
        log.info("构建SupervisorAgent（监督代理）...");

        return dev.langchain4j.agentic.AgenticServices
                .supervisorBuilder(SupervisorAgent.class)
                .chatModel(supervisorModel)  // 使用强模型
                .chatMemoryProvider(memoryId -> chatMemory)
                .name("SupervisorAgent")
                .description("智能调度Agent，协调多个L2领域Agent完成复杂任务")
                .subAgents(  // ✅ 关键：注册子Agent
                    smartRecommendationAgent,
                    healthManagementAgent,
                    fullOrderAgent,
                    intelligentAssistantAgent
                )
                .outputKey("supervisorResult")
                .supervisorContext("""
                    # 角色定义
                    你是一个智能监督代理（SupervisorAgent），负责协调各个领域专家Agent为用户提供服务。

                    # 可用的领域专家Agent
                    1. SmartRecommendationAgent - 智能推荐专家（菜品推荐、个性化推荐）
                    2. HealthManagementAgent - 健康管理专家（营养分析、饮食建议）
                    3. FullOrderAgent - 订单处理专家（从商家选择到订单提交）
                    4. IntelligentAssistantAgent - 综合智能助手（处理各类用户问题）

                    # 工作流程
                    1. 理解用户的完整需求和上下文
                    2. 分析问题涉及哪些领域（推荐、健康、订单等）
                    3. 选择最合适的领域Agent或协调多个Agent协作
                    4. 综合各Agent的结果，生成清晰、友好的最终回复

                    # 约束条件
                    - 优先使用用户ID进行个性化查询
                    - 数据不足时主动询问用户
                    - 提供准确的操作结果和状态反馈
                    - 多Agent协作时，明确说明每个Agent的职责
                    - 确保最终回复对用户友好、易于理解

                    # 响应策略
                    - 如果单一Agent可以解决，直接使用其结果
                    - 如果需要多个Agent协作，综合所有结果
                    - 如果涉及用户数据，先确认用户身份
                    """)
                .contextGenerationStrategy(
                    dev.langchain4j.agentic.supervisor.SupervisorContextStrategy.CHAT_MEMORY_AND_SUMMARIZATION
                )
                .responseStrategy(
                    dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy.SCORED
                )
                .maxAgentsInvocations(10)  // 最多调用10个子Agent
                .build();
    }
}

