# LangChain4j Agentic System 改进建议

> 基于官方文档对比分析当前实现，提供系统性改进方案
>
> 文档日期：2026-03-24
> 参考文档：https://github.com/langchain4j/langchain4j/blob/main/docs/docs/tutorials/agents.md

---

## 📊 当前实现分析

### ✅ 做得好的地方

1. **工具函数设计良好**
   - 使用`@Tool`注解清晰定义工具
   - 参数使用`@P`注解提供描述
   - 有完善的错误处理和日志

2. **AI驱动的意图分类**
   - `IntentClassifierService`使用LLM进行智能路由
   - 有缓存机制和降级方案

3. **清晰的Agent接口**
   - 系统提示词结构化、详细
   - 职责划分明确

### ❌ 存在的问题

1. **使用传统AiServices，而非Agentic模块**
2. **手动路由，缺少Conditional Workflow**
3. **缺少错误处理机制（errorHandler）**
4. **缺少监控和可观察性（AgentMonitor）**
5. **ChatMemory配置不灵活**
6. **缺少AgenticScope数据共享**
7. **未考虑Supervisor Agent架构**

---

## 🎯 核心改进点

### 1. 迁移到 Agentic 模块（最关键）

**当前做法：**
```java
// LangChain4jConfig.java
@Bean
public NutritionAiAgent nutritionAiAgent(ChatLanguageModel model) {
    return AiServices.builder(NutritionAiAgent.class)
        .chatLanguageModel(chatLanguageModel)
        .chatMemory(chatMemory)
        .tools(nutritionTools, nutritionRecordTools, userTools)
        .build();
}
```

**改进方案：使用 AgenticServices**

```java
// 1. 在Agent接口添加@Agent注解
public interface NutritionAiAgent {
    @SystemMessage("...")
    @Agent(
        value = "分析食物营养成分和提供营养建议",
        outputKey = "nutritionAdvice"
    )
    String chat(@UserMessage String userMessage);
}

// 2. 使用AgenticServices构建
@Bean
public NutritionAiAgent nutritionAiAgent(ChatLanguageModel model) {
    return AgenticServices
        .agentBuilder(NutritionAiAgent.class)
        .chatModel(model)
        .outputKey("nutritionAdvice")
        .tools(nutritionTools, userTools)
        .build();
}
```

**优势：**
- 符合官方最新实践
- 支持AgenticScope数据共享
- 更好的可组合性
- 支持workflow模式

---

### 2. 手动路由 → Conditional Workflow

**当前做法：IntelligentAdvisorAgent使用switch语句**
```java
// IntelligentAdvisorAgent.java
String intent = intentClassifierService.classifyIntent(userMessage);
switch (intent) {
    case "NUTRITION":
        response = nutritionAgent.chat(userMessage, userId);
        break;
    case "RECOMMENDATION":
        response = recommendationAgent.chat(userMessage, userId);
        break;
    // ...
}
```

**改进方案：使用Conditional Workflow**

```java
// 1. 定义意图路由Agent
public interface IntentRouterAgent {
    @UserMessage("""
        分析用户消息并分类为以下意图之一:
        - NUTRITION: 营养咨询
        - RECOMMENDATION: 美食推荐
        - ORDER: 订餐服务
        - GREETING: 问候
        - GENERAL: 一般咨询

        只返回意图类型代码。
        用户消息: {{request}}
    """)
    @Agent("路由用户请求到合适的专家", outputKey = "intent")
    String classify(@V("request") String request);
}

// 2. 使用Conditional Workflow
@Bean
public UntypedAgent intelligentAdvisor(
    ChatLanguageModel model,
    NutritionAiAgent nutritionAgent,
    RecommendationAiAgent recommendationAgent,
    OrderAiAgent orderAgent
) {
    // 构建子agent
    var nutrition = AgenticServices.agentBuilder(NutritionAiAgent.class)
        .chatModel(model).build();
    var recommendation = AgenticServices.agentBuilder(RecommendationAiAgent.class)
        .chatModel(model).build();
    var order = AgenticServices.agentBuilder(OrderAiAgent.class)
        .chatModel(model).build();

    // 构建路由agent
    var router = AgenticServices.agentBuilder(IntentRouterAgent.class)
        .chatModel(model).build();

    // 使用Sequential + Conditional组合
    return AgenticServices
        .sequenceBuilder()
        .subAgents(router)  // 先分类意图
        .subAgents(
            AgenticServices.conditionalBuilder()
                .subAgents(
                    scope -> "NUTRITION".equals(scope.readState("intent", "")),
                    nutrition
                )
                .subAgents(
                    scope -> "RECOMMENDATION".equals(scope.readState("intent", "")),
                    recommendation
                )
                .subAgents(
                    scope -> "ORDER".equals(scope.readState("intent", "")),
                    order
                )
                .build()
        )
        .outputKey("response")
        .build();
}
```

