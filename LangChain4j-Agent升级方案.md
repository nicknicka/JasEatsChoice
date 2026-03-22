# LangChain4j Agent升级方案

> 从"伪Agent"到真正的AI Agent

---

## 📊 当前问题分析

### 当前实现（伪Agent）

```java
@Service
public class NutritionAgent {

    @Resource
    private NutritionTools nutritionTools;

    public String chat(String userMessage, String userId) {
        // ❌ 手动判断调用哪个工具
        String response;

        if (userMessage.contains("营养") || userMessage.contains("成分")) {
            response = nutritionTools.analyzeNutrition("苹果");
        } else if (userMessage.contains("卡路里") || userMessage.contains("热量")) {
            response = nutritionTools.analyzeNutrition("苹果");
        }

        return response;
    }
}
```

**问题**：
- ❌ 需要手动维护关键词规则
- ❌ 无法理解复杂语义
- ❌ Tool函数手动调用
- ❌ 没有真正的Agent能力

---

## ✅ LangChain4j真正的Agent

### 1. 使用AiServices构建Agent

```java
@Service
public class NutritionAgent {

    @Resource
    private ChatLanguageModel chatLanguageModel;

    @Resource
    private ChatMemory chatMemory;

    @Resource
    private NutritionTools nutritionTools;

    /**
     * 使用LangChain4j的AiServices构建真正的Agent
     */
    private NutritionAiAgent agent;

    @PostConstruct
    public void init() {
        // ✅ 构建真正的Agent
        this.agent = AiServices.builder(NutritionAiAgent.class)
            .chatLanguageModel(chatLanguageModel)
            .chatMemory(chatMemory)
            .tools(nutritionTools)  // ✅ 注册工具
            .build();
    }

    public String chat(String userMessage, String userId) {
        // ✅ 直接调用Agent，LLM自动决定调用哪个Tool
        return agent.chat(userMessage);
    }
}

/**
 * Agent接口（LangChain4j会自动实现）
 */
public interface NutritionAiAgent {
    String chat(String userMessage);
}
```

### 2. 核心变化对比

| 维度 | 当前实现（伪Agent） | LangChain4j Agent |
|------|-------------------|------------------|
| **Tool调用** | 手动调用 | ✅ LLM自动决策 |
| **意图理解** | 关键词匹配 | ✅ 语义理解 |
| **对话记忆** | 手动管理List | ✅ ChatMemory自动管理 |
| **多轮对话** | 不支持 | ✅ 支持 |
| **复杂推理** | 不支持 | ✅ 支持 |
| **代码量** | 多（需写大量if-else） | 少（声明式） |

### 3. 真实案例对比

#### 场景：用户问"我早餐吃了苹果和鸡蛋，今天还剩多少卡路里额度？"

**当前实现（伪Agent）**：
```java
// ❌ 无法回答，因为需要：
// 1. 识别多个食物（苹果、鸡蛋）
// 2. 查询各自的营养信息
// 3. 计算总摄入
// 4. 查询用户每日需求
// 5. 计算剩余额度
// 手动逻辑太复杂！
return "抱歉，我暂时无法回答这个问题";
```

**LangChain4j Agent（真Agent）**：
```java
// ✅ LLM自动完成：
// 1. 理解用户意图
// 2. 自动调用 analyzeNutrition("苹果")
// 3. 自动调用 analyzeNutrition("鸡蛋")
// 4. 自动调用 calculateDailyCalories(...)
// 5. 自动计算并回答
String response = agent.chat("我早餐吃了苹果和鸡蛋，今天还剩多少卡路里额度？");

// LLM自动生成回复：
// "根据营养分析：
//  - 苹果：52 kcal
//  - 鸡蛋：155 kcal
//  早餐总计：207 kcal
//
//  您的每日建议摄入量是2000 kcal，
//  今天还剩余 1793 kcal 的额度。
//
//  建议午餐和晚餐控制在900-1000 kcal左右。"
```

---

## 🚀 升级实施方案

### 方案一：渐进式升级（推荐）

#### 第一阶段：保持兼容性

```java
@Service
public class NutritionAgent {

    @Resource
    private ChatLanguageModel chatLanguageModel;

    @Resource
    private NutritionTools nutritionTools;

    private NutritionAiAgent langchain4jAgent;
    private boolean useLegacyMode = false;  // 兼容开关

    @PostConstruct
    public void init() {
        try {
            // 构建LangChain4j Agent
            this.langchain4jAgent = AiServices.builder(NutritionAiAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(nutritionTools)
                .build();
        } catch (Exception e) {
            log.warn("LangChain4j Agent初始化失败，降级到传统模式", e);
            useLegacyMode = true;
        }
    }

    public String chat(String userMessage, String userId) {
        if (useLegacyMode) {
            // 降级：使用传统模式
            return chatLegacy(userMessage, userId);
        }

        // 使用LangChain4j Agent
        return langchain4jAgent.chat(userMessage);
    }

    private String chatLegacy(String userMessage, String userId) {
        // 原有的手动逻辑
        // ...
    }
}

interface NutritionAiAgent {
    String chat(String userMessage);
}
```

#### 第二阶段：完全迁移

