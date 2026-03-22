# 佳食宜选 - LangChain4j + 智谱AI后端实现计划指南

## 📋 项目概览

**技术栈**：SpringBoot + LangChain4j + 智谱AI (GLM-4)
**实现周期**：4-6周
**核心功能**：多Agent智能体系统

---

## 🎯 功能需求分析

### 已有功能（基于现有数据库）
✅ 用户管理
✅ 商家管理
✅ 菜品管理
✅ 订单管理
✅ 聊天系统
✅ 评价系统

### 新增AI功能
🤖 营养分析Agent
🤖 智能推荐Agent
🤖 订单助手Agent

---

## 📊 RAG与向量数据库分析

### ❌ 不需要RAG的场景

#### 1. 营养计算
```
需求：查询食物营养成分
方案：结构化数据库查询
原因：
- 营养数据是精确的数值（卡路里、蛋白质等）
- 数据变化频率低
- 需要精确匹配，不需要语义搜索

实现：
food_nutrition表
├─ food_id (主键)
├─ food_name (食品名称)
├─ calories (卡路里/100g)
├─ protein (蛋白质/100g)
├─ fat (脂肪/100g)
└─ carbs (碳水/100g)

通过SQL查询即可，无需向量搜索
```

#### 2. 菜品推荐
```
需求：根据用户偏好推荐菜品
方案：推荐算法 + 结构化数据
原因：
- 推荐基于标签、评分、历史订单
- 数据关系明确
- 可用传统推荐算法（协同过滤）

实现：
dish表 + user_preference表 + order_history表
通过推荐算法计算相似度，无需RAG
```

#### 3. 订单处理
```
需求：创建、查询、修改订单
方案：CRUD操作
原因：
- 标准的数据库操作
- 业务逻辑明确
- 无需AI理解

实现：
直接调用订单Service，无需LLM
```

### ✅ 可能需要RAG的场景

#### 场景1：营养知识问答
```
用户问题：
"糖尿病患者应该怎么安排饮食？"
"痛风患者能吃什么？"
"孕妇饮食有什么禁忌？"

是否需要RAG：⚠️ 可选

方案A（不用RAG）：
- 在Prompt中嵌入基础营养知识
- 依赖GLM-4的内置知识
- 适合：常见问题

方案B（用RAG）：
- 构建营养知识库（专业文档、指南）
- 向量化存储
- 适合：专业、准确度要求高的问题

建议：先用方案A，如果效果不好再考虑RAG
```

#### 场景2：食谱解释
```
用户问题：
"为什么清蒸鱼比油炸鱼健康？"
"糙米和白米有什么区别？"
"发酵食品有什么好处？"

是否需要RAG：❌ 不需要

建议：
- 依赖GLM-4的通用知识
- 通过Function Call获取营养数据
- 让LLM基于数据解释
```

### 📊 决策矩阵

| 功能 | 是否需要RAG | 是否需要向量库 | 推荐方案 |
|------|-----------|--------------|---------|
| 营养计算 | ❌ | ❌ | SQL查询 |
| 菜品推荐 | ❌ | ❌ | 推荐算法 |
| 食谱搜索 | ❌ | ❌ | 数据库查询 |
| 订单处理 | ❌ | ❌ | CRUD |
| 营养建议 | ⚠️ 可选 | ⚠️ 可选 | Prompt + GLM-4知识 |
| 饮食计划 | ❌ | ❌ | 规则引擎 |
| 健康问答 | ⚠️ 可选 | ⚠️ 可选 | RAG（可选） |

### 💡 最终建议

**第一阶段（MVP）**：不用RAG和向量数据库
- 成本低
- 开发快
- GLM-4的内置知识足够应对80%的问题

**第二阶段（优化）**：根据实际效果决定
- 如果用户经常问专业营养知识 → 加入RAG
- 如果需要个性化记忆 → 加入向量数据库

---

## 🗂️ 数据库设计

