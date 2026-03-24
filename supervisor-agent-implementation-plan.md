# Supervisor Agent 架构实施计划

> 基于LangChain4j Agentic System的Supervisor模式改造
>
> 文档日期：2026-03-24
> 目标：将当前的手动路由架构改造为LLM自主决策的Supervisor架构

---

## 📐 架构对比

### 当前架构：手动路由

```
用户消息
    ↓
IntelligentAdvisorAgent
    ↓
IntentClassifierService (AI分类)
    ↓
switch-case手动路由
    ↓
├─ NutritionAiAgent
├─ RecommendationAiAgent
└─ OrderAiAgent
```

**问题：**
- ❌ 意图分类和Agent执行分离
- ❌ 无法处理多步骤任务
- ❌ Agent之间无法协作
- ❌ 路由逻辑固定，不够灵活

### 目标架构：Supervisor Agent

```
用户消息
    ↓
SupervisorAgent (LLM自主决策)
    ↓
动态规划和调用子Agent
    ↓
├─ NutritionAiAgent
├─ RecommendationAiAgent
├─ OrderAiAgent
└─ 可多步调用，Agent间协作
    ↓
汇总结果
```

**优势：**
- ✅ LLM自主决策调用哪个Agent
- ✅ 支持多步骤任务规划
- ✅ Agent之间可以协作
- ✅ 自动处理复杂场景
- ✅ 可解释的执行过程

---

## 🔧 核心改造点

### 1. Agent接口改造（增加能力描述）

**当前问题：** Agent接口缺少对Supervisor的能力说明

**改造方案：**

```java
/**
 * 营养分析AI Agent
 *
 * 重要：@Agent注解的description会被Supervisor读取
 * 必须清晰描述此Agent的能力、输入、输出
 */
public interface NutritionAiAgent {

    @SystemMessage("""
        你是"佳食宜选"的专业营养师助手。

        # 专业身份
        你拥有扎实的营养学知识，能够：
        1. 精确计算食物营养成分（卡路里、蛋白质、脂肪等）
        2. 评估饮食健康度和营养均衡性
        3. 提供科学、实用的营养建议

        # 用户识别
        用户消息格式：[当前用户ID: {userId}]\n\n{实际消息}
        调用工具时必须使用正确的userId

        # 工作原则
        - 数据必须准确：基于真实的营养数据，不编造
        - 建议必须科学：基于营养学原理
        - 回答简洁明了：专业但易懂
    """)
    @Agent(
        value = """
            营养分析专家，能够：

            **核心能力：**
            1. 分析食物营养成分（卡路里、蛋白质、脂肪、碳水）
            2. 计算每日热量需求（基于身高、体重、年龄、性别、活动水平）
            3. 评估饮食健康度
            4. 提供个性化营养建议

            **输入：**
            - foodName: 食物名称（如"苹果"）
            - userId: 用户ID（从消息中提取）

            **输出：**
            - 营养成分数据
            - 健康评估
            - 改进建议

            **何时调用：**
            - 用户询问食物营养、卡路里、热量
            - 用户想要健康饮食建议
            - 用户需要营养成分分析
            """,
        outputKey = "nutritionResult"
    )
    String chat(@UserMessage String userMessage);
}

/**
 * 智能推荐AI Agent
 */
public interface RecommendationAiAgent {

    @SystemMessage("""
        你是"佳食宜选"的智能美食推荐专家。

        # 用户识别
        用户消息格式：[当前用户ID: {userId}]\n\n{实际消息}

        # 推荐原则
        1. 个性化：基于用户历史偏好
        2. 多维度：口味、营养、价格、评分
        3. 透明化：说明推荐理由
        4. 多样性：避免重复推荐
    """)
    @Agent(
        value = """
            美食推荐专家，能够：

            **核心能力：**
            1. 个性化菜品推荐（基于用户偏好、历史）
            2. 菜品搜索和筛选
            3. 热门菜品推荐
            4. 健康饮食推荐
            5. 时令/季节性推荐

            **输入：**
            - userRequest: 用户需求描述
            - userId: 用户ID

            **输出：**
            - 推荐菜品列表
            - 推荐理由
            - 价格、评分、营养信息

            **何时调用：**
            - 用户想要菜品推荐
            - 用户询问"吃什么"、"推荐什么"
            - 用户搜索特定类型的菜
            - 用户想发现新菜品
            """,
        outputKey = "recommendationResult"
    )
    String chat(@UserMessage String userMessage);
}

/**
 * 订单助手AI Agent
 */
public interface OrderAiAgent {

    @SystemMessage("""
        你是"佳食宜选"的智能订单助手。

        # 用户识别
        用户消息格式：[当前用户ID: {userId}]\n\n{实际消息}

        # 核心职责
        1. 理解用户下单需求
        2. 智能填写订单信息
        3. 跟踪订单状态
        4. 处理订单问题
    """)
    @Agent(
        value = """
            订单服务专家，能够：

            **核心能力：**
            1. 创建订单（智能填充信息）
            2. 查询订单状态
            3. 取消/修改订单
            4. 推荐最优优惠
            5. 跟踪配送进度
            6. 处理售后问题

            **输入：**
            - userRequest: 用户需求
            - userId: 用户ID

            **输出：**
            - 订单操作结果
            - 订单详情
            - 配送信息
            - 优惠建议

            **何时调用：**
            - 用户想要下单、订餐
            - 用户查询订单状态
            - 用户取消/修改订单
            - 用户询问配送、优惠
            """,
        outputKey = "orderResult"
    )
    String chat(@UserMessage String userMessage);
}
```

