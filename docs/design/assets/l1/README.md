# L1 组件视觉基准

这 10 张图在 main `5631120`（L1 全部合并后）渲染，是 `DESIGN-BASELINE.md` 的**可对照实物**。
L2 逐页改造时，接活 agent 不必起 dev server 就能知道「合规的卡片/页头/表单长什么样」。

| 文件 | 组件 | 场景 |
|---|---|---|
| `265-appstatcard-grid.png` | AppStatCard | 图标+趋势 / 图标无趋势 / 无图标带趋势 / 纯数值，四变体同排 |
| `266-apppageheader-full.png` | AppPageHeader | 标准：标题 + extra 内联标签 + 副标题 + 右侧操作 |
| `266-apppageheader-no-subtitle.png` | AppPageHeader | 无副标题 |
| `266-apppageheader-compact.png` | AppPageHeader | compact（merchant/admin；标题 `--text-h2`、内边距 `--space-4`） |
| `268-emptystate-statustag-band.png` | AppEmptyState + StatusTag | 空状态 + 六档语义色带 |
| `269-rowactions-default.png` | RowActions | 常态「查看 + 更多」 |
| `269-rowactions-menu.png` | RowActions | 菜单展开，danger 项红字 |
| `269-rowactions-confirm.png` | RowActions | danger 项 `ElMessageBox` 二次确认 |
| `270-appformcontainer-auth.png` | AppFormContainer | auth 档 400px 窄列 |
| `270-appformcontainer-settings.png` | AppFormContainer | settings 档 720px 两栏、操作靠右 |

重拍方式：把本目录图片与 `docs/design/DESIGN-BASELINE.md` 对照，任一不符即为回归。

已知偏差（待修，见 issue）：`269-rowactions-confirm.png` 里「确认执行」按钮为品牌橙，
危险操作确认的主按钮应使用 `--danger`。
