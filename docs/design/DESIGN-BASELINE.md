# JasEatsChoice 全站设计基调（冻结）

- 适用范围：public（4 页）、user（34 页）、merchant（16 页）、admin（21 页），共 75 个已评审页面；小程序（`apps/miniapp`）不在 75 页截图范围内，但按 §3.6 收敛到同一主色。
- 依据：`docs/design-review/{public,user,merchant,admin}/*.md` 全部 75 份评审 + `docs/agents/design-overhaul-workflow.md`。
- 性质：本文只冻结视觉与交互基线，不含实现代码；落地时按本文 token 与组件约定改造现有 Vue 3 + Element Plus 组件。
- 一句话原则：**先统一语言，再谈风格。** 任何角色、任何页面都不允许出现第二套主色、第二套字阶或第二套状态色。
- 第二轮冻结（2026-11）在草案基础上追加四条硬约束：颜色收敛到唯一 `tokens.css`（§2、§7）、卡片按 L0–L3 表面阶梯给质感（§3.4）、动效引入 GSAP 但限定用途（§3.5）、小程序主色并入 `--brand-500`（§3.6）。

## 0. 北极星

**全站用一套暖橙品牌色 + 一套中性表面 + 一套语义状态色讲同一件事：暖橙代表「佳食宜选」是谁和「主操作在这里」，中性色承载信息，绿/红/黄/蓝只做成功、危险、警示、信息的语义标签——public 与 user 更舒展，merchant 与 admin 更紧凑，但紧凑只压缩间距与字阶档位，不换颜色、不换字体、不换图标体系。**

这句话同时约束三件事：

1. 品牌识别只由暖橙承担，任何页面不得用绿色满屏、蓝色按钮或渐变图标块替代品牌色。
2. 角色差异只允许出现在密度、内容宽度和布局结构上，不允许出现在色彩、字体、状态语义和交互反馈上。
3. 主操作每屏最多一个实心主按钮；其余动作一律降级为描边、文字或危险色。

## 1. 参考取证

本轮按 `design-inspiration` 技能"一轮最多查 3 个站、只交参数、每条带出处"的规则执行，实际抓取以下 3 个站点的具体条目。以下参数均为抓取到的原文内容，不是转述想象。

### 1.1 Refero Styles · Wise DESIGN.md

- 出处：<https://styles.refero.design>（抓取时该 style 记录展示的是 Wise 的 DESIGN.md 导出，标题 "Wise deep moss with lime voltage"）
- 色彩角色分工（原文规则）：主品牌深色 `Forest Ink #163300` 承担导航、正文、深色区块；强调绿 `Lime Voltage #9fe870` 只做低频强调，原文明确写 **"Do not promote it to the primary CTA color"**；另有 `Spruce #054d28`、`Linen Mist #e2f6d5`、`Signal Blue #0b4c72`、`Alarm Red #cb272f`、`Obsidian #0e0f0c`、`Charcoal #454745`、`Slate #6a6c6a`、`Pebble #868685`、`Fog #e8ebe6`、`Paper #ffffff`。
- 字体与字阶：正文/UI 用 `Inter`，字号档位 `12 / 14 / 16 / 18 / 20 / 22 / 25 / 36 / 45 / 61px`；字距随字号收紧：`-0.003em@12px → -0.011em@36px`。
- 形状：卡片与输入 `10px` 圆角，按钮与导航段为 pill。
- 本轮采用：**借鉴它的"色彩角色分工 + 字阶/字距结构 + 10–12px 卡片圆角"，不采用它的深绿主色。** 理由是 JasEatsChoice 的既有品牌识别（Logo、侧栏选中态、01–03 公共端）已经是暖橙，评审也把"绿色满屏"（public/04）判为孤例。原文"强调色不得夺主操作"这条规则，正是本基线裁决蓝色不得做主按钮的依据。

### 1.2 Component Gallery · Table / Empty state

- 出处：<https://component.gallery/components/table/> 与 <https://component.gallery/components/empty-state/>
- Table 条目：收录 73 个真实设计系统实例，含 Ant Design、Atlassian Design System（dynamic table、table tree 两种解法）、Auro、Backpack 等；该站把表格定义为"大量数据的行列展示，含排序/筛选时称 data table"。
- Empty state 条目：收录 16 个实例，含 Ant Design Empty、Atlassian Empty state、Geist Empty State、Primer Blankslate；原文定义空状态是"当前视图无数据可显示时的提示，通常包含一个替代动作（例如换个搜索词）"。
- 该站自身设计参数：交互圆角 `6px`，过渡 `0.2s cubic-bezier(.3,0,.5,1)`，hover 缩短到 `0.1s`，字距 `0.01em`。
- 本轮采用：表格管线固定为"筛选 → 合计 → 列 → 行操作"；空状态固定为"图标 + 一句中文说明 + 一个替代动作"。这两条正好对应评审中最高频的两类问题（admin 表格列折行/危险操作混同、13 个页面空状态缺失或英文）。

### 1.3 Kinetics · Card Resize / Tab Pill Glide

- 出处：<https://kinetics.colorion.co/effects/card-resize/> 与 <https://kinetics.colorion.co/effects/animated-tabs/>
- Card Resize：参数 `spring(320, 24)`，实现 `cubic-bezier(0.34, 1.56, 0.64, 1)`，高度展开时长 `0.5s`，次要文字 `opacity 0.3s ease 0.1s` 延迟淡入。
- Tab Pill Glide：`--glide: cubic-bezier(0.16, 1, 0.3, 1)`，指示器移动 `0.4s`；同页 hover 态 `background / border-color / color 0.15s ease`。
- 该站明确提示实现时必须"保持可见焦点态、为手势提供按钮替代、尊重 reduced-motion"。
- 本轮采用：把 `cubic-bezier(0.16, 1, 0.3, 1)` 作为标准出场曲线，把 `cubic-bezier(0.34, 1.56, 0.64, 1)` 限定为「一次性进场/展开」的弹性曲线；时长从示例的 `0.4–0.5s` 收敛到 `150–240ms`，避免后台频繁交互显得拖沓。引入 GSAP 时沿用这两条曲线（`CustomEase` 或等价 `ease`），不为 GSAP 另立一套动效语言。

## 2. 冲突裁决

评审中存在互相冲突的建议，以下是最终裁决及理由。后文 token 与组件约定均以此为准，不接受"两套并存"。

