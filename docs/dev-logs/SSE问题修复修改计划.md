# SSE 问题修复修改计划

> 依据：[SSE流程分析报告_20260412.md](SSE流程分析报告_20260412.md)

## 一、目标

本次修复的目标不是单点修补，而是把一次“根据我的口味推荐几道菜”的完整链路稳定下来，避免以下问题再次发生：

- SSE 进度事件重复发送，前端收到重复提示。
- Supervisor 路由顺序错误，出现“先推荐，再反问，再查偏好”的回路。
- 推荐 Agent 直接生成看似合理但缺少数据来源的内容。
- Supervisor done 阶段耗时过高，拖慢整条链路。

最终希望把流程收敛为：

1. 先获取或补齐用户偏好。
2. 再基于真实数据进行菜品推荐。
3. 最后一次性输出结果并结束 SSE。

---

## 二、问题结论

### 1. SSE 重复事件

`SSEAgentListener` 当前会继承到子 Agent，导致同一监听器在 Supervisor 层和子 Agent 层重复触发。结合当前执行链路中的多次规划和评分，最终表现为同一事件被发送多次，前端收到重复进度消息，并可能触发 `ResponseBodyEmitter has already completed`。

### 2. Supervisor 路由顺序不合理

当前 Supervisor 的路由规则是“关键词平铺匹配”，只能识别“推荐”这类单一意图，无法处理“推荐 + 偏好”这种复合意图。结果是先把原话给 DishRecommendationAgent，再由它反问用户，再把反问转给 UserPreferenceAgent，形成无效循环。

### 3. 推荐内容缺少强约束

DishRecommendationAgent 虽然声明了“必须使用工具”，但提示词约束不够硬，模型仍可能直接生成菜品、价格、热量、评分等内容。工具本身是查库的，问题更可能出在 Agent 没有被强制要求先调用工具。

### 4. done 阶段耗时过高

Supervisor 使用了评分型响应策略，导致结束前还要进行额外 LLM 评估。报告里 done 决策耗时接近 12 秒，说明这一步的性价比很低，应该改成更轻量的策略。

---

## 三、修复优先级

### P0：必须先修

- SSE 重复发送
- Supervisor 路由顺序错误
- 推荐 Agent 输出缺少数据约束

### P1：性能优化

- Supervisor done 阶段评分开销过高
- 路由规则过度依赖 LLM 推理

### P2：补充治理

- 补测试和回归样例
- 统一推荐数据返回格式
- 降低日志噪音，保留关键链路日志

---

## 四、详细修改计划

### 任务 1：修复 SSE 重复发送

#### 目标

保证一次 Agent 调用只发送一次 `AGENT_START` 和一次 `AGENT_COMPLETE`，避免重复触发和连接异常。

#### 修改点

- 文件：`JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/listener/SSEAgentListener.java`

#### 建议方案

- 将 `inheritedBySubagents()` 改为不继承到子 Agent，避免监听器在子链路重复触发。
- 在 `sendEvent()` 内增加去重保护，按 `agentName + eventType + 会话维度` 做幂等控制。
- 保留 `emitterFailed` 兜底逻辑，避免连接异常后继续发送。

#### 验收标准

- 同一轮请求中，单个 Agent 的 START / COMPLETE 只出现一次。
- 不再出现 `ResponseBodyEmitter has already completed`。
- 前端不再看到重复进度文案。

---

### 任务 2：重写 Supervisor 路由规则

#### 目标

让“查偏好 + 推荐”成为稳定的执行顺序，而不是“推荐失败后再补偏好”的回路。

#### 修改点

- 文件：`JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/service/SupervisorAgentFactory.java`

#### 建议方案

- 把当前平铺的关键词规则改成“优先级 + 组合规则”。
- 规则建议如下：
  - 纯推荐且上下文已有偏好：直接走 `DishRecommendationAgent`。
  - 任何包含口味、忌口、过敏、健康目标的推荐请求：先走 `UserPreferenceAgent`，再走 `DishRecommendationAgent`。
  - 纯偏好类请求：直接走 `UserPreferenceAgent`。
- 明确禁止 Supervisor 把子 Agent 的追问再次当成新的主任务继续分发。

#### 验收标准

- 输入“根据我的口味推荐几道菜”时，稳定顺序是 `UserPreferenceAgent → DishRecommendationAgent`。
- 不再出现 `DishRecommendationAgent → UserPreferenceAgent → DishRecommendationAgent` 的回路。
- 单轮请求内不会出现无意义的重复路由。

---

### 任务 3：收紧 DishRecommendationAgent 的输出约束

#### 目标

让推荐内容必须来自工具结果，禁止模型凭空生成价格、热量、评分等字段。

