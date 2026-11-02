# 公告 Embedding 换模型自动重建索引验收记录（Issue #188）

- 验收日期：2026-11-02
- 分支：`codex/188-embedding-index-rebuild`
- 基线：main @ `f72473b`（含 #187）
- 结论：**自动化通过**。换同维度 Embedding 模型保存后自动全量重建公告索引；重建期间公告检索失败关闭并显示「重建中」；重建失败停在「不可用」并保留原因，可手动重试且不回滚配置；回滚走同一条路径。

## 语义与边界

- **状态机**：`READY`（就绪）/ `REBUILDING`（重建中）/ `UNAVAILABLE`（不可用，带失败原因）/ `NOT_CONFIGURED` / `DISABLED`。
- **触发**：保存或回滚后，若 Embedding 模型或维度变化（向量空间变化），立即同步进入「重建中」，再异步全量重建；只换供应商/地址/密钥不触发重建（#187 的零等待路径保持不变）。
- **失败关闭**：非 `READY` 时 `RagKnowledgeService.search` 直接抛错，AI 工具层返回「公告检索暂不可用」，不返回任何基于旧向量的结果。
- **并发写入**：重建期间公告变更只记账，重建完成后补索引；写入侧对状态做第二次检查，覆盖「记账与重建收尾并发」的窗口。
- **失败与重试**：重建失败不会回滚已切换配置，停在 `UNAVAILABLE` 并保留原因；管理员点「重试重建」重新执行，成功后回到 `READY`。
- **单实例**：沿用 ADR-0007，不做跨实例广播。

## 自动化验收命令与结果

### 后端全量单测

```bash
cd apps/server && ./mvnw test
```

结果：`Tests run: 272, Failures: 0, Errors: 0, Skipped: 20`（Skipped 为需要真实外部凭据的 AI 集成用例），`BUILD SUCCESS`。隔离栈 `--project-name codex188`（MySQL / Redis / Qdrant，宿主机端口 `13306 / 16379 / 16334`）。

覆盖矩阵：

| 验收项 | 状态 | 证据 |
|---|---|---|
| 换同维度模型保存成功 → 自动重建 | 通过 | `AiRuntimeConfigEmbeddingServiceTest#switchingModelSavesAndTriggersIndexRebuild` |
| 只换地址/密钥不触发重建 | 通过 | `#changingOnlyKeyDoesNotTriggerIndexRebuild` |
| 重建期间检索失败关闭 | 通过 | `RagKnowledgeServiceRebuildTest#searchFailsClosedWhileRebuilding` |
| 重建完成回到就绪 | 通过 | `AnnouncementIndexRebuilderTest#successfulRebuildMarksReadyAndDrainsPendingWrites` |
| 重建期间公告写入最终被索引 | 通过 | `RagKnowledgeServiceRebuildTest#announcementChangedDuringRebuildIsIndexedWhenDrained`、`#announcementChangedAfterRebuildIsIndexedDirectly` |
| 重建失败停在不可用并给出原因 | 通过 | `AnnouncementIndexRebuilderTest#failedRebuildStopsUnavailableWithReason` |
| 重试后成功 | 通过 | `#retryAfterFailureReachesReady`、`AiRuntimeConfigEmbeddingServiceTest#manualRebuildReturnsSnapshotWithStatusAndRecordsAudit` |
| 重复请求不并发重建 | 通过 | `#requestRebuildMarksRebuildingBeforeDispatchAndDeduplicates` |
| 回滚走同一路径 | 通过 | `AiRuntimeConfigEmbeddingServiceTest#rollbackRestoresPreviousEmbeddingConfigThroughSamePath`（回滚后按向量空间变化触发重建） |
| 管理端可见状态与原因 | 通过 | `16-admin-ai-release` 断言 READY / REBUILDING / UNAVAILABLE + `statusReason` 原文 |
| 真实 Qdrant 验证旧向量被替换 | 通过 | `AnnouncementIndexRebuildQdrantTest`（Testcontainers）：换模型重建后直接读回点向量，`[1,0,0,0]` → `[-1,0,0,0]` |

### 静态防线

```bash
make check
```

结果：Python 单测 44 条通过；路由最新增 0 条；小程序 API 无缺失；ESLint error 数持平冻结基线（157）。

### 管理端发布流

```bash
cd apps/client && VITE_DEV_HOST=127.0.0.1 VITE_DEV_PORT=5173 npm run dev:web
cd tests && PLAYWRIGHT_BASE_URL=http://127.0.0.1:5173 npx playwright test specs/16-admin-ai-release.spec.js
```

结果：`1 passed`。断言覆盖「切换 Embedding 模型 → `/activate` 请求带新模型且页面显示『重建中』→ 点『重试重建』→ 显示『就绪』」，以及「刷新状态 → 显示『不可用』与失败原因原文」；原有断言全部保留。

## 未验证项与风险

- **未接真实外部 Embedding 端点做端到端冒烟**：本 worktree 没有真实 key / 本地 Ollama；向量替换由「真实 Qdrant + 可切换 embedding 替身」验证，真实端点行为由 `docs/testing/issue-168-embedding-endpoint-e2e-acceptance.md` 的既有结论覆盖。
- **重建耗时线性增长**：公告量大时重建窗口较长，期间公告检索不可用（ADR-0008 已接受，不做双 collection 别名切换）。
- **进程内 pending 记账**：重建期间若后端进程崩溃，pending 集合会丢失；公告表仍是事实来源，可用既有手动全量重建入口恢复，不在本切片做持久化 outbox。
- **多实例**：沿用 ADR-0007 单实例约束，重建状态与 pending 不跨实例共享。

## 环境收尾

- 隔离栈 `codex188` 已 `docker compose --project-name codex188 ... down -v`，容器、网络、数据卷全部删除。
- 运行中的 `jaseats` 开发栈未被触碰。
- `tests/progress.txt` 为运行产物，已还原，不进入提交。