| 议题 | 采纳 | 否决 | 理由 | 被否一侧怎么处理 |
|---|---|---|---|---|
| 公共端第 4 页（商户注册）的视觉语言 | 纳入 public 01–03 的暖橙单列体系 | 绿底满屏 + 白卡 + Element 默认蓝按钮 + 衬线标题 | 75 份评审中 01–03、user、merchant 侧栏、admin 侧栏选中态均以暖橙为品牌识别，04 是唯一绿底孤例；绿色在语义色表中已被"成功"占用，不能再当品牌底色 | 底色/主色/按钮/标题字体按本基线重做；绿色仅保留给审核通过、营业中、支付成功等成功标签 |
| 主操作色 | 品牌暖橙 `--brand-500 #ED6A42` | 蓝色主按钮、绿色主按钮 | 主操作必须全站同一色，才能让用户跨角色识别"点这里"；蓝色属信息语义，绿色属成功语义 | 蓝色只保留信息提示、消息气泡、可点击的次要文字链接；绿色只保留成功标签与成功反馈 |
| 语义色映射（来自商家端建议） | 品牌橙 = 主操作/品牌，绿 = 成功，红 = 异常/危险，黄 = 警示/待处理，蓝 = 信息 | 把蓝提升为"并列主操作色"或普通链接强调色 | 采纳商家端的四语义映射，但必须加一条硬约束：蓝色不得出现在实心主按钮上，也不得作为正文链接的强调色。否则商家首页会再次出现"深绿编辑按钮 + 蓝色订单链接"式的多主色 | 蓝色限定在 info 标签、通知、消息气泡、次级文字链接与信息类图标 |
| 金额颜色 | 金额用中性深色 + 符号 + 文字角色（收入/支出/退款） | 红色金额、红绿蓝三色同时编码金额 | 金额本身不是状态；用颜色编码金额会与状态色冲突，且 admin 05/06/12/13 的红色金额已被明确判为问题 | 正负用 `+`/`-` 符号，方向用「收入/支出/退款」文字；仅在退款/失败等确有语义时才用 danger 标签，不给数字本身染色 |
| 订单状态色 | 「已完成/已通过/成功」一律成功绿；「进行中/已上架」一律信息蓝；「待处理/待审核」一律警示黄；「已取消/已拒绝/失败」一律危险红；「草稿/已下架/已停用」一律中性灰 | 同一状态在不同页面用不同颜色（如已完成在列表绿、详情蓝） | 状态色是跨角色、跨页面的公共词汇，必须一一映射，不能按页面重新解释 | 各页面按状态表替换；旧页面中"已完成用蓝""列表绿详情蓝"一律改回成功绿 |
| 结构性 emoji | Element Plus 线性图标；确无合适图标时去掉图标只留文字 | emoji 承担字段标签、统计卡图标、菜单图标等结构性职责 | emoji 在不同系统渲染不一致，且与经营/资金/后台场景的专业度冲突 | 9 份评审点到的页面逐处替换；正文语气类 emoji 也一并清除，避免页面出现两套图标语言 |
| 统计卡/KPI 形态 | 统一"一个数值 + 一个标签 + 一个线性图标"的浅底卡片；4 个一行，5 个改"主指标 + 次级条" | 彩色渐变图标块、5 卡 4+1 空网格、KPI 卡双重选中边框 | admin 01/08/20 的渐变图标块与 merchant 01/02/03 的 4+1 空网格是同一类问题的两端，必须用同一组件收口 | admin 去掉渐变与六色实色图标；merchant 把 4+1 改为闭合等宽网格或主+次结构；双重边框删除 |
| 页头语言 | 统一"标题容器"：标题 + 可选描述 + 右侧动作区；merchant/admin 用紧凑版，高度减一档 | merchant 的深绿整条标题条、白卡标题区、无标题三种并存 | 页头是用户判断"我在哪、能做什么"的第一锚点，跨页分裂会直接造成割裂感 | merchant 01–13 统一到同一页头容器，颜色由深绿改为中性表面 + 品牌橙点缀；admin 08/09/15、user 26/32/34 同步 |
| 表单宽度 | 登录/注册/找回/支付密码等单列表单固定窄列居中（`--form-width-auth: 400px`）；设置/编辑类表单限宽 2 栏铺满卡片（`--form-width-settings: 720px`） | 表单铺满 1400px 全宽、或单列窄表单让卡片右半永远空白 | public 01–03 与 merchant 06/08、admin 17 是同一个问题的两种表现：要么太宽，要么太窄导致右侧空白 | 认证类收窄到 400px 居中；编辑类改两栏铺满；所有表单列宽来自 token，不逐页自定义 |
| 主题色常量来源 | 单一 `tokens.css`（`:root` CSS 变量）作为唯一颜色源，Element Plus 与 less 变量都从它取值 | `nordic-theme.less`、`merchant-theme.less` 与 `HomeContent.vue` 内 `@savour-*` 三套 less 变量 + 组件内硬编码色值 | 同一主色历史上并存陶土 `#D4845A`、深绿 `#4A7A4D`、`#C67B5C` 三个值，改一次主色要动三处且必然漂移；`constants/nordicTheme.js` 还是 less 的手抄第二份 | 新增 `apps/client/src/renderer/src/assets/css/tokens.css` 作为唯一来源；三套 less 变量改为指向 CSS 变量后逐步退役；`nordicTheme.js` 改为运行时读 `getComputedStyle`，不再手抄 |
| 动效引入方式 | 引入 GSAP，但限定「进场 / 展开 / 列表重排」三类用途，只在 Web 与桌面 renderer 生效；其余交互仍走 CSS transition + Element Plus 过渡 | 全站铺开 GSAP、用 GSAP 做 hover 等高频微交互、在小程序端引入 GSAP | 用户明确要求用 GSAP 提升生气与交互感；GSAP 自 2025-04 起全部插件免费商用，Electron 内置 Chromium 原生支持、无原生依赖 | 日常 hover/focus/press 仍用 §3.5 的 CSS token；GSAP 只在 §3.5 列出的三类场景注册，`prefers-reduced-motion` 下直接落终值 |

## 3. Design Tokens（CSS 变量）

以下变量可直接复制到全局样式入口（现有工程为 `apps/client` 的全局样式文件）。命名与 Element Plus 的 `--el-*` 变量并行，落地时把 `--el-color-primary` 映射到 `--brand-500`，把 `--el-border-radius-base` 映射到 `--radius-sm`。

