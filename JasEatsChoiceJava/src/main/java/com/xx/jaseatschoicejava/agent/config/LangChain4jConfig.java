package com.xx.jaseatschoicejava.agent.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.xx.jaseatschoicejava.agent.agents.CardRendererAgent;
import com.xx.jaseatschoicejava.agent.agents.CustomerServiceAgent;
import com.xx.jaseatschoicejava.agent.agents.DishRecommendationAgent;
import com.xx.jaseatschoicejava.agent.agents.LocationServiceAgent;
import com.xx.jaseatschoicejava.agent.agents.MerchantInfoAgent;
import com.xx.jaseatschoicejava.agent.agents.NutritionGuideAgent;
import com.xx.jaseatschoicejava.agent.agents.OrderHelperAgent;
import com.xx.jaseatschoicejava.agent.agents.TimeAwareAgent;
import com.xx.jaseatschoicejava.agent.agents.UserPreferenceAgent;
import com.xx.jaseatschoicejava.agent.tools.menu.MenuQueryTools;
import com.xx.jaseatschoicejava.agent.tools.merchant.MerchantQueryTools;
import com.xx.jaseatschoicejava.agent.tools.merchant.MerchantStatsTools;
import com.xx.jaseatschoicejava.agent.tools.nutrition.CalorieCalculatorTools;
import com.xx.jaseatschoicejava.agent.tools.nutrition.DietRecordAnalysisTools;
import com.xx.jaseatschoicejava.agent.tools.nutrition.NutritionAnalysisTools;
import com.xx.jaseatschoicejava.agent.tools.nutrition.NutritionQueryTools;
import com.xx.jaseatschoicejava.agent.tools.order.OrderCreateTools;
import com.xx.jaseatschoicejava.agent.tools.order.OrderGuideTools;
import com.xx.jaseatschoicejava.agent.tools.order.OrderQueryTools;
import com.xx.jaseatschoicejava.agent.tools.recommendation.RecommendationFilterTools;
import com.xx.jaseatschoicejava.agent.tools.recommendation.RecommendationQueryTools;
import com.xx.jaseatschoicejava.agent.tools.recommendation.RecommendationRankTools;
import com.xx.jaseatschoicejava.agent.tools.system.LocationRecommendationTools;
import com.xx.jaseatschoicejava.agent.tools.system.LocationTools;
import com.xx.jaseatschoicejava.agent.tools.system.TimeRecommendationTools;
import com.xx.jaseatschoicejava.agent.tools.system.TimeTools;
import com.xx.jaseatschoicejava.agent.tools.user.HealthGoalTrackerTools;
import com.xx.jaseatschoicejava.agent.tools.user.UserDietRecordTools;
import com.xx.jaseatschoicejava.agent.tools.user.UserHealthGoalTools;
import com.xx.jaseatschoicejava.agent.tools.user.UserPreferenceTools;
import com.xx.jaseatschoicejava.agent.tools.user.UserProfileTools;
import com.xx.jaseatschoicejava.agent.tools.user.UserQueryTools;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.community.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

/**
 * Agent配置类 — L1 专家 Agent 构建
 *
 * 仅构建 L1 专家 Agent 和所需的 ChatModel Bean。
 * L2 SupervisorAgent 由 SupervisorAgentFactory 动态创建。
 *
 * @author Claude
 * @since 2026-03-25
 * @updated 2026-04-03 清理废弃Agent和旧版工具
 */
@Configuration
@EnableConfigurationProperties(ZhipuAIConfig.class)
public class LangChain4jConfig {

    private static final Logger log = LoggerFactory.getLogger(LangChain4jConfig.class);

    @Resource
    private ZhipuAIConfig zhipuAIConfig;

    // ==================== L1 Agent 工具注入 ====================

    @Resource
    private UserProfileTools userProfileTools;

    @Resource
    private HealthGoalTrackerTools healthGoalTrackerTools;

