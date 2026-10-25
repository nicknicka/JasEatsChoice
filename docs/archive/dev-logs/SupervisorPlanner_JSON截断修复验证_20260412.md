# SupervisorPlanner JSON截断修复验证分析

> **验证日期**: 2026-04-12
> **验证场景**: 用户发送"根据我的口味推荐几道菜"，userId=3384650106421960
> **修复依据**: [SupervisorPlanner_JSON截断分析_20260412.md](SupervisorPlanner_JSON截断分析_20260412.md)
> **修复内容**: Phase 1（收紧Planner输出约束）+ Phase 3（Controller失败兜底）

---

## 一、Phase 1 修复效果验证

### JSON截断问题：已修复 ✅

| 指标 | 修复前 | 修复后 |
|------|--------|--------|
| JSON解析异常 | `OutputParsingException` → `JsonEOFException` | 无异常 |
| Planner第2轮输出 | `userMessage`包含5道菜品+总结（~200字） | `userMessage="根据您的饮食偏好标签川菜、清淡、素食、低脂、低卡推荐菜品"`（28字） |
| Planner第2轮耗时 | **11.3秒** | **7.2秒** |
| 请求是否成功 | 失败 | 成功（无异常抛出） |

**结论**：prompt约束生效，Planner不再在`arguments.userMessage`中生成推荐内容。

---

## 二、新发现的问题

### 问题1：未经授权修改用户偏好数据（P0 - 数据安全）

**现象**：用户只问了"推荐几道菜"，UserPreferenceAgent在后续轮次中直接修改了用户偏好。

**日志证据**：

```
第3轮: UserPreferenceAgent → 更新用户饮食偏好, preference: {"dietType": "素食", "priceRange": "中", "nutritionNeeds": "低卡", "allergies": ["鸡蛋", "牛奶"]}
第5轮: UserPreferenceAgent → 更新用户饮食偏好, preference: {"dietType": "素食", "priceRange": "中", "nutritionNeeds": "低卡", "allergies": ["鸡蛋", "牛奶"]}
```

**分析**：

- Planner在第3轮传递了`userMessage="调整用户饮食偏好标签"`，但用户原始意图是"推荐菜品"而非"修改偏好"
- UserPreferenceAgent收到"调整偏好"指令后，直接调用了写入工具修改数据库
- 用户从未发出修改偏好的指令，这是**未经授权的数据变更**

**影响**：
- 用户的偏好数据被覆盖（原来可能是川菜/清淡等，被改成了素食/低卡/过敏）
- 违反数据安全原则：读取操作被放大为写入操作

---

### 问题2：违反禁止规则，Agent调用达到上限（P0）

**现象**：共调用5次Agent，达到`maxAgentsInvocations=5`上限，违反两条禁止规则。

**实际调用链**：

```
用户: "根据我的口味推荐几道菜"

① UserPreferenceAgent    → 查询用户偏好（查到川菜/清淡/素食/低脂/低卡）     ✅
② DishRecommendationAgent → 按川菜查询推荐菜品                              ✅
③ UserPreferenceAgent    → 修改用户偏好为素食/中价/低卡/过敏                   ❌
④ DishRecommendationAgent → 按修改后的偏好再查推荐                            ⚠️
⑤ UserPreferenceAgent    → 再次修改偏好（相同内容）                           ❌
```

**违反的规则**：

| 规则 | 违反情况 |
|------|----------|
| 禁止重复调用同一个Agent | UserPreferenceAgent调了3次，DishRecommendationAgent调了2次 |
| 单轮请求最多调用3个Agent | 实际调用了5次 |

**期望调用链**：

```
① UserPreferenceAgent    → 查询偏好     ✅
② DishRecommendationAgent → 带偏好推荐   ✅
③ done                    → 结束
```

---

### 问题3：最终回答完全偏离用户意图（P0）

**现象**：用户要的是"推荐菜品"，最终回答是"偏好调整确认"。

**根因**：`responseStrategy=LAST` 取最后一个Agent的结果作为最终输出，第5轮是UserPreferenceAgent修改偏好，所以最终回答变成了偏好确认。

**日志证据**：

```
最终结果（63字符）:
"已成功调整您的饮食偏好标签。您的新的饮食偏好为素食，价格区间为中，营养需求为低卡，并添加了鸡蛋和牛奶的过敏信息。感谢您的使用！"
```

**对比**：