### 3.1 色彩

```css
:root {
  /* 品牌暖橙：Logo、侧栏选中态、主按钮、主色链接 */
  --brand-50:  #FEF4EF;
  --brand-100: #FDE4D8;
  --brand-200: #FAC8B2;
  --brand-300: #F5A583;
  --brand-400: #F08458;
  --brand-500: #ED6A42; /* 主色；现有 public 01 登录按钮实测值 rgb(237,106,66) */
  --brand-600: #D9532C;
  --brand-700: #B43F1F;
  --brand-800: #8C2F16;
  --brand-900: #6B2410;

  /* 中性：暖调灰，避免纯黑白带来的冷感 */
  --ink-900: #1F1B18; /* 标题、关键数值 */
  --ink-700: #3D3733; /* 正文 */
  --ink-500: #6B625B; /* 次要文字、表头 */
  --ink-400: #8C8179; /* 占位、禁用文字 */
  --line-strong: #D9CFC6;
  --line:        #E8E1DA; /* 卡片描边、分割线 */
  --surface:     #FFFFFF; /* 卡片、表格 */
  --canvas:      #FBF7F3; /* 页面底色，暖白 */
  --canvas-sunken: #F4EDE6; /* 页面底、工具条 */
  --overlay: rgba(31, 27, 24, 0.45);

  /* 语义色：只做标签、图标、反馈，不做整屏底色 */
  --success:    #2E9E5B;
  --success-bg: #E8F6EE;
  --warning:    #E08A00;
  --warning-bg: #FFF3DC;
  --danger:     #D93B36;
  --danger-bg:  #FDECEB;
  --info:       #2F6FE4;
  --info-bg:    #EAF1FE;
  --neutral:    #6B625B;
  --neutral-bg: #EFEAE5;
}
```

语义色硬规则：

- `--brand-500` 只用于品牌识别与主操作，不得用于成功/失败提示。
- `--success / --warning / --danger / --info` 只用于小面积语义标签、图标与反馈，不得铺满整屏或整卡。
- 订单/审核/上架状态一律按下表映射，不允许页面自创颜色。

| 状态 | 颜色 token | 标签底色 | 适用示例 |
|---|---|---|---|
| 待处理 / 待审核 / 待回复 | `--warning` | `--warning-bg` | 待审核商家、待处理订单、待回复评价 |
| 进行中 / 已上架 / 生效中 / 配送中 | `--info` | `--info-bg` | 进行中订单、已上架菜品、生效中话题 |
| 已完成 / 已通过 / 成功 / 营业中 | `--success` | `--success-bg` | 已完成订单、审核通过、支付成功 |
| 已取消 / 已拒绝 / 失败 / 异常 | `--danger` | `--danger-bg` | 已取消订单、审核拒绝、退款失败 |
| 草稿 / 已下架 / 已停用 / 已暂停 | `--neutral` | `--neutral-bg` | 草稿教程、已下架菜品、停用角色 |

### 3.2 字体与字阶

```css
:root {
  --font-sans: "Inter", "PingFang SC", "Microsoft YaHei", "Noto Sans SC",
               -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  --font-num: "Inter", ui-monospace, "SF Mono", Menlo, monospace;

  --text-display: 32px; /* 行高 40px，字重 700 */
  --text-h1:      24px; /* 行高 32px，字重 600 */
  --text-h2:      20px; /* 行高 28px，字重 600 */
  --text-h3:      16px; /* 行高 24px，字重 600 */
  --text-body:    14px; /* 行高 22px，字重 400 */
  --text-body-sm: 13px; /* 行高 20px，字重 400 */
  --text-caption: 12px; /* 行高 18px，字重 400；仅限非关键辅助信息 */

  --weight-regular: 400;
  --weight-medium:  500;
  --weight-semibold: 600;
  --weight-bold:    700;

  --tracking-tight: -0.011em; /* 20px 及以上标题 */
  --tracking-normal: 0;
  --tracking-wide: 0.01em;    /* 全大写/编号类标签 */
}
```

字体硬规则：

- 全站统一无衬线，**删除 admin 金额、public/04 标题等处出现的衬线字体**；不引入第二套字体家族。
- 正文最小 `13px`；`12px` 只允许用于不承载操作含义的辅助说明，评审中"辅助链接 12–13px 偏小"的页面统一提到 `13px`。
- 所有金额、金额列、统计数值、订单号使用 `--font-num` 并加 `font-variant-numeric: tabular-nums;`，保证跨行对齐。
- 后台表格正文用 `--text-body-sm`（13px），user/public 正文用 `--text-body`（14px）或 `--text-body-sm`。

### 3.3 间距与栅格

```css
:root {
  --space-1:  4px;
  --space-2:  8px;
  --space-3:  12px;
  --space-4:  16px;
  --space-5:  20px;
  --space-6:  24px;
  --space-8:  32px;
  --space-10: 40px;
  --space-12: 48px;

  --page-max:        1200px; /* public / user 内容最大宽度 */
  --page-gutter:     24px;   /* 桌面页边距；≤768px 降到 16px */
  --form-width-auth: 400px;  /* 登录/注册/找回/支付密码单列表单列宽 */
  --form-width-settings: 720px; /* 设置/资料类表单限宽 */
  --sidebar-width:   240px;  /* merchant / admin 侧栏，允许 200–240px */

  --card-padding:       20px; /* user / public */
  --card-padding-compact: 16px; /* merchant / admin */
  --card-gap:           20px;
  --card-gap-compact:   12px;
}
```

栅格硬规则：

- public/user：12 列栅格，最大宽度 `--page-max`，页面左右 `--page-gutter`。
- merchant/admin：左侧 `--sidebar-width` 固定 + 右侧内容区自适应；内容区内边距 `--space-6`（≤1280px 时 `--space-4`）。
- 卡片间距统一用 `--card-gap`（紧凑端 `--card-gap-compact`），不允许逐页写 15px、18px、22px 之类非 token 值。
- 表单宽度只能用 `--form-width-auth` 或 `--form-width-settings` 两个档位，不允许出现第三个自定义宽度。

### 3.4 圆角与阴影