    @Resource
    private NutritionAnalysisTools nutritionAnalysisTools;

    @Resource
    private CalorieCalculatorTools calorieCalculatorTools;

    @Resource
    private DietRecordAnalysisTools dietRecordAnalysisTools;

    @Resource
    private NutritionQueryTools nutritionQueryTools;

    @Resource
    private RecommendationQueryTools recommendationQueryTools;

    @Resource
    private RecommendationFilterTools recommendationFilterTools;

    @Resource
    private RecommendationRankTools recommendationRankTools;

    @Resource
    private MerchantQueryTools merchantQueryTools;

    @Resource
    private MerchantStatsTools merchantStatsTools;

    @Resource
    private TimeTools timeTools;

    @Resource
    private TimeRecommendationTools timeRecommendationTools;

    @Resource
    private LocationRecommendationTools locationRecommendationTools;

    // OrderQueryTools 手动创建，避免Spring AOP代理导致@Tool注解无法扫描
    // OrderCreateTools 手动创建，避免Spring AOP代理导致@Tool注解无法扫描

    // OrderGuideTools 手动创建，避免Spring AOP代理导致@Tool注解无法扫描

    // MenuQueryTools 手动创建，避免Spring AOP代理导致@Tool注解无法扫描

    @Resource
    private com.xx.jaseatschoicejava.service.OrderService orderService;

    @Resource
    private com.xx.jaseatschoicejava.service.OrderDishService orderDishService;

    @Resource
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Resource
    private UserQueryTools userQueryTools;

    @Resource
    private UserPreferenceTools userPreferenceTools;

    @Resource
    private UserHealthGoalTools userHealthGoalTools;

    @Resource
    private UserDietRecordTools userDietRecordTools;

    // 需要手动创建的工具（避免Spring AOP代理导致@Tool注解无法扫描）
    @Resource
    private com.xx.jaseatschoicejava.service.MerchantService merchantService;

    @Resource
    private com.xx.jaseatschoicejava.service.DishService dishService;

    // ==================== ChatModel Bean ====================

    /**
     * Supervisor专用模型（更强推理能力）
     * 使用glm-4-plus提供更好的规划和决策能力
     *
     * 包装MarkdownStrippingChatModel：GLM模型倾向将JSON包裹在```json中，
     * langchain4j的extractAndParseJson无法处理，需要预先剥离
     */
    @Bean("supervisorModel")
    public ChatModel supervisorModel() {
        log.info("初始化Supervisor专用模型，模型：{}（含Markdown剥离包装）", zhipuAIConfig.getModel());

        ChatModel rawModel = ZhipuAiChatModel.builder()
                .apiKey(zhipuAIConfig.getApiKey())
            .model(zhipuAIConfig.getModel())
                .temperature(0.3)
                .maxToken(4096)
                .maxRetries(1)
                .build();

        return new MarkdownStrippingChatModel(rawModel);
    }

    /**
     * 通用AI模型
     * 实际模型由 application.yml 中的 zhipuai.model 决定
     */
    @Bean("aiModel")
    public ChatModel aiModel() {
        log.info("初始化通用AI模型，模型：{}", zhipuAIConfig.getModel());

        return ZhipuAiChatModel.builder()
                .apiKey(zhipuAIConfig.getApiKey())
                .model(zhipuAIConfig.getModel())
                .temperature(0.7)
                .maxRetries(2)
                .connectTimeout(java.time.Duration.ofSeconds(60))
                .readTimeout(java.time.Duration.ofSeconds(60))
                .build();
    }

    /**
     * 子Agent通用模型（兼容别名）
     */
    @Bean("agentModel")
    @Deprecated
    public ChatModel agentModel(@Qualifier("aiModel") ChatModel aiModel) {
        log.info("初始化子Agent通用模型别名：agentModel -> aiModel");
        return aiModel;
    }