**优势：**
- 声明式配置，代码更简洁
- Agent之间可以共享AgenticScope数据
- 支持更复杂的workflow组合

---

### 3. 添加错误处理机制

**当前问题：** 缺少统一的错误处理

**改进方案：使用errorHandler**

```java
@Bean
public NutritionAiAgent nutritionAiAgent(ChatLanguageModel model) {
    return AgenticServices
        .agentBuilder(NutritionAiAgent.class)
        .chatModel(model)
        .errorHandler(errorContext -> {
            log.error("Agent执行失败: {}",
                errorContext.agentName(),
                errorContext.exception()
            );

            // 根据错误类型决定处理策略
            var exception = errorContext.exception();

            if (exception instanceof ToolExecutionException) {
                // 工具执行失败：重试
                return ErrorRecoveryResult.retry();
            }

            if (exception instanceof MissingArgumentException) {
                // 缺少参数：提供默认值并重试
                MissingArgumentException ex = (MissingArgumentException) exception;
                errorContext.agenticScope()
                    .writeState(ex.argumentName(), getDefaultArgument(ex.argumentName()));
                return ErrorRecoveryResult.retry();
            }

            if (exception instanceof RateLimitException) {
                // 限流：返回友好提示
                return ErrorRecoveryResult.result(
                    "请求过于频繁，请稍后再试。"
                );
            }

            // 其他异常：抛出
            return ErrorRecoveryResult.throwException();
        })
        .build();
}

private String getDefaultArgument(String argumentName) {
    // 根据参数名返回默认值
    return switch (argumentName) {
        case "userId" -> "anonymous";
        case "foodName" -> "未知食物";
        default -> "";
    };
}
```

**支持的错误恢复策略：**
- `ErrorRecoveryResult.throwException()` - 抛出异常（默认）
- `ErrorRecoveryResult.retry()` - 重试执行
- `ErrorRecoveryResult.result(Object)` - 返回默认结果

---

### 4. 添加监控和可观察性

**改进方案：使用AgentMonitor和AgentListener**

```java
@Bean
public NutritionAiAgent nutritionAiAgent(ChatLanguageModel model) {
    // 1. 创建监控器
    AgentMonitor monitor = new AgentMonitor();

    // 2. 创建自定义监听器
    AgentListener loggingListener = new AgentListener() {
        @Override
        public void beforeAgentInvocation(AgentRequest request) {
            log.info("🚀 Agent调用开始: {}, 输入: {}",
                request.agentName(),
                request.inputs()
            );
        }

        @Override
        public void afterAgentInvocation(AgentResponse response) {
            log.info("✅ Agent调用完成: {}, 耗时: {}ms, Token: {}",
                response.agentName(),
                response.duration(),
                response.tokenUsage()
            );
        }

        @Override
        public void onAgentInvocationError(AgentInvocationError error) {
            log.error("❌ Agent调用失败: {}",
                error.agentName(),
                error.exception()
            );
        }

        @Override
        public void beforeToolExecution(BeforeToolExecution execution) {
            log.debug("🔧 工具调用: {}({})",
                execution.toolName(),
                execution.toolArguments()
            );
        }

        @Override
        public void afterToolExecution(ToolExecution execution) {
            log.debug("✓ 工具执行完成: {}, 耗时: {}ms",
                execution.toolName(),
                execution.duration()
            );
        }
    };

    // 3. 应用监听器
    return AgenticServices
        .agentBuilder(NutritionAiAgent.class)
        .chatModel(model)
        .listener(monitor)          // 添加监控器
        .listener(loggingListener)  // 添加日志监听器
        .build();
}

// 4. 生成HTML报告（可选）
// HtmlReportGenerator.generateReport(monitor, Path.of("agent-execution.html"));
```

