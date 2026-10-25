# AI Controller 详细分析报告

## 📋 总览

佳食宜选后端共有 **6个AI相关的Controller**，分为用户端和商家端两大类。

---

## 🎯 Controller 1: AIStreamController（用户端主流式接口）

**文件路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/controller/AIStreamController.java`

**基础信息**:
- **路由**: `/v1/ai/stream`
- **标签**: "AI流式对话（LangChain4j）"
- **Agent**: StreamingIntelligentAssistantAgent（L2智能调度）
- **输出方式**: SSE（Server-Sent Events）流式响应

**主要接口**:
```
POST /v1/ai/stream/chat
- 功能：SSE流式聊天
- Agent：L2智能调度Agent（调用7个L1专家Agent）
- 返回：SseEmitter（流式Token + 卡片数据）
- 超时：5分钟
```

**特点**:
- ✅ 真正的流式输出（逐Token推送）
- ✅ 支持工具调用
- ✅ 自动生成卡片数据
- ✅ L2→L1架构，智能调度

**状态**: **✅ 当前主流接口**（推荐使用）

---

## 🎯 Controller 2: AIController（用户端多功能接口）

**文件路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/controller/AIController.java`

**基础信息**:
- **路由**: `/v1/ai`
- **Agent**: NutritionAiAgent、RecommendationAiAgent（旧版Agent）

**主要接口**:
```java
POST /v1/ai/dish-recognize
- 功能：AI菜品识别
- 注意：GLM-4-Flash不支持，需要GLM-4V

POST /v1/ai/chat
- 功能：AI对话
- Agent：旧版NutritionAiAgent、RecommendationAiAgent

POST /v1/ai/analyze-nutrition
- 功能：营养分析
- Agent：NutritionAiAgent

GET /v1/ai/recommendations
- 功能：智能推荐
- Agent：RecommendationAiAgent
```

**状态**: **⚠️ 使用旧版Agent，功能已被StreamingIntelligentAssistantAgent替代**

---

## 🎯 Controller 3: MerchantAIController（商家端流式接口）

**文件路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/controller/MerchantAIController.java`

**基础信息**:
- **路由**: `/v1/merchant/ai`
- **标签**: "商家AI助手（LangChain4j）"
- **Agent**: StreamingMerchantAssistantAgent（L2智能调度）

**主要接口**:
```
POST /v1/merchant/ai/chat
- 功能：SSE流式聊天（商家端）
- Agent：StreamingMerchantAssistantAgent
- 返回：SseEmitter（流式响应）

GET /v1/merchant/ai/health
- 功能：健康检查
- 特性：销售数据分析、评价管理、菜品优化、营销策略、订单处理
```

**特点**:
- ✅ 商家经营助手
- ✅ 流式响应
- ✅ 数据分析功能

**状态**: **✅ 当前主流接口（商家端）**

---

## 🎯 Controller 4: AIFunctionCallingController（函数调用接口）

**文件路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/controller/AIFunctionCallingController.java`

**基础信息**:
- **路由**: `/v1/ai/assistant`
- **标签**: "AI助手（LangChain4j）"
- **描述**: 使用LangChain4j的函数调用功能

**主要接口**:
```
GET /v1/ai/assistant/agents
- 功能：获取所有可用的Agent列表

POST /v1/ai/assistant/chat
- 功能：聊天（支持函数调用）
```

**状态**: **❓ 用途不明确（可能用于测试或特殊场景）**

---

## 🎯 Controller 5: AIChatHistoryController（聊天历史管理）

