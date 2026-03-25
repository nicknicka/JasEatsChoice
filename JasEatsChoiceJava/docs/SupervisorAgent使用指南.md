# SupervisorAgent 使用指南

## 概述

SupervisorAgent 是 L3 层级的监督代理，负责智能调度 L2 领域 Agent，协调多个 Agent 完成复杂任务。

## 架构定位

```
┌─────────────────────────────────────────────────┐
│           SupervisorAgent (L3)                  │
│         智能调度 + 协调 + 综合                   │
└────────────────┬────────────────────────────────┘
                 │
    ┌────────────┼────────────┬────────────┐
    ▼            ▼            ▼            ▼
┌────────┐  ┌─────────┐  ┌──────────┐  ┌──────────┐
│ 推荐Agent│  │健康Agent│  │订单Agent  │  │助手Agent │
│  (L2)   │  │  (L2)   │  │  (L2)    │  │  (L2)    │
└────────┘  └─────────┘  └──────────┘  └──────────┘
    │            │            │            │
    └────────────┴────────────┴────────────┘
                 │
                 ▼
            L1 工具层
```

## 核心功能

### 1. 智能路由

SupervisorAgent 能够分析用户问题，自动路由到最合适的 L2 Agent：

| 用户问题类型 | 路由目标 | 示例 |
|-------------|---------|------|
| 菜品推荐 | SmartRecommendationAgent | "推荐一些低卡路里的川菜" |
| 营养咨询 | HealthManagementAgent | "宫保鸡丁有多少卡路里？" |
| 订单处理 | FullOrderAgent | "我想点一份宫保鸡丁" |
| 综合问题 | 协调多个 Agent | "推荐健康的菜并分析营养" |

### 2. 多 Agent 协作

当问题涉及多个领域时，SupervisorAgent 会：

1. **分析问题**：识别涉及的领域和 Agent
2. **任务分解**：将复杂问题分解为子任务
3. **并行调度**：同时调用多个 Agent
4. **结果综合**：整合多个 Agent 的结果
5. **生成回复**：生成清晰友好的最终回复

**示例**：
```
用户: "我想减肥，推荐一些低卡路里的菜，并告诉我营养分析"

SupervisorAgent 执行流程:
1. 识别需求：推荐 + 营养分析
2. 调用 SmartRecommendationAgent → 推荐低卡菜品
3. 调用 HealthManagementAgent → 分析营养成分
4. 综合两个 Agent 的结果
5. 生成包含推荐和营养分析的完整回复
```

### 3. 上下文管理

SupervisorAgent 使用 **CHAT_MEMORY_AND_SUMMARIZATION** 策略：

- **聊天记忆**：保持对话历史，支持多轮对话
- **自动摘要**：长对话自动生成摘要，节省 token
- **状态共享**：多个 Agent 之间共享 AgenticScope 状态

## 配置说明

### 核心配置

```java
@Bean
public SupervisorAgent supervisorAgent(ChatModel chatModel, ChatMemory chatMemory) {
    return AgenticServices
            .supervisorBuilder(SupervisorAgent.class)
            .chatModel(chatModel)
            .chatMemoryProvider(memoryId -> chatMemory)
            .name("SupervisorAgent")
            .description("智能调度Agent，协调多个L2领域Agent完成复杂任务")
            .outputKey("supervisorResult")
            .supervisorContext("""
                # 角色定义
                你是一个智能监督代理（SupervisorAgent）...

                # 可用的领域专家Agent
                1. SmartRecommendationAgent - 智能推荐专家
                2. HealthManagementAgent - 健康管理专家
                3. FullOrderAgent - 订单处理专家
                4. IntelligentAssistantAgent - 综合智能助手
                """)
            .contextGenerationStrategy(
                SupervisorContextStrategy.CHAT_MEMORY_AND_SUMMARIZATION
            )
            .responseStrategy(
                SupervisorResponseStrategy.SCORED
            )
            .maxAgentsInvocations(10)
            .build();
}
```

### 配置参数说明

| 参数 | 说明 | 可选值 |
|------|------|--------|
| `chatModel` | ChatModel 实例 | - |
| `chatMemoryProvider` | ChatMemory 提供者 | Lambda 表达式 |
| `name` | Agent 名称 | 字符串 |
| `description` | Agent 描述 | 字符串 |
| `outputKey` | 输出键名 | 字符串 |
| `supervisorContext` | 系统提示词 | 多行文本 |
| `contextGenerationStrategy` | 上下文策略 | CHAT_MEMORY / SUMMARIZATION / CHAT_MEMORY_AND_SUMMARIZATION |
| `responseStrategy` | 响应策略 | SCORED / SUMMARY / LAST |
| `maxAgentsInvocations` | 最大 Agent 调用次数 | 整数 |

### 上下文策略对比

| 策略 | 优点 | 缺点 | 适用场景 |
|------|------|------|----------|
| `CHAT_MEMORY` | 保留完整上下文 | Token 消耗大 | 短对话 |
| `SUMMARIZATION` | 节省 Token | 可能丢失细节 | 长对话 |
| `CHAT_MEMORY_AND_SUMMARIZATION` | 平衡性能和准确性 | 配置复杂 | **推荐使用** |

### 响应策略对比

| 策略 | 说明 | 适用场景 |
|------|------|----------|
| `SCORED` | LLM 评分选择最佳响应 | **推荐使用** |
| `SUMMARY` | 生成摘要回复 | 需要简洁总结 |
| `LAST` | 使用最后一个 Agent 的响应 | 单 Agent 场景 |