**监控数据包含：**
- Agent调用链路
- 执行时间
- Token使用量
- 工具调用详情
- 错误信息

---

### 5. 改进 ChatMemory 配置

**当前问题：** 所有agent共享同一个ChatMemory Bean

**改进方案：使用ChatMemoryProvider按用户ID隔离**

```java
@Bean
public NutritionAiAgent nutritionAiAgent(ChatLanguageModel model) {
    return AgenticServices
        .agentBuilder(NutritionAiAgent.class)
        .chatModel(model)
        .chatMemoryProvider(memoryId ->
            // 为每个memoryId创建独立的ChatMemory
            MessageWindowChatMemory.withMaxMessages(20)
        )
        .build();
}
```

**更高级的配置：**

```java
@Bean
public NutritionAiAgent nutritionAiAgent(ChatLanguageModel model) {
    return AgenticServices
        .agentBuilder(NutritionAiAgent.class)
        .chatModel(model)
        .chatMemoryProvider(memoryId -> {
            // 根据memoryId类型使用不同的策略
            if (memoryId.startsWith("vip_")) {
                // VIP用户：保留更多历史
                return MessageWindowChatMemory.withMaxMessages(50);
            } else {
                // 普通用户：标准配置
                return MessageWindowChatMemory.withMaxMessages(20);
            }
        })
        .build();
}
```

**优势：**
- 用户数据隔离
- 支持差异化配置
- 避免内存混淆

---

### 6. 实现 AgenticScope 数据共享

**使用场景：** Agent之间需要协作

```java
// 示例：推荐agent需要营养agent的信息

public interface NutritionAiAgent {
    @Agent(outputKey = "nutritionInfo")
    String analyze(@V("foodName") String foodName);
}

public interface RecommendationAiAgent {
    @Agent(outputKey = "recommendation")
    String recommend(
        @V("userRequest") String request,
        @V("nutritionInfo") String nutritionInfo  // 从scope读取
    );
}

// 使用Sequential Workflow
var healthyRecommendationWorkflow = AgenticServices
    .sequenceBuilder()
    .subAgents(nutritionAgent, recommendationAgent)
    .outputKey("finalRecommendation")
    .build();

// 调用
String result = healthyRecommendationWorkflow.invoke(Map.of(
    "foodName", "苹果",
    "userRequest", "推荐健康的早餐"
));
```

**AgenticScope的生命周期：**
- 自动创建：当workflow被调用时
- 自动传递：在agent之间共享
- 自动清理：无状态时自动销毁

**持久化AgenticScope（可选）：**

```java
// 实现自定义持久化
public class DatabaseAgenticScopeStore implements AgenticScopeStore {

    @Override
    public void save(AgenticScope scope) {
        // 保存到数据库
        String scopeId = scope.scopeId();
        String stateJson = serializeState(scope.state());
        repository.save(scopeId, stateJson);
    }

    @Override
    public Optional<AgenticScope> load(String scopeId) {
        // 从数据库加载
        return repository.findById(scopeId)
            .map(this::deserializeState);
    }
}

// 配置持久化
AgenticScopePersister.setStore(new DatabaseAgenticScopeStore());
```

---

### 7. 考虑 Supervisor Agent（高级）

**适用场景：** 复杂的多步骤任务，需要LLM自主决策