### 核心表（已有）
```sql
-- 菜品表（已有）
CREATE TABLE dish (
    dish_id VARCHAR(50) PRIMARY KEY,
    merchant_id VARCHAR(50),
    name VARCHAR(100),
    description TEXT,
    price DECIMAL(10,2),
    calories INT,
    protein DECIMAL(5,2),
    fat DECIMAL(5,2),
    carbs DECIMAL(5,2),
    tags VARCHAR(200),
    rating DECIMAL(3,2)
);

-- 订单表（已有）
CREATE TABLE `order` (
    order_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50),
    merchant_id VARCHAR(50),
    total_amount DECIMAL(10,2),
    status VARCHAR(20),
    create_time DATETIME
);

-- 订单明细表（已有）
CREATE TABLE order_item (
    item_id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(50),
    dish_id VARCHAR(50),
    quantity INT,
    price DECIMAL(10,2)
);
```

### 新增表（AI相关）
```sql
-- 营养知识库表（可选，用于RAG）
CREATE TABLE nutrition_knowledge (
    knowledge_id VARCHAR(50) PRIMARY KEY,
    category VARCHAR(50), -- 分类：营养学、疾病饮食、食品安全等
    title VARCHAR(200),
    content TEXT,
    source VARCHAR(200),
    create_time DATETIME
);

-- 用户画像表（用于个性化）
CREATE TABLE user_profile (
    user_id VARCHAR(50) PRIMARY KEY,
    health_goal VARCHAR(50), -- 健康目标：减脂、增肌、保持
    allergies TEXT, -- 过敏源（JSON）
    preferences TEXT, -- 饮食偏好（JSON）
    calorie_target INT, -- 目标卡路里
    height INT, -- 身高
    weight INT, -- 体重
    update_time DATETIME
);

-- 对话历史表（Redis存储，定期归档）
CREATE TABLE conversation_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50),
    conversation_id VARCHAR(50),
    role VARCHAR(20), -- user/assistant
    content TEXT,
    agent_type VARCHAR(50), -- nutrition/recommendation/order
    create_time DATETIME,
    INDEX idx_user_conversation (user_id, conversation_id)
);

-- Agent执行日志表（用于监控和优化）
CREATE TABLE agent_execution_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50),
    agent_type VARCHAR(50),
    input_text TEXT,
    output_text TEXT,
    tools_called TEXT, -- JSON: 调用了哪些工具
    execution_time INT, -- 执行耗时(ms)
    tokens_used INT, -- Token消耗
    create_time DATETIME,
    INDEX idx_user_time (user_id, create_time)
);
```

---

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────┐
│              前端 (UniApp)                       │
│  ┌────────────┐  ┌────────────┐  ┌───────────┐ │
│  │  AI聊天页  │  │  菜品详情  │  │  订单页   │ │
│  └────────────┘  └────────────┘  └───────────┘ │
└────────────────────┬────────────────────────────┘
                     │ HTTP/WebSocket
                     ▼
┌─────────────────────────────────────────────────┐
│         后端 (SpringBoot + LangChain4j)         │
│  ┌──────────────────────────────────────────┐  │
│  │  Controller层 (REST API)                 │  │
│  │  - AIController                          │  │
│  └──────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────┐  │
│  │  Service层 (业务逻辑)                     │  │
│  │  ┌─────────────────────────────────┐     │  │
│  │  │  MultiAgentService              │     │  │
│  │  │  - intelligentRouting()         │     │  │
│  │  │  - coordinateAgents()           │     │  │
│  │  └─────────────────────────────────┘     │  │
│  │  ┌─────────────────────────────────┐     │  │
│  │  │  NutritionAgentService          │     │  │
│  │  │  - analyzeNutrition()           │     │  │
│  │  │  - assessHealth()               │     │  │
│  │  └─────────────────────────────────┘     │  │
│  │  ┌─────────────────────────────────┐     │  │
│  │  │  RecommendationAgentService     │     │  │
│  │  │  - recommendDishes()            │     │  │
│  │  │  - createMealPlan()             │     │  │
│  │  └─────────────────────────────────┘     │  │
│  │  ┌─────────────────────────────────┐     │  │
│  │  │  OrderAgentService              │     │  │
│  │  │  - createOrder()                │     │  │
│  │  │  - queryOrder()                 │     │  │
│  │  └─────────────────────────────────┘     │  │
│  └──────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────┐  │
│  │  Tool层 (工具调用)                        │  │
│  │  - NutritionToolService                  │  │
│  │  - RecipeToolService                     │  │
│  │  - OrderToolService                      │  │
│  └──────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────┐  │
│  │  LangChain4j Agent层                     │  │
│  │  - ChatLanguageModel (智谱GLM-4)         │  │
│  │  - ChatMemory (Redis)                    │  │
│  │  - ToolSpecification                     │  │
│  └──────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        ▼            ▼            ▼
  ┌──────────┐ ┌──────────┐ ┌──────────┐
  │  MySQL   │ │  Redis   │ │ 智谱AI   │
  │  数据库  │ │  缓存    │ │  LLM     │
  └──────────┘ └──────────┘ └──────────┘