```css
:root {
  --radius-sm:   6px;  /* 输入框、标签、小控件 */
  --radius-md:   8px;  /* 按钮、分段控件 */
  --radius-lg:   12px; /* 卡片、弹层 */
  --radius-full: 999px; /* 头像、胶囊标签、进度条 */

  --control-height-md: 40px; /* 主按钮、页头动作区 */
  --control-height-sm: 32px; /* 工具条、表格行内、次级动作 */

  --shadow-sm: 0 1px 2px rgba(31, 27, 24, 0.06);
  --shadow-md: 0 4px 12px rgba(31, 27, 24, 0.08);
  --shadow-lg: 0 12px 32px rgba(31, 27, 24, 0.12);
}
```

形状硬规则：

- 按钮统一 `--radius-md`；不允许同一页面同时出现 2px、4px、8px、pill 四种按钮圆角。
- 按钮高度只用 `--control-height-md`(40px) 或 `--control-height-sm`(32px)：主按钮与页头动作区取 md，工具条 / 表格行内 / 次级动作取 sm；**禁止在页面里自写按钮高度**。
- 卡片圆角只用 `--radius-md` / `--radius-lg`，同一列表内所有卡片圆角一致。
- 禁止使用彩色渐变作为卡片或统计块背景；渐变只允许出现在 public 品牌背景与 user 首页/热点等营销区块。
- 只有浮层/弹窗用 `--shadow-md/lg`，列表卡片不用大阴影。

#### 卡片质感阶梯（L0–L3）

纯色平铺显得廉价，全站铺玻璃又会脏。统一用四级阶梯表达质感，**同一列表内只用一个层级**，层级由卡片所处的背景决定，不由页面风格决定：

| 层级 | 组成 | 用在哪 |
|---|---|---|
| L0 平面 | `--surface` 纯色，无描边、无阴影 | merchant/admin 密集表格行、卡片内嵌分区 |
| L1 微质感 | L0 + `1px solid var(--line)` 描边 + 顶部内高光 `inset 0 1px 0 rgba(255, 255, 255, 0.6)` | 默认卡片：KPI 卡、表单分组、列表卡片 |
| L2 浮起 | L1 + `--shadow-sm`，hover 升 `--shadow-md` 并 `translateY(-1px)` | 可点击卡片、弹层触发器 |
| L3 玻璃 | L2 + 半透明底 `rgba(255, 255, 255, 0.72)` + `backdrop-filter: blur(16px)` | **仅**叠加在彩色或图像背景之上：public 品牌区、user 首页营销块、横幅上的浮层 |

质感硬规则：

- 质感只靠「描边 + 顶部内高光 + 阴影 + 半透明」四件事表达，不引入噪点贴图、纹理图片或彩色渐变铺底。
- L3 玻璃只在彩色/图像背景之上成立；白底页面（merchant/admin 绝大多数页面）不得用玻璃卡，会显脏。
- L2 的 hover 位移上限 `2px`，不做缩放跳动。
- 同一屏内不混用两层，例如同一列表里既有 L1 又有 L3。

### 3.5 动效 token

```css
:root {
  --duration-instant: 120ms; /* 按压、颜色即时反馈 */
  --duration-fast:    150ms; /* hover、focus、图标反馈 */
  --duration-base:    200ms; /* 分区进入、状态切换、弹层 */
  --duration-slow:    240ms; /* 卡片展开、抽屉、数字变化上限 */
  --duration-num:     280ms; /* 统计数值 count-up 上限 */

  --ease-standard: cubic-bezier(0.2, 0, 0, 1);      /* 通用出入场 */
  --ease-out:      cubic-bezier(0.16, 1, 0.3, 1);   /* 标准出场；来自 Kinetics --glide */
  --ease-in-out:   cubic-bezier(0.65, 0, 0.35, 1);  /* 位置/尺寸移动；来自 Kinetics tab 指示器 */
  --ease-spring:   cubic-bezier(0.34, 1.56, 0.64, 1); /* 一次性展开；来自 Kinetics spring(320,24) */

  --spin-duration: 800ms; /* loading 图标持续旋转周期 */
  --refresh-feedback: 300ms; /* 刷新完成后的对勾/回弹反馈 */
}

/* 无障碍：减少动态偏好下直接落终值 */
@media (prefers-reduced-motion: reduce) {
  :root {
    --duration-instant: 0.01ms;
    --duration-fast:    0.01ms;
    --duration-base:    0.01ms;
    --duration-slow:    0.01ms;
    --duration-num:     0.01ms;
    --spin-duration:    0.01ms;
    --refresh-feedback: 0.01ms;
  }
}
```

动效使用约定（每个角色都用同一套，只是触发频率不同）：

| 场景 | 时长 | 曲线 | 说明 |
|---|---|---|---|
| 按钮/链接 hover、focus | 150ms | `--ease-out` | 颜色、边框、轻微 `translateY(-1px)`；不做缩放跳动 |
| 按钮 press | 120ms | `--ease-standard` | 只做 1px 位移或亮度变化 |
| 卡片/分区首次进入 | 200ms | `--ease-out` | 单个卡片/分区用 CSS：`opacity 0→1` + `translateY(4px)→0`，只播一次；卡片流用 GSAP stagger（见下） |
| 状态标签切换 | 180–200ms | `--ease-standard` | 底色与文字色同时过渡 |
| 抽屉/弹层 | 200ms | `--ease-out` | 位移不超过 16px |
| 可展开卡片 | 240ms | `--ease-spring` | 仅用于"展开/收起"这类一次性动作，不用于列表 hover |
| tab 指示器 | 200ms | `--ease-in-out` | 参考 Kinetics 指示器，但时长从 400ms 收敛到 200ms |
| 统计数字 count-up | 240–280ms | `--ease-out` | 只做一次；`prefers-reduced-motion` 下直接显示终值 |
| loading 旋转 | 800ms/圈 | linear | 持续旋转；不做缓动 |
| 刷新完成反馈 | 300ms | `--ease-spring` | 旋转停 + 一次轻微回弹 |

#### GSAP 使用边界

已确认引入 GSAP（在 `apps/client/package.json` 增加 `gsap` 依赖，只 import 用到的模块），但**只解决 CSS 做不好的三类场景**，其余一律走上面的 CSS token：

1. **一次性进场**：卡片流/列表首次渲染的 stagger 入场，只播一次，不重复播。
2. **展开 / 收起**：折叠面板、详情展开、抽屉内容的时序编排。
3. **列表重排**：同一列表内增删、排序时的位置补间（`Flip`），让重排可被看懂。