```java
@Bean
public SupervisorAgent intelligentSupervisor(
    ChatLanguageModel plannerModel,
    NutritionAiAgent nutritionAgent,
    RecommendationAiAgent recommendationAgent,
    OrderAiAgent orderAgent
) {
    return AgenticServices
        .supervisorBuilder()
        .chatModel(plannerModel)  // 使用强大的规划模型
        .subAgents(nutritionAgent, recommendationAgent, orderAgent)
        .responseStrategy(SupervisorResponseStrategy.LAST)
        .contextGenerationStrategy(
            SupervisorContextStrategy.CHAT_MEMORY_AND_SUMMARIZATION
        )
        .supervisorContext("""
            你是佳食宜选的智能助手协调器。

            # 可用专家
            - NutritionAiAgent: 营养咨询专家
            - RecommendationAiAgent: 美食推荐专家
            - OrderAiAgent: 订餐服务专家

            # 工作原则
            1. 理解用户的核心需求
            2. 选择最合适的专家处理
            3. 如需多个专家协作，制定执行计划
            4. 汇总专家意见，给用户完整答复

            # 示例场景
            用户："我想吃健康的午餐，大概200卡路里"

            计划：
            1. 调用NutritionAiAgent分析200卡路里的食物选择
            2. 调用RecommendationAiAgent推荐符合条件的菜品
            3. 汇总建议给用户
        """)
        .build();
}
```

**Supervisor的优势：**
- LLM自主规划执行步骤
- 支持多agent协作
- 自动处理复杂任务
- 可解释的执行过程

---

### 8. 声明式API（可选）

**使用注解简化配置：**

```java
public interface IntelligentWorkflow {

    @ConditionalAgent(
        outputKey = "response",
        subAgents = {
            NutritionAiAgent.class,
            RecommendationAiAgent.class,
            OrderAiAgent.class
        }
    )
    String process(@V("request") String request);

    @ActivationCondition(NutritionAiAgent.class)
    static boolean activateNutrition(@V("intent") String intent) {
        return "NUTRITION".equals(intent);
    }

    @ActivationCondition(RecommendationAiAgent.class)
    static boolean activateRecommendation(@V("intent") String intent) {
        return "RECOMMENDATION".equals(intent);
    }

    @ActivationCondition(OrderAiAgent.class)
    static boolean activateOrder(@V("intent") String intent) {
        return "ORDER".equals(intent);
    }

    @ChatModelSupplier
    static ChatModel chatModel() {
        return chatLanguageModel();
    }
}

// 使用
IntelligentWorkflow workflow = AgenticServices
    .createAgenticSystem(IntelligentWorkflow.class, chatLanguageModel);

String response = workflow.process("苹果有多少卡路里？");
```

---

## 📋 改进优先级

### P0 - 立即改进（影响稳定性和可维护性）

- ✅ **添加错误处理机制**（errorHandler）
- ✅ **添加Agent监控**（AgentMonitor + AgentListener）
- ✅ **改进ChatMemory配置**（使用ChatMemoryProvider）

**预期收益：**
- 提升系统稳定性
- 便于问题排查
- 避免数据混淆

### P1 - 短期改进（1-2周内）

- ✅ **迁移到AgenticServices**和@Agent注解
- ✅ **使用Conditional Workflow**替代手动路由
- ✅ **实现AgenticScope数据共享**

**预期收益：**
- 代码更简洁
- 符合官方最佳实践
- 支持更复杂的workflow

### P2 - 长期优化（1-2月内）

- ✅ **考虑Supervisor Agent架构**
- ✅ **实现AgenticScope持久化**
- ✅ **使用声明式API**简化配置

**预期收益：**
- 更强的自主性
- 支持有状态的对话
- 配置更简洁

---

## 🔄 迁移步骤建议

### 阶段1：基础设施升级（不影响现有功能）

1. 添加错误处理和监控
2. 改进ChatMemory配置
3. 编写单元测试验证

### 阶段2：逐步迁移到Agentic模块

1. 选择一个Agent（如NutritionAiAgent）试点
2. 迁移到AgenticServices
3. 验证功能一致性
4. 逐步迁移其他Agent

### 阶段3：引入高级特性

1. 实现Conditional Workflow
2. 添加Agent协作场景
3. 考虑Supervisor架构

---

