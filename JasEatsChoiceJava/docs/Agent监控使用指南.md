# Agent监控使用指南

## 概述

Agent监控系统提供了调用链追踪和性能监控功能，帮助开发者理解和优化AI Agent的运行效率。

## 功能特性

### 1. 调用链追踪 (AgentCallTracer)

记录每次Agent调用的完整路径，包括：
- Agent调用开始
- 请求发送到LLM
- LLM响应接收
- Agent调用完成

### 2. 性能监控 (AgentPerformanceMonitor)

统计Agent的性能指标：
- 总调用次数
- 平均耗时
- 最大耗时
- 总耗时

### 3. 调用链服务 (CallChainTraceService)

提供完整的调用链追踪服务，包括：
- Agent调用链记录
- 工具调用记录
- 调用链报告生成

## 配置

### 启用监控

监控默认启用，可通过配置关闭：

```yaml
# application.yml
agent:
  monitoring:
    enabled: true  # 默认为true
```

### 配置类

监控配置位于：`src/main/java/.../agent/monitoring/AgentMonitoringConfig.java`

```java
@Configuration
@ConditionalOnProperty(
    name = "agent.monitoring.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class AgentMonitoringConfig {
    // 自动注册监听器
}
```

## 监听器

### AgentCallTracer

追踪每次Agent调用的生命周期：

```java
public class AgentCallTracer implements AiServiceListener<AiServiceEvent> {
    // 记录Agent调用开始
    private void onAgentStarted(AiServiceStartedEvent event, int callId);

    // 记录请求发送
    private void onRequestIssued(AiServiceRequestIssuedEvent event, int callId);

    // 记录响应接收
    private void onResponseReceived(AiServiceResponseReceivedEvent event, int callId);

    // 记录Agent调用完成
    private void onAgentCompleted(AiServiceCompletedEvent event, int callId);
}
```

### AgentPerformanceMonitor

统计性能指标：

```java
public class AgentPerformanceMonitor implements AiServiceListener<AiServiceEvent> {
    // 性能统计
    public record PerformanceStats(
        long totalCalls,      // 总调用次数
        long averageDuration, // 平均耗时(ms)
        long maxDuration,     // 最大耗时(ms)
        long totalDuration    // 总耗时(ms)
    ) {}
}
```

## API接口

### 1. 获取性能统计

```http
GET /api/admin/agent-monitoring/performance-stats
```

响应示例：

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "totalCalls": 150,
    "averageDuration": 1234,
    "maxDuration": 5000,
    "totalDuration": 185100
  }
}
```

### 2. 获取调用链报告

```http
GET /api/admin/agent-monitoring/call-chain/{sessionId}
```

响应示例：

```
================================================================================
📊 Agent调用链报告
================================================================================
会话ID: session-1
用户: user123
开始时间: 14:30:25.123
结束时间: 14:30:27.456
总耗时: 2333ms

🤖 Agent调用链:
--------------------------------------------------------------------------------
1. [14:30:25.234] SmartRecommendationAgent
   参数: 我想减肥，推荐一些低卡路里的川菜
2. [14:30:25.567] UserPreferenceAgent
   参数: user123
3. [14:30:26.123] DishRecommendationAgent
   参数: 低卡路里 川菜

🔧 工具调用列表:
--------------------------------------------------------------------------------
1. [14:30:25.345] UserProfileTools.getUserProfile
2. [14:30:25.678] RecommendationQueryTools.queryDishes
3. [14:30:26.234] NutritionAnalysisTools.analyzeDish

💬 最终响应:
--------------------------------------------------------------------------------
根据您的减肥目标和饮食偏好，我为您推荐以下低卡路里川菜：...
================================================================================
```

### 3. 清理过期调用链

```http
POST /api/admin/agent-monitoring/call-chain/cleanup?maxAgeMinutes=60
```

响应示例：

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "message": "清理完成",
    "maxAgeMinutes": 60
  }
}
```

### 4. 获取监控概览

```http
GET /api/admin/agent-monitoring/overview
```