```

---

## 📅 实施计划（4-6周）

### 第一周：环境搭建与基础配置

#### 任务清单
- [ ] 申请智谱AI API Key
- [ ] 搭建SpringBoot项目
- [ ] 集成LangChain4j依赖
- [ ] 配置Redis连接
- [ ] 创建数据库表
- [ ] 编写配置类

#### 详细步骤

**步骤1：申请智谱AI账号**
```bash
1. 访问 https://open.bigmodel.cn/
2. 注册账号并完成实名认证
3. 创建API Key
4. 充值（建议先充100元测试）
```

**步骤2：创建项目**
```bash
# 使用Spring Initializr创建项目
https://start.spring.io/

选择依赖：
- Spring Web
- Spring Data JPA
- Spring Data Redis
- Lombok
- Validation

# 添加LangChain4j依赖（手动添加到pom.xml）
```

**步骤3：配置application.yml**
```yaml
spring:
  application:
    name: jiashiyixuan-ai
  datasource:
    url: jdbc:mysql://localhost:3306/jia_shi_yi_xuan
    username: root
    password: 123456
  data:
    redis:
      host: localhost
      port: 6379

zhipu:
  ai:
    api-key: ${ZHIPU_API_KEY}
    model: glm-4-plus
    temperature: 0.7
    max-tokens: 2000

logging:
  level:
    com.jiashiyixuan: DEBUG
    io.github.langchain4j: DEBUG
```

**步骤4：创建配置类**
```java
@Configuration
public class LangChainConfig {
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return ZhipuAiChatModel.builder()
            .apiKey(apiKey)
            .modelName("glm-4-plus")
            .temperature(0.7)
            .build();
    }
}
```

**验收标准**：
- ✅ 项目能正常启动
- ✅ 能连接到MySQL和Redis
- ✅ 能调用智谱AI API（写个测试类验证）

---

### 第二周：营养Agent开发

#### 任务清单
- [ ] 创建营养Agent服务
- [ ] 实现营养计算工具
- [ ] 实现健康评估工具
- [ ] 编写Agent提示词
- [ ] 单元测试

#### 详细步骤

**步骤1：创建营养工具**
```java
@Service
public class NutritionToolService {

    /**
     * 查询营养数据（从数据库）
     */
    public NutritionInfo getNutrition(String foodName, Double amount) {
        // 1. 从dish表查询
        Dish dish = dishRepository.findByName(foodName);

        // 2. 计算营养成分
        return NutritionInfo.builder()
            .foodName(foodName)
            .amount(amount)
            .calories(dish.getCalories() * amount / 100)
            .protein(dish.getProtein() * amount / 100)
            .fat(dish.getFat() * amount / 100)
            .carbs(dish.getCarbs() * amount / 100)
            .build();
    }

