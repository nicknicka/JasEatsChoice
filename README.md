# 佳食宜选 JasEatsChoice

围绕「点餐 + 营养管理 + 社交互动 + AI 助手」的多端餐饮系统单仓。
桌面端与 Web 端共用同一份渲染进程代码，后端与数据层用 Docker Compose 一键起。

## 目录结构

| 路径 | 内容 |
|---|---|
| `apps/server` | Spring Boot 后端（认证、订单、推荐、钱包、聊天、AI、商家与管理员后台） |
| `apps/client` | Electron + Vue 3 渲染进程，双目标：桌面端与 Web 版 |
| `apps/miniapp` | UniApp 微信小程序端 |
| `deploy` | Compose 基础文件 + dev/prod overlay、nginx 配置、环境变量模板 |
| `db/init` | Flyway 建表与种子迁移的来源；后端从 classpath 下的 `db/migration` 自动执行，运行时不挂载到容器 |
| `db/migrations` | 历史迁移脚本归档，仅作追溯，不参与启动 |
| `tests` | 15 个 spec / 114 条 Playwright 用例与进度台账 |
| `scripts` | 生成环境变量、等待健康、CI 入口、种子校验等脚本 |
| `docs` | 当前有效的产品、接口、技术、测试、交付文档；`docs/archive` 存历史总结 |

## 快速开始

```bash
make doctor      # 检查 Docker 守护进程、端口、.env、依赖
make env         # 生成 deploy/.env（随机密码与 JWT 密钥）
make deps        # 安装 apps/client 与 tests 的 Node 依赖
make up          # 起 MySQL + Redis + Spring Boot + nginx，并等待全部 healthy
```

栈起来后：

- 接口网关 `http://localhost/api`，后端直连 `http://localhost:7777/api`
- 健康检查 `http://localhost/api/actuator/health`、`http://localhost/nginx-health`
- 前端在宿主机跑开发服务器（保留 HMR）：`cd apps/client && npm run dev:web` → `http://localhost:5173`

演示账号在后端 classpath 下的 `db/migration` 种子迁移里，也是测试夹具依赖的账号：

| 角色 | 账号 | 密码 |
|---|---|---|
| 用户 / 商家 | `17322222222` | `asdasd` |
| 管理员 | `admin` | `admin123` |

## 测试

```bash
make test-smoke   # 只跑 01/04/10 三条冒烟
make test         # 全部 114 条
```

用例打的是 Web 版渲染进程（`PLAYWRIGHT_BASE_URL`，默认 `http://localhost:5173`），
后端地址由 `TEST_API_BASE` 控制。跑之前需要：容器栈 healthy、`npm run dev:web` 在监听、
种子账号可登录。`scripts/ci-e2e.sh` 会把这三件事按顺序做完，CI 与本地都可以直接用它。

## 前端双目标

同一份 `apps/client/src/renderer` 编译成两种产物：

- **桌面端**：`electron.vite.config.js`，页面走 `file:` 协议，后端地址从 `VITE_*` 环境变量读绝对 URL。
- **Web 版**：`vite.web.config.js`（`npm run build:web` 产出 `dist-web/`），同源相对路径 `/api`、`/ws`，
  所以同一个产物挂到任何域名都不用重新构建。

`src/renderer/src/platform/webShim.js` 在浏览器里补齐桌面端 preload 暴露的 `window.api`
（`store` / `window` / `clipboard`，共 6 个方法）。Playwright 注入的是同一个文件，
两边行为不会分叉。

## 环境变量与密钥

- `deploy/.env` 已被 gitignore，只提交 `deploy/.env.example`。
- 数据库口令、Redis 口令、JWT 密钥由 `scripts/gen-env.sh` 用 `openssl rand` 生成。
- 后端不再硬编任何密钥：`application.yml` 里 DB / Redis / JWT / AI / 高德 / 腾讯地图 /
  邮箱全部改成 `${ENV:}` 注入。
- `AI_API_KEY` 留空也能启动：AI 相关 Bean 会降级成占位实现并打 WARN，
  只有真正发起对话时才报错。CI 因此不需要任何密钥。
- 生产 overlay 缺 `DB_URL` / `DB_PASSWORD` 等必填项时直接启动失败，而不是悄悄连到占位域名。

## 部署形态

```bash
# 本地演示（无 SSL，前端走宿主 vite dev）
docker compose --env-file deploy/.env -f deploy/compose.yml -f deploy/compose.dev.yml up -d

# 公网（nginx 挂 dist-web 静态产物 + HTTPS 反代）
bash scripts/gen-env.sh   # 按需改 API_DOMAIN / SSL_CERT_DIR
npm --prefix apps/client run build:web
docker compose --env-file deploy/.env -f deploy/compose.yml -f deploy/compose.prod.yml up -d
```

prod overlay 的证书路径与域名来自 `deploy/.env`；本机没有真实证书时，
用 `make nginx-test` 和 `make config-prod` 做静态校验。

## 小程序

`apps/miniapp` 不进 Compose——小程序必须通过微信开发者工具构建上传，没有容器化形态。

```bash
cd apps/miniapp
npm install
npm run build:mp-weixin    # 产物 dist/build/mp-weixin，用微信开发者工具打开并上传
```

后端地址、WebSocket 地址、分享域名都在 `apps/miniapp/.env.production` 与 `.env.development`
的 `VITE_*` 键里，不再写死在源码中。

## CI

- `.github/workflows/smoke.yml`：PR 与主干推送跑 3 条冒烟。
- `.github/workflows/regression.yml`：每晚定时 + 手动触发跑全量 114 条，上传 HTML 报告。

## 历史仓库

本仓由多个旧仓合并而来。旧的 `JasEatsChoiceFront` 已转为私有归档仓并保留完整 git 历史与
二进制素材（截图、论文附件），本仓不再收录这些内容。
