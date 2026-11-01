# AI 热加载发布验收记录（Issue #156）

- 验收日期：2026-10-31（自动化与 UI）；2026-11-01（真实外部 provider 冒烟）
- 分支：`codex/156-ai-release-acceptance`（自动化）；`codex/156-real-provider-smoke`（真实冒烟）
- 基线：自动化 `codex/151-ai-runtime-config` @ `9998d1a0446e610cfdc0e5429552b216e08e4bc9`；真实冒烟 main @ `160e194`
- 结论：**通过**。自动化恢复、回滚、脱敏与 UI 发布流通过；真实外部 provider（`openai-compatible`，Trae/Sub2API 网关 `glm-5.3-solo`）端到端冒烟通过。

## 配置前置条件

1. Java 21；Node/npm 可用。
2. 单后端实例部署，管理员账号拥有 `admin:setting:ai` 权限。
3. 非密钥配置保存在 MySQL；运行时 key 写入 `AI_ACTIVE_KEY_FILE` 指向的受限文件，compose 使用 `ai-runtime-keys:/app/runtime/secrets/ai` 持久卷。
4. `AI_API_KEY` 作为 bootstrap key 供删除运行时 key 后回退。
5. 真实 provider 冒烟使用本机 Trae/Sub2API 网关（OpenAI Chat Completions 兼容，`http://host.docker.internal:3065/v1`）与 `glm-5.3-solo`；key 从 cc-switch 的 `Sub2API-Trae` 读取，仅经内存传给 HTTP 请求体。

## 自动化验收命令与结果

### 后端恢复与发布行为

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./apps/server/mvnw -f apps/server/pom.xml \
  -Dtest='AiApiKeyResolverTest,AiRuntimeKeyStoreTest,AiConfigAuditServiceImplTest,AiConfigHistoryStoreImplTest,AiModelHotSwitchServiceTest,AiRuntimeConfigReleaseAcceptanceTest,AiRuntimeConfigRollbackServiceTest,AiRuntimeConfigServiceTest,AiRuntimeKeyRotationServiceTest,AdminAiConfigControllerTest,AdminAiConfigPermissionTest,AdminAiModelHotSwitchControllerTest,AdminSystemConfigAiIsolationTest,DynamicAiModelRegistryTest,ZhipuRuntimeModelKeyTest,AnnouncementRagConfigRuntimeKeyTest' \
  test
```

结果：`Tests run: 67, Failures: 0, Errors: 0, Skipped: 0`。

覆盖矩阵：

| 验收项 | 状态 | 证据 |
|---|---|---|
| 无效模型不能激活 | 通过 | `invalidModelAndProviderFailurePreserveCurrentConfigAndRuntimeKey` |
| 无效 key 不覆盖当前 key | 通过 | 同上用例使用独立 invalid key，并断言旧 key 文件和版本不变 |
| provider 测试失败保留旧配置 | 通过 | 候选测试抛错后版本、DB 写入次数和活动快照均不变 |
| 容器重启后恢复 key 与活动配置 | 通过 | `hotSwitchRestartAndRollbackRestoreRuntimeKeyAndActiveConfig` 使用新的 registry/service 模拟重启；真实容器重启见下节 |
| 删除运行时 key 回退 `AI_API_KEY` | 通过 | `deletedRuntimeKeyFallsBackToEnvironmentKeyAfterRestart` |
| API 响应无 key 明文 | 通过 | `currentAndSaveResponsesDoNotEchoKeyMaterial` |
| DB/历史无 key 明文 | 通过 | release 用例序列化非密钥配置与 history rows 并断言无 key 值 |

### 前端构建

```bash
npm --prefix apps/client run build:web
```

结果：构建成功。既有 Sass legacy API 和 chunk size warning 不属于本切片改动。

### 管理端 UI 发布流

```bash
cd tests && PW_CHANNEL=chrome npx playwright test 16-admin-ai-release.spec.js --project=chromium
```

结果：`1 passed`。覆盖 AI tab 查看、候选测试、保存并激活、版本/模型热切换、key 指纹脱敏、清空 key 输入、回滚上一版，并检查页面和 admin API 响应不含输入 key。该用例使用 API contract mock 驱动浏览器流；真实 provider 行为由下节的真实冒烟与后端测试覆盖。

页面同时显示“首期仅支持单后端实例，运行时配置和 key 不会广播到其他实例”。

### 静态防线

```bash
make check
```

结果：44 个 Python 单测通过；OpenAPI 路由 450 条、新增差异 0；小程序 API 调用 253 处无缺失；ESLint error 数持平基线 157。

## 密钥红线检查

- 提交内容仅包含固定假 key 字符串用于行为测试；真实 provider key 未写入命令输出、报告或仓库。
- 自动化阶段 `deploy/.env` 中 `ZHIPU_API_KEY` 为空、`AI_API_KEY` 不存在；真实冒烟阶段改用 cc-switch 持有的 Trae/Sub2API key，不落 `deploy/.env`。
- 运行时 key 只写入 `AiRuntimeKeyStore` 指向的受限文件；`AiModelCandidateDTO` 不持久化，快照只返回 configured/fingerprint。
- release acceptance 测试断言非密钥 DB 配置、历史 JSON、当前快照均不包含 runtime/bootstrap key。

## 真实外部 provider 冒烟（2026-11-01）

环境：独立 compose 栈 `codex156`，镜像内容等同于 main @ `160e194`；网关 `http://host.docker.internal:3065/v1`；模型 `glm-5.3-solo`；key 从 cc-switch 的 `Sub2API-Trae` 读取，只在子进程内存中传入请求体，未写入命令行参数、日志或仓库。