**关键要点：**
- ✅ `@Agent`的`value`必须详细描述能力
- ✅ 说明"何时调用"帮助Supervisor决策
- ✅ 列出输入输出格式
- ✅ 突出核心能力

---

### 2. 构建Supervisor Agent

**核心配置：**

```java
/**
 * Supervisor Agent配置
 */
@Configuration
public class SupervisorAgentConfig {

    @Resource
    private ZhipuAIConfig zhipuAIConfig;

    @Resource
    private NutritionTools nutritionTools;

    @Resource
    private RecommendationTools recommendationTools;

    @Resource
    private OrderTools orderTools;

    @Resource
    private UserTools userTools;

    /**
     * Supervisor专用的强大模型
     * 建议使用GPT-4或智谱GLM-4等能力强的模型
     */
    @Bean("supervisorModel")
    public ChatLanguageModel supervisorModel() {
        return ZhipuAiChatModel.builder()
                .apiKey(zhipuAIConfig.getApiKey())
                .model("glm-4-plus")  // 使用更强的模型
                .temperature(0.3)     // 降低温度，更确定的规划
                .maxRetries(2)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    /**
     * 子Agent使用的模型
     */
    @Bean("agentModel")
    public ChatLanguageModel agentModel() {
        return ZhipuAiChatModel.builder()
                .apiKey(zhipuAIConfig.getApiKey())
                .model("glm-4-flash")  // 使用快速模型
                .temperature(0.7)
                .maxRetries(2)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    /**
     * 构建Supervisor Agent
     */
    @Bean
    public SupervisorAgent intelligentSupervisor(
        @Qualifier("supervisorModel") ChatLanguageModel supervisorModel,
        @Qualifier("agentModel") ChatLanguageModel agentModel
    ) {
        log.info("🤖 构建Supervisor Agent...");

        // 1. 构建所有子Agent
        var nutritionAgent = AgenticServices
            .agentBuilder(NutritionAiAgent.class)
            .chatModel(agentModel)
            .tools(nutritionTools, userTools)
            .chatMemoryProvider(memoryId ->
                MessageWindowChatMemory.withMaxMessages(20)
            )
            .build();

        var recommendationAgent = AgenticServices
            .agentBuilder(RecommendationAiAgent.class)
            .chatModel(agentModel)
            .tools(recommendationTools, nutritionTools, userTools)
            .chatMemoryProvider(memoryId ->
                MessageWindowChatMemory.withMaxMessages(20)
            )
            .build();

        var orderAgent = AgenticServices
            .agentBuilder(OrderAiAgent.class)
            .chatModel(agentModel)
            .tools(orderTools, recommendationTools, userTools)
            .chatMemoryProvider(memoryId ->
                MessageWindowChatMemory.withMaxMessages(20)
            )
            .build();

        // 2. 配置Supervisor
        var supervisor = AgenticServices
            .supervisorBuilder()
            .chatModel(supervisorModel)
            .subAgents(nutritionAgent, recommendationAgent, orderAgent)
            .responseStrategy(SupervisorResponseStrategy.LAST)
            .contextGenerationStrategy(
                SupervisorContextStrategy.CHAT_MEMORY_AND_SUMMARIZATION
            )
            .supervisorContext(buildSupervisorContext())
            .errorHandler(this::handleSupervisorError)
            .listener(new AgentMonitor())
            .build();

        log.info("✅ Supervisor Agent构建完成");
        return supervisor;
    }

    /**
     * 构建Supervisor的系统提示词
     * 这是Supervisor决策的核心
     */
    private String buildSupervisorContext() {
        return """
            你是"佳食宜选"智能助手的核心协调器（Supervisor）。

            # 你的角色
            你不是直接回答用户问题，而是作为指挥官，协调专家团队完成任务。
            你需要理解用户需求，制定执行计划，调用合适的专家，汇总结果。

            # 你的专家团队

            ## 1. 营养分析专家 (NutritionAiAgent)
            **能力：** 分析食物营养成分、计算热量、评估饮食健康度
            **调用场景：** 用户询问营养、卡路里、健康饮食
            **示例问题：**
            - "苹果有多少卡路里？"
            - "这道菜营养如何？"
            - "我每天应该摄入多少热量？"

            ## 2. 美食推荐专家 (RecommendationAiAgent)
            **能力：** 个性化菜品推荐、搜索菜品、发现热门美食
            **调用场景：** 用户想要推荐、搜索、发现新菜品
            **示例问题：**
            - "今天推荐什么菜？"
            - "有什么低卡路里的菜？"
            - "我想吃辣的"

            ## 3. 订单服务专家 (OrderAiAgent)
            **能力：** 创建订单、查询状态、处理售后
            **调用场景：** 用户要下单、查订单、修改订单
            **示例问题：**
            - "我要下单"
            - "查询我的订单"
            - "取消订单123"

            # 工作流程

            ## 步骤1：理解用户需求
            - 仔细阅读用户的问题
            - 识别用户的核心诉求
            - 判断是否需要多个专家协作

            ## 步骤2：制定执行计划
            - 如果问题简单，直接调用一个专家
            - 如果问题复杂，制定多步骤计划
            - 说明每一步的目的和预期结果

            ## 步骤3：执行计划
            - 按顺序调用专家
            - 将前一个专家的结果传递给下一个
            - 监控执行过程

            ## 步骤4：汇总结果
            - 整合所有专家的意见
            - 给用户一个完整、清晰的答复
            - 确保答复满足用户需求

            # 典型场景示例

            ## 场景1：简单单专家任务
            用户："苹果有多少卡路里？"
            计划：
            1. 调用NutritionAiAgent分析苹果营养

            ## 场景2：多专家协作任务
            用户："我想吃健康的午餐，大概200卡路里"
            计划：
            1. 调用NutritionAiAgent了解200卡路里是什么概念
            2. 调用RecommendationAiAgent推荐符合200卡路里的健康菜品
            3. 汇总营养分析和推荐结果

            ## 场景3：复杂订单任务
            用户："我要一份营养均衡的午餐，帮我下单"
            计划：
            1. 调用NutritionAiAgent了解营养均衡的标准
            2. 调用RecommendationAiAgent推荐营养均衡的菜品
            3. 调用OrderAiAgent创建订单
            4. 汇总推荐和订单信息

            # 重要原则

            1. **用户至上：** 一切以用户需求为核心
            2. **高效执行：** 避免不必要的调用
            3. **准确理解：** 不确定时可以询问用户
            4. **完整答复：** 确保用户得到满意的答案
            5. **透明沟通：** 让用户知道你在做什么

            # 用户识别（重要）
            所有用户消息格式：[当前用户ID: {userId}]\n\n{实际消息}
            子Agent需要userId来获取用户相关信息

            # 何时完成任务
            当满足以下条件之一时，调用done工具结束：
            1. 已完全回答用户问题
            2. 已完成用户请求的操作
            3. 需要用户提供更多信息才能继续
            """;
    }

    /**
     * Supervisor错误处理
     */
    private ErrorRecoveryResult handleSupervisorError(ErrorContext context) {
        log.error("Supervisor执行失败: {}",
            context.agentName(),
            context.exception()
        );

        // 如果是Supervisor规划失败，返回友好提示
        if ("supervisor".equals(context.agentName())) {
            return ErrorRecoveryResult.result(
                "抱歉，我遇到了一些技术问题。请您重新描述一下需求，我会尽力帮助您。"
            );
        }

        // 子Agent失败，让Supervisor重试或调整策略
        return ErrorRecoveryResult.retry();
    }
}
```

