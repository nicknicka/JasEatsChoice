# 公告 Embedding 在线切换（零等待路径）验收记录（Issue #187）

- 验收日期：2026-11-02
- 分支：`codex/187-embedding-hot-switch`
- 基线：main @ `46aa35a`
- 结论：**自动化通过**。Embedding 配置已并入 AI 运行时配置快照，独立密钥槽位、保存前真实调用与维度校验、检索侧按当前生效配置解析都已落地；换模型在本切片明确拒绝并提示需要重建索引。

## 范围与语义

本切片只交付「零等待路径」：只改供应商、服务地址与密钥时保存即生效，不触发索引重建。换 Embedding 模型会改变向量空间，需要通过重建索引才能安全切换，因此本切片直接拒绝保存并提示需要重建索引；自动重建与「重建中」状态由后续切片（#188）补齐。维度与现有 collection 不一致同样拒绝，并提示需要新建 collection。

## 配置前置条件

1. 隔离验证栈：`--project-name codex187`（MySQL / Redis / Qdrant），宿主机端口映射到 `13306 / 16379 / 16334`，避免与正在运行的 `jaseats` 栈冲突。
2. `deploy/.env` 由 `scripts/gen-env.sh` 生成一次性随机凭据，未写入仓库。
3. `deploy/compose.yml` 新增 `AI_EMBEDDING_ACTIVE_KEY_FILE=/app/runtime/secrets/ai/embedding-key`，与对话模型 key 同卷（`ai-runtime-keys`）、同权限、独立文件。

## 自动化验收命令与结果

### 后端全量单测

```bash
cd apps/server && ./mvnw test
```

结果：`Tests run: 259, Failures: 0, Errors: 0, Skipped: 20`（Skipped 为需要真实外部凭据的 AI 集成用例），`BUILD SUCCESS`。

覆盖矩阵：

| 验收项 | 状态 | 证据 |
|---|---|---|
| 快照包含 Embedding 配置，管理端可读 | 通过 | `AiRuntimeConfigEmbeddingServiceTest#snapshotExposesEmbeddingConfigAndKeyStatus`、`AdminAiModelHotSwitchControllerTest#embeddingConfigIsAcceptedAndReturnedWithoutEchoingKey` |
| 独立运行时密钥槽位，轮换不影响对话密钥 | 通过 | `AiRuntimeConfigEmbeddingServiceTest#savingNewAddressAppliesToRetrievalWithoutTouchingChatKey`、`#embeddingKeyRotationDoesNotTouchChatKeySlot` |
| 保存前连通性 + 一次真实 Embedding 调用 | 通过 | `AnnouncementRagEmbeddingRuntimeTest#validateRunsRealEmbeddingCallWithMatchingConfiguredDimension`、`#validateWrapsConnectivityFailure` |
| 维度与现有 collection 不一致拒绝并提示新建 collection | 通过 | `AnnouncementRagEmbeddingRuntimeTest#validateRejectsConfiguredDimensionDifferentFromMeasured`；真实 Qdrant 维度比对见 `QdrantCollectionInitializerTest` |
| 免密钥端点（Ollama 类）可保存生效 | 通过 | `AnnouncementRagStartupTest#keylessOpenAiCompatibleEndpointPassesConfigurationGate`、`AnnouncementRagEmbeddingRuntimeTest#missingKey_returnsDisabledModelAndNoUsableConfig` |
| 保存后检索立即使用新端点 | 通过 | `RagKnowledgeService` 改为每次向 `AnnouncementRagInfrastructure` 解析 store；`AnnouncementRagEmbeddingRuntimeTest#runtimeConfigIsResolvedOnEachCall` |
| 只改地址/密钥不触发索引重建 | 通过 | 切换路径只做一次探测 + 重建 store 引用，不调用全量重建；`AiRuntimeConfigEmbeddingServiceTest#savingNewAddressAppliesToRetrievalWithoutTouchingChatKey` |
| 管理端 Embedding 区块 + 移除旧说明 | 通过 | `tests/specs/16-admin-ai-release.spec.js` 断言区块字段、请求体带 `embedding`、旧文案计数为 0 |
| 变更写入审计、重启恢复 | 通过 | `AiRuntimeConfigEmbeddingServiceTest#auditRecordsEmbeddingChange`、`#restartRecoversPersistedEmbeddingConfig` |
| 未配置 Embedding 仍可启动、只降级检索 | 通过 | `AnnouncementRagStartupTest#missingEmbeddingKey_startsInNotConfiguredState` |
| 换模型拒绝保存并保留旧配置 | 通过 | `AiRuntimeConfigEmbeddingServiceTest#switchingModelIsRejectedUntilIndexRebuildExists`、`#failedEmbeddingValidationDoesNotSwitchConfig` |
| GLOSSARY 术语与 ADR-0008 | 通过 | 首个提交 `docs(adr): 落地 ADR-0008 与公告 Embedding 在线切换术语 (#187)` |

### 静态防线

```bash
make check
```

结果：Python 单测 44 条通过；路由最新增 0 条；小程序 API 无缺失；ESLint error 数持平冻结基线（157）。

### 管理端发布流

```bash
# 仅启动 Vite（16.1 为 API contract mock，不依赖后端）
cd apps/client && VITE_DEV_HOST=127.0.0.1 VITE_DEV_PORT=5173 npm run dev:web
cd tests && PLAYWRIGHT_BASE_URL=http://127.0.0.1:5173 npx playwright test specs/16-admin-ai-release.spec.js
```

结果：`1 passed`。断言覆盖 Embedding 区块渲染、`/test` 与 `/activate` 请求体带完整 `embedding`、改服务地址后请求体携带新地址、旧文案不再出现。

## 未验证项与风险

- **未接真实外部 Embedding 端点做切换冒烟**：本 worktree 没有真实 key / 本地 Ollama。真实维度比对由「mock Embedding 模型 + 真实 Qdrant（Testcontainers）」覆盖。
- **未跑全栈 E2E**：16.1 按设计使用 API contract mock；前后端字段一致性由控制器层用例断言请求体反序列化与响应序列化。
- **运行中「未配置 → 已配置」的首次初始化**：`AnnouncementRagInfrastructure#embeddingStore()` 在 store 为空时按当前配置重建，失败时仍按失败关闭处理；重试策略与「重建中」状态归 #188。
- **回滚到不同 Embedding 模型**同样被拒绝（与保存同一条路径），#188 接自动重建后放开。

## 环境收尾

- 隔离栈 `codex187` 已 `docker compose --project-name codex187 ... down -v`，容器、网络、数据卷全部删除。
- 运行中的 `jaseats` 开发栈未被触碰。
- `tests/progress.txt` 为运行产物，已还原，不进入提交。