**文件路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/controller/AIChatHistoryController.java`

**基础信息**:
- **路由**: `/v1/ai/chat`
- **标签**: "AI聊天历史管理"

**主要功能**:
- 保存聊天历史
- 查询聊天历史
- 删除聊天历史

**状态**: **✅ 辅助功能接口（支持聊天历史管理）**

---

## 🎯 Controller 6: StreamingAgentController（旧版流式接口）

**文件路径**: `JasEatsChoiceJava/src/main/java/com/xx/jaseatschoicejava/controller/StreamingAgentController.java`

**基础信息**:
- **路由**: `/agent/stream`
- **描述**: 旧版流式Agent接口

**状态**: **⚠️ 旧版接口，可能已被AIStreamController替代**

---

## 📊 Controller 分类汇总

### 按用户类型分类

#### 用户端（3个）
1. **AIStreamController** - 主流式接口（推荐）✅
2. **AIController** - 多功能接口（使用旧Agent）⚠️
3. **AIFunctionCallingController** - 函数调用接口❓

#### 商家端（1个）
4. **MerchantAIController** - 商家流式接口✅

#### 辅助功能（2个）
5. **AIChatHistoryController** - 聊天历史管理✅
6. **StreamingAgentController** - 旧版流式接口⚠️

---

### 按架构层级分类

#### L2架构（新架构）- 2个
1. **AIStreamController** → StreamingIntelligentAssistantAgent（L2）
2. **MerchantAIController** → StreamingMerchantAssistantAgent（L2）

#### 旧版架构（待迁移）- 3个
3. **AIController** → NutritionAiAgent、RecommendationAiAgent（旧版）
4. **AIFunctionCallingController** → 用途不明
5. **StreamingAgentController** → 旧版接口

#### 辅助功能 - 1个
6. **AIChatHistoryController** → 聊天历史管理

---

## 🎯 推荐使用指南

### 用户端推荐

**首选接口**:
```
POST /v1/ai/stream/chat
```

**原因**:
- ✅ 使用L2智能调度Agent
- ✅ 调用7个L1专家Agent
- ✅ 真正的流式响应
- ✅ 自动生成卡片数据
- ✅ 性能优化（避免不必要调用）

### 商家端推荐

**首选接口**:
```
POST /v1/merchant/ai/chat
```

**原因**:
- ✅ 商家经营助手
- ✅ 流式响应
- ✅ 数据分析功能

---

## ⚠️ 架构不一致问题

### 当前存在的问题

1. **新旧架构并存**:
   - 新架构：L2→L1（AIStreamController、MerchantAIController）
   - 旧架构：直接使用旧Agent（AIController）

2. **功能重复**:
   - AIStreamController 和 AIController 都有聊天功能
   - AIStreamController 和 StreamingAgentController 都是流式接口

3. **旧Agent未移除**:
   - NutritionAiAgent
   - RecommendationAiAgent
   - OrderAiAgent
   - MerchantAssistantAgent

---

## 💡 优化建议

### 1. 统一到L2架构

**建议方案**:
- ✅ 保留：AIStreamController（用户端）
- ✅ 保留：MerchantAIController（商家端）
- ⚠️ 废弃：AIController（迁移到AIStreamController）
- ❓ 评估：AIFunctionCallingController（确定用途后决定保留或废弃）
- ❓ 评估：StreamingAgentController（可能是旧版，建议废弃）

### 2. 删除或重构旧版Controller

**AIController重构建议**:
```java
// 移除旧Agent依赖
// NutritionAiAgent → 使用L1的NutritionGuideAgent
// RecommendationAiAgent → 使用L1的DishRecommendationAgent
// 或直接重定向到AIStreamController
```

### 3. 接口整合建议

**保留的核心接口**:
1. `/v1/ai/stream/chat` - 用户端流式聊天
2. `/v1/merchant/ai/chat` - 商家端流式聊天
3. `/v1/ai/chat/*` - 聊天历史管理

**可以废弃的接口**:
1. `/v1/ai/chat` (AIController) - 功能重复
2. `/agent/stream/*` - 旧版接口

---

## 📁 文件清单

| Controller | 路由 | Agent | 架构 | 状态 |
|-----------|------|-------|------|------|
| AIStreamController | `/v1/ai/stream` | StreamingIntelligentAssistantAgent (L2) | L2→L1 | ✅ 推荐 |
| MerchantAIController | `/v1/merchant/ai` | StreamingMerchantAssistantAgent (L2) | L2→L1 | ✅ 推荐 |
| AIController | `/v1/ai` | 旧版Agent | 旧版 | ⚠️ 待重构 |
| AIFunctionCallingController | `/v1/ai/assistant` | 不明 | 不明 | ❓ 待评估 |
| AIChatHistoryController | `/v1/ai/chat` | 无 | 辅助 | ✅ 辅助 |
| StreamingAgentController | `/agent/stream` | 不明 | 旧版 | ⚠️ 待废弃 |

---

## 🎯 总结

**总数**: 6个AI Controller

**推荐使用**（用户端）:
- ✅ **AIStreamController** (`/v1/ai/stream/chat`) - 主流式接口

**推荐使用**（商家端）:
- ✅ **MerchantAIController** (`/v1/merchant/ai/chat`) - 商家流式接口

**需要重构**:
- ⚠️ **AIController** - 使用旧Agent，功能已被新架构替代

**架构目标**:
- 🎯 统一使用L2→L1架构
- 🎯 所有流式接口统一使用SSE
- 🎯 移除旧版Agent和Controller

---

**生成时间**: 2026-04-02
**作者**: Claude
**版本**: 1.0