---

### 3. 改造服务层（简化逻辑）

**当前问题：** `IntelligentAdvisorAgent`包含复杂的路由逻辑

**改造方案：** 大幅简化，只负责调用Supervisor

```java
/**
 * 智能顾问Agent服务（Supervisor版本）
 *
 * 职责简化为：接收用户请求，调用Supervisor，返回结果
 *
 * @author Claude
 * @since 2026-03-24 v3.0
 */
@Service
public class IntelligentAdvisorAgent {

    private static final Logger log = LoggerFactory.getLogger(IntelligentAdvisorAgent.class);

    @Resource
    private SupervisorAgent intelligentSupervisor;

    /**
     * 处理用户消息（主入口）
     */
    public String chat(String userMessage) {
        return chat(userMessage, "anonymous");
    }

    /**
     * 处理用户消息（带用户ID）
     */
    public String chat(String userMessage, String userId) {
        log.info("🤖 Supervisor收到消息 [用户:{}]：{}", userId, userMessage);

        try {
            // 1. 构建带用户ID的消息
            String messageWithUserId = String.format(
                "[当前用户ID: %s]\n\n%s",
                userId,
                userMessage
            );

            // 2. 调用Supervisor（让它自主决策）
            Map<String, Object> input = Map.of("request", messageWithUserId);
            String response = (String) intelligentSupervisor.invoke(input);

            log.info("✅ Supervisor回复 [用户:{}]：{}", userId, response);
            return response;

        } catch (Exception e) {
            log.error("❌ Supervisor处理失败 [用户:{}]", userId, e);
            return getFallbackResponse(userMessage);
        }
    }

    /**
     * 降级响应
     */
    private String getFallbackResponse(String userMessage) {
        return """
            抱歉，智能服务暂时不可用。

            请稍后再试，或者尝试：
            - 重新描述您的需求
            - 使用更简洁的表达
            - 联系客服获取帮助

            给您带来不便，敬请谅解！
            """;
    }

    /**
     * 清除用户对话记忆
     */
    public void clearMemory(String userId) {
        // Supervisor的内存管理
        log.info("清除用户 {} 的对话记忆", userId);
        // TODO: 实现AgenticScope清理
    }

    /**
     * 获取对话历史
     */
    public List<String> getChatHistory(String userId) {
        // TODO: 从AgenticScope获取历史
        return List.of();
    }
}
```