| 维度 | 期望 | 实际 |
|------|------|------|
| 回答内容 | 菜品推荐列表 | 偏好修改确认 |
| 结果长度 | ~500-1000字符 | 63字符 |
| 来源Agent | DishRecommendationAgent | UserPreferenceAgent |

---

## 三、完整耗时分析

| 阶段 | 耗时 | 占比 |
|------|------|------|
| 总耗时 | **41162ms** | 100% |
| 第1轮规划 + UserPreferenceAgent | ~12.5s | 30% |
| 第2轮规划 + DishRecommendationAgent | ~6.8s | 17% |
| 第3轮规划 + UserPreferenceAgent（修改偏好） | ~8.8s | 21% |
| 第4轮规划 + DishRecommendationAgent | ~9.2s | 22% |
| 第5轮规划 + UserPreferenceAgent（再次修改） | ~5.3s | 13% |

**瓶颈**：如果只调用2个Agent（偏好+推荐），预估耗时 **~12000ms**，可减少70%。

---

## 四、根因链条

```
Phase 1修复后，Planner不再在arguments中塞推荐内容 ✅
    ↓
但Planner仍然无法在合理轮次内决定"done"
    ↓
第2轮DishRecommendationAgent返回结果后，Planner没有输出done
    ↓
Planner认为还需要继续，开始"优化"偏好数据
    ↓
第3轮发给UserPreferenceAgent"调整偏好"（LLM自作主张）
    ↓
UserPreferenceAgent收到"调整"指令就执行写入（没有判断是否是用户原始意图）
    ↓
循环继续直到达到maxAgentsInvocations=5上限
    ↓
LAST策略取最后一次（偏好修改）作为最终回答
    ↓
用户看到"偏好调整确认"而非菜品推荐
```

**三层问题叠加**：

1. **Planner层**：没有在合适时机停止，且"优化"意图超出了用户原始请求范围
2. **Agent层**：UserPreferenceAgent没有区分"查询"和"修改"操作的授权边界
3. **策略层**：LAST策略取最后一个Agent结果，当调用链跑偏时最终回答必然错误

---

## 五、修复方案

### 修复1：UserPreferenceAgent 区分查询与修改（P0 - 数据安全）

**目标**：UserPreferenceAgent 只在用户明确要求修改偏好时才执行写入，推荐场景只做读取。

**方案**：在 UserPreferenceAgent 的 system prompt 中增加授权判断规则：

```text
## 操作授权规则
- 用户明确说"修改偏好"/"设置忌口"/"更新偏好"等 → 允许写入
- 用户说"推荐"/"查询"/"查看"等 → 只允许读取，禁止调用写入工具
- 当userMessage包含"调整偏好"但原始用户意图是"推荐"时 → 只读取，不写入
```

**涉及文件**：UserPreferenceAgent 的 `@SystemMessage`

---

### 修复2：收紧 Planner 的停止条件和调用上限（P0）

**目标**：复合意图场景最多调用2个Agent就结束，不超过3个。

**方案**：在 `createSupervisorContext()` 中强化停止条件：

```text
## 停止条件（强制）
1. 已调用过推荐类Agent并返回结果后，必须立即输出done
2. 禁止在推荐完成后继续调用其他Agent"优化"数据
3. 用户只发了一条消息，你的任务就是把正确Agent的结果转交回去，不做额外操作
```

同时将 `maxAgentsInvocations` 从5降至3，作为硬上限保底：

```java
.maxAgentsInvocations(3)  // 从5降至3
```

**涉及文件**：`SupervisorAgentFactory.java`

---

### 修复3：考虑响应策略调整（P1）

**目标**：确保最终回答来自推荐Agent而非偏好Agent。

**方案A（推荐）**：保持LAST策略，但通过修复1和修复2确保调用链在2次内结束，最后一次必然是DishRecommendationAgent。

**方案B（更安全）**：在Controller层记录最后一次DishRecommendationAgent的结果，如果最终结果不包含推荐内容，用记录的结果替代。

---

## 六、预期修复后效果

| 指标 | 当前 | 修复后（预期） |
|------|------|---------------|
| 总耗时 | 41162ms | ~12000ms |
| Agent调用次数 | 5次 | 2次 |
| 最终回答 | 偏好修改确认 | 菜品推荐列表 |
| 用户偏好数据 | 被覆盖 | 不变 |
| 遵守禁止规则 | 否 | 是 |