GSAP 硬规则：

- **只在 Web 与桌面 renderer 生效**：GSAP 依赖 DOM，小程序没有 DOM，动效仍走 CSS/uni 方式，不引入 GSAP。
- 只动 `transform` 与 `opacity`，不动 `width` / `height` / `margin` / `top` 等触发布局的属性。
- 用 `gsap.context()` 挂载、`onUnmounted` 里 `ctx.revert()` 清理，组件卸载不留悬挂 tween。
- 注册在 `gsap.matchMedia()` 的 `(prefers-reduced-motion: no-preference)` 分支里；减少动态偏好下不注册任何 tween，直接落终值。
- **merchant / admin 不加装饰性进场**：这两端只允许用 GSAP 做「展开 / 收起」和「列表重排」，不做首屏 stagger。

动效硬规则：

- 高频微交互（hover / focus / press / 状态切换）只用 CSS `transition` / `animation` 与 Element Plus 自带过渡，不用 GSAP。
- 所有动效必须有 `prefers-reduced-motion` 降级。
- 动效不得改变布局尺寸（用 transform/opacity，不用 width/height 动画）。折叠面板高度是唯一例外，且必须用 Kinetics 式单曲线近似，不做 JS 逐帧。

### 3.6 小程序（UniApp）跨端一致

小程序不在 75 页截图范围内，但它是同一产品的第五个端，颜色必须和 Web/桌面同一套：

- 主色 `$primary-500: #FF6B35` 收敛为 `#ED6A42`，与 `--brand-500` 同值，`$primary-color` 同步。
- 现有 10 级色阶（`$primary-50` … `$primary-900`）按 §3.1 的 `--brand-*` 十档重取；现有色阶本身不自洽（`$primary-800 #FF8B5D` 比 `$primary-700 #FF4D1F` 更浅），一并修正。
- `$secondary-color: #667eea` 这类紫色辅助色与「唯一主色」冲突，退为信息语义或删除。
- 成功/警告/危险/信息语义色对齐 §3.1；小程序不做玻璃质感，卡片只走 L1 / L2。
- 小程序不引入 GSAP，动效走 §3.5 的 CSS token。

## 4. 组件约定

### 4.1 按钮层级

每屏最多一个实心主按钮。层级与用法固定如下，四端一致：

| 层级 | 外观 | 用途 | Element Plus 映射 |
|---|---|---|---|
| 主按钮 primary | `--brand-500` 实心 + 白字，`--radius-md` | 每屏唯一最高频正向动作（登录、提交评价、保存、注册、支付） | `type="primary"`，但需把 `--el-color-primary` 覆盖为 `--brand-500` |
| 次按钮 secondary | `--surface` 底 + `--line-strong` 描边 + `--ink-700` 文字 | 与主按钮同屏的次要动作（取消、返回、预览、导出） | `type="default"` |
| 文字按钮 text | 无底色，`--brand-600` 文字 | 跳转、查看全部、更多；同一组不超过 3 个 | `link` 但覆盖颜色为 `--brand-600` |
| 危险按钮 danger | `--danger` 实心或描边，必须与普通动作留出 `--space-6` 间距 | 删除、禁用、拒绝、退款驳回 | `type="danger"` |
| 禁用/加载 | 禁用态用 `--ink-400` 文字 + `--neutral-bg` 底；提交中显示 button loading 并禁止重复点击 | 防止重复提交 | `:loading` + `:disabled` |

硬约束：

- 禁止同一屏出现两个及以上实心主按钮（含品牌橙与危险红同时实心的情况）。
- 禁止蓝色实心按钮、绿色实心按钮（public/04 的蓝色「注册」、merchant/01 的深绿「编辑信息」都在此列）。
- 危险操作不得与普通操作同色同形；列表内的危险动作一律收进「更多」菜单并加二次确认（admin 02/04/08/18 的问题）。
- "返回/刷新"必须是次级或文字层级，不得与主按钮同为蓝色/橙色实心（public/04 的蓝色「返回」「刷新」）。

### 4.2 卡片

统一字段骨架，顺序固定，缺项就留空位或省略，不改变位置：

```
[可选主图/头像] → 标题 → 状态标签 → 关键数值(金额/评分/距离等) → 次要信息 → 操作区(右上或底部)
```

- 外形：默认走 §3.4 的 L1 微质感（`--surface` 底 + `1px solid var(--line)` + 顶部内高光），可点击卡片升 L2（`--shadow-sm`，hover `--shadow-md`）；圆角 `--radius-lg`；user/public 内边距 `--card-padding`，merchant/admin 用 `--card-padding-compact`。
- 图片：固定比例（菜品卡 4:3、商家头像 1:1、教程封面 16:9），`object-fit: cover`；加载失败显示中性底 + 首字母或线性图标，**不允许出现碎图链接、白色空块或占位文字**（user 01/03/04/05/11/28/29/31/33、merchant 07/08 的问题）。
- 同一列表内卡片高度与图片比例必须一致；首屏至少露出 2 张完整卡片（user）或 3 行（admin 表格）。
- 禁止彩色左边框当作"分类"，分类/状态只能用标签表达（user 08 的问题）。
- 禁止卡片双重边框（merchant 01 KPI 卡的绿框 + 蓝框）。

### 4.3 表格（admin 为主，merchant/user 复用）

- 结构固定：筛选条 → 合计/分页信息 → 列头 → 数据行 → 行操作。
- 字号：表头 `--text-body-sm` / 600 / `--ink-500`；单元格 `--text-body-sm` / 400 / `--ink-700`。
- **行高只允许两档**（2026-11-09 冻结，实测同批页面曾散成 32/33/35/44/52/55/56 七档）：
  - 单元格单行内容 → **40px**；
  - 单元格本身就含双行内容（头像+双行、标题+摘要）→ 允许 **≤56px**，但同族页面必须一致。
  - 「列宽不足导致内容折行」不算双行内容：必须改单行省略 + 悬浮完整内容（见下一条）。
