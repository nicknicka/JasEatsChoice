# 0012 — 准入、选任务与合并交给 dsh-superagent，JAS 不再自设闸

## 背景

JAS 的反馈闭环把「已接受 + 平台系统问题 + 规格完整」的反馈变成脱敏 GitHub Issue，再交给 dsh-superagent（另一个运行时）处理。第一版落地时，JAS 侧自己定了两道闸：必须授权维护者在该 Issue 评论 `@agent`，且 PR 一律人工审查、人工合并；`ready-for-agent` 只当队列标签。

dsh-superagent 本身已有一套完整机制：

- **准入**由 Source 与 Policy 决定：消息是否点到代理、发送者在不在名单、Source 的准入表，以及 `accept mode` 三档 `off` / `observe` / `auto`。
- **选任务**由 Policy rules 决定：至少命中一条启用规则才建 Ticket，Mandate 记录命中的规则。
- **合并**由 Delivery requirements 决定：点名一个单独的 merge CLI 账号，条件为 PR 就绪、必需审批覆盖 head commit、增删合计少于 200 行。

JAS 侧自设的闸与这套机制重复，而且覆盖不到 dsh 内部的 Eval、Mandate 与交付判断。

## 决策

1. **触发、选任务、合并三层都按 dsh 官方机制走**，JAS 不再自设闸。JAS 只负责反馈闭环、Issue 桥接与只读观测。
2. **触发起步用 `observe`**：先只评估并记录 Decision，不接活；复核过一批 Decision 后再考虑切 `auto`。
3. **合并按 Delivery requirements 的行数阈值**（增删合计 < 200 行）执行。本仓**不建 merge 专用账号、不在 `main` 设必需审批**，合并门槛为 CI 必需状态检查（`backend` / `e2e` / `miniapp` / `static / checks`）全部通过，即 CI 绿即可合并；审批条件因未设必需审批而为空集。
4. **`ready-for-agent` 降为普通标签**：只表示队列可领取，不参与 dsh 准入。
5. **不变的部分**：隐私红线不变（真实反馈正文、附件、身份、订单/支付标识都不入 Git、Issue、文档或模型上下文）；`no-deploy` / `no-production-data` 硬边界不变。

## 替代方案

- **保留 JAS 侧自设闸**：与 dsh 的 Policy 职责重复，且无法约束 dsh 内部的 Eval 与 Mandate。
- **只在 JAS 侧做批准**：JAS 看不到 dsh 的准入输入，批准会变成无法校验的形式动作。
- **建 merge 专用账号并设必需审批**：本仓为单人 + agent，设必需审批后作者本人 approve 不算，自己开的 PR 无法合并，需第二 reviewer；徒增账号维护与信任面，收益不抵成本，故不采用。

## 后果

- `docs/agent-runtime/` 下的使用手册、README、受控试点三份文档同步改写；`#543` 的验收标准随之调整。
- 启用官方路径的仓库外前置为两件：打开仓库 auto-merge、给 `main` 加分支保护与必需状态检查（已于 2026-11-13 完成）。merge 专用账号经评估不建，合并走 CI 绿即合。
- 不建 merge 专用账号，agent 不额外获得 merge CLI 权限，信任面以 dsh 运行账号本身的权限为限。
- JAS 仓库是公开仓库。放宽 Source 观察范围或未列名者的准入，会让任意 GitHub 用户都能驱动代理；本轮不改这两项。
