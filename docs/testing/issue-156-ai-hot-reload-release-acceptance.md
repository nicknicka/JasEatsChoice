# AI 热加载发布验收记录（Issue #156）

- 验收日期：2026-10-31
- 分支：`codex/156-ai-release-acceptance`
- 基线：`codex/151-ai-runtime-config` @ `9998d1a0446e610cfdc0e5429552b216e08e4bc9`
- 结论：**自动化恢复、回滚、脱敏与 UI 发布流通过；真实外部 provider 端到端冒烟未通过**。`deploy/.env` 中 `ZHIPU_API_KEY` 为空，`AI_API_KEY` 也不存在，因此未运行真实 provider 调用，也未将该阻塞项报告为通过。

## 配置前置条件

1. Java 21；Node/npm 可用。
2. 单后端实例部署，管理员账号拥有 `admin:setting:ai` 权限。
3. 非密钥配置保存在 MySQL；运行时 key 写入 `AI_ACTIVE_KEY_FILE` 指向的受限文件，compose 使用 `ai-runtime-keys:/app/runtime/secrets/ai` 持久卷。
4. `AI_API_KEY` 作为 bootstrap key 供删除运行时 key 后回退。
5. 真实 provider 冒烟还需要有效的 `AI_API_KEY`，并且模型可在智谱 provider 的已知/自定义模型中使用。

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
| 容器重启后恢复 key 与活动配置 | 通过 | `hotSwitchRestartAndRollbackRestoreRuntimeKeyAndActiveConfig` 使用新的 registry/service 模拟重启 |
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

结果：`1 passed`。覆盖 AI tab 查看、候选测试、保存并激活、版本/模型热切换、key 指纹脱敏、清空 key 输入、回滚上一版，并检查页面和 admin API 响应不含输入 key。该用例使用 API contract mock 驱动浏览器流；真实 provider 行为由后端测试覆盖，但不等于真实外部 provider 端到端冒烟。

页面同时显示“首期仅支持单后端实例，运行时配置和 key 不会广播到其他实例”。

### 静态防线

```bash
make check
```

结果：44 个 Python 单测通过；OpenAPI 路由 448 条、新增差异 0；小程序 API 调用 253 处无缺失；ESLint error 数持平基线 157。

## 密钥红线检查

- 提交内容仅包含固定假 key 字符串用于行为测试；真实 `deploy/.env` 值未被读取到命令输出、报告或仓库。
- `deploy/.env` 中 `ZHIPU_API_KEY` 长度为 0，`AI_API_KEY` 不存在；无真实 provider key 可泄漏。
- 运行时 key 只写入 `AiRuntimeKeyStore` 指向的受限文件；`AiModelCandidateDTO` 不持久化，快照只返回 configured/fingerprint。
- release acceptance 测试断言非密钥 DB 配置、历史 JSON、当前快照均不包含 runtime/bootstrap key。

## 外部 provider 冒烟

尝试在子进程内读取 `deploy/.env` 并调用智谱 chat completions；只输出状态与模型响应，不打印或复制 key。

结果：进程检测到 `ZHIPU_API_KEY` 为空，冒烟在发出 HTTP 请求前阻塞。因此以下项未通过且不能视为通过：

- 真实 provider 保存候选配置后的连通性确认。
- 真实 provider 下的端到端热切换。

## 未解决问题

1. 需要有效 `AI_API_KEY` 后重跑真实 provider 端到端冒烟；只有外部调用确认通过，Issue #156 才可视为完整发布验收通过。
2. UI E2E 使用 API contract mock；获得真实 key 后可在独立 compose 栈上复跑同一页面流以移除该限制。
3. 多实例广播和共享 key 明确不在首期范围，见 `docs/adr/0007-ai-runtime-config-single-instance.md`。
