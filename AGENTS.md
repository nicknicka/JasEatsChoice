# AGENTS.md

## 语言

始终用简体中文回答。描述性概念用中文，代码名、路径、接口名、错误信息保留原文，不要为了"看起来专业"塞英文术语。

## 凭据红线

- 所有真实密钥、密码只写在 `deploy/.env`（已 gitignore，永不入库）。仓库里只提交 `deploy/.env.example`，值全是占位符。
- 不要把凭据写进 Java 代码、`application*.yml` 的默认值、文档、测试或提交信息。`application.yml` 只允许 `${ENV:}` 形式的注入，缺省值必须是无意义的占位。
- 数据库连接参数（账号、密码、库名）来自 `deploy/.env`，不要在别处再抄一份。应用账号是 `DB_USERNAME`，它不是 root。
- 外部服务密钥（智谱、高德、腾讯地图、QQ 邮箱授权码）由所有者在控制台吊销后重新申请，本地填 `deploy/.env`，CI 用 GitHub Secrets。

## 模块地图

| 目录 | 内容 |
|---|---|
| `apps/server` | Spring Boot 3.2.5 / Java 21 / MyBatis-Plus，Netty WebSocket 独立端口 |
| `apps/client` | Electron 主进程 + preload + renderer；renderer 同时是 Web 版前端 |
| `apps/client/src/renderer/src/platform/webShim.js` | 桌面 `window.api` 的 Web 垫片，Playwright 也复用它 |
| `apps/miniapp` | UniApp 微信小程序，**不容器化**，走微信开发者工具上传 |
| `deploy` | compose base/dev/prod、nginx、`.env.example` |
| `db/init` | `01_schema.sql` + `02_seed.sql`，挂到 `/docker-entrypoint-initdb.d` |
| `db/migrations` | 历史脚本，仅作追溯，不参与启动 |
| `tests` | Playwright 用例与台账，独立 `package.json` |
| `scripts` | `gen-env.sh`、`wait-healthy.sh`、`ci-e2e.sh` |
| `docs` | 当前有效的产品/架构/测试文档，`archive/` 存历史总结 |

技术栈：后端 SpringBoot + MyBatis-Plus + MySQL + Redis + Netty；桌面/前端 Electron + Vue 3 + Element Plus；小程序 UniApp。

## 已排除的功能

不做线下门店收银、不做供应链仓储管理、不做第三方配送调度（只集成配送状态查询）。加需求前先确认不在这三条里。

## 常用命令

```
make doctor        # Docker/端口/.env/依赖体检
make check         # 静态防线：单测 + 前后端路由漂移 + lint 冻结基线（不起容器，约 1 分钟）
make env           # 生成 deploy/.env（随机库密码 + JWT 密钥）
make deps          # 装 apps/client 与 tests 的 node_modules
make up            # 起四件套并等健康
make web           # 构建 Web 版静态产物到 apps/client/dist-web
make test          # 全部 Playwright 用例
make desktop       # Electron 打包回归（build:mac）
bash scripts/ci-e2e.sh [spec ...]   # 一条命令从空仓到跑完用例
```

## 几条真实的坑

- **不要在 `@Bean` 工厂方法里无条件抛异常。** 智谱没配 key 时整个 Spring 上下文会起不来，所以 `LangChain4jConfig` / `ZhipuClientConfig` 在 key 为空时返回占位实现，调用 AI 接口才报错。`ZHIPU_API_KEY` 留空也必须能启动。
- **接口路径以运行中的 OpenAPI 为准。** 后端 `context-path` 是 `/api`，登录实际是 `/v1/users/login` 和 `/admin/login`，不是 `/v1/user/login`。改前端调用前先读 `/api/v3/api-docs`。
- **Web 与桌面共用同一份 renderer。** 新增 `window.api.xxx` 调用，必须同步 `platform/webShim.js`，否则 Web 版和 CI 直接挂。路由已按 `file:` 协议在 hash / history 间自动切换，不要另加判断。
- **种子数据是用例的前提。** `db/init` 只在空卷执行，改种子后要 `make reset-db`。测试账号 `17322222222 / asdasd`、`admin / admin123` 必须存在且密码哈希与明文匹配，否则用例会整片红。
- **`tests/progress.txt` 是用例台账**，跑测试会写它，不要手工改成"通过"。
- macOS 自带 `/bin/bash` 是 3.2，没有关联数组；脚本里别用 `declare -A`。

## Agent skills

### Issue tracker

Issue 与 spec 都放 GitHub Issues（私有仓 nicknicka/JasEatsChoice），一律用 `gh` CLI 操作。见 `docs/agents/issue-tracker.md`。

### Triage labels

五个标准 triage 角色与本仓标签同名（needs-triage / needs-info / ready-for-agent / ready-for-human / wontfix）。见 `docs/agents/triage-labels.md`。

### Domain docs

single-context：词表在根目录 `GLOSSARY.md`，架构决策在 `docs/adr/`（当前 3 篇 ADR）。见 `docs/agents/domain.md`。

## 代码规范

遵循 Vue 3 组合式 API 与 Element Plus 的既有用法，保持结构清晰；ESLint 只清零 error，warning 不强求。不要为"以后可能用到"写抽象，不要加永远不会触发的兜底判断。