    /**
     * 评估饮食健康度
     */
    public HealthAssessment assessHealth(List<NutritionInfo> meals) {
        Double totalCalories = meals.stream()
            .mapToDouble(NutritionInfo::getCalories)
            .sum();

        // 计算健康评分
        Integer score = calculateScore(meals);

        return HealthAssessment.builder()
            .totalCalories(totalCalories)
            .score(score)
            .suggestions(generateSuggestions(score))
            .build();
    }
}
```

**步骤2：创建营养Agent**
```java
@Service
public class NutritionAgentService {

    private final ChatLanguageModel chatModel;
    private final NutritionToolService nutritionToolService;

    /**
     * 营养Agent接口
     */
    public interface NutritionAgent {
        @SystemMessage("""
            你是专业的营养师Agent。

            # 专业能力
            1. 精确计算营养成分
            2. 评估饮食健康度
            3. 提供科学建议

            # 回复风格
            - 专业但不晦涩
            - 数据准确
            - 建议具体可操作
        """)
        String chat(@UserMessage String message);
    }

    /**
     * 创建Agent实例
     */
    public NutritionAgent createAgent() {
        return AiServices.builder(NutritionAgent.class)
            .chatLanguageModel(chatModel)
            .tools(new NutritionTools(nutritionToolService))
            .build();
    }
}
```

**步骤3：测试**
```java
@SpringBootTest
class NutritionAgentTest {

    @Autowired
    private NutritionAgentService nutritionAgentService;

    @Test
    void testNutritionAnalysis() {
        NutritionAgent agent = nutritionAgentService.createAgent();

        String response = agent.chat("鸡胸肉100g有多少卡路里？");

        System.out.println(response);
        // 预期输出：鸡胸肉每100克含有约165卡路里...
    }
}
```

**验收标准**：
- ✅ 能准确查询营养数据
- ✅ 能评估饮食健康度
- ✅ 回复专业且易懂

---

### 第三周：推荐Agent开发

#### 任务清单
- [ ] 创建推荐Agent服务
- [ ] 实现菜品推荐工具
- [ ] 实现食谱搜索工具
- [ ] 实现饮食计划工具
- [ ] 单元测试

#### 详细步骤

**步骤1：创建推荐工具**
```java
@Service
public class RecommendationToolService {

    /**
     * 推荐菜品
     */
    public List<Dish> recommendDishes(RecommendRequest request) {
        // 1. 获取用户画像
        UserProfile profile = getUserProfile(request.getUserId());

        // 2. 查询符合条件的菜品
        List<Dish> candidates = dishRepository.findByTags(
            request.getPreferences(),
            request.getCalorieLimit()
        );

        // 3. 排序（根据评分、偏好匹配度）
        return candidates.stream()
            .sorted(Comparator.comparing(Dish::getRating).reversed())
            .limit(10)
            .collect(Collectors.toList());
    }

    /**
     * 搜索食谱
     */
    public Recipe searchRecipe(String dishName) {
        // 从recipe表查询
        return recipeRepository.findByDishName(dishName);
    }

    /**
     * 制定饮食计划
     */
    public MealPlan createMealPlan(MealPlanRequest request) {
        // 根据目标、天数、预算生成计划
        List<DailyPlan> dailyPlans = new ArrayList<>();

        for (int i = 0; i < request.getDays(); i++) {
            dailyPlans.add(generateDailyPlan(request));
        }

        return MealPlan.builder()
            .dailyPlans(dailyPlans)
            .shoppingList(generateShoppingList(dailyPlans))
            .build();
    }
}
```

**步骤2：创建推荐Agent**
```java
@Service
public class RecommendationAgentService {

    public interface RecommendationAgent {
        @SystemMessage("""
            你是专业的推荐Agent。

            # 专业能力
            1. 根据用户画像推荐菜品
            2. 提供详细食谱
            3. 制定饮食计划

            # 推荐原则
            - 个性化：基于用户历史和偏好
            - 多样性：避免重复推荐
            - 健康优先：营养均衡
        """)
        String chat(@UserMessage String message);
    }
}
```

**验收标准**：
- ✅ 能根据偏好推荐菜品
- ✅ 能提供详细食谱
- ✅ 能制定合理的饮食计划

---

### 第四周：订单Agent开发

#### 任务清单
- [ ] 创建订单Agent服务
- [ ] 实现订单创建工具
- [ ] 实现订单查询工具
- [ ] 实现订单修改工具
- [ ] 单元测试

#### 详细步骤

**步骤1：创建订单工具**
```java
@Service
public class OrderToolService {