- ID、订单号、时间、金额列一律 `white-space: nowrap`；列宽不足时用单行省略号 + 悬浮完整内容，**不允许折行导致行高参差**（admin 03/04/06/08/09/13/18/20 的问题）。
- 时间统一 `YYYY-MM-DD HH:mm`，禁止 ISO 的 `T`（admin 03/05/06/10/14/16）。日期型列表（如公告）统一 `YYYY-MM-DD`。
- 金额：右对齐、`--font-num`、`tabular-nums`、`--ink-900`，**不带红绿蓝**（admin 05/06/12/13 的问题）。
- 行操作（2026-11-09 冻结，收口 #441 的边界问题）：
  - 可见**业务动作**最多 2 个 = `primary` 槽位 1 个 + `items` 中 `maxVisible` 个（`RowActions` 默认 1）。「更多」是容器不是业务动作，不计入。
  - 立刻生效的破坏性动作（删除/下架/禁用）一律进「更多」，危险色 + 由 `RowActions` 承担二次确认。
  - 先弹对话框再提交的动作（审核/拒绝/状态变更）与日常动作（查看/编辑）都可占可见位。
  - **两者冲突时审核类优先占可见位**：审核时效性最强、语义最重，同行的日常动作让位到「更多」。实现上不必改组件——把审核动作排在 `items` 数组首位，它自然占用 `maxVisible` 的那个名额。
  - `primary` 允许按行计算（同一列不同行可以是不同动作）。
  - 通过类动作可留可见，但要补文字或 tooltip（admin 02/04/08/09/18）。
- 空数据：用紧凑空状态替换表体，并隐藏分页条；分页合计必须等于实际记录数（admin 04/07/11/13/20 的 "Total 0" 与英文 "No Data"）。
- 数字/价格/ID 一律无衬线，禁止表格里出现衬线数字（admin 05/06/12/13）。

### 4.4 表单

- 认证类（登录/注册/找回/支付密码）：`--form-width-auth` 窄列水平居中；标签、输入框、主按钮同一列宽；品牌区与表单共用中轴（public 01–03 的问题）。
- 设置/编辑类：`--form-width-settings` 限宽或两栏铺满卡片；禁止单列窄表单贴在左侧、右侧一半永远空白（merchant 06/08/13、admin 17 的问题）。
- 标签：同一表单内统一"标签在上"或"标签在左"，不混用；必填星号 `--danger`；错误提示 `--danger` / 12px，紧跟字段下方。
- 字段间距 `--space-4`；表单分组之间 `--space-8`；超长设置页必须分组（账号/显示/通知/隐私/安全），不允许一张超长白卡加细分割线堆到底（user 20、merchant 13 的问题）。
- 密码规则写成字段下方常驻提示，不只放在 placeholder（public 02）。
- 提交按钮固定在表单末尾或页头右侧；长表单允许吸底，但要预留滚动安全距离，不得遮挡最后一个字段。
- 危险主按钮：承载危险动作的主动作按钮必须用 `--danger`，包括确认框主按钮、删除页主按钮；hover 用 `--danger` 的浅档且保持 `--surface` 文字，品牌橙只表示正常流程的下一步。

### 4.5 空状态

四端共用同一结构，允许 admin/merchant 用紧凑尺寸：

```
线性图标(32px, --ink-400) → 一句中文说明 → 一个主行动(实心)或次行动(link) → 可选次行动(link)
```

- 文案必须中文。禁止 "No Data"、"暂无数据" 这类无信息量文案；要说清"为什么空 + 下一步做什么"，例如「今天还没有订单，去菜单页上架一道菜」。
- 必须有替代动作（Component Gallery 原文：空状态"通常包含一个替代动作"）。0 单/0 消息/0 教程时，收起无效统计与筛选，把空状态上移（merchant 03/05/14/15、admin 07/20、user 06/07/09/16/19/31/34 的问题）。
- 替代动作分两档（对应 §4.1 按钮层级）：**主行动**用实心 `--brand-500` + `--surface` 文字 + `--radius-md`；**次行动**用 `link` 文字按钮。同一空状态内至多一个主行动（每屏至多一个实心主按钮，见 §4.1）。
- 列表内空状态高度不超过 240px；整页空状态不超过 360px，CTA 放在视觉中心。
- 空状态不得与顶部统计卡重复讲述同一件事（user 33、merchant 14/15 的"统计卡 + tabs + 空状态"三连）。

### 4.6 状态与反馈

- 状态标签一律走 3.1 的状态表；同一状态在列表、详情、卡片、导出中必须同色。
- 所有可点击元素必须有 hover、focus-visible、active、disabled 四态；焦点可见环用 `--brand-400`，不允许只靠颜色区分。
- 提交类操作：按钮 loading + 禁止重复点击 + 成功后 Element message + 失败后 inline 错误；危险操作二次确认（ElMessageBox）。
- 异步加载：列表首屏用骨架屏；刷新不整页白屏；刷新完成用 `--refresh-feedback` 反馈。
- 删除/移除类操作提供撤销入口或确认弹窗二选一，不对同一动作叠加两层。
- 浮动控件（购物车、聊天输入、吸底栏）不得遮挡内容热区与最后一行；user 04/17 的浮动购物车必须移出菜单/对话热区或改为底部固定栏。

### 4.7 页头

统一容器，四端同一语言，只调节密度：

```
标题(--text-h1 或 h2)  [可选一行描述]        [右侧动作区: 返回 + 次级 + 主按钮]
─────────────────────────────────────────────────────────────────
```

- 「返回」统一放**右侧动作区**（次级动作，样式按 §4.9 次按钮），不进标题行内；动作区控件高度取 `--control-height-md`。
- 标题容器用 `--surface` 或页面底色 + 下边线，**禁止深绿整条标题条**（merchant 01–13）。
- merchant/admin 用紧凑版：标题 `--text-h2`，上下内边距 `--space-4`；public/user 用标准版：标题 `--text-h1`，上下内边距 `--space-6`。
- 侧栏标签名必须与页面标题一致（admin 15「系统配置 / 系统设置」、merchant 09 的问题）。
- 每页有且只有一个 `h1`；页内区块标题从 `h2` 起。

### 4.8 概览卡 / KPI

- 结构固定：线性图标（24px，浅底圆形或方形）+ 数值（`--font-num`，`--text-h1`，`tabular-nums`）+ 标签（`--text-body-sm`，`--ink-500`）。
- 布局：4 个一行；5 个改为「1 个主指标 + 4 个次级条」，**禁止 4+1 空网格**（admin 01、merchant 02/03）。
- 一律浅底 + 统一中性/品牌色调，**禁止彩色渐变图标块与六色实色图标**（admin 01/08/20）。
- 数值与标签垂直距离不超过 `--space-2`，禁止数值左上、标签右下隔一整张卡（merchant 10）。
- 统计卡不得抢占首屏：当页面核心是列表时，统计卡压缩为一条摘要或移出首屏（user 09/10/30/33、merchant 05/14、admin 12）。

