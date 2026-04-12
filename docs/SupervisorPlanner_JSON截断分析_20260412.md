# SupervisorPlanner JSON 输出截断分析

> **分析日期**: 2026-04-12
> **触发场景**: 用户发送"根据我的口味推荐几道菜"，userId=3384650106421960
> **错误类型**: `OutputParsingException` → `JsonEOFException`
> **严重级别**: P0（请求完全失败）

---

## 一、问题现象

用户发送一条正常请求后，后端直接返回错误，前端无推荐结果。

```
用户输入: "根据我的口味推荐几道菜"
预期结果: 获得个性化菜品推荐
实际结果: 请求失败，SupervisorAgent 处理异常
```

---

## 二、完整执行时间线

| 时间戳 | 阶段 | 耗时 | 状态 |
|--------|------|------|------|
| 09:48:20.382 | 收到SSE请求 | - | 正常 |
| 09:48:20.390 | SupervisorAgent创建 | 5ms | 正常 |
| 09:48:20.393 | Agent调用开始 | - | 正常 |
| 09:48:21.451 | Planner第1轮：路由到 `UserPreferenceAgent` | ~1.1s | 正常 |
| 09:48:23.110 | UserPreferenceAgent调用工具查询偏好 | ~1.7s | 正常 |
| 09:48:23.124 | 查询结果：**无偏好** | - | 正常 |
| 09:48:34.451 | Planner第2轮：路由到 `DishRecommendationAgent` | **~11.3s** | **失败** |

---

## 三、错误详情

### 3.1 异常链

```
OutputParsingException
  └── PojoOutputParser.parse() 无法将文本解析为 AgentInvocation
        └── JacksonJsonCodec.fromJson() JSON解析失败
              └── JsonEOFException: Unexpected end-of-input
                    "expected close marker for Object" at line 5, column 2
```

### 3.2 LLM 实际输出

```json
{
  "agentName": "DishRecommendationAgent",
  "arguments": {
    "userMessage": "根据您的口味偏好，为您推荐以下几道菜：1. 蒜蓉西兰花 2. 清炒时蔬 3. 蒜香排骨 4. 花菜炒肉片 5. 蒜蓉粉丝蒸虾。这些菜品口感清淡，营养丰富，符合您的口味偏好和饮食需求。"
  }
```

**缺失**: 外层闭合 `}`

### 3.3 期望输出

```json
{
  "agentName": "DishRecommendationAgent",
  "arguments": {
    "userMessage": "根据用户口味偏好推荐菜品"
  }
}
```

---

## 四、根因分析

### 4.1 直接原因

LLM（GLM-4-plus）在 Planner 的第2轮规划中，**越权生成了推荐内容**，将完整的菜品推荐列表塞入 `arguments.userMessage` 字段，导致 JSON 总长度超出限制被截断，缺少闭合 `}`。

### 4.2 证据链

| 证据 | 说明 |
|------|------|
| 第2轮规划耗时 **11.3秒** | 正常规划只需1-2秒，11秒说明LLM在生成长文本 |
| userMessage 包含5道菜+总结 | Planner 的职责是路由决策，不应生成推荐内容 |
| 用户偏好查询结果为"无偏好" | LLM可能在偏好为空时"自作主张"编造了推荐 |
| 第1轮规划仅耗时1.1秒 | 正常的规划输出是简短的JSON，速度很快 |

### 4.3 深层原因

**Planner 的 `supervisorContext` 缺少对 `arguments.userMessage` 的长度和内容约束**。

