# 专用 Agent 运行环境与账号隔离

dsh-superagent 在这台机器上以**专用 OS 账号**运行，而不是开发者的账号。目的很直接：即使代理被
诱导或出错，它也读不到你的 home、生产凭据或宿主机 root。

> 边界：这台机器**不部署生产服务**。不要挂载生产数据库、生产上传卷、Docker socket，
> 也不要把 `deploy/.env` 复制过来。

## 账号与目录约定

| 项 | 值 | 说明 |
| --- | --- | --- |
| OS 账号 | `dsh` | 系统账号，`nologin`，**不在 `docker` 组** |
| 账号 home | `/var/lib/dsh-agent` | 权限 700，state / worktree / gh 配置 / 模型 key 都在这里 |
| 日志目录 | `/var/log/dsh-agent` | 仅 `dsh` 可写 |
| 仓库克隆 | `/srv/dsh-agent/repo` | dsh 自己的 clone 与 worktree 根 |
| 面板 | `127.0.0.1:8099` | 只监听回环地址 |

同一个仓库里，人的 clone 与 dsh 的 clone 是两份，互不共享 home。

## 一次性初始化

```bash
sudo scripts/agent-runtime/install-account.sh
```

脚本幂等，只补齐缺失的账号与目录，`chmod 700` 收紧 home。它**不会**代填任何凭据。

初始化后手工做两件事：

1. 用专用 GitHub 账号登录 `gh`，配置落在 dsh 的 home，绝不复用个人登录：
   ```bash
   sudo -u dsh -H env HOME=/var/lib/dsh-agent gh auth login --hostname github.com
   sudo -u dsh -H gh auth status
   ```
2. 把模型 key 写进 `/var/lib/dsh-agent/.config/model.key`（0600，属主 `dsh`）。
   不要写进 `deploy/.env`，不要写进仓库。

## 启动 / 停止 / 状态 / 日志

dsh 用 **profile** 承载：启动就是 `dsh --profile <名字>`，**没有 `dsh panel` 这种子命令**。
`dsh-ctl.sh` 默认就用 `dsh --profile superagent`，可用 `DSH_BIN` / `DSH_PROFILE` / `DSH_START_CMD` 覆盖。

```bash
scripts/agent-runtime/dsh-ctl.sh start    # 默认 dsh --profile superagent
scripts/agent-runtime/dsh-ctl.sh status   # pid + 监听地址（非回环会判失败）
scripts/agent-runtime/dsh-ctl.sh logs     # tail 日志
scripts/agent-runtime/dsh-ctl.sh stop
```

升级/安装的构建由 dsh-superagent 负责：`pnpm install && pnpm build`，再 `dsh plugin --profile superagent add "link:<repo>"`。

`dsh-ctl.sh` 始终以 `HOME=/var/lib/dsh-agent` 调用，不继承调用者的 home。

## 隔离验证清单

```bash
sudo scripts/agent-runtime/verify-isolation.sh
```

逐条检查并在失败时退出非零：

- [ ] `dsh` 账号存在，且**不在** `docker` 组。
- [ ] `/var/lib/dsh-agent` 权限为 `700`。
- [ ] `dsh` 读不到开发者 home 下的文件（脚本放一个 canary 再尝试读取）。
- [ ] `dsh` 读不到 `deploy/.env`。
- [ ] `dsh` 访问不到 `/var/run/docker.sock`。
- [ ] `/var/lib/dsh-agent/.ssh` 下没有私钥。
- [ ] 面板端口只在 `127.0.0.1` / `::1` 监听。

非 root 运行时，可读性探测会标记 `SKIP`；要用 `sudo` 复跑才能得到完整结论。

## 与反馈闭环的关系

- 反馈桥接（#536）只用专用 GitHub 账号在目标仓库开 Issue / PR，人体量的是 `dsh` 这个账号的提交与评论。
- `ready-for-agent` 只是队列标签，不参与准入；准入与选任务由 dsh 的 Source 与 Policy 决定，起步用 `observe`。
- dsh 不改标签、不部署、不碰生产数据；合并按 Delivery requirements 的 auto-merge 条件，由指定的 merge 账号执行，未点名或条件不满足时人工合并。
- 决策见 `docs/adr/0012-agent-admission-and-merge-follow-dsh.md`。

## 运行时状态页（#538 / #552）

后台「Agent 运行时」页面**只读**，对 `admin:agent:runtime` 开放；接口只有一个：

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| GET | `/admin/agent/runtime` | 主机侧健康（GitHub / DSH profile / 隔离账号）+ 硬边界 |

- **agent 的模型 / 密钥 / 策略配置在 DSH（另一个运行时）**，JAS 不再保存，也没有写接口；产品 AI 的模型 key 走后台「系统设置 → AI 配置」（`/admin/settings/ai`）。
- `no-deploy` / `no-production-data` 两条硬边界固定开启，页面无法关闭；**合并按 dsh 的 Delivery requirements**（指定 merge 账号，条件为 PR 就绪 + 必需审批覆盖 head commit + 增删合计 <200 行），未点名或条件不满足时人工合并；硬边界的真实生效依赖 DSH 部署与 agent 账号权限。

## 版本锁定与升级 / 回退（#541）

锁定值在 `scripts/agent-runtime/versions.env`：`DSH_SUPERAGENT_COMMIT`、`DSH_VERSION`、
`NODE_MAJOR`、`PNPM_VERSION`。改它必须走升级流程，不允许"顺手升一下"。

```bash
# 升级：备份 -> 升级到锁定版本 -> 探针（健康/认证/Source/Policy/模型）-> 失败自动回退
sudo scripts/agent-runtime/upgrade.sh

# 手工回退：只恢复升级前备份，不做原地降级（新版迁移过的 state 旧版打不开）
sudo scripts/agent-runtime/rollback.sh /var/log/dsh-agent/upgrade-backups/<ts>

# 切换模型/插件前存快照，切回旧配置
sudo scripts/agent-runtime/snapshot-config.sh model-switch
sudo scripts/agent-runtime/restore-config.sh /var/log/dsh-agent/config-snapshots/<ts>-model-switch
```

- `versions.env` 里 dsh-superagent / DSH 仍是 `CHANGE_ME_*` 时，`upgrade.sh` 直接拒绝启动。
- 升级、模型切换、回退都有可执行脚本；验证记录写到 `$DSH_LOG_DIR/upgrade-records/`，
  模板见 `docs/agent-runtime/upgrade-record-template.md`。
- 升级只在无开放 Ticket、无进行中派单的维护窗口进行。