| # | 步骤 | 结果 |
|---|---|---|
| 1 | 管理员登录 | 200，SUPER_ADMIN |
| 2 | 初始 `GET /admin/settings/ai/current` | `provider=zhipu`、`model=glm-4-flash`、`version=1`、`keyConfigured=false` |
| 3 | `POST /admin/settings/ai/models` | 200，返回 18 个真实模型，含 `glm-5.3-solo` |
| 4 | `POST /admin/settings/ai/test`（候选 `openai-compatible` + `glm-5.3-solo`） | 200，`连接成功` |
| 5 | `POST /admin/settings/ai/activate` | 200，`provider=openai-compatible`、`model=glm-5.3-solo`、`version=2`、`keyConfigured=true`、fingerprint `26677139451b` |
| 6 | 激活后 `current` 与能力状态 | `available/textChat/streaming/vision` 全为 true；响应不含 key 明文 |
| 7 | `POST /admin/settings/ai/connectivity-test` | 200，`连接成功` |
| 8 | `POST /v1/ai/recipe` 真实聊天 | 200，返回真实生成菜谱（含食材与步骤），响应不含 key 明文 |
| 9 | 重启 `spring-boot` 容器 | 健康恢复 `UP` |
| 10 | 重启后 `current` | 仍为 `openai-compatible`/`glm-5.3-solo`、`version=2`、fingerprint `26677139451b` 不变 |
| 11 | 重启后再次真实聊天 | 200，返回真实生成菜谱 |
| 12 | 再激活第二版（`deepseek-v4.1-flash-solo`） | 200，`version=3`；`previous-summary` 变为 `glm-5.3-solo` v2 |
| 13 | `POST /admin/settings/ai/rollback` | 200，回到 `glm-5.3-solo`，新版本号 4 |
| 14 | 回滚到上一版为 `zhipu` 的场景 | 被拒绝（返回空），原因是缺少可用的智谱 key，符合「回滚不能恢复已被清除或无效的 key」 |

密钥不落盘核查（均以真实 key 字符串为匹配目标，匹配结果只输出布尔值）：

| 检查项 | 结果 |
|---|---|
| 工作树文件含 key 明文 | 0 个文件 |
| Git 全历史含 key 明文 | false |
| Redis 键含 key 明文 | 0 个键 |
| `sys_system_config`(ai) + `ai_config_history` 含 key 明文 | false |
| 运行时密钥文件权限 | `-rw-------`（600，仅应用用户可读） |

冒烟结束后执行 `docker compose --project-name codex156 down -v`，容器、网络与数据卷均已删除。

## 未通过项

无。原「真实外部 provider 端到端冒烟」阻塞项已由上述真实调用消除，未运行项未被报告为通过。

## 范围说明

- UI E2E 仍使用 API contract mock 驱动浏览器流；真实 provider 行为由真实冒烟与后端测试覆盖。
- 多实例广播和共享 key 明确不在首期范围，见 `docs/adr/0007-ai-runtime-config-single-instance.md`。
