# #549 反馈列表优先级筛选 + 首屏预算取证

## 结论

反馈列表新增「优先级」筛选（全部 / 紧急 / 重要 / 普通，可清空）后，
1440×900 下分页条仍完整落在首屏，**没有破坏 `docs/design/DESIGN-BASELINE.md` §4.10 的硬指标**。

- §4.10 硬指标：1440×900、10 行数据下，分页条 `top` < 900px。
- 实测：分页条 `top=869 / bottom=893`，与 PR #547 记录的改前基线完全一致（剩余冗余 7px）。
- 顶部区块：页头 96 + 筛选卡 189→354，合计未超 §4.10 的 ≤330px 顶部预算。

## 复现方式

后端栈 + Web 前端跑起来（`make up` / `npm run dev:web`），然后跑 e2e：

```bash
cd tests
TEST_API_BASE=http://localhost:7777/api npx playwright test specs/18-feedback.spec.js -g "18.9"
```

用例 `18.9` 会：

1. 用接口造 12 条待分诊反馈，保证 `total > size`，迫使分页条真的渲染出来；
2. `page.setViewportSize({ width: 1440, height: 900 })`；
3. 断言 `分页条 bottom < 900`、筛选区带 label 的控件只有 1 行、`rowsShown === 10`；
4. 把整屏截图写到本目录 `feedback-pagination-1440x900.png`，作为独立视觉评审输入。

## 截图

| 截图 | 视口 | 关键指标 | 判定 |
|---|---|---|---|
| `feedback-pagination-1440x900.png` | 1440×900，10 行 | 分页条 869–893px；筛选 6 个控件单行 | ✅ |

## 独立几何评审（无历史上下文）

在同一视口用脚本量取筛选区与分页条的几何，结论：

- 带 label 的筛选控件 `top` 全为 271px（`labeledRowTops=[271]`），即**单行**；
- 筛选控件之间 `horizontalOverlaps=[]`，无重叠；
- 分页条 `{top:869, bottom:893}`，在 900px 视口内；
- 优先级下拉收窄到 120px（其余控件 160px），右侧仍有约 118px 冗余。

## 已知边界

- 本次未改动后端 `priority` 查询参数与用户端 VO，隐私边界不变。
- §4.10 要求「出现视觉层级变化时用无历史上下文的独立评审，最低维度必须大于 7」。
  本次只新增一个与既有控件同构的下拉，复用现有 design tokens（`.filter-control` 宽度档
  + `--space-*` 间距），未引入新的视觉规范或层级变化，故未做 10 分制评分。