**对比：**
- ❌ 删除：意图分类逻辑
- ❌ 删除：switch-case路由
- ❌ 删除：手动Agent调用
- ✅ 简化：只负责Supervisor调用

---

### 4. Controller层改造（可选优化）

```java
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    @Resource
    private IntelligentAdvisorAgent intelligentAdvisorAgent;

    /**
     * 聊天接口
     */
    @PostMapping("/chat")
    public Result<String> chat(
        @RequestBody ChatRequest request,
        @RequestHeader(value = "X-User-Id", defaultValue = "anonymous") String userId
    ) {
        String response = intelligentAdvisorAgent.chat(
            request.getMessage(),
            userId
        );
        return Result.success(response);
    }

    /**
     * 流式聊天（推荐用于Supervisor）
     *
     * 优势：用户可以看到Supervisor的思考和执行过程
     */
    @GetMapping("/chat/stream")
    public Flux<String> chatStream(
        @RequestParam String message,
        @RequestParam(defaultValue = "anonymous") String userId
    ) {
        return Flux.create(sink -> {
            try {
                // TODO: 实现流式响应
                // 需要Supervisor支持流式输出
                sink.next("正在思考...");
                sink.next("调用营养分析专家...");
                sink.next("调用推荐专家...");
                sink.next("最终建议...");
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }
}
```

---

### 5. 工具函数增强（支持Supervisor）

**关键改进：** 工具函数需要更清晰的描述