响应示例：

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "performanceStats": {
      "totalCalls": 150,
      "averageDuration": 1234,
      "maxDuration": 5000,
      "totalDuration": 185100
    },
    "monitoringEnabled": true,
    "timestamp": 1711348227000
  }
}
```

## 使用示例

### 1. 查看性能统计

```bash
curl -X GET http://localhost:8080/api/admin/agent-monitoring/performance-stats
```

### 2. 生成调用链报告

```bash
curl -X GET http://localhost:8080/api/admin/agent-monitoring/call-chain/session-1
```

### 3. 清理过期数据

```bash
curl -X POST "http://localhost:8080/api/admin/agent-monitoring/call-chain/cleanup?maxAgeMinutes=60"
```

## 日志输出

### 调用链日志

```log
🤖 [Agent调用开始 #1] 用户消息: 我想减肥，推荐一些低卡路里的川菜
📤 [请求发出 #1] 发送到LLM模型
📥 [响应接收 #1] LLM响应接收
✅ [Agent调用完成 #1] Agent处理完成
```

### 性能监控日志

```log
⏱️ [性能监控 #1] Agent调用开始
⏱️ [性能监控 #1] 耗时: 1234ms | 平均: 1234ms | 最大: 1234ms | 总调用: 1
⏱️ [性能监控 #2] 耗时: 2345ms | 平均: 1789ms | 最大: 2345ms | 总调用: 2
```

### 性能警告

```log
⚠️ [性能警告] Agent调用耗时过长: 5234ms
```

## 性能优化建议

### 1. 监控指标

关注以下性能指标：
- **平均耗时**：建议 < 2000ms
- **最大耗时**：建议 < 5000ms
- **P95耗时**：95%的调用应 < 3000ms

### 2. 优化方向

- **工具调用优化**：减少不必要的数据库查询
- **缓存策略**：使用Redis缓存热点数据
- **LLM调用优化**：减少token使用量，优化提示词

### 3. 异常监控

关注以下异常情况：
- 单次调用耗时 > 5秒
- 错误率突然上升
- 工具调用失败

## 故障排查

### 问题1：监控数据不准确

**症状**：性能统计数据与实际不符

**解决**：
1. 检查监控是否启用：`agent.monitoring.enabled=true`
2. 查看日志中是否有监听器注册成功的信息

### 问题2：调用链报告为空

**症状**：调用链报告显示"调用链不存在"

**解决**：
1. 检查sessionId是否正确
2. 确认调用链未过期（默认60分钟）
3. 查看应用日志确认调用链是否创建

### 问题3：性能数据丢失

**症状**：重启后性能统计数据清零

**说明**：当前版本的性能统计数据存储在内存中，重启后会清零。

**解决方案**：
- 可选：集成持久化存储（Redis/数据库）
- 定期导出性能报告

## 最佳实践

### 1. 定期查看性能报告

建议每天查看一次性能统计，了解系统运行状况：

```bash
# 每天查看性能统计
curl http://localhost:8080/api/admin/agent-monitoring/performance-stats
```

### 2. 定期清理过期数据

建议每天清理一次过期调用链，避免内存占用过高：

```bash
# 清理60分钟前的调用链
curl -X POST "http://localhost:8080/api/admin/agent-monitoring/call-chain/cleanup?maxAgeMinutes=60"
```

### 3. 关注性能瓶颈

当发现性能问题时：
1. 查看调用链报告，找出耗时的Agent和工具
2. 优化数据库查询
3. 添加缓存
4. 优化LLM提示词

### 4. 设置性能告警

建议设置性能告警阈值：
- 平均耗时 > 3秒
- 单次耗时 > 5秒
- 错误率 > 5%

## 后续优化方向

1. **持久化存储**：将监控数据存储到数据库
2. **可视化面板**：开发监控Dashboard
3. **实时告警**：集成邮件/短信告警
4. **性能分析**：添加更多性能指标（P50/P95/P99）
5. **调用链可视化**：生成调用链图表

---

**文档版本**: 1.0
**更新时间**: 2026-03-25
**作者**: Claude