#### 修改点

- 文件：`JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/agents/DishRecommendationAgent.java`

#### 建议方案

- 强化 system prompt，明确以下约束：
  - 推荐前必须先调用工具。
  - 价格、热量、评分只能引用工具返回值。
  - 偏好不足时，先补齐偏好，不允许直接猜测。
  - 不允许输出工具中不存在的菜名或数值。
- 如果仍存在自由生成风险，建议把工具输出改成更结构化的文本或统一 DTO。

#### 验收标准

- 推荐结果中的字段都能在工具返回或数据库中找到来源。
- 不出现凭空编造的菜品信息。
- 偏好信息不足时，模型不会直接胡乱推荐。

---

### 任务 4：核对推荐工具链路真实性

#### 目标

确认推荐结果确实来自数据库，而不是 Agent 在工具结果基础上继续扩写出虚构内容。

#### 修改点

- 文件：`JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/recommendation/RecommendationQueryTools.java`

#### 建议方案

- 检查 `queryRecommendations`、`getHotDishes`、`getPersonalizedRecommendations`、`queryLowCalorieDishes` 的查询逻辑是否完整。
- 确认 `userId` 从 `AgenticScope` 读取后，确实参与了用户资料查询和偏好过滤。
- 检查返回文本是否过于自由，是否需要统一成结构化输出。
- 若必要，补充统一推荐 DTO，减少 LLM 在结果拼装阶段的发挥空间。

#### 验收标准

- 推荐结果能追溯到真实数据库字段。
- 用户偏好、饮食目标、过敏信息都能影响推荐结果。
- 不存在数据库里没有的数据却出现在输出中的情况。

---

### 任务 5：降低 Supervisor done 阶段耗时

#### 目标

减少结束阶段的额外 LLM 评分成本，让任务更快收敛。

#### 修改点

- 文件：`JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/service/SupervisorAgentFactory.java`

#### 建议方案

- 将 `responseStrategy` 从 `SCORED` 调整为更轻量的策略，优先考虑 `LAST`。
- 如果后续确实需要评分，建议只在复杂任务或歧义场景启用。
- 保持路由规则足够清晰，减少模型在结束条件上的反复判断。

#### 验收标准

- done 阶段耗时明显下降。
- 不再出现为了结束任务而额外做多轮评分的情况。
- 总体链路时延明显改善。

---

## 五、推荐执行顺序

建议按下面顺序实施，避免相互干扰：

1. 先修 [SSEAgentListener.java](../JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/listener/SSEAgentListener.java) 的重复事件问题。
2. 再修 [SupervisorAgentFactory.java](../JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/service/SupervisorAgentFactory.java) 的路由规则和响应策略。
3. 接着收紧 [DishRecommendationAgent.java](../JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/agents/DishRecommendationAgent.java) 的提示词和约束。
4. 然后核对 [RecommendationQueryTools.java](../JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/agent/tools/recommendation/RecommendationQueryTools.java) 的真实数据链路。
5. 最后补测试和回归验证。

---

## 六、建议分派给其他 Agent 的任务包

### Agent A：SSE 修复

- 任务：修复重复事件发送和 SSE 异常关闭问题。
- 产出：监听器改造、幂等保护、对应测试。

### Agent B：Supervisor 路由修复

- 任务：重写路由优先级和组合规则，减少无效回路。
- 产出：SupervisorContext 改造、路由测试。

### Agent C：推荐 Agent 约束收紧

- 任务：强化推荐输出必须依赖工具结果。
- 产出：System prompt 调整、输出约束说明。

### Agent D：工具链路核查

- 任务：验证推荐数据是否真实来自数据库，并补齐结构化输出。
- 产出：工具逻辑核查、必要时的返回格式优化。

### Agent E：性能与测试

- 任务：降低 done 阶段耗时并补回归测试。
- 产出：响应策略调整、性能测试、回归用例。

---

## 七、完成标准

本次修改完成后，至少满足以下条件：

- 推荐链路不会出现重复 SSE。
- 推荐请求不会再出现“先推荐、再反问、再查偏好”的回路。
- 推荐内容中的价格、热量、评分都可追溯。
- done 阶段耗时显著下降。
- 核心链路有测试覆盖，后续不易回退。

---

## 八、补充说明

如果只做最小修复，优先级应是：

1. SSE 去重。
2. Supervisor 路由顺序纠正。
3. 推荐 Agent 强约束。
4. responseStrategy 下调。

如果要做成稳定版本，建议把“路由规则”和“数据返回格式”一起标准化，不要只改 prompt，否则后续还会出现类似的自问自答和虚构数据问题。
