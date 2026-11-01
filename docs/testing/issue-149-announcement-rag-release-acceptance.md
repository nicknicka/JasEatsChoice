# 公告 RAG 发布验收记录（Issue #149）

- 验收日期：2026-10-31（自动化与边界）；2026-11-01（真实端到端冒烟）
- 证据分支：`codex/149-release-hardening`（自动化）；`codex/149-aligned-token-and-real-smoke`（真实冒烟）
- 基线：自动化 `codex/144-announcement-rag` @ `5b115fd`；真实冒烟 main（含 #166/#167/#168）
- 结论：**通过**。失败恢复、重建补齐、降级与数据边界由自动化测试验证；用户端与商家端真实 AI 聊天冒烟通过，回答包含公告标题与有效期，且其他商家与过期公告未出现。

## 配置前置条件

1. Java 21（本机使用 `/usr/libexec/java_home -v 21`）。
2. `ANNOUNCEMENT_RAG_ENABLED=true`。
3. 有效的 `AI_API_KEY`，用于 `glm-4-flash` 聊天与 `embedding-3` 向量化；优先级为 `AI_API_KEY`。
4. Qdrant gRPC 端口可达；compose 内部服务名 `qdrant`、端口 `6334`。
5. Qdrant collection 由启动初始化器自动创建，默认 `jaseats_knowledge_v1`，维度 `2048`。
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

## 真实端到端冒烟（2026-11-01）

### 配置前置条件

1. 本地 OpenAI 兼容 embedding 端点：Ollama `bge-m3`（1024 维），监听 `11434`。
2. 聊天模型：OpenAI 兼容网关（`glm-5.3-solo`）。
3. 隔离验证栈 `codex149`（MySQL / Redis / Qdrant v1.17.0），后端以宿主机进程运行。
4. `CAPTCHA_TEST_FIXED_CODE_ENABLED=true` 配合固定验证码，供自动化登录使用。
5. 运行时 key 由 `AI_ACTIVE_KEY_FILE` 指向的文件提供；`AI_API_KEY` 保持为空。

### 冒烟数据（均为临时构造）

| 类型 | 标题 | 可见范围 |
|---|---|---|
| 平台公告 | `codex149 平台满减活动` | 用户端 + 商家端 |
| 本商家公告 | `codex149 本店满赠` | 仅该商家 |
| 其他商家公告 | `codex149 其他店活动` | 仅其他商家（不应出现） |
| 过期公告 | `codex149 已过期活动` | 已过期（不应出现） |

平台公告通过管理端 API 真实创建（触发自动索引）；其余为构造数据，经全量重建写入向量库。索引结果中过期公告被正确排除（`isIndexable` 阶段即过滤）。

### 结果

**用户端**（普通用户身份提问「现在有什么平台满减活动？」）：

- 回答包含《codex149 平台满减活动》及有效期 `2026-11-01 00:00 至 2026-12-31 23:59`
- 未出现本店公告、其他商家公告、过期公告标题

**商家端**（商家身份提问「我店里现在有什么活动？」）：

- 回答包含《codex149 本店满赠》与《codex149 平台满减活动》及各自有效期
- 未出现其他商家公告、过期公告标题

### 顺带修复的阻断缺陷

真实冒烟最初无法进行：密码登录签发的是自定义 base64 token（`phone:timestamp:jaseatschoice`），而 `JwtAuthenticationFilter` 与 `AgentChatContextResolver` 都只按标准 JWT 解析（微信登录路径本来就是标准 JWT）。结果是走密码登录的用户既无法通过认证，也无法为 AI 聊天建立用户/商家上下文，检索工具直接返回「公告检索暂不可用」。

已修复：`UserController.generateJWTToken` 改为调用 `jwtUtil.generateToken(userId, phone)`，与微信登录、认证过滤器、聊天上下文解析三处对齐。

### 清理与收尾

- 临时公告删除后执行全量重建，向量库中无 `codex149` 残留（仅剩种子公告）
- 隔离栈 `codex149` 已 `docker compose --project-name codex149 down -v`
- Ollama 服务已停止；`bge-m3` 模型保留在 `~/.ollama/models`

## 未通过项

无。
