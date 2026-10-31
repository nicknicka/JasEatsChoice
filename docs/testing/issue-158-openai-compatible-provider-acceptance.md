# OpenAI 兼容 provider 验收记录（Issue #158）

- 验收日期：2026-10-31
- 分支：`codex/158-openai-compatible`
- 基线：`codex/151-ai-runtime-config` @ `b6efe7d`
- 结论：**通过**。新增 `openai-compatible` 适配器可由管理员热切换；使用真实 Trae/Sub2API 的
  `glm-5.3-solo` 完成候选测试、激活、连通性与 AI 聊天冒烟；原有智谱适配器行为未回归。

## 变更概述

- 新增 `OpenAiCompatibleModelClientFactory`（provider 代码 `openai-compatible`，显示名 `OpenAI 兼容`），
  基于 `langchain4j-open-ai` 构建聊天、Supervisor、视觉与流式客户端。
- `pom.xml` 增加 `dev.langchain4j:langchain4j-open-ai:1.12.1`。
- `ZhipuChatModelFactory` 不再直接注入单个 `AiModelClientFactory`，改为按当前激活快照从
  `AiProviderRegistry` 解析适配器，并去掉 `@ConditionalOnProperty(ai.provider=zhipu)`，
  使 `ChatModelProvider` 对任意已注册 provider 都可用。

## 配置约定（通用变量，无厂商前缀）

`AI_PROVIDER` / `AI_API_KEY` / `AI_MODEL` / `AI_SUPERVISOR_MODEL` / `AI_VISION_MODEL` /
`AI_BASE_URL` / `AI_TIMEOUT` / `AI_TEMPERATURE`。`AI_PROVIDER=openai-compatible` 时即使用本适配器，
`AI_BASE_URL` 指向 OpenAI Chat Completions 兼容地址（如 `http://host.docker.internal:3065/v1`）。

## 验收矩阵

| 验收项 | 状态 | 证据 |
|---|---|---|
| registry 暴露 `openai-compatible` | 通过 | `AiProviderRegistryTest`；`GET /admin/settings/ai/current` 的 `providers` 含 `openai-compatible` / “OpenAI 兼容” |
| 支持 AI_BASE_URL/模型/超时/温度 | 通过 | `OpenAiCompatibleModelClientFactoryTest.environmentConfig_usesGenericConfig`；候选测试使用的正是这些字段 |
| 聊天/Supervisor/视觉/流式由该适配器构建 | 通过 | 候选测试 `pingRoles` 覆盖四种角色；`capabilities` 四项能力均为 true |
| 运行时 key 优先于 `AI_API_KEY` | 通过 | `OpenAiCompatibleModelClientFactoryTest.runtimeKey_winsOverGenericKey` |
| key 不入 Git/DB/日志/响应 | 通过 | 激活与快照响应断言不含明文；快照仅返回 `configured` + `fingerprint` |
| 候选测试/激活/能力状态/回滚可用 | 通过 | 下方真实冒烟；回滚在 `openai-compatible` 两个版本间成功 |
| 真实 Trae/Sub2API `glm-5.3-solo` 冒烟 | 通过 | 候选测试“连接成功”、AI 聊天返回真实菜谱 |
| 智谱适配器不回归 | 通过 | 78 条 AI 相关单测全绿；整栈以 `AI_PROVIDER=zhipu` 正常启动 |
| 不依赖外部网络的单元测试 | 通过 | 新增 3 个测试类，共 7 条用例，全部通过 |

## 真实 provider 冒烟（独立 compose 栈）

栈：`docker compose --project-name codex158 ... up -d --build mysql redis qdrant spring-boot`，
后端发布在 `127.0.0.1:17777`。key 在子进程内从 `cc-switch` 读取，只经内存传给 HTTP 请求体，
未写入命令参数、日志或仓库。

1. 候选测试 `POST /api/admin/settings/ai/test`（provider `openai-compatible`，模型 `glm-5.3-solo`，
   base URL `http://host.docker.internal:3065/v1`）→ `{"success":true,"message":"连接成功"}`。
2. 激活 `POST /api/admin/settings/ai/activate` → 版本 2，`provider=openai-compatible`，
   `keyStatus.configured=true`，响应不含 key 明文。
3. 当前快照 `GET /api/admin/settings/ai/current` → `capabilities.available/textChat/streaming/vision = true`，
   `providers = [openai-compatible, zhipu]`。
4. 连通性 `POST /api/admin/settings/ai/connectivity-test` → `{"success":true,"message":"连接成功"}`。
5. AI 聊天 `POST /api/v1/ai/recipe` `{"foodName":"番茄炒蛋","goal":"低油少盐"}` → HTTP 200，
   返回真实生成的菜谱（含食材、步骤、营养），证明聊天链路走的是该适配器。
6. 回滚 `POST /api/admin/settings/ai/rollback`（在 `openai-compatible` 的两个版本之间）→ 回到上一版
   provider/模型，key 未泄漏。

冒烟结束后 `docker compose ... down -v` 清理容器、网络与数据卷。

> 备注：key 指纹（非密钥本体）在快照中展示，用于管理员核对，不构成泄漏。

## 单元测试

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./apps/server/mvnw -f apps/server/pom.xml \
  -Dtest='AiApiKeyResolverTest,AiRuntimeKeyStoreTest,AiConfigAuditServiceImplTest,AiConfigHistoryStoreImplTest,AiModelHotSwitchServiceTest,AiRuntimeConfigReleaseAcceptanceTest,AiRuntimeConfigRollbackServiceTest,AiRuntimeConfigServiceTest,AiRuntimeKeyRotationServiceTest,AdminAiConfigControllerTest,AdminAiConfigPermissionTest,AdminAiModelHotSwitchControllerTest,AdminSystemConfigAiIsolationTest,DynamicAiModelRegistryTest,ZhipuRuntimeModelKeyTest,AnnouncementRagConfigRuntimeKeyTest,ZhipuChatModelFactoryTest,OpenAiCompatibleModelClientFactoryTest,AiProviderRegistryTest' \
  test
```

结果：`Tests run: 78, Failures: 0, Errors: 0, Skipped: 0`。

## 集成的回归修复

新增第二个 `AiModelClientFactory` bean 后，`ZhipuChatModelFactory.chatModelProvider(...)` 对单个
`AiModelClientFactory` 的按类型注入变为歧义，整栈首次启动报
`required a single bean, but 2 were found`。修复：改为注入 `AiProviderRegistry`，按当前激活快照的
provider 解析适配器，并移除 zhipu 专属 `@ConditionalOnProperty`，使 `ChatModelProvider` 与具体厂商解耦。

## 范围与未决

- 单实例热切换语义不变，多实例广播不在范围，见 `docs/adr/0007-ai-runtime-config-single-instance.md`。
- `mvn test` 全量仍会因缺少 DB 环境变量（`${DB_USERNAME}`）在需要真实数据库的 `@SpringBootTest` 下失败；
  与本次改动无关：基线 `220` 条用例为 `5 failures / 43 errors`，本次为 `227` 条用例、同样的 `5 / 43`，
  多出的 7 条为本切片新增且全部通过。