## 📚 代码示例：完整的改进版Agent

```java
/**
 * 营养分析AI Agent（改进版）
 */
public interface NutritionAiAgent {

    @SystemMessage("""
        你是"佳食宜选"的专业营养师助手。

        # 专业身份
        你拥有扎实的营养学知识，能够：
        1. 精确计算食物营养成分
        2. 评估饮食健康度和营养均衡性
        3. 提供科学、实用的营养建议

        # 核心原则
        - 数据必须准确：基于真实的营养数据
        - 建议必须科学：基于营养学原理
        - 回答简洁明了：专业但易懂
    """)
    @Agent(
        value = "分析食物营养成分和提供营养建议",
        outputKey = "nutritionAdvice"
    )
    String chat(@UserMessage String userMessage);

    // 声明式配置
    @ChatModelSupplier
    static ChatModel chatModel() {
        return chatLanguageModel();
    }

    @ChatMemoryProviderSupplier
    static ChatMemoryProvider memoryProvider() {
        return memoryId -> MessageWindowChatMemory.withMaxMessages(20);
    }

    @ToolsSupplier
    static Object[] tools() {
        return new Object[]{
            nutritionTools,
            nutritionRecordTools,
            userTools
        };
    }

    @AgentListenerSupplier
    static AgentListener listener() {
        return new AgentListener() {
            @Override
            public void beforeAgentInvocation(AgentRequest request) {
                log.info("营养Agent调用: {}", request.inputs());
            }
        };
    }
}

/**
 * 配置类（改进版）
 */
@Configuration
public class ImprovedLangChain4jConfig {

    @Bean
    public NutritionAiAgent nutritionAiAgent(ChatLanguageModel model) {
        return AgenticServices
            .agentBuilder(NutritionAiAgent.class)
            .chatModel(model)
            .errorHandler(this::handleError)
            .listener(new AgentMonitor())
            .build();
    }

    private ErrorRecoveryResult handleError(ErrorContext context) {
        log.error("Agent错误: {}", context.agentName(), context.exception());

        if (context.exception() instanceof ToolExecutionException) {
            return ErrorRecoveryResult.retry();
        }

        return ErrorRecoveryResult.result(
            "抱歉，营养分析服务暂时不可用，请稍后再试。"
        );
    }
}
```

---

## 📖 参考资源

### 官方文档
- [LangChain4j Agents Tutorial](https://github.com/langchain4j/langchain4j/blob/main/docs/docs/tutorials/agents.md)
- [LangChain4j Official Site](https://langchain4j.dev/)

### 教程文章
- [Build AI Apps and Agents in Java](https://javapro.io/2025/04/23/build-ai-apps-and-agents-in-java-hands-on-with-langchain4j/)
- [The LangChain4j Agentic AI API](https://medium.com/oracledevs/the-langchain4j-agentic-ai-api-creating-rag-agents-with-oracle-ai-vector-search-e11846fc266d)
- [Building Self-Correcting AI Agents](https://levelup.gitconnected.com/building-self-correcting-ai-agents-a-practical-guide-to-agentic-workflows-105fdc485229)

### 2026最新指南
- [How to Build AI Agent from Scratch 2026](https://www.agilesoftlabs.com/blog/2026/03/how-to-build-ai-agent-from-scratch-2026)
- [The Realistic Guide to Mastering AI Agents in 2026](https://www.decodingai.com/p/realistic-guide-to-mastering-ai-agents-in-2026)

---

## 💡 关键要点总结

1. **使用Agentic模块**：从传统AiServices迁移到AgenticServices
2. **声明式配置**：使用@Agent等注解简化配置
3. **Workflow模式**：使用Conditional、Sequential等模式替代手动路由
4. **错误处理**：添加errorHandler提升稳定性
5. **监控可观察性**：使用AgentMonitor和AgentListener
6. **数据共享**：利用AgenticScope实现Agent协作
7. **渐进式迁移**：分阶段改进，避免大规模重构

---

**文档维护：** 根据项目进展持续更新
**最后更新：** 2026-03-24
**作者：** Claude Code Analysis