```java
@Service
public class NutritionTools {

    /**
     * 分析食物营养成分
     *
     * 重要：工具描述会被Supervisor读取
     */
    @Tool(
        """
        分析食物的营养成分，包括卡路里、蛋白质、脂肪、碳水化合物等。

        **何时使用：**
        - 用户询问某种食物的营养成分
        - 用户想知道食物的卡路里
        - 用户需要营养数据做决策

        **参数说明：**
        - foodName: 食物名称，如"苹果"、"鸡蛋"

        **返回信息：**
        - 卡路里（kcal）
        - 蛋白质（g）
        - 脂肪（g）
        - 碳水化合物（g）
        - 营养评级
        """
    )
    public NutritionInfo analyzeNutrition(
        @P("食物名称，如'苹果'、'鸡蛋'") String foodName
    ) {
        // 实现不变
    }

    /**
     * 计算每日建议卡路里摄入
     */
    @Tool(
        """
        计算每日建议卡路里摄入量，基于Mifflin-St Jeor公式。

        **何时使用：**
        - 用户想知道每天应该吃多少热量
        - 用户制定饮食计划
        - 用户减肥/增肌需求

        **参数说明：**
        - weight: 体重（公斤）
        - height: 身高（厘米）
        - age: 年龄
        - gender: 性别（男/女）
        - activityLevel: 活动水平（久坐/轻度/中度/重度）

        **返回信息：**
        - 基础代谢率(BMR)
        - 每日总消耗(TDEE)
        - 建议摄入范围
        """
    )
    public String calculateDailyCalories(
        @P("体重（公斤）") Double weight,
        @P("身高（厘米）") Double height,
        @P("年龄") Integer age,
        @P("性别：男/女") String gender,
        @P("活动水平：久坐/轻度/中度/重度") String activityLevel
    ) {
        // 实现不变
    }
}
```

---

## 📋 Supervisor架构专项改进计划

### 阶段1：基础准备（1-2天）

**目标：** 搭建Supervisor基础框架

- [ ] 1.1 更新所有Agent接口的`@Agent`注解
  - 添加详细的能力描述
  - 说明"何时调用"
  - 列出输入输出格式

- [ ] 1.2 增强所有工具函数的`@Tool`描述
  - 添加使用场景说明
  - 详细描述参数含义
  - 说明返回信息

- [ ] 1.3 添加依赖（如需要）
  ```xml
  <dependency>
      <groupId>dev.langchain4j</groupId>
      <artifactId>langchain4j-agentic</artifactId>
      <version>0.36.2</version>
  </dependency>
  ```

### 阶段2：Supervisor实现（3-5天）

**目标：** 构建并测试Supervisor

- [ ] 2.1 配置双模型
  - supervisorModel: 使用强大模型（GLM-4-Plus）
  - agentModel: 使用快速模型（GLM-4-Flash）

- [ ] 2.2 实现Supervisor Agent
  - 编写Supervisor系统提示词
  - 配置子Agent列表
  - 设置错误处理

- [ ] 2.3 简化IntelligentAdvisorAgent
  - 移除意图分类逻辑
  - 移除手动路由
  - 只负责调用Supervisor

- [ ] 2.4 单元测试
  - 测试简单单专家场景
  - 测试多专家协作场景
  - 测试错误处理

### 阶段3：监控和优化（2-3天）

**目标：** 确保Supervisor稳定可靠

- [ ] 3.1 添加AgentMonitor
  - 监控Supervisor决策过程
  - 记录子Agent调用链路
  - 生成执行报告

- [ ] 3.2 优化Supervisor提示词
  - 根据实际运行情况调整
  - 添加更多示例
  - 优化决策逻辑

- [ ] 3.3 性能优化
  - 调整模型参数
  - 优化调用链路
  - 添加缓存机制

### 阶段4：高级特性（可选，1-2周）

**目标：** 提升用户体验

- [ ] 4.1 实现流式响应
  - 展示Supervisor思考过程
  - 实时显示执行进度
  - 增强用户体验

- [ ] 4.2 添加执行可视化
  - 生成执行流程图
  - 展示Agent协作过程
  - 提供调试界面

- [ ] 4.3 实现AgenticScope持久化
  - 保存对话历史
  - 支持跨会话协作
  - 提供历史查询

---

## ⚠️ 关键注意事项

### 1. 模型选择

**Supervisor模型要求：**
- ✅ 强大的推理能力
- ✅ 支持长上下文
- ✅ 稳定的输出格式
- ❌ 不要用小模型

**推荐配置：**
```java
// Supervisor: 使用GPT-4或GLM-4-Plus
supervisorModel = "glm-4-plus"  // 或 "gpt-4-turbo"

// 子Agent: 使用快速模型
agentModel = "glm-4-flash"  // 或 "gpt-3.5-turbo"
```

### 2. 提示词工程