### 4.10 列表页首屏预算

- 硬指标（1440×900，10 行数据）：**分页条 `top` < 900px**，即首屏内要能看到合计与分页。
- 顶部区块预算：页头 + 统计卡 + 筛选条合计 **≤ 330px**。超预算时按顺序压缩，不要靠缩表格行高：
  1. 统计卡换紧凑档（图标与数值同排，高 ≤96px；常规档现为 134px）；
  2. 筛选项并排成一行，或把次要筛选项收进「更多筛选」；
  3. 仍是 6 张以上统计卡时，允许 3×2 网格，但必须同时启用紧凑档。
- 实测基线（2026-11-09，改前）：`admin/02-users` 分页条 top 946、`10-topics` 1054、`11-announcements` 976、`12-withdrawals` 1229、`13-recharges` 924、`14-refunds` 1031、`20-logs` 1070 —— 13 页里 9 页越界。

### 4.9 图标

- 结构性图标统一用 Element Plus 线性图标（`@element-plus/icons-vue`）。
- 替换范围：统计卡、菜单、字段标签、状态、空状态、消息类型、钱包/安全/联系人等；评审点到的 9 份（merchant 01/04/06/08/13、user 14/20/22/27）逐处替换。
- 同一语义全站同一图标（例如"订单"在 user/merchant/admin 都用同一线性图标）。
- 保留品牌 Logo 与头像；emoji 不参与信息架构。

## 5. 正反例

以下"反例"均来自本次评审与截图（截图路径在主检出，不复制进仓库）：

### 5.1 品牌主色

- 正例：public/01 登录页——暖橙渐变背景 + 橙色实心主按钮 + 橙色链接，品牌识别集中在一处。`output/web-screenshots/public/01-login.png`
- 反例：public/04 商户注册页——绿色满屏 + 白卡 + Element 默认蓝按钮 + 蓝色「返回/刷新」+ 衬线标题，等同另起一套语言。`output/web-screenshots/public/04-merchant-register.png`
- 处理：04 的表单布局可保留，颜色、按钮、标题字体全部按本基线替换。

### 5.2 金额与数值

- 正例：`-¥12.00  支出`、`+¥50.00  收入`、`¥0.00  退款`——中性深色数字 + 正负符号 + 文字角色，右对齐、tabular-nums。
- 反例：admin 05/06/12/13 的红色金额与衬线数字；user 14 用红绿蓝三色同时编码收入/支出/退款。
- 处理：金额数字不染色；状态（退款中/失败）用状态标签表达。

### 5.3 状态色

- 正例：全站「已完成」= `--success` 绿；「进行中」= `--info` 蓝；「待审核」= `--warning` 黄；「已取消」= `--danger` 红。
- 反例：merchant 11 详情页「已完成」用蓝、列表页「已完成」用绿；merchant 01/02/03 的 KPI 卡绿框+蓝框叠用。

### 5.4 表格

- 正例：`商家ID` 列 `seedm20260520_01` 单行完整；`注册时间` 显示 `2026-05-20 15:46`；行操作只有「查看」+「更多」；空数据时表体显示中文空状态且分页条隐藏。
- 反例：admin 03/04 的 ID 折行、ISO 时间 `2026-05-20T15:46:00`；admin 04/11/13 的 `Total 0` 与实际 8 行不符；admin 02 的禁用/删除与查看同权同色。

### 5.5 空状态

- 正例：线性图标 + 「今天还没有订单」+ 「去上架第一道菜」主按钮 + 「查看全部订单」文字按钮；高度 ≤ 240px。
- 反例：admin 01 表格内英文 `No Data`；admin 07/20 缺失中文引导；merchant 03/05 把 0 值统计卡放在空状态上方。

### 5.6 图标与统计卡

- 正例：admin 概览卡统一为浅底 + 线性图标 + `tabular-nums` 数值 + 标签，4 个一行；数字与标签间距 `--space-2`。
- 反例：admin 01 紫/粉/橙/青渐变图标块 + 5 卡 4+1 空网格；merchant 01 用 📊📋💬 emoji 承担统计与标题图标。

### 5.7 主按钮层级

- 正例：一屏一个暖橙实心主按钮（如「提交评价」），其余为描边/文字；危险删除进「更多」并二次确认。
- 反例：merchant 01「编辑信息」深绿实心与页面其他动作抢主色；public/04 蓝色「注册」+ 蓝色「返回/刷新」同色；merchant 06/08 三个保存动作并列。

## 6. 哪些必须全站一致、哪些允许角色内差异

### 6.1 必须全站一致（硬约束，不允许任何角色例外）

1. 品牌主色：`--brand-500 #ED6A42` 是全站唯一主色与唯一主操作色。
2. 语义色与状态表：绿=成功、黄=待处理/警示、红=危险/失败、蓝=信息、灰=中性/草稿；订单与审核状态一一映射。
3. 字体家族与字阶：统一 `--font-sans` / `--font-num`；标题、正文、辅助文字使用同一字阶表；禁止衬线数字与第二套字体。
4. 金额与数值规则：金额中性色 + 符号 + 文字角色；数值 tabular-nums；金额右对齐。
5. 日期时间格式：`YYYY-MM-DD HH:mm` / `YYYY-MM-DD`；禁止 ISO 的 `T`。
6. 按钮层级：每屏一个实心主按钮；危险操作独立颜色与二次确认；蓝色不得做主按钮。
7. 卡片与表格骨架：卡片字段顺序、表格列/行操作顺序、ID 与时间不折行、空数据隐藏分页。
8. 空状态结构：图标 + 中文说明 + 替代动作；禁止英文无信息量文案。
9. 图标体系：Element Plus 线性图标；禁止结构性 emoji。
10. 页头语言：同一标题容器结构；侧栏命名与页面标题一致。
11. 动效 token：时长与曲线来自 3.5；必须支持 `prefers-reduced-motion`。
12. 交互反馈：hover/focus/active/disabled 四态、loading、防重复提交、危险二次确认。

### 6.2 允许角色内差异（只影响密度与布局，不影响语言）