当前的 prompt（[SupervisorAgentFactory.java:137-165](JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/service/SupervisorAgentFactory.java#L137-L165)）只定义了路由规则和禁止行为，没有明确要求：

1. `userMessage` 必须简短（只传递用户意图，不生成回答内容）
2. 禁止在规划阶段生成任何推荐、建议或回答内容
3. JSON 输出必须完整且简洁

### 4.4 与上次修复的关系

上次修复（[SSE问题修复实施报告_20260412.md](SSE问题修复实施报告_20260412.md)）已解决了路由顺序问题——本次日志显示 Planner 正确地先调用了 `UserPreferenceAgent`，再调 `DishRecommendationAgent`，路由顺序已修复。

但 Planner 在第2轮调用时产生了新的问题：**在 arguments 中塞入了过长内容**。

---

## 五、修复方案

### 方案：优化 supervisorContext 的 prompt

在 `createSupervisorContext()` 中增加对 Planner 输出格式的严格约束：

**修改文件**: [SupervisorAgentFactory.java:137-165](JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/service/SupervisorAgentFactory.java#L137-L165)

**新增内容**（添加到现有 prompt 末尾）:

```text
## 输出格式约束（强制）
1. arguments.userMessage 只能传递用户的原始意图或简短转述，不超过30个字
2. 禁止在 arguments.userMessage 中生成任何推荐内容、菜品列表、建议或回答
3. 你的职责仅是选择Agent和传递意图，实际回答由子Agent生成
4. 示例：
   正确: {"agentName":"DishRecommendationAgent","arguments":{"userMessage":"根据用户口味推荐菜品"}}
   错误: {"agentName":"DishRecommendationAgent","arguments":{"userMessage":"推荐：蒜蓉西兰花、清炒时蔬...这些菜品口感清淡..."}}
```

**预期效果**:
- Planner 输出的 JSON 长度从 ~200字符 降至 ~100字符
- 消除因输出过长导致的 JSON 截断
- 第2轮规划耗时从 11秒 降至 1-2秒

### 补充方案：降低 supervisorModel 的 maxToken

当前 supervisorModel 的 `maxToken` 设置为 4096（[LangChain4jConfig.java:167](JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/config/LangChain4jConfig.java#L167)）。Planner 每次输出只需要一个简短 JSON（<200字符），可以降低到 512 或 1024，从物理层面限制 LLM 输出长度。

```java
// 当前
.maxToken(4096)

// 建议
.maxToken(512)
```

> **注意**: maxToken 会影响 Supervisor 所有 LLM 调用的输出上限，包括最终总结。如果 Supervisor 还负责整合最终回复，512 可能不够。需要根据实际场景权衡。

---

## 六、影响范围评估

| 维度 | 影响 |
|------|------|
| 功能影响 | **所有涉及多Agent协作的复合意图请求都会受影响**，不只是"推荐+偏好"场景 |
| 触发概率 | 偏好查询返回空结果时，LLM 更容易"越权"生成内容，触发概率较高 |
| 用户体验 | 请求直接失败，前端无结果展示 |
| 数据安全 | LLM 在截断前生成的推荐内容是虚构的，如果未被截断则会导致虚假推荐 |

---

## 七、同类场景风险排查

以下场景可能触发相同的 JSON 截断问题：

| 场景 | 风险 | 原因 |
|------|------|------|
| "推荐营养健康的菜品" | 高 | NutritionGuide + DishRecommendation 复合意图 |
| "根据我的位置推荐附近美食" | 中 | LocationService + DishRecommendation 复合意图 |
| "中午吃什么好" | 中 | TimeAware + DishRecommendation 复合意图 |
| "有什么热门菜品" | 低 | 单一意图，LLM 不太可能生成过长内容 |

---

## 八、修复优先级

| 优先级 | 修复项 | 工作量 |
|--------|--------|--------|
| P0 | 在 supervisorContext 中增加输出格式约束 | 5分钟 |
| P1 | 降低 supervisorModel 的 maxToken | 1分钟 |
| P2 | 在 Controller 层增加 JSON 解析失败的降级处理 | 30分钟 |

---

## 九、可执行修复方案

### 目标

在不破坏现有多 Agent 路由能力的前提下，先消除 Planner 生成过长或越权内容的风险，再补上失败兜底，确保这类请求即使再次异常也不会直接中断整条 SSE 链路。

### Phase 1：收紧 Planner 输出约束（必须先做）

**修改文件**：
- [SupervisorAgentFactory.java](JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/service/SupervisorAgentFactory.java#L136-L165)

**执行内容**：
1. 在 `createSupervisorContext()` 末尾补充强约束：`arguments.userMessage` 只能是用户意图的简短转述，不允许包含菜品列表、推荐理由、总结性回答。
2. 明确要求 Planner 只做“路由决策”，不做“内容生成”。
3. 补充一组正反示例，让模型知道什么是可接受输出，什么是越界输出。

**验收标准**：
- Planner 输出始终是完整 JSON。
- `arguments.userMessage` 保持短文本，不出现具体菜品内容。
- `DishRecommendationAgent` 只接收意图，不接收成品答案。

### Phase 2：降低 Planner 的生成不确定性（建议做）

**修改文件**：
- [LangChain4jConfig.java](JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/config/LangChain4jConfig.java#L156-L171)

**执行内容**：
1. 先不要直接把全局 `supervisorModel` 的 `maxToken` 一刀切降到很小。
2. 如果后续验证仍有 JSON 截断或长输出问题，再把 Supervisor 单独拆成更严格的规划模型配置。
3. 保持子 Agent 的生成能力不受影响，避免误伤最终回答质量。

**验收标准**：
- Supervisor 规划更稳定。
- 子 Agent 的正常回答长度和质量不受影响。

### Phase 3：补失败兜底（必须做）

**修改文件**：
- [SupervisorSSEController.java](JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/controller/SupervisorSSEController.java#L160-L187)

**执行内容**：
1. 保留当前错误事件推送。
2. 增加一次轻量重试或降级分支，用更短的提示词重新触发 Planner。
3. 如果二次仍失败，返回稳定的用户提示，并记录完整错误链用于排查。

**验收标准**：
- 规划失败时，SSE 链路不中断。
- 前端能收到明确错误提示，而不是空白或异常中断。
- 后端日志保留足够信息供定位。

### Phase 4：回归验证

**验证场景**：
1. `根据我的口味推荐几道菜`
2. `推荐营养健康的菜品`
3. `根据我的位置推荐附近美食`

**验收标准**：
- 复合意图场景按预期先查上下文、再做推荐。
- Planner 不再输出菜品列表。
- JSON 解析异常不再出现。
- SSE 正常完成，不出现中途失败。

---

## 十、建议的实际执行顺序

1. 先改 [SupervisorAgentFactory.java](JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/service/SupervisorAgentFactory.java#L136-L165) 的 prompt。
2. 立即回归验证“根据我的口味推荐几道菜”。
3. 如果仍有异常，再考虑拆分 Supervisor 的模型配置，而不是直接压低全局 `maxToken`。
4. 最后补控制器兜底，确保失败路径也可用。

---

## 十一、附录：错误日志关键片段

```
2026-04-12T09:48:23.124  UserPreferenceAgent: ✅ [Tool] 查询用户偏好成功: 无偏好
2026-04-12T09:48:34.451  SupervisorSSEController: SupervisorAgent处理失败

dev.langchain4j.service.output.OutputParsingException:
  Failed to parse "{...}" into dev.langchain4j.agentic.supervisor.AgentInvocation

Caused by: com.fasterxml.jackson.core.io.JsonEOFException:
  Unexpected end-of-input: expected close marker for Object
  at line 5, column 2
```
