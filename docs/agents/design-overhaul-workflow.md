# 全站设计质量提升工作流

本文件是设计评审与逐页改造的**流程契约**。任何人（人或 agent）接活前先读这里，不要在对话里凭记忆复述流程。

## 1. 目标与基调

- 设计要**自己会说话**：用构图、层级、留白、色彩和动效表达信息，不用大段文字说明设计意图。
- 全站一套**核心基调**，页面之间不许各自为政、割裂。
- 加动效是为了提升交互感和生气，不是为了炫技；同一套动效 token，跨页一致。

## 2. 产物与目录

| 产物 | 位置 | 是否入库 |
|---|---|---|
| 页面评审文件（75 份） | `docs/design-review/<role>/<slug>.md` | 入库 |
| 评审汇总索引 | `docs/design-review/index.md` | 入库 |
| 设计基调（北极星 + token） | `docs/design/DESIGN-BASELINE.md` | 入库 |
| 页面截图（75 张，约 32MB） | `output/web-screenshots/` | **不入库**（已 gitignore） |
| 逐页修改方案 | 主 issue 正文 + 各 sub issue 正文 | GitHub |
| 接取总账 | 主 issue 正文的进度表 | GitHub |

`<role>` ∈ `public` / `user` / `merchant` / `admin`；`<slug>` 取 `output/web-screenshots/manifest.json` 里的同名 slug（如 `user/05-order-confirmation`）。

## 3. 阶段

### 阶段 0 · 入场准备

- `make screenshots` 必须能一条命令重拍全部 75 张，退出时自动回收 vite。
- 截图脚本的改动先单独提一个小 PR 入库，作为后续所有 worktree 的前提。
- 截图基线不入库；需要时在自己的 worktree 里跑 `make screenshots` 重拍。

### 阶段 1 · 逐页评审（只读）

- **一个评审 agent 负责一个页面**，产出该页的评审文件；**评审 agent 不修改任何实现代码**。
- 每个评审 agent 必须加载 `$oil-ui` 与 `$huashu-design`；`$design-inspiration` 由汇总 agent 统一做一次，避免 75 个 agent 重复调研。
- 评审文件内容围绕：这页在讲什么、主角/主动作是否成立、层级与构图问题、色彩与字体问题、动效缺失点、与相邻页的不一致点。**少写形容词，多写可执行的判断**。
- 按角色分 4 批启动（public / user / merchant / admin），避免一次开 75 个 agent。

### 阶段 2 · 汇总与基调冻结

- **一个汇总 agent**，输入是 75 份评审文件 + `$design-inspiration` 的调研结果。
- 产出 `docs/design-review/index.md`（索引 + 跨页共性问题）与 `docs/design/DESIGN-BASELINE.md`（北极星一句话 + 色彩/字体/间距/圆角/动效 token + 组件约定 + 正反例）。
- **基调冻结后必须经用户确认**，确认前不放行任何实现任务。

### 阶段 3 · 与用户对齐修改路线

- 拿汇总结果和用户对需求，确认最终修改范围与优先级。
- 输出每个页面的修改方案：改什么、不改什么、参照基调的哪几条。

### 阶段 4 · 逐页实现

- 主 issue 下发**逐页修改 sub issue**，每个 sub issue 正文必须引用 `DESIGN-BASELINE.md` 并写明该页的修改方案。
- 接取、心跳、超时见第 5、6 节。
- 实现 agent **必须开 worktree**，不许在主检出上直接改。

### 阶段 5 · PR 与审查

- 完成即提 PR；**审查 agent 独立看图 + 跑验证**，通过才 merge，不通过打回同一分支继续改。
- merge 后立即清理 worktree 与本地/远端分支（见第 8 节）。

## 4. Issue 模型

- **主 issue**（map）：标 `wayfinder:map` + `design`，正文持有问题陈述、修改路线、进度总账表。
- **子 issue**：GitHub 原生 sub-issues 挂在主 issue 下。
  - 评审阶段：标 `design-review`（按角色 4 个 + 汇总 1 个）。
  - 实现阶段：标 `design-impl`。
  - 一律加 `wayfinder:task`。
- **阻塞关系**：用 GitHub 原生依赖（`blocked_by`）表达；汇总 issue 被 4 个角色评审阻塞，实现 issue 被汇总阻塞。
- **可领取判定**：开放、无开放阻塞、无 assignee。

## 5. 接取协议（防重复）

1. 领取动作**第一笔写**是 `gh issue edit <n> --add-assignee @me`。
2. 紧接着发一条接取评论，格式固定：

   ```
   [CLAIM] agent=<标识> worktree=<绝对路径> branch=codex/<n>-<slug> plan=<要改的文件>
   ```

