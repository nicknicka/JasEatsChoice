# 公告 RAG 发布验收记录（Issue #149）

- 验收日期：2026-10-31
- 分支：`codex/149-release-hardening`
- 基线：`codex/144-announcement-rag` @ `5b115fd`
- 结论：**未达到完整发布验收通过**。失败恢复、重建补齐、降级与数据边界已由自动化测试验证；真实智谱 LLM/Embedding 冒烟被有效凭据缺失阻塞。

## 配置前置条件

1. Java 21（本机使用 `/usr/libexec/java_home -v 21`）。
2. `ANNOUNCEMENT_RAG_ENABLED=true`。
3. 有效的 `ZHIPU_API_KEY`，用于 `glm-4-flash` 聊天与 `embedding-3` 向量化。
4. Qdrant gRPC 端口可达；compose 内部服务名 `qdrant`、端口 `6334`。
5. Qdrant collection 由启动初始化器自动创建，默认 `jaseats_announcements`，维度 `2048`。
6. 冒烟只使用临时公告，脚本 `finally` 删除公告并检查向量残留。

## 自动化验证

命令：

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./apps/server/mvnw -f apps/server/pom.xml \
  -Dtest='AnnouncementRagInfrastructureTest,AnnouncementRagStartupTest,RagKnowledgeServiceTest,RagIndexEventListenerTest,AnnouncementKnowledgeToolsTest,AnnouncementRagSyncTest,QdrantCollectionInitializerTest' \
  test
```

结果：`Tests run: 40, Failures: 0, Errors: 0, Skipped: 0`。

Agent 上下文与路由补充命令：

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./apps/server/mvnw -f apps/server/pom.xml \
  -Dtest='SupervisorAgentRoutingTest,AgentChatContextResolverTest,AgentChatContextScopeTest,AgentChatContextEndpointTest' \
  test
```

结果：`Tests run: 18, Failures: 0, Errors: 0, Skipped: 0`。

覆盖：

- 缺 key、Qdrant 不可用、RAG 显式关闭时启动状态正确。
- 公告写入发布同步事件；同步异常被捕获，不影响公告写入。
- 同步失败后全量重建可恢复向量。
- 全量重建清除数据库中不存在的孤儿公告向量；重复重建不引入重复向量。
- 工具异常返回“公告检索暂不可用，请稍后再试。”，不返回内部异常或堆栈。
- 检索结果包含标题与有效期，不包含公告 ID、商家 ID、分数或过滤条件。
- 商家绑定、其他商家、过期/未开始/已下线数据边界由服务层测试覆盖。

补充修复：

- `RagKnowledgeService.rebuildAllAnnouncements()` 现在会先按 `sourceType=announcement` 清理既有向量，再按数据库当前状态重建，避免公告硬删除同步失败后留下孤儿向量。
- RAG 未初始化时全量重建返回 `0`，不再访问空 embedding store。

## 真实端到端冒烟尝试

使用独立 compose 项目 `codex149` 启动 MySQL/Redis/Qdrant，并用本地构建 JAR 启动后端：

- `/api/actuator/health` 返回 `UP`，DB、Redis 正常。
- Qdrant REST `/collections` 确认 `jaseats_announcements` 已创建。
- 使用种子账号完成管理员登录和带上下文的用户/商家登录。
- 临时创建平台公告、本商家公告、其他商家公告、过期公告共 4 条。
- 调用 `/api/agent/supervisor/chat` 分别执行用户端与商家端提问。

结果：

- 4 条临时公告全部通过对应 API 删除，`finally` 清理错误为 0。
- 删除后按临时公告 `contentId` 查询 Qdrant，结果为空，确认向量不残留。
- 用户端与商家端 AI 回答均为空，后端日志显示智谱 Embedding/LLM 调用返回 HTTP 401 语义错误：智谱 `code=1000`（身份验证失败）。
- 因此无法验证“回答包含公告标题和有效期”与“不包含其他商家/过期公告标题”这两个真实 LLM 冒烟断言。

## 未通过项与后续动作

| 项目 | 状态 | 阻塞原因 |
|---|---|---|
| 用户端真实 AI 聊天包含临时平台公告标题与有效期 | 未通过 | 有效智谱凭据不可用 |
| 商家端真实 AI 聊天包含自家公告标题与有效期 | 未通过 | 有效智谱凭据不可用 |
| 真实检索隔离断言（其他商家/过期标题不出现） | 未通过 | 依赖上述 LLM/Embedding 真实调用 |
| 自动化降级、写入、恢复、幂等与隔离测试 | 通过 | 无 |
| 临时数据清理与 Qdrant 向量残留检查 | 通过 | 无 |

获得有效 `ZHIPU_API_KEY` 后，需重跑真实冒烟；只有用户端/商家端回答断言通过后，才可将 Issue #149 视为完整验收通过。

## 冒烟栈收尾证据

- 临时公告数据库计数：`0`（条件：`title like 'codex149%'`）。
- Qdrant `jaseats_announcements` 点数：`0`。
- 独立验证栈已执行 `docker compose --project-name codex149 down -v`；容器、网络和数据卷均已删除。