**Supervisor提示词关键要素：**
1. 清晰的角色定位
2. 详细的专家能力说明
3. 典型场景示例
4. 明确的工作流程
5. 完成任务的判断标准

**常见问题：**
- ❌ 描述不够详细 → Supervisor不知道调用谁
- ❌ 缺少示例 → 规划能力弱
- ❌ 场景不全面 → 边缘case处理差

### 3. 成本控制

**Supervisor调用成本：**
```
单次调用成本 = Supervisor调用 + N个子Agent调用

例如：
- 用户问题："推荐健康的午餐"
- Supervisor: 调用1次（GLM-4-Plus）
- NutritionAgent: 调用1次（GLM-4-Flash）
- RecommendationAgent: 调用1次（GLM-4-Flash）
```

**优化建议：**
- 为简单场景提供快捷入口
- 缓存常见问题的答案
- 设置最大调用步数

### 4. 降级策略

**Supervisor失败时的备选方案：**

```java
public String chat(String userMessage, String userId) {
    try {
        // 优先使用Supervisor
        return intelligentSupervisor.invoke(Map.of("request", userMessage));
    } catch (Exception e) {
        log.warn("Supervisor失败，降级到传统模式", e);

        // 降级：使用传统的意图分类 + 手动路由
        String intent = intentClassifierService.classifyIntent(userMessage);
        return fallbackRouter(intent, userMessage, userId);
    }
}

private String fallbackRouter(String intent, String message, String userId) {
    // 保留原来的switch-case逻辑作为降级方案
    return switch (intent) {
        case "NUTRITION" -> nutritionAgent.chat(message, userId);
        case "RECOMMENDATION" -> recommendationAgent.chat(message, userId);
        case "ORDER" -> orderAgent.chat(message, userId);
        default -> getWelcomeMessage();
    };
}
```

---

## 📊 预期效果对比

### 当前架构

| 指标 | 数值 | 说明 |
|-----|-----|-----|
| 单次调用 | 2次LLM | 1次意图分类 + 1次Agent |
| 支持多步骤 | ❌ | 需要用户多次交互 |
| Agent协作 | ❌ | 各Agent独立工作 |
| 灵活性 | 低 | 固定路由规则 |
| 可解释性 | 低 | 无法看到决策过程 |

### Supervisor架构

| 指标 | 数值 | 说明 |
|-----|-----|-----|
| 单次调用 | 2-4次LLM | 1次Supervisor + N个子Agent |
| 支持多步骤 | ✅ | 自动规划多步骤任务 |
| Agent协作 | ✅ | Agent间可以传递数据 |
| 灵活性 | 高 | LLM自主决策 |
| 可解释性 | 高 | 可看到完整执行链路 |

---

## 🎯 成功标准

### 功能指标
- ✅ 支持单专家场景（替代当前功能）
- ✅ 支持多专家协作（新增能力）
- ✅ 降级方案可用（稳定性保障）
- ✅ 响应时间 < 10秒（用户体验）

### 质量指标
- ✅ Supervisor决策准确率 > 90%
- ✅ 复杂任务完成率 > 85%
- ✅ 用户满意度 > 4.5/5

### 技术指标
- ✅ 系统稳定性 > 99.5%
- ✅ 监控覆盖率 100%
- ✅ 错误处理完善

---

## 📚 参考资源

### 官方文档
- [LangChain4j Supervisor Agent](https://github.com/langchain4j/langchain4j/blob/main/docs/docs/tutorials/agents.md#pure-agentic-ai)
- [Agentic Patterns](https://github.com/langchain4j/langchain4j/blob/main/docs/docs/tutorials/agents.md#custom-agentic-patterns)

### 设计参考
- [Anthropic: Building effective agents](https://docs.anthropic.com/claude/docs/building-effective-agents)
- [OpenAI: Prompt engineering for agents](https://platform.openai.com/docs/guides/prompt-engineering)

---

## 🚀 下一步行动

1. **评审本计划**：确认Supervisor架构是否符合项目需求
2. **准备测试环境**：搭建模型调用和测试框架
3. **开始阶段1**：更新Agent接口和工具描述
4. **持续迭代优化**：根据测试结果调整

**预计总工时：** 1-2周
**风险等级：** 中等（需要模型能力和提示词调优）
**预期收益：** 大幅提升系统的智能性和用户体验

---

**文档维护：** 根据实施进度持续更新
**最后更新：** 2026-03-24
**作者：** Claude Code Analysis
**状态：** 待评审