    /**
     * 创建订单（调用现有订单服务）
     */
    public Order createOrder(String userId, OrderRequest request) {
        // 调用现有的OrderService
        return orderService.createOrder(userId, request);
    }

    /**
     * 查询订单
     */
    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("订单不存在"));
    }

    /**
     * 修改订单
     */
    public Order modifyOrder(String orderId, OrderModification modification) {
        Order order = getOrder(orderId);

        if (!canModify(order)) {
            throw new RuntimeException("订单已发货，无法修改");
        }

        // 应用修改
        applyModification(order, modification);

        return orderRepository.save(order);
    }
}
```

**步骤2：创建订单Agent**
```java
@Service
public class OrderAgentService {

    public interface OrderAgent {
        @SystemMessage("""
            你是专业的订单管理Agent。

            # 工作流程
            1. 创建订单：确认菜品、地址、联系方式
            2. 查询订单：提供详细状态和进度
            3. 修改订单：仅在未发货时允许

            # 注意事项
            - 确认关键信息后再下单
            - 及时告知订单状态变化
            - 清晰说明退款规则
        """)
        String chat(@UserMessage String message);
    }
}
```

**验收标准**：
- ✅ 能正确创建订单
- ✅ 能查询订单状态
- ✅ 能处理订单修改和取消

---

### 第五周：多Agent协调与集成

#### 任务清单
- [ ] 创建多Agent协调服务
- [ ] 实现智能路由逻辑
- [ ] 实现Agent协作机制
- [ ] 集成测试

#### 详细步骤

**步骤1：创建协调服务**
```java
@Service
public class MultiAgentService {

    private final NutritionAgentService nutritionAgent;
    private final RecommendationAgentService recommendationAgent;
    private final OrderAgentService orderAgent;

    /**
     * 智能路由
     */
    public String intelligentRouting(String message) {
        // 方案1：关键词匹配（简单）
        if (containsKeyword(message, NUTRITION_KEYWORDS)) {
            return "nutrition";
        } else if (containsKeyword(message, RECOMMENDATION_KEYWORDS)) {
            return "recommendation";
        } else if (containsKeyword(message, ORDER_KEYWORDS)) {
            return "order";
        }

        // 方案2：使用LLM判断（更准确）
        return classifyWithLLM(message);
    }

    /**
     * 执行Agent
     */
    public AgentResponse executeAgent(String agentType, String message) {
        switch (agentType) {
            case "nutrition":
                return nutritionAgent.chat(message);
            case "recommendation":
                return recommendationAgent.chat(message);
            case "order":
                return orderAgent.chat(message);
            default:
                throw new IllegalArgumentException("Unknown agent type");
        }
    }

    /**
     * 协调多个Agent（复杂任务）
     */
    public AgentResponse coordinateAgents(String message) {
        // 1. 理解任务
        TaskAnalysis analysis = analyzeTask(message);

        // 2. 按顺序调用Agent
        List<AgentResult> results = new ArrayList<>();

        for (String agentType : analysis.getRequiredAgents()) {
            AgentResult result = executeAgent(agentType, message);
            results.add(result);

            // 将前一个Agent的结果传递给下一个Agent
            message = message + "\n前序结果：" + result.getOutput();
        }

        // 3. 综合结果
        return synthesizeResults(results);
    }
}
```

**步骤2：创建控制器**
```java
@RestController
@RequestMapping("/api/ai/agent")
public class AIController {