    /**
     * 兼容性ChatModel Bean
     * @deprecated 优先使用 supervisorModel 或 agentModel
     */
    @Bean(destroyMethod = "")
    @Deprecated
    public ChatModel chatLanguageModel(@Qualifier("aiModel") ChatModel aiModel) {
        log.info("初始化兼容ChatModel别名：chatLanguageModel -> aiModel");
        return aiModel;
    }

    /**
     * 视觉模型（用于菜品识别、图像理解）
     * 使用 glm-4.6v-flash 支持视觉识别
     */
    @Bean("visionModel")
    public ChatModel visionModel() {
        log.info("初始化视觉模型，模型：{}", zhipuAIConfig.getVisionModel());

        return ZhipuAiChatModel.builder()
                .apiKey(zhipuAIConfig.getApiKey())
                .model(zhipuAIConfig.getVisionModel())
                .temperature(0.3)
                .maxToken(2048)
                .maxRetries(1)
                .build();
    }

    // ==================== L1 专家 Agent ====================

    /**
     * L1 客服助手Agent（无个性化服务）
     */
    @Bean
    @Scope("prototype")
    public CustomerServiceAgent customerServiceAgent(@Qualifier("aiModel") ChatModel aiModel) {
        log.info("构建CustomerServiceAgent（客服助手）...");

        return dev.langchain4j.service.AiServices.builder(CustomerServiceAgent.class)
                .chatModel(aiModel)
                .build();
    }