```java
@Service
public class NutritionAgent {

    @Resource
    private ChatLanguageModel chatLanguageModel;

    @Resource
    private ChatMemoryProvider chatMemoryProvider;  // 每个用户独立的ChatMemory

    @Resource
    private NutritionTools nutritionTools;

    public String chat(String userMessage, String userId) {
        // ✅ 为每个用户创建独立的Agent和ChatMemory
        NutritionAiAgent agent = AiServices.builder(NutritionAiAgent.class)
            .chatLanguageModel(chatLanguageModel)
            .chatMemory(chatMemoryProvider.get(userId))  // 用户专属记忆
            .tools(nutritionTools)
            .build();

        return agent.chat(userMessage);
    }
}

interface NutritionAiAgent {
    String chat(String userMessage);
}
```

### 方案二：一步到位

```java
@Configuration
public class AgentConfig {

    @Resource
    private ChatLanguageModel chatLanguageModel;

    @Resource
    private NutritionTools nutritionTools;

    @Resource
    private RecommendationTools recommendationTools;

    @Resource
    private OrderTools orderTools;

    @Bean
    public NutritionAiAgent nutritionAgent() {
        return AiServices.builder(NutritionAiAgent.class)
            .chatLanguageModel(chatLanguageModel)
            .tools(nutritionTools)
            .build();
    }

    @Bean
    public RecommendationAiAgent recommendationAgent() {
        return AiServices.builder(RecommendationAiAgent.class)
            .chatLanguageModel(chatLanguageModel)
            .tools(recommendationTools)
            .build();
    }

    @Bean
    public OrderAiAgent orderAgent() {
        return AiServices.builder(OrderAiAgent.class)
            .chatLanguageModel(chatLanguageModel)
            .tools(orderTools)
            .build();
    }
}

// 然后直接在Controller中使用
@RestController
public class AgentController {

    @Resource
    private NutritionAiAgent nutritionAgent;

    @PostMapping("/v1/agent/nutrition/chat")
    public ResponseResult<?> nutritionChat(@RequestBody Map<String, Object> params) {
        String message = (String) params.get("message");
        String response = nutritionAgent.chat(message);  // ✅ 直接调用
        return ResponseResult.success(response);
    }
}
```

---

## 📋 实施清单

### 前置条件

- [ ] 确认LangChain4j版本 >= 0.29.1
- [ ] 确认所有Tool类使用`@Tool`注解
- [ ] 确认ChatLanguageModel配置正确
- [ ] 确认智谱AI API密钥有效

### 实施步骤

1. **创建Agent接口**（每个Agent一个）
   ```java
   public interface NutritionAiAgent {
       String chat(String userMessage);
   }
   ```

2. **使用AiServices构建Agent**
   ```java
   @Bean
   public NutritionAiAgent nutritionAgent() {
       return AiServices.builder(NutritionAiAgent.class)
           .chatLanguageModel(chatLanguageModel)
           .tools(nutritionTools)
           .build();
   }
   ```

3. **更新Agent服务类**
   ```java
   @Service
   public class NutritionAgent {
       @Resource
       private NutritionAiAgent agent;  // 注入LangChain4j Agent

       public String chat(String userMessage, String userId) {
           return agent.chat(userMessage);  // 直接调用
       }
   }
   ```

4. **测试验证**
   - [ ] 测试简单查询（单个Tool调用）
   - [ ] 测试复杂查询（多个Tool调用）
   - [ ] 测试多轮对话
   - [ ] 测试错误处理

---

## 💡 关键差异总结

| 特性 | 伪Agent（当前） | LangChain4j Agent |
|------|---------------|------------------|
| **Tool调用** | 手动`if-else` | LLM自动决策 |
| **语义理解** | 关键词匹配 | 真正理解意图 |
| **多步推理** | 需要编码实现 | LLM自动完成 |
| **对话记忆** | 手动管理List | ChatMemory自动管理 |
| **复杂查询** | 无法处理 | 可以处理 |
| **代码维护** | 高（规则复杂） | 低（声明式） |
| **扩展性** | 差（改代码） | 好（加Tool即可） |

---

## ⚠️ 注意事项

1. **成本考虑**
   - 真Agent每次对话都会调用LLM
   - 建议设置合适的temperature（0.3-0.7）
   - 考虑缓存常见问题

2. **性能优化**
   - 使用ChatMemory限制上下文长度
   - 考虑Tool调用超时设置
   - 实现降级机制

3. **监控指标**
   - Token消耗量
   - Tool调用成功率
   - 平均响应时间
   - 用户满意度

---

## 🎯 推荐方案

**建议采用"渐进式升级"**：

1. **Phase 1**：先在NutritionAgent试点
   - 保留传统模式作为降级
   - 收集性能和用户体验数据

2. **Phase 2**：扩展到其他Agent
   - RecommendationAgent
   - OrderAssistantAgent

3. **Phase 3**：完全迁移
   - 移除传统模式代码
   - 优化Prompt和Tool设计

---

**为什么要升级？**

真正的LangChain4j Agent能提供：
- ✅ 更自然的对话体验
- ✅ 更强的语义理解
- ✅ 更灵活的Tool组合
- ✅ 更少的代码维护

**这不是技术炫技，而是用户体验的质变！**