    @PostMapping("/chat")
    public ResponseEntity<AgentResponse> chat(
        @RequestBody AgentChatRequest request
    ) {
        AgentResponse response = multiAgentService.routeAndExecute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat/stream")
    public SseEmitter chatStream(@RequestBody AgentChatRequest request) {
        // 实现流式响应
    }
}
```

**验收标准**：
- ✅ 能正确路由到对应Agent
- ✅ 多个Agent能协作完成复杂任务
- ✅ API响应正常

---

### 第六周：优化与部署

#### 任务清单
- [ ] 性能优化
- [ ] 提示词优化
- [ ] 错误处理完善
- [ ] 日志和监控
- [ ] 文档编写
- [ ] 部署上线

#### 详细步骤

**步骤1：性能优化**
```java
// 1. 添加缓存
@Cacheable(value = "nutrition", key = "#foodName")
public NutritionInfo getNutrition(String foodName, Double amount) {
    // ...
}

// 2. 异步处理
@Async
public CompletableFuture<String> chatAsync(String message) {
    // ...
}

// 3. 限制Token消耗
@Component
public class TokenLimiter {
    public boolean checkLimit(String userId) {
        // 检查用户是否超限
    }
}
```

**步骤2：提示词优化**
```java
// 使用few-shot learning
@SystemMessage("""
    你是专业的营养师Agent。

    # 示例对话

    用户：苹果有多少卡路里？
    助手：苹果每100克含有52卡路里，富含维生素C和膳食纤维，
          是理想的健康零食。建议每天1-2个。

    用户：减脂期可以吃香蕉吗？
    助手：可以适量食用。香蕉每100克约89卡路里，富含钾元素。
          建议在运动前后食用，每次不超过1根。

    # 现在请回答用户的问题
""")
```

**步骤3：错误处理**
```java
@ControllerAdvice
public class AIExceptionHandler {

    @ExceptionHandler(AIServiceException.class)
    public ResponseEntity<ErrorResponse> handleAIException(
        AIServiceException ex
    ) {
        log.error("AI服务异常", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.builder()
                .code("AI_SERVICE_ERROR")
                .message("AI服务暂时不可用，请稍后再试")
                .build());
    }
}
```

**步骤4：监控**
```java
@Component
public class AgentMetrics {

    private final MeterRegistry meterRegistry;

