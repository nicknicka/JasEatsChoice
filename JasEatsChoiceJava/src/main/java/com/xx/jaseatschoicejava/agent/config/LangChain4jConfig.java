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
                .maxRetries(2)
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
                .maxRetries(2)
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
    public UserPreferenceAgent userPreferenceAgent(@Qualifier("agentModel") ChatModel agentModel, ChatMemory chatMemory) {
        log.info("构建UserPreferenceAgent...");

        return AgenticServices.agentBuilder(UserPreferenceAgent.class)
                .chatModel(agentModel)
                .chatMemory(chatMemory)
                .name("UserPreferenceAgent")
                .tools(
                    userProfileTools
                )
                .build();
    }

    /**
     * 构建L1营养指导Agent
     */
    @Bean
    public NutritionGuideAgent nutritionGuideAgent(@Qualifier("agentModel") ChatModel agentModel, ChatMemory chatMemory) {
        log.info("构建NutritionGuideAgent...");

        return AgenticServices.agentBuilder(NutritionGuideAgent.class)
                .chatModel(agentModel)
                .chatMemory(chatMemory)
                .name("NutritionGuideAgent")
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
    public DishRecommendationAgent dishRecommendationAgent(
            @Qualifier("agentModel") ChatModel agentModel,
            ChatMemory chatMemory) {
        log.info("构建DishRecommendationAgent...");

        return AgenticServices.agentBuilder(DishRecommendationAgent.class)
                .chatModel(agentModel)  // ✅ 使用agentModel而不是chatLanguageModel
                .chatMemory(chatMemory)
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
    public MerchantInfoAgent merchantInfoAgent(@Qualifier("agentModel") ChatModel agentModel, ChatMemory chatMemory) {
        log.info("构建MerchantInfoAgent...");

        return AgenticServices.agentBuilder(MerchantInfoAgent.class)
                .chatModel(agentModel)
                .chatMemory(chatMemory)
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
    public TimeAwareAgent timeAwareAgent(@Qualifier("agentModel") ChatModel agentModel, ChatMemory chatMemory) {
        log.info("构建TimeAwareAgent...");

        return AgenticServices.agentBuilder(TimeAwareAgent.class)
                .chatModel(agentModel)
                .chatMemory(chatMemory)
                .name("TimeAwareAgent")
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

        return AgenticServices.agentBuilder(LocationServiceAgent.class)
                .chatModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .name("LocationServiceAgent")
                .tools(
                    createLocationTools()
                )
                .build();
    }

    /**
     * 构建L1订单辅助Agent
     */
    @Bean
    public OrderHelperAgent orderHelperAgent(@Qualifier("agentModel") ChatModel agentModel, ChatMemory chatMemory) {
        log.info("构建OrderHelperAgent...");

        return AgenticServices.agentBuilder(OrderHelperAgent.class)
                .chatModel(agentModel)
                .chatMemory(chatMemory)
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
    public CardRendererAgent cardRendererAgent(ChatModel chatLanguageModel) {
        log.info("构建CardRendererAgent（L1 Agent）...");

        return AgenticServices.agentBuilder(CardRendererAgent.class)
                .chatModel(chatLanguageModel)
                .name("CardRendererAgent")
                .description("消息格式化专家，将结果渲染为卡片格式")
                .build();
    }

    // ==================== Week 5: L2 领域智能体 ====================

    /**
     * 构建L2智能推荐Agent（Supervisor模式）
     * 综合多个维度的信息提供个性化推荐服务
     *
     * 调用L1 Agent：DishRecommendationAgent, MerchantInfoAgent, UserPreferenceAgent
     */
    @Bean("smartRecommendationAgent")
    public SupervisorAgent smartRecommendationAgent(
            @Qualifier("agentModel") ChatModel agentModel,
            ChatMemory chatMemory,
            // 注入L1 Agent作为子Agent
            DishRecommendationAgent dishRecommendationAgent,
            MerchantInfoAgent merchantInfoAgent,
            UserPreferenceAgent userPreferenceAgent,
            LocationServiceAgent locationServiceAgent) {
        log.info("构建SmartRecommendationAgent（L2 Supervisor）...");

        return AgenticServices.supervisorBuilder()
                .chatModel(agentModel)
                .chatMemoryProvider(memoryId -> chatMemory)
                .name("SmartRecommendationAgent")
                .description("智能推荐专家，综合用户偏好、菜品和商家信息提供个性化推荐")
                .subAgents(
                    dishRecommendationAgent,
                    merchantInfoAgent,
                    userPreferenceAgent,
                    locationServiceAgent
                )
                .supervisorContext("""
                    你是"佳食宜选"的智能推荐专家，为用户提供个性化的菜品和商家推荐。

                    你可以调用以下Agent：
                    - **DishRecommendationAgent** - 菜品搜索和推荐
                    - **MerchantInfoAgent** - 商家信息和统计数据
                    - **UserPreferenceAgent** - 用户偏好和饮食忌口
                    - **LocationServiceAgent** - 位置服务和附近美食推荐

                    根据用户需求灵活组合多个Agent的能力，提供贴心的推荐服务。
                    注意结合用户的个人偏好，给出有针对性的建议。

                    ⚠️ 终止条件：
                    - 获得推荐结果后，立即停止调用子Agent
                    - 最多调用3个子Agent后必须返回结果
                    - 不要重复调用同一个Agent

                    ⚠️ 输出格式要求：
                    - 如果子Agent返回了推荐数据，整理成友好的格式返回给用户
                    - 在推荐结果前添加简短的引导语
                    - 在推荐结果后添加温馨提示（可选）
                    """)
                .maxAgentsInvocations(3)  // ✅ 最多调用3次子Agent
                .build();
    }

    /**
     * 构建L2健康管理Agent（Supervisor模式）
     * 综合营养分析、健康目标和饮食记录提供全面的健康管理服务
     *
     * 调用L1 Agent：NutritionGuideAgent, UserPreferenceAgent
     */
    @Bean("healthManagementAgent")
    public SupervisorAgent healthManagementAgent(
            @Qualifier("agentModel") ChatModel agentModel,
            ChatMemory chatMemory,
            // 注入L1 Agent作为子Agent
            NutritionGuideAgent nutritionGuideAgent,
            UserPreferenceAgent userPreferenceAgent) {
        log.info("构建HealthManagementAgent（L2 Supervisor）...");

        return AgenticServices.supervisorBuilder()
                .chatModel(agentModel)
                .chatMemoryProvider(memoryId -> chatMemory)
                .name("HealthManagementAgent")
                .description("健康管理专家，提供营养分析、饮食建议和目标管理")
                .subAgents(
                    nutritionGuideAgent,
                    userPreferenceAgent
                )
                .supervisorContext("""
                    你是"佳食宜选"的健康管理专家，帮助用户进行营养分析、卡路里管理和健康目标追踪。

                    你可以调用以下Agent：
                    - **NutritionGuideAgent** - 营养分析和卡路里计算
                    - **UserPreferenceAgent** - 用户健康目标和饮食记录

                    关注用户的健康状况，提供科学的饮食建议，帮助用户达成健康目标。
                    灵活运用多个Agent的能力，给出全面的健康分析。

                    ⚠️ 终止条件：
                    - 获得健康分析结果后，立即停止调用子Agent
                    - 最多调用3个子Agent后必须返回结果
                    - 不要重复调用同一个Agent

                    ⚠️ 输出格式要求：
                    - 整理子Agent的分析结果，用友好的方式呈现给用户
                    - 添加个性化的健康建议
                    """)
                .maxAgentsInvocations(3)  // ✅ 最多调用3次子Agent
                .build();
    }

    /**
     * 构建L2全流程订单Agent（Supervisor模式）
     * 处理从商家选择、菜品选择到订单提交的完整流程
     *
     * 调用L1 Agent：OrderHelperAgent, MerchantInfoAgent, LocationServiceAgent
     */
    @Bean("fullOrderAgent")
    public SupervisorAgent fullOrderAgent(
            @Qualifier("agentModel") ChatModel agentModel,
            ChatMemory chatMemory,
            // 注入L1 Agent作为子Agent
            OrderHelperAgent orderHelperAgent,
            MerchantInfoAgent merchantInfoAgent,
            LocationServiceAgent locationServiceAgent) {
        log.info("构建FullOrderAgent（L2 Supervisor）...");

        return AgenticServices.supervisorBuilder()
                .chatModel(agentModel)
                .chatMemoryProvider(memoryId -> chatMemory)
                .name("FullOrderAgent")
                .description("订单处理专家，处理从商家选择到订单提交的完整流程")
                .subAgents(
                    orderHelperAgent,
                    merchantInfoAgent,
                    locationServiceAgent
                )
                .supervisorContext("""
                    你是"佳食宜选"的订单处理专家，协助用户完成从商家选择到订单提交的完整订餐流程。

                    你可以调用以下Agent：
                    - **OrderHelperAgent** - 订单创建和查询
                    - **MerchantInfoAgent** - 商家信息和营业状态
                    - **LocationServiceAgent** - 地址和配送范围确认

                    引导用户完成订餐流程，逐步确认关键信息（商家、菜品、地址、数量）。
                    保持对话流畅，提供清晰的订单状态反馈。

                    ⚠️ 终止条件：
                    - 完成订单操作后，立即停止调用子Agent
                    - 最多调用4个子Agent后必须返回结果
                    - 不要重复调用同一个Agent

                    ⚠️ 输出格式要求：
                    - 整理订单信息和状态，用清晰的方式呈现给用户
                    - 提供下一步操作的指引
                    """)
                .maxAgentsInvocations(4)  // ✅ 订单流程可能需要更多步骤
                .build();
    }

    /**
     * 构建L2智能助手Agent（Supervisor模式）
     * 综合性智能助手，能够处理各类用户问题并智能路由到相应的L1 Agent
     *
     * 调用L1 Agent：所有L1 Agent
     */
    @Bean("intelligentAssistantAgent")
    public SupervisorAgent intelligentAssistantAgent(
            @Qualifier("agentModel") ChatModel agentModel,
            ChatMemory chatMemory,
            // 注入所有L1 Agent作为子Agent
            UserPreferenceAgent userPreferenceAgent,
            NutritionGuideAgent nutritionGuideAgent,
            DishRecommendationAgent dishRecommendationAgent,
            MerchantInfoAgent merchantInfoAgent,
            TimeAwareAgent timeAwareAgent,
            LocationServiceAgent locationServiceAgent,
            OrderHelperAgent orderHelperAgent) {
        log.info("构建IntelligentAssistantAgent（L2 Supervisor）...");

        return AgenticServices.supervisorBuilder()
                .chatModel(agentModel)
                .chatMemoryProvider(memoryId -> chatMemory)
                .name("IntelligentAssistantAgent")
                .description("综合智能助手，处理各类用户问题并智能路由")
                .subAgents(
                    userPreferenceAgent,
                    nutritionGuideAgent,
                    dishRecommendationAgent,
                    merchantInfoAgent,
                    timeAwareAgent,
                    locationServiceAgent,
                    orderHelperAgent
                )
                .supervisorContext("""
                    你是"佳食宜选"的智能助手，帮助用户解决各种饮食相关的问题。

                    你可以调用以下专业Agent来协助用户：
                    - **DishRecommendationAgent** - 菜品推荐
                    - **NutritionGuideAgent** - 营养与健康建议
                    - **MerchantInfoAgent** - 商家信息查询
                    - **OrderHelperAgent** - 订单管理
                    - **UserPreferenceAgent** - 用户偏好设置
                    - **TimeAwareAgent** - 时间相关信息
                    - **LocationServiceAgent** - 位置与配送服务

                    根据用户的问题智能选择合适的Agent，灵活协调多个Agent共同完成复杂任务。
                    保持对话自然友好，以解决用户问题为核心目标。

                    ⚠️ 终止条件：
                    - 问题得到解答后，立即停止调用子Agent
                    - 最多调用3个子Agent后必须返回结果
                    - 不要重复调用同一个Agent

                    ⚠️ 输出格式要求：
                    - 整理子Agent的回复，用友好的方式呈现给用户
                    - 确保直接回答用户的问题
                    """)
                .maxAgentsInvocations(3)  // ✅ 最多调用3次子Agent
                .build();
    }

    // ==================== L3 监督代理 ====================

    /**
     * 构建L3监督代理 (SupervisorAgent)
     * 智能调度L2领域Agent，协调多个Agent完成复杂任务
     *
     * 使用AgenticServices.supervisorBuilder()构建
     * 配置所有L2领域Agent作为子Agent
     *
     * 注意：使用 LangChain4j 内置的 SupervisorAgent 接口
     */
    @Bean
    public SupervisorAgent supervisorAgent(
            @Qualifier("supervisorModel") ChatModel supervisorModel,
            ChatMemory chatMemory,
            // 注入L2 Agent作为子Agent（使用SupervisorAgent类型+@Qualifier）
            @Qualifier("smartRecommendationAgent") SupervisorAgent smartRecommendationAgent,
            @Qualifier("healthManagementAgent") SupervisorAgent healthManagementAgent,
            @Qualifier("fullOrderAgent") SupervisorAgent fullOrderAgent,
            @Qualifier("intelligentAssistantAgent") SupervisorAgent intelligentAssistantAgent) {
        log.info("构建SupervisorAgent（监督代理）...");

        return dev.langchain4j.agentic.AgenticServices
                .supervisorBuilder()
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
                    你是"佳食宜选"的智能监督代理，协调各个领域专家Agent为用户提供全面的服务。

                    你可以调用以下领域专家Agent：
                    - **SmartRecommendationAgent** - 个性化菜品和商家推荐
                    - **HealthManagementAgent** - 营养分析、健康目标和饮食建议
                    - **FullOrderAgent** - 订餐流程和订单管理
                    - **IntelligentAssistantAgent** - 综合问题解答

                    根据用户需求灵活选择最合适的Agent，或协调多个Agent协作完成复杂任务。
                    理解用户的完整需求，提供个性化的解决方案。

                    ⚠️ 输出格式要求：
                    - 子Agent返回的JSON数据，保持原样，不要修改
                    - 使用markdown代码块包裹JSON
                    - 在JSON前后添加自然语言的总结和建议
                    - 不要破坏JSON的结构
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