| 维度 | public / user | merchant / admin |
|---|---|---|
| 密度 | 宽松：卡片内边距 20px、间距 20px、行高舒适 | 紧凑：卡片内边距 16px、间距 12px、表格行高 40–44px |
| 正文字号 | 14px 起，营销区可用 16px | 表格与密集信息 13px |
| 内容宽度 | 单列居中，最大 1200px；表单窄列 400px | 侧栏 240px + 自适应内容区；表单两栏或限宽 720px |
| 布局 | 单列/卡片流，强调图片与留白 | 多列、表格、筛选条，强调信息密度 |
| 字阶上限 | 允许 `--text-display` 32px 用于首页/热点 | 不使用 `--text-display`，最高 `--text-h1` 24px |
| 渐变 | 允许品牌背景与营销区块使用暖橙渐变 | 禁止渐变，统一纯色表面 |
| 动效频率 | 允许一次性进场（含 GSAP stagger）、count-up、卡片 hover | 只允许 hover/focus/loading/状态切换，加 GSAP 展开与列表重排，不加装饰性首屏进场 |
| 装饰图形 | 允许适度插画/氛围 | 只保留功能性图标 |

角色内差异的边界：**紧凑不等于换语言。** merchant/admin 可以更小、更密、更快，但颜色、字体、状态语义、图标、金额与日期格式必须与 public/user 完全一致；反过来，public/user 也不能使用蓝色主按钮或结构性 emoji。

## 7. 落地：唯一颜色源与 Element Plus 映射

### 7.1 唯一颜色源 `tokens.css`

全站颜色只有一份来源：新增 `apps/client/src/renderer/src/assets/css/tokens.css`，在 `main.js` 里 import，文件内只有 `:root { … }` 定义，即本文 §3.1 的色彩表 + §3.4 的形状/阴影 + §3.5 的动效 token。

- 现有三套 less 变量（`nordic-theme.less`、`merchant-theme.less`、`HomeContent.vue` 内的 `@savour-*`）不再是颜色来源：变量值改为引用 CSS 变量（如 `@nordic-accent: var(--brand-500);`），随后逐步退役；不允许新增第二处定义。
- 组件里一律写 `var(--brand-500)` 这类 CSS 变量，不再写 `#ED6A42` 之类的裸色值；圆角、阴影、`backdrop-filter` 同理。
- JS 里需要颜色时（图表、canvas 等）用 `getComputedStyle(document.documentElement).getPropertyValue('--brand-500')` 读取；`constants/nordicTheme.js` 按此改造，不再手抄 less 值。
- 小程序是独立工程，颜色源是 `apps/miniapp/src/styles/variables.scss`，其值必须与 §3.1 对齐（§3.6）。

### 7.2 Element Plus 变量映射

落地时在全局样式覆盖 Element Plus 默认变量，避免出现「组件库默认蓝」与品牌橙并存：

```css
:root {
  --el-color-primary: var(--brand-500);
  --el-color-primary-light-3: var(--brand-400);
  --el-color-primary-light-5: var(--brand-300);
  --el-color-primary-light-7: var(--brand-200);
  --el-color-primary-light-8: var(--brand-100);
  --el-color-primary-light-9: var(--brand-50);
  --el-color-primary-dark-2: var(--brand-600);

  --el-color-success: var(--success);
  --el-color-warning: var(--warning);
  --el-color-danger:  var(--danger);
  --el-color-error:   var(--danger);
  --el-color-info:    var(--info);

  --el-text-color-primary:   var(--ink-900);
  --el-text-color-regular:   var(--ink-700);
  --el-text-color-secondary: var(--ink-500);
  --el-text-color-placeholder: var(--ink-400);

  --el-border-color:        var(--line);
  --el-border-color-light:  var(--line);
  --el-border-color-lighter: var(--line);
  --el-bg-color:            var(--surface);
  --el-bg-color-page:       var(--canvas);
  --el-fill-color-blank:    var(--surface);

  --el-border-radius-base:  var(--radius-sm);
  --el-border-radius-small: var(--radius-sm);
  --el-border-radius-round: var(--radius-full);

  --el-font-family: var(--font-sans);
  --el-transition-duration: var(--duration-base);
}
```

注意：Element Plus 的 `--el-color-primary-light-*` 默认为蓝色系，必须整体覆盖；只改 `--el-color-primary` 会让 hover/disabled 态残留蓝色。

## 8. 落地检查清单

改造任一页面后，逐条自查，全部通过才算符合基线：

- [ ] 页面主按钮是 `--brand-500`，同屏只有一个实心主按钮。
- [ ] 没有蓝色主按钮、绿色主按钮、蓝色普通链接强调、深绿标题条。
- [ ] 金额为中性色 + 符号 + 文字角色，未使用红/绿/蓝给数字染色。
- [ ] 数字、金额、订单号使用 `--font-num` + `tabular-nums`，不是衬线字体。
- [ ] 时间显示 `YYYY-MM-DD HH:mm` 或 `YYYY-MM-DD`，没有 ISO 的 `T`。
- [ ] 状态色符合状态表，同一状态在列表与详情同色。
- [ ] 表格 ID/时间/金额列不折行，行高一致，空数据隐藏分页。
- [ ] 行操作不超过 2 个可见动作，危险动作在「更多」内且有二次确认。
- [ ] 空状态为中文，含图标、说明和一个替代动作。
- [ ] 卡片字段顺序、图片比例、卡片高度在同列表内一致。
- [ ] 表单宽度取自 `--form-width-auth` 或 `--form-width-settings`，无右侧大面积空白。
- [ ] 页头为统一标题容器，侧栏名称与页面标题一致。
- [ ] 统计卡为浅底 + 线性图标 + 数值 + 标签，无渐变图标块、无 4+1 空网格。
- [ ] 结构性 emoji 已替换为 Element Plus 线性图标。
- [ ] hover/focus/active/disabled 四态可见，提交类操作有 loading 与防重复。
- [ ] 动效时长与曲线取自 3.5，且 `prefers-reduced-motion` 下直接落终值。
- [ ] GSAP 只用于进场 / 展开 / 列表重排三类，只动 transform 与 opacity，`prefers-reduced-motion` 下不注册 tween；hover 等高频微交互仍是 CSS。
- [ ] 卡片质感取自 L0–L3，同一列表内只用一个层级；白底页面没有用玻璃卡。
- [ ] 颜色全部来自 `tokens.css` 的 CSS 变量，没有新增裸色值或第二处主题变量定义。