    public void recordAgentExecution(
        String agentType,
        long executionTime,
        int tokensUsed
    ) {
        // 记录指标
        meterRegistry.counter("agent.execution",
            "type", agentType).increment();

        meterRegistry.timer("agent.execution.time",
            "type", agentType)
            .record(executionTime, TimeUnit.MILLISECONDS);
    }
}
```

**步骤5：部署**
```bash
# Docker部署
docker build -t jiashiyixuan-ai .
docker run -p 8080:8080 \
  -e ZHIPU_API_KEY=xxx \
  -e REDIS_HOST=xxx \
  jiashiyixuan-ai
```

**验收标准**：
- ✅ 响应时间 < 3秒
- ✅ 错误率 < 1%
- ✅ Token消耗在预算内
- ✅ 监控指标正常

---

## 📊 成本估算

### 智谱AI API费用

| 模型 | 输入价格 | 输出价格 | 平均每次对话 | 月度费用（1万日活）|
|------|---------|---------|------------|----------------|
| GLM-4-Flash | ¥0.005/千tokens | ¥0.01/千tokens | ¥0.05 | ¥500-1000 |
| GLM-4-Plus | ¥0.05/千tokens | ¥0.1/千tokens | ¥0.5 | ¥3000-5000 |

**Token消耗估算**：
- 简单问答：约500 tokens（输入300 + 输出200）
- 复杂任务：约2000 tokens（输入1000 + 输出1000）

**建议**：
- 开发测试：使用GLM-4-Flash（成本低）
- 生产环境：使用GLM-4-Plus（效果好）

### 其他成本

| 项目 | 月度费用 |
|------|---------|
| Redis云服务 | ¥200-500 |
| 服务器 | ¥500-1000 |
| 域名+SSL | ¥100 |
| **总计** | **¥1300-2600**（不含LLM） |

---

## 🎓 最佳实践

### 1. Prompt工程

**DO ✅**：
```java
@SystemMessage("""
    你是专业的营养师Agent。

    # 角色
    你是一名有5年经验的专业营养师，擅长运动营养和健康饮食。

    # 任务
    1. 精确计算营养成分
    2. 评估饮食健康度
    3. 提供科学建议

    # 约束
    - 所有数据必须基于营养数据库
    - 不能编造数据
    - 如果数据不足，明确告知用户

    # 输出格式
    使用友好、专业的语言，避免过于学术化。
""")
```

**DON'T ❌**：
```java
@SystemMessage("你是一个营养师")
```

### 2. 工具设计

**DO ✅**：
```java
@Tool("查询食物的营养成分，输入：食物名称和重量（克）")
public String calculateNutrition(String foodName, Double amount) {
    // 参数明确
    // 描述清晰
}
```

**DON'T ❌**：
```java
@Tool("营养")
public String nutrition(String input) {
    // 参数不明确
    // 描述不清楚
}
```

### 3. 错误处理

**DO ✅**：
```java
try {
    return agent.chat(message);
} catch (AIServiceException e) {
    log.error("AI调用失败", e);
    // 返回友好的错误消息
    return "抱歉，AI服务暂时不可用，请稍后再试。";
}
```

### 4. 记忆管理

**DO ✅**：
```java
// 使用Redis存储对话历史
@Bean
public ChatMemory chatMemory(RedisTemplate<String, Object> redisTemplate) {
    return RedisChatMemory.builder()
        .redisTemplate(redisTemplate)
        .maxMessages(50) // 限制长度
        .ttl(Duration.ofDays(7)) // 7天过期
        .build();
}
```

---

## 📚 参考资料

### LangChain4j文档
- [官方文档](https://docs.langchain4j.dev/)
- [Spring Boot集成](https://docs.langchain4j.dev/tutorials/spring-boot)
- [Agent开发](https://docs.langchain4j.dev/tutorials/agents)

### 智谱AI文档
- [API文档](https://open.bigmodel.cn/dev/api)
- [Function Call](https://open.bigmodel.cn/dev/api#function_call)
- [最佳实践](https://open.bigmodel.cn/dev/howuse)

### 示例项目
- [LangChain4j Examples](https://github.com/langchain4j/langchain4j-examples)

---

## ✅ 检查清单

### 开发前
- [ ] 申请智谱AI账号并获取API Key
- [ ] 准备开发环境（JDK 17+）
- [ ] 安装Redis
- [ ] 准备MySQL数据库

### 开发中
- [ ] 完成环境搭建
- [ ] 完成营养Agent开发
- [ ] 完成推荐Agent开发
- [ ] 完成订单Agent开发
- [ ] 完成多Agent协调
- [ ] 通过所有单元测试

### 上线前
- [ ] 性能测试
- [ ] 压力测试
- [ ] 安全检查
- [ ] 文档完善
- [ ] 监控配置

---

## 🎯 总结

### 关键决策
1. **不使用RAG**：第一阶段不需要，数据都是结构化的
2. **不使用向量数据库**：营养数据精确匹配，不需要语义搜索
3. **使用LangChain4j**：Java生态，与SpringBoot完美集成
4. **使用智谱GLM-4**：性价比高，中文能力强

### 技术栈
```
后端：SpringBoot 3.x + LangChain4j
LLM：智谱AI GLM-4
数据库：MySQL（已有）
缓存：Redis（对话历史）
```

### 实施周期
```
6周完成
- 第1周：环境搭建
- 第2-4周：Agent开发
- 第5周：集成测试
- 第6周：优化部署
```

### 预期效果
```
✅ 准确的营养分析
✅ 个性化的菜品推荐
✅ 智能的订单处理
✅ 流畅的多轮对话
✅ 低延迟（<3秒）
✅ 可控成本（<5000元/月）
```

---

祝开发顺利！🚀