3. 同一条评论写入主 issue 正文的进度总账表：页面 → 子 issue → 接取人 → 分支 → PR。
4. 进度状态取值：`已接取` → `进行中` → `待审查` → `已合并`；领取后立刻置 `已接取`。

> `--add-assignee` 不是原子操作，极小概率并发抢同一 issue。以"第一笔写 + 谁先发 CLAIM 评论谁得"兜底，发现撞车由后到者让出。

## 6. 心跳与超时

- 接取 agent **每 15 分钟**发一条进度评论，格式：

  ```
  [HEARTBEAT] <ISO8601 时间戳> status=<进行中|待审查> note=<一句话进展>
  ```

- 外部巡检每 15 分钟扫一遍所有开放的 `design-impl` issue：
  - 最后一条评论距今 **< 60 分钟**：正常。
  - 最后一条评论距今 **≥ 60 分钟**：判为超时，移出 assignee、摘 `design-impl`、回 `ready-for-agent`、评论"领取超时，重新放回队列"，允许新 agent 接取。
- 巡检由 `scripts/design-issue-sweep.sh` 执行，挂在 Codex automation 心跳上；脚本是唯一判定来源，不靠人肉记时间。

## 7. Worktree 与分支

- 一个 issue = 一个 worktree + 一个分支，分支名 `codex/<issue>-<slug>`。
- 起隔离验证栈时统一 `docker compose --project-name codex<issue> ...`，收尾 `down -v`。
- `deploy/.env` 是 gitignore 的，worktree 里不存在。直接软链主检出的那份最省事，路径同样被 gitignore，不会污染 diff：
  `ln -sf /Users/nickxiao/jaseats-choice/deploy/.env deploy/.env`。
- **不软链就会掉用例**：`04-order.spec.js` 的优惠券清理用例依赖后端连到真库，缺 `deploy/.env` 时该 spec 稳定失败 1 条；掉的是环境不是代码，别按代码问题排查。
- 提交前 `git checkout -- tests/progress.txt`，任何 PR 都不该含它的 diff。

## 8. 验收标准

单页实现 sub issue 通过需同时满足：

1. `make screenshots` 重拍该页 1440×900；渲染/对比图落 `output/design-l1/<issue>-<名字>.png` 与 `output/design-l2/<slug>-{before,after}.png`（两者都被 gitignore，不入库），PR 正文记录截图路径与渲染场景，由主 agent 看图复核。
2. 组件类 issue（L1）允许临时挂载渲染取图，但**合入前必须撤掉临时挂载**，PR diff 只允许含目标组件文件。
3. 视觉对齐 `DESIGN-BASELINE.md` 的 token，并已按该页修改方案逐条落实。
4. 该页相关的 Playwright spec 全绿（涉及 03/05/06/07/08/09/11~14 的改动必须本地跑对应 spec，不能只看 PR smoke `01/04/10`）。
5. 审查 agent 独立看图通过，且不只看制作过程与代码。
6. 不新增 ESLint error；warning 不强求。

不引入自动视觉 diff——截图基线不入库，跑不通。

## 9. 收尾清理

- merge 后立即：`git worktree remove --force <path>`、删本地与远端分支。
- 确认没有 agent 还在写该 worktree 才动手。
- 关闭 issue 前搜一遍指向它的 `test.fixme` / `skip`，别留孤儿。

## 10. 技术约定

- 动效分两层：高频微交互用 CSS transition/animation + Element Plus 现有过渡；进场 / 展开 / 列表重排用 GSAP，用途边界与降级规则见 `DESIGN-BASELINE.md` §3.5。GSAP 只在 Web 与桌面 renderer 生效，小程序不用。
- 颜色只有一个来源：`apps/client/src/renderer/src/assets/css/tokens.css`。组件写 CSS 变量，不写裸色值；改主色只改这一处，见 `DESIGN-BASELINE.md` §7.1。
- Web 与桌面共用同一份 renderer；新增 `window.api.xxx` 必须同步 `platform/webShim.js`。
- ESLint 只清零 error，warning 不强求。
- **已知共性残留：`#409eff`（Element Plus 默认蓝）被页面写死**。L0 只切干净了全局变量，源码里仍有 71 处裸色值，集中在 `components/chat/dialogs/*`（`GroupOrderDrawer` 12 处最多）、`ContentSourceList.vue`、`WishListAudit.vue`、`chat/MessageSearchPanel.vue` 与若干 `views/admin/*`。这些不单独开 issue，归到 L2 对应页面的 issue 里清掉；排查用 `grep -rn --include='*.vue' '409eff' apps/client/src/renderer/src`。