## 使用方式

### 1. 注入 SupervisorAgent

```java
@Service
public class ChatService {

    @Autowired
    private SupervisorAgent supervisorAgent;

    public String chat(String userMessage) {
        return supervisorAgent.chat(userMessage);
    }
}
```

### 2. 基本对话

```java
String response = supervisorAgent.chat("推荐一些低卡路里的川菜");
```

### 3. 带用户ID的对话

```java
String response = supervisorAgent.chatWithContext(
    "根据我的历史记录推荐菜品",
    "user123"
);
```

### 4. Controller 层使用

```java
@RestController
@RequestMapping("/api/agent/supervisor")
public class SupervisorAgentController {

    @Resource
    private SupervisorAgent supervisorAgent;

    @PostMapping("/chat")
    public ResponseResult<String> chat(@RequestBody ChatRequest request) {
        String response = supervisorAgent.chat(request.getMessage());
        return ResponseResult.success(response);
    }
}
```

## 工作流程

### 完整流程示例

```
1. 用户提问
   "我想减肥，推荐一些健康的川菜"

2. SupervisorAgent 分析问题
   - 识别关键词: 减肥、健康、川菜
   - 确定涉及的领域: 推荐、营养

3. Agent 选择和调度
   - 选择: SmartRecommendationAgent + NutritionGuideAgent
   - 并行调用两个 Agent

4. Agent 执行
   - SmartRecommendationAgent: 推荐低卡川菜
   - NutritionGuideAgent: 分析营养信息

5. 结果综合
   - 整合推荐列表和营养分析
   - 生成结构化回复

6. 返回最终结果
   "根据您的减肥目标，我为您推荐以下健康川菜：
    1. 清炒豆苗 - 80卡路里
    2. 水煮鱼片 - 150卡路里
    ..."
```

## 监控和调试

### 查看调度日志

启用 Agent 监控后，可以查看 Supervisor 的调度日志：

```log
🤖 [Agent调用开始 #1] SupervisorAgent
📤 [请求发出 #1] 发送到LLM模型
📥 [响应接收 #1] LLM响应接收
🤖 [Agent调用开始 #2] SmartRecommendationAgent
🤖 [Agent调用开始 #3] NutritionGuideAgent
✅ [Agent调用完成 #2] SmartRecommendationAgent
✅ [Agent调用完成 #3] NutritionGuideAgent
✅ [Agent调用完成 #1] SupervisorAgent
⏱️ [性能监控] 总耗时: 3456ms
```

### 查看调用链报告

```bash
curl http://localhost:8080/api/admin/agent-monitoring/call-chain/session-1
```

## 性能优化

### 1. 调整最大调用次数

根据任务复杂度调整 `maxAgentsInvocations`：

```java
.maxAgentsInvocations(5)  // 简单任务: 5次
.maxAgentsInvocations(10) // 复杂任务: 10次
.maxAgentsInvocations(20) // 超级复杂: 20次
```

### 2. 优化上下文策略

- 短对话使用 `CHAT_MEMORY`
- 长对话使用 `CHAT_MEMORY_AND_SUMMARIZATION`
- 超长对话定期清理历史

### 3. 响应策略选择

- 需要最佳质量：`SCORED`
- 需要简洁回复：`SUMMARY`
- 单 Agent 任务：`LAST`

## 最佳实践

### 1. 提示词优化

在 `supervisorContext` 中明确：

- Agent 的职责和能力边界
- Agent 之间的协作规则
- 输出格式要求
- 错误处理策略

### 2. 错误处理

SupervisorAgent 会自动处理 Agent 调用失败：

- 单个 Agent 失败不影响其他 Agent
- 尽量使用成功的 Agent 结果
- 明确告知用户部分功能不可用

### 3. 用户体验

- 多 Agent 协作时，明确说明每个 Agent 的职责
- 避免让用户感知到底层的复杂性
- 提供清晰的进度反馈

## 常见问题

### Q1: SupervisorAgent 如何选择 Agent？

**A**: SupervisorAgent 通过 LLM 分析用户问题，根据 `supervisorContext` 中定义的 Agent 职责，自动选择最合适的 Agent。

### Q2: 如果所有 Agent 都无法处理怎么办？

**A**: SupervisorAgent 会尝试使用 IntelligentAssistantAgent 作为兜底，并明确告知用户无法完全处理该问题。

### Q3: 如何限制 Supervisor 的调用次数？

**A**: 通过 `maxAgentsInvocations` 参数设置最大调用次数，防止无限循环。

### Q4: 上下文策略如何选择？

**A**:
- 短对话（<10轮）：`CHAT_MEMORY`
- 中等对话（10-50轮）：`CHAT_MEMORY_AND_SUMMARIZATION`
- 长对话（>50轮）：`SUMMARIZATION` + 定期清理

### Q5: 如何测试 SupervisorAgent？

**A**: 使用提供的测试类 `SupervisorAgentTest.java`，包含多种场景的测试用例。

## 后续优化方向

1. **动态路由规则**：基于历史数据优化路由决策
2. **性能监控**：添加更详细的性能指标
3. **A/B 测试**：测试不同的提示词和策略
4. **多模态支持**：支持图片、语音等多模态输入

---

**文档版本**: 1.0
**更新时间**: 2026-03-25
**作者**: Claude
