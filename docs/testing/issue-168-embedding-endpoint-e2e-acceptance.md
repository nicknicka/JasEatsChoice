# 公告 Embedding 真实端点端到端验收记录（Issue #168）

- 验收日期：2026-11-01
- 分支：`codex/168-embedding-e2e`
- 基线：main @ `dc0a728`（含 #166、#167）
- 结论：**通过**。在真实 OpenAI 兼容 embedding 端点（本地 Ollama `bge-m3`）上完成公告写入 → 向量索引 → 检索命中，并确认维度不匹配会被明确挡住。验证过程中发现并修复了一个阻断检索的真实缺陷。

## 配置前置条件

1. 本地 OpenAI 兼容 embedding 端点：Ollama，监听 `11434`，模型 `bge-m3`（1024 维）。Ollama 的 OpenAI 兼容接口支持 `dimensions` 参数（实测可返回 1024/512 维）。
2. 隔离验证栈：`codex168`（MySQL / Redis / Qdrant），Qdrant 为 `qdrant/qdrant:v1.17.0`。
3. 后端以宿主机进程运行（避免本机 Docker 镜像构建耗时），配置：

```
AI_EMBEDDING_PROVIDER=openai-compatible
AI_EMBEDDING_BASE_URL=http://127.0.0.1:11434/v1
AI_EMBEDDING_MODEL=bge-m3
AI_EMBEDDING_DIMENSIONS=1024
```

4. `AI_API_KEY` 保持为空（智谱客户端使用占位实现），运行时 key 由 `AI_ACTIVE_KEY_FILE` 指向的文件提供。

## 验证步骤与结果

| # | 步骤 | 结果 |
|---|---|---|
| 1 | 后端以 openai-compatible embedding 启动 | 健康检查 `UP`，DB/Redis 正常 |
| 2 | Qdrant 集合创建 | `jaseats_knowledge_v1`，**维度 1024**（与配置一致） |
| 3 | 管理员创建临时公告 | 创建成功，`status=active`，有效期 2026-11-01 至 2026-12-31 |
| 4 | 公告写入后自动索引 | Qdrant 点数 **0 → 1**，无需人工触发 |
| 5 | 向量 payload 检查 | 含 `title`、`startAt`、`endAt`、`status=active`、`audienceTags=all`、`sourceType=announcement` |
| 6 | 检索命中（真实 embedding + 真实 Qdrant + 产品检索代码） | 返回 1 条：`title=codex168 临时活动公告`，有效期 `2026-11-01T00:00 ~ 2026-12-31T23:59:59` |
| 7 | 维度不匹配 | 配置改为 2048 后启动，被明确挡住：`公告 RAG collection jaseats_knowledge_v1 向量维度为 1024，与配置的 embedding 维度 2048 不兼容` |
| 8 | 维度匹配后链路 | 1024 维配置 + 1024 维集合，链路正常（步骤 1–6） |
| 9 | 临时数据清理 | 公告记录 0 条、Qdrant 点数 0 |

检索返回的内容通过 AI 助手工具层格式化后只包含「公告标题 + 原文 + 有效期」，不包含公告 ID、向量分数或 Qdrant point ID（见 `AnnouncementKnowledgeTools#format`）。

## 验证中发现的缺陷与修复

**缺陷：公告检索在真实 Qdrant 上不可用（P1）**

- 现象：`RagKnowledgeService.search()` 抛 `Unsupported value type for IsLessThanOrEqualTo condition. Only supports Number`
- 根因：`visibilityFilter()` 对 `startAt`/`endAt`（Datetime 类型 payload）使用字符串做 range 条件，而 langchain4j-qdrant 的 `QdrantFilterConverter` 只支持数值 range
- 已确认 Qdrant 服务端本身支持 datetime range（原生 API 可正常过滤），问题在 Java 客户端转换层
- 影响面：真实环境下所有公告检索都会失败，异常被 AI 工具层捕获后对用户表现为「公告检索暂不可用」
- 为什么此前未发现：#149 的自动化测试使用 mock `embeddingStore`，filter 只被构造不执行；真实冒烟又被智谱 key 401 阻塞

**修复**：有效期过滤从向量检索条件移到应用层。索引阶段本就只写入「当前生效」的公告，因此该过滤属于双保险，移到应用层不改变正确性。同时把单次召回上限提高到 50，为过滤留出余量。

## 环境限制（如实记录）

在 `@SpringBootTest` 上下文中调用同一检索路径时，gRPC 会报 `INTERNAL: Encountered end-of-stream mid-frame`，且 Qdrant 服务端无对应请求日志。

已排除的假设：

- Qdrant 服务本身正常（REST 与裸 gRPC 调用均成功）
- langchain4j 的 `QdrantEmbeddingStore` 本身正常（builder 版与构造函数版、带 filter 与不带 filter 均成功）
- 与 devtools 的 restart classloader 无关（禁用后仍复现）

因此本条的检索验证改用「真实 Ollama embedding + 真实 Qdrant + 产品 `RagKnowledgeService`（包内构造器）」组合完成，绕开 Spring 测试上下文。该 gRPC 现象仅在测试上下文出现，未影响独立运行的应用（索引写入经同一路径成功）。

## 未通过项

无功能项未通过。上述 gRPC 现象属于测试上下文的限制，已如实记录，未按通过处理。

## 环境收尾

- 临时公告与向量数据已清理（公告 0 条、Qdrant 0 点）
- 隔离栈 `codex168` 已 `docker compose --project-name codex168 down -v`，容器、网络、数据卷全部删除
- Ollama 服务已停止；`bge-m3` 模型保留在 `~/.ollama/models`（约 1.2GB），后续如需复跑可执行 `ollama serve` 与 `ollama pull bge-m3`