    /**
     * L1 用户偏好Agent
     */
    @Bean
    @Scope("prototype")
    public UserPreferenceAgent userPreferenceAgent(@Qualifier("aiModel") ChatModel aiModel) {
        log.info("构建UserPreferenceAgent...");

        return AgenticServices.agentBuilder(UserPreferenceAgent.class)
                .chatModel(aiModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .name("UserPreferenceAgent")
                .tools(userProfileTools, healthGoalTrackerTools, userQueryTools, userPreferenceTools, userHealthGoalTools)
                .build();
    }

    /**
     * L1 营养指导Agent
     */
    @Bean
    @Scope("prototype")
    public NutritionGuideAgent nutritionGuideAgent(@Qualifier("aiModel") ChatModel aiModel) {
        log.info("构建NutritionGuideAgent...");

        return AgenticServices.agentBuilder(NutritionGuideAgent.class)
                .chatModel(aiModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .name("NutritionGuideAgent")
                .tools(nutritionQueryTools, nutritionAnalysisTools, calorieCalculatorTools, dietRecordAnalysisTools, userDietRecordTools)
                .build();
    }

    /**
     * L1 菜品推荐Agent
     */
    @Bean
    @Scope("prototype")
    public DishRecommendationAgent dishRecommendationAgent(@Qualifier("aiModel") ChatModel aiModel) {
        log.info("构建DishRecommendationAgent...");

        return AgenticServices.agentBuilder(DishRecommendationAgent.class)
                .chatModel(aiModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .name("DishRecommendationAgent")
                .tools(recommendationQueryTools, recommendationFilterTools, recommendationRankTools, createMenuQueryTools())
                .build();
    }

    /**
     * L1 商家信息Agent
     */
    @Bean
    @Scope("prototype")
    public MerchantInfoAgent merchantInfoAgent(@Qualifier("aiModel") ChatModel aiModel) {
        log.info("构建MerchantInfoAgent...");

        return AgenticServices.agentBuilder(MerchantInfoAgent.class)
                .chatModel(aiModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .name("MerchantInfoAgent")
                .tools(merchantQueryTools, merchantStatsTools)
                .build();
    }

    /**
     * L1 时间感知Agent
     */
    @Bean
    @Scope("prototype")
    public TimeAwareAgent timeAwareAgent(@Qualifier("aiModel") ChatModel aiModel) {
        log.info("构建TimeAwareAgent...");

        return AgenticServices.agentBuilder(TimeAwareAgent.class)
                .chatModel(aiModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .name("TimeAwareAgent")
                .tools(timeTools, timeRecommendationTools)
                .build();
    }

    /**
     * L1 位置服务Agent
     */
    @Bean
    @Scope("prototype")
    public LocationServiceAgent locationServiceAgent(@Qualifier("aiModel") ChatModel aiModel) {
        log.info("构建LocationServiceAgent...");

        return AgenticServices.agentBuilder(LocationServiceAgent.class)
                .chatModel(aiModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .name("LocationServiceAgent")
                .tools(createLocationTools(), locationRecommendationTools)
                .build();
    }

    /**
     * L1 订单辅助Agent
     */
    @Bean
    @Scope("prototype")
    public OrderHelperAgent orderHelperAgent(@Qualifier("aiModel") ChatModel aiModel) {
        log.info("构建OrderHelperAgent...");

        return AgenticServices.agentBuilder(OrderHelperAgent.class)
                .chatModel(aiModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .name("OrderHelperAgent")
                .tools(createOrderQueryTools(), createOrderCreateTools(), createOrderGuideTools())
                .build();
    }

    /**
     * L1 卡片渲染Agent
     * 负责将SupervisorAgent的结果格式化为卡片格式
     */
    @Bean
    @Scope("prototype")
    public CardRendererAgent cardRendererAgent(@Qualifier("aiModel") ChatModel aiModel) {
        log.info("构建CardRendererAgent（L1 Agent）...");

        return AgenticServices.agentBuilder(CardRendererAgent.class)
                .chatModel(aiModel)
                .name("CardRendererAgent")
                .description("消息格式化专家，将结果渲染为卡片格式")
                .build();
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建非代理的OrderQueryTools实例
     * 必须手动创建，避免Spring AOP代理导致LangChain4j无法扫描@Tool注解
     */
    private OrderQueryTools createOrderQueryTools() {
        OrderQueryTools tools = new OrderQueryTools();
        setField(tools, "orderService", orderService);
        setField(tools, "orderDishService", orderDishService);
        return tools;
    }

    /**
     * 创建非代理的OrderCreateTools实例
     * 必须手动创建，避免Spring AOP代理导致LangChain4j无法扫描@Tool注解
     */
    private OrderCreateTools createOrderCreateTools() {
        OrderCreateTools tools = new OrderCreateTools();
        setField(tools, "orderService", orderService);
        setField(tools, "orderDishService", orderDishService);
        setField(tools, "merchantService", merchantService);
        setField(tools, "dishService", dishService);
        setField(tools, "objectMapper", objectMapper);
        return tools;
    }

    /**
     * 创建非代理的OrderGuideTools实例
     * 必须手动创建，避免Spring AOP代理导致LangChain4j无法扫描@Tool注解
     */
    private OrderGuideTools createOrderGuideTools() {
        OrderGuideTools tools = new OrderGuideTools();
        setField(tools, "merchantService", merchantService);
        setField(tools, "dishService", dishService);
        return tools;
    }

    /**
     * 创建非代理的MenuQueryTools实例
     * 必须手动创建，避免Spring AOP代理导致LangChain4j无法扫描@Tool注解
     */
    private MenuQueryTools createMenuQueryTools() {
        MenuQueryTools tools = new MenuQueryTools();
        setField(tools, "dishService", dishService);
        return tools;
    }

    /**
     * 创建非代理的LocationTools实例
     * 必须手动创建，避免Spring AOP代理导致LangChain4j无法扫描@Tool注解
     */
    private LocationTools createLocationTools() {
        LocationTools tools = new LocationTools();
        setField(tools, "merchantService", merchantService);
        setField(tools, "dishService", dishService);
        return tools;
    }

    /**
     * 反射设置字段值
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("设置字段失败: {} = {}", fieldName, value, e);
        }
    }

    @PreDestroy
    public void cleanup() {
        log.info("LangChain4j资源清理完成");
    }
}
