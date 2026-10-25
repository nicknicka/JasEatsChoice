# SupervisorAgent API 接口文档

## 概述

SupervisorAgent 提供统一的 L3 监督代理接口，自动智能路由到合适的 L2 领域 Agent。

## 基础信息

**Base URL**: `/api/agent/supervisor`

**Content-Type**: `application/json`

**认证**: 需要JWT Token（根据项目配置）

---

## API 接口

### 1. 统一聊天接口（推荐）

**接口描述**: SupervisorAgent会自动分析用户问题，智能路由到合适的L2 Agent

**请求方式**: `POST`

**接口路径**: `/api/agent/supervisor/chat`

**请求参数**:

```json
{
  "message": "用户消息内容",
  "userId": "用户ID（可选）",
  "sessionId": "会话ID（可选）"
}
```

**响应示例**:

```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": "根据您的需求，我为您推荐以下健康川菜：..."
}
```

**使用示例**:

```bash
curl -X POST http://localhost:8080/api/agent/supervisor/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "推荐一些低卡路里的川菜",
    "userId": "user123"
  }'
```

---

### 2. 带用户上下文的聊天接口

**接口描述**: 使用用户ID进行个性化查询和推荐

**请求方式**: `POST`

**接口路径**: `/api/agent/supervisor/chatWithContext`

**请求参数**:

```json
{
  "message": "根据我的历史记录推荐菜品",
  "userId": "user123",
  "sessionId": "session-456"
}
```

**响应示例**:

```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": "根据您的历史订单和偏好，我为您推荐：..."
}
```

**使用示例**:

```bash
curl -X POST http://localhost:8080/api/agent/supervisor/chatWithContext \
  -H "Content-Type: application/json" \
  -d '{
    "message": "根据我的历史记录推荐菜品",
    "userId": "user123"
  }'
```

---

### 3. GET方式快速聊天

**接口描述**: 快速聊天接口，适合简单场景

**请求方式**: `GET`

**接口路径**: `/api/agent/supervisor/chat`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| message | String | 是 | 用户消息 |
| userId | String | 否 | 用户ID |

**响应示例**:

```json
{
  "success": true,
  "code": "200",
  "message": "成功",
  "data": "宫保鸡丁约300卡路里，主要营养成分：..."
}
```

**使用示例**:

```bash
# 带用户ID
curl "http://localhost:8080/api/agent/supervisor/chat?message=宫保鸡丁有多少卡路里？&userId=user123"

# 不带用户ID
curl "http://localhost:8080/api/agent/supervisor/chat?message=你好"
```

---

## 使用场景

### 场景1：菜品推荐

**请求**:
```json
{
  "message": "推荐一些低卡路里的川菜",
  "userId": "user123"
}
```

**说明**: SupervisorAgent会路由到 `SmartRecommendationAgent`

---

### 场景2：营养咨询

**请求**:
```json
{
  "message": "宫保鸡丁有多少卡路里？主要营养成分是什么？",
  "userId": "user123"
}
```

**说明**: SupervisorAgent会路由到 `HealthManagementAgent`

---

### 场景3：订单处理

**请求**:
```json
{
  "message": "我想点一份宫保鸡丁，配米饭，送到学校",
  "userId": "user123"
}
```

**说明**: SupervisorAgent会路由到 `FullOrderAgent`

---

### 场景4：综合查询（多Agent协作）

**请求**:
```json
{
  "message": "我想减肥，推荐一些健康的菜，并告诉我营养分析",
  "userId": "user123"
}
```

**说明**: SupervisorAgent会协调 `SmartRecommendationAgent` 和 `HealthManagementAgent`

---

## 错误处理

### 错误响应格式

```json
{
  "success": false,
  "code": "500",
  "message": "处理失败: 具体错误信息",
  "data": null
}
```

### 常见错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 500 | 服务器内部错误 | 检查日志，确认LLM服务正常 |
| 400 | 请求参数错误 | 检查请求格式是否正确 |
| 401 | 未授权 | 确认JWT Token有效 |

---

## 性能指标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 平均响应时间 | < 2秒 | 简单查询 |
| P95响应时间 | < 3秒 | 95%的请求 |
| P99响应时间 | < 5秒 | 99%的请求 |
| 并发支持 | 100+ QPS | 取决于LLM限流 |

---

## 最佳实践

### 1. 使用POST接口进行复杂对话

推荐使用 `/chat` 或 `/chatWithContext` POST接口，支持更复杂的请求体。

### 2. 提供用户ID以获得个性化体验

始终提供 `userId` 参数，SupervisorAgent可以根据用户历史提供更精准的推荐。

### 3. 合理设置消息长度

建议单次消息长度 < 500字符，过长消息可能导致响应时间增加。

### 4. 处理流式响应

如需流式响应，请使用现有的 `/v1/ai/stream/chat` 接口。

### 5. 监控和日志

- 所有请求都会记录详细日志
- 可通过 `/api/admin/agent-monitoring/overview` 查看监控数据
- 可通过 `/api/admin/agent-monitoring/call-chain/{sessionId}` 查看调用链

---

## 前端集成示例

### JavaScript/Fetch

```javascript
async function chatWithSupervisor(message, userId) {
  const response = await fetch('/api/agent/supervisor/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token
    },
    body: JSON.stringify({
      message: message,
      userId: userId
    })
  });

  const result = await response.json();

  if (result.success) {
    return result.data;
  } else {
    throw new Error(result.message);
  }
}

// 使用示例
chatWithSupervisor('推荐一些健康的菜', 'user123')
  .then(response => console.log(response))
  .catch(error => console.error(error));
```

### Axios

```javascript
import axios from 'axios';

async function chatWithSupervisor(message, userId) {
  try {
    const response = await axios.post('/api/agent/supervisor/chat', {
      message: message,
      userId: userId
    }, {
      headers: {
        'Authorization': 'Bearer ' + token
      }
    });

    return response.data.data;
  } catch (error) {
    console.error('请求失败:', error.response.data);
    throw error;
  }
}
```

---

## 监控端点

### 查看性能统计

```bash
curl http://localhost:8080/api/admin/agent-monitoring/performance-stats
```

### 查看调用链报告

```bash
curl http://localhost:8080/api/admin/agent-monitoring/call-chain/session-1
```

### 查看监控概览

```bash
curl http://localhost:8080/api/admin/agent-monitoring/overview
```

---

## 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0.0 | 2026-03-25 | 初始版本，支持SupervisorAgent基础功能 |

---

## 相关文档

- [SupervisorAgent使用指南](./SupervisorAgent使用指南.md)
- [Agent监控使用指南](./Agent监控使用指南.md)
- [AgenticScope状态共享指南](./AgenticScope状态共享指南.md)
- [监督代理架构设计](../监督代理架构设计.md)

---

**文档版本**: 1.0
**更新时间**: 2026-03-25
**作者**: Claude
