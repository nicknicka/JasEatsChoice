# 全站设计评审索引与共性问题汇总

- 覆盖范围：public 4 页 + user 34 页 + merchant 16 页 + admin 21 页 = 75 页。
- 依据：`docs/design-review/{public,user,merchant,admin}/*.md` 全部 75 份评审原文，以及主检出 `output/web-screenshots/` 下对应 75 张截图。
- 配套基调：`docs/design/DESIGN-BASELINE.md`（token、组件约定、正反例、一致/差异边界）。
- 全文中的"频次"= 命中的评审文件份数；同一页面可能同时命中多个问题。
- 本文件只汇总与裁决方向，不含实现代码。

## 一、75 页索引

| role | slug | 评审文件 | 一句话结论 |
|---|---|---|---|
| public | 01-login | [public/01-login.md](public/01-login.md) | 暖橙渐变与橙色主按钮的基调正确，但表单列近全宽、主按钮约 1400px、社交入口贴底，需收成 400px 居中窄列。 |
| public | 02-register | [public/02-register.md](public/02-register.md) | 两步注册第 1 步沿用暖橙体系，但表单全宽、进度条第二步无文字、底部约 460px 空档，需收窄并补常驻密码规则。 |
| public | 03-forgot-password | [public/03-forgot-password.md](public/03-forgot-password.md) | 找回密码第 1 步与注册同构，但表单全宽、第二步无标签、底部约 510px 空档、验证码无倒计时，需收窄并补可见状态。 |
| public | 04-merchant-register | [public/04-merchant-register.md](public/04-merchant-register.md) | 绿底满屏 + 白卡 + Element 默认蓝按钮 + 衬线标题，是公共端唯一断裂点，需整体并入暖橙体系。 |
| user | 01-home | [user/01-home.md](user/01-home.md) | 暖橙决策首页的推荐卡字段骨架不统一、热点红条压过主内容，需统一卡骨架并把热点条纳入暖色 token。 |
| user | 02-recommend | [user/02-recommend.md](user/02-recommend.md) | 推荐列表被顶部定位提示与进度条打断，缩略图裁掉关键文字、裸百分比含义不明，需降级定位提示并改「匹配度」文案。 |
| user | 03-merchants | [user/03-merchants.md](user/03-merchants.md) | 商家卡图片与头像加载失败态缺失，评分/距离/营业状态排在按钮之后，需修图片回退、调整信息顺序并统一主按钮色。 |
| user | 04-merchant-detail | [user/04-merchant-detail.md](user/04-merchant-detail.md) | 菜单页浮动购物车遮挡内容热区、菜品图与分类图标缺失，需改为底部固定购物车并补齐素材。 |
| user | 05-order-confirmation | [user/05-order-confirmation.md](user/05-order-confirmation.md) | 支付前确认页商品重复、金额重叠、原价/优惠/实付关系不清，需修明细并让底部支付栏成为唯一主动作。 |
| user | 06-today-recipe | [user/06-today-recipe.md](user/06-today-recipe.md) | 今日食谱空页有「添加食谱」与「从订单导入」两个同级入口，需把「添加食谱」设为主按钮、空状态只留一个下一步。 |
| user | 07-calorie | [user/07-calorie.md](user/07-calorie.md) | 卡路里环形图零数据无空状态、两个 2000 未区分目标值/已摄入/剩余，需补空状态与明确数字语义。 |
| user | 08-my-recipe | [user/08-my-recipe.md](user/08-my-recipe.md) | 食谱卡用彩色左边框充当状态、批量操作无显式模式，需改语义标签并显示已选数量。 |
| user | 09-diet-record | [user/09-diet-record.md](user/09-diet-record.md) | 空记录卡过高、CTA 不在视觉中心，需压缩空状态并说明记录后会得到什么结果。 |
| user | 10-wish-list | [user/10-wish-list.md](user/10-wish-list.md) | 想吃条字段与图片裁切不统一、绿色标签语义不明，需统一字段并让「去下单/查看详情」成为唯一主操作。 |
| user | 11-orders | [user/11-orders.md](user/11-orders.md) | 订单卡字段不统一、商品缩略图失效、蓝色筛选与主色冲突，需统一字段并把筛选改为主色。 |
| user | 12-order-detail | [user/12-order-detail.md](user/12-order-detail.md) | 已取消订单仍显示四步进度、商品明细是空卡，需改为「取消时间/原因 + 下一步」并补齐明细。 |
| user | 13-evaluate-order | [user/13-evaluate-order.md](user/13-evaluate-order.md) | 评价页缺被评菜品的名称/图片/数量、提交按钮不唯一，需补齐被评对象并固定底部唯一主按钮。 |
| user | 14-consume-history | [user/14-consume-history.md](user/14-consume-history.md) | 收入/支出/退款用红绿蓝三色编码，需改为「符号 + 文字角色」交易行并把 emoji 统计图标换成线性图标。 |
| user | 15-message-center | [user/15-message-center.md](user/15-message-center.md) | 消息类型与标签映射错误、时间未本地化、缺未读标记与空状态，需重做类型映射与时间格式。 |
| user | 16-system-notification | [user/16-system-notification.md](user/16-system-notification.md) | 系统通知与消息中心职责重叠、灰色 tab 重复，需并回统一消息框架或只保留「平台公告」一类。 |
| user | 17-chat | [user/17-chat.md](user/17-chat.md) | 聊天页浮动购物车遮挡、无会话时缺开始入口、头像无回退，需移除浮层并补空状态与输入区可读性。 |
| user | 18-contacts | [user/18-contacts.md](user/18-contacts.md) | 通讯录表头字段不明确、头像回退缺失、群发未与选择状态绑定，需明确字段并把群发做成选择态工具栏。 |
| user | 19-ai | [user/19-ai.md](user/19-ai.md) | AI 助手「未配置」没有转成不可用状态、订单卡红色取消标签误导，需隐藏或禁用对话控件并只留配置与重试。 |
| user | 20-settings | [user/20-settings.md](user/20-settings.md) | 设置塞进一张超宽白卡、开关与按钮主色不统一，需限宽并按账号/显示/通知/隐私/安全分组。 |
| user | 21-profile | [user/21-profile.md](user/21-profile.md) | 个人中心快捷入口顺序与颜色语义不固定，需固定「订单/钱包/饮食记录/设置」位置并统一中性卡面。 |
| user | 22-wallet-management | [user/22-wallet-management.md](user/22-wallet-management.md) | 充值与提现未定义为两个主操作、上方快捷卡与下方功能重复，需收敛主操作并删除重复卡。 |
| user | 23-wallet-transactions | [user/23-wallet-transactions.md](user/23-wallet-transactions.md) | 余额 banner + 筛选 + 三指标卡四层叠加把交易列表挤出首屏，需收敛为「余额摘要 + 一个筛选工具栏」。 |
| user | 24-payment-password-setup | [user/24-payment-password-setup.md](user/24-payment-password-setup.md) | 支付密码设置未做成单列步骤页、安全提示抢位，需限宽居中并把安全提示降为表单上方说明。 |
| user | 25-wallet-security | [user/25-wallet-security.md](user/25-wallet-security.md) | 安全等级 banner 未指出当前最需处理的风险项，需补「风险项 + 直达按钮」并统一「状态 + 一个动作」。 |
| user | 26-address | [user/26-address.md](user/26-address.md) | 重复标题未统一页头、默认地址用绿/蓝边框表达，需统一页头并用「默认」标签替代边框。 |
| user | 27-contact | [user/27-contact.md](user/27-contact.md) | 联系方式与在线反馈混在一张卡、主按钮颜色不统一，需拆成两个区块并统一主色末尾提交。 |
| user | 28-dish-detail | [user/28-dish-detail.md](user/28-dish-detail.md) | 菜品主图缺失、分类/热量/介绍为空、加购按钮不是暖橙，需补真实素材并统一主色。 |
| user | 29-my-collection | [user/29-my-collection.md](user/29-my-collection.md) | 收藏卡高度/图片比例/按钮色不统一，且残留测试文本与原始长 ID，需按类型定义摘要字段并清理脏数据。 |
| user | 30-tutorials | [user/30-tutorials.md](user/30-tutorials.md) | 教程页统计卡抢占首屏、卡片标签过多，需把统计收缩为一条摘要并只留类型/难度/评分。 |
| user | 31-tutorial-detail | [user/31-tutorial-detail.md](user/31-tutorial-detail.md) | 教程详情媒体区没有真实封面或播放器、正文标题未成第二锚点，需补封面或明确空态并强化标题。 |
| user | 32-tutorials-publish | [user/32-tutorials-publish.md](user/32-tutorials-publish.md) | 发布页首屏只剩取消、缺固定保存/发布主操作与完成度，需在页头或底部固定动作。 |
| user | 33-tutorials-my | [user/33-tutorials-my.md](user/33-tutorials-my.md) | 统计卡与状态 tabs 内容重复、失败封面无统一样式，需去重并补统一下架/失败封面。 |
| user | 34-hot-topic | [user/34-hot-topic.md](user/34-hot-topic.md) | 热点详情缺来源、时间与正文，空内容直接渲染白卡，需补齐或改为明确空状态并返回列表。 |
| merchant | 01-home | [merchant/01-home.md](merchant/01-home.md) | 首屏先讲店铺档案、经营数据与订单被压下，KPI 绿蓝双框、emoji 图标、深绿编辑按钮与蓝链混色，需改为「待处理 > 今日数据 > 店铺资料」驾驶舱。 |
| merchant | 02-orders | [merchant/02-orders.md](merchant/02-orders.md) | 4+1 KPI 网格失衡、重复刷新按钮稀释待处理订单，需修网格并在首屏暴露待处理订单与其操作。 |
| merchant | 03-today-orders | [merchant/03-today-orders.md](merchant/03-today-orders.md) | 零值统计与筛选占满首屏、「今日无订单」埋在下方，需 0 单时收起筛选并上移空状态。 |
| merchant | 04-menu | [merchant/04-menu.md](merchant/04-menu.md) | 首屏控制链过长、第一张真实菜单到中下部才出现，需压缩控制区并明确「新增/编辑」主次。 |
| merchant | 05-messages | [merchant/05-messages.md](merchant/05-messages.md) | 五个全为 0 的统计块压过消息列表、配色混乱，需 0 条时收起统计并把「暂无消息」提到筛选区正下方。 |
| merchant | 06-menu-edit | [merchant/06-menu-edit.md](merchant/06-menu-edit.md) | 表单只占卡片左三分之一、右侧大片空白、三个保存动作并列，需改两栏铺满并收敛为一个主按钮。 |
| merchant | 07-dish-management | [merchant/07-dish-management.md](merchant/07-dish-management.md) | 菜品卡约 430px 高、900px 首屏只看得到一张半、删除与普通按钮同色，需压缩卡高并把删除分离。 |
| merchant | 08-dish-edit | [merchant/08-dish-edit.md](merchant/08-dish-edit.md) | 单列窄表单造成卡片右半空白、总卡路里用红色，需改两栏铺满、补底部保存栏并让卡路里改中性色。 |
| merchant | 09-chat | [merchant/09-chat.md](merchant/09-chat.md) | 页面标题与侧栏命名不一致、搜索与未读筛选占用独立卡，需统一命名并把筛选并回左栏。 |
| merchant | 10-statistics | [merchant/10-statistics.md](merchant/10-statistics.md) | KPI 数值在左上、标签在右下相距一整张卡，图表标题重复，需重排卡内结构并删重复标题。 |
| merchant | 11-order-detail | [merchant/11-order-detail.md](merchant/11-order-detail.md) |「已完成」与四步进度分离、进度节点配色与列表状态不一致，需统一状态色并把订单号提到标题旁。 |
| merchant | 12-comments | [merchant/12-comments.md](merchant/12-comments.md) | 待回复评价被统计压到页面下部、评分柱未按星级分色，需把待回复列表提到首屏并统一指标色。 |
| merchant | 13-settings | [merchant/13-settings.md](merchant/13-settings.md) | 四类设置塞进一张超高白卡、缺标题容器、右侧空白，需补统一页头并按类分组收窄。 |
| merchant | 14-tutorials | [merchant/14-tutorials.md](merchant/14-tutorials.md) | 四块高饱和实色统计块占据顶部、核心教程列表完全缺席且无空状态，需改浅底卡并补列表空状态与创建入口。 |
| merchant | 15-wish-list-audit | [merchant/15-wish-list-audit.md](merchant/15-wish-list-audit.md) | 三张 0 值统计卡 + 一行 tab 后接居中小空状态，需统一统计卡样式并给「待审核」一处行动强调色。 |
| merchant | 16-ai | [merchant/16-ai.md](merchant/16-ai.md) | 助手回答被挤在约 300px 窄列、右侧近 700px 空白、首张卡被裁切，需修正对话区宽度失衡。 |
| admin | 01-dashboard | [admin/01-dashboard.md](admin/01-dashboard.md) | 5 卡 4+1 空网格、四色渐变图标块与全站概览卡不一致，需统一概览卡组件并修网格。 |
| admin | 02-users | [admin/02-users.md](admin/02-users.md) |「查看/禁用/删除」行操作同权同色、位置列折行，需收敛为「查看 + 更多」并把危险动作进菜单。 |
| admin | 03-merchants | [admin/03-merchants.md](admin/03-merchants.md) | 商家 ID 折行、注册时间带 ISO 的 T，需给足列宽并统一格式化时间。 |
| admin | 04-merchants-audit | [admin/04-merchants-audit.md](admin/04-merchants-audit.md) | 分页 Total 0 与实际 8 行不符、ID 折行、拒绝无二次确认，需修统计并补行内反馈。 |
| admin | 05-orders | [admin/05-orders.md](admin/05-orders.md) | 金额用红色且数字为衬线、下单时间为 ISO，需改中性无衬线金额并统一时间格式。 |
| admin | 06-dishes | [admin/06-dishes.md](admin/06-dishes.md) | 价格红色与衬线数字、菜品 ID 折行、创建时间 ISO，需统一金额字体与时间格式。 |
| admin | 07-dishes-audit | [admin/07-dishes-audit.md](admin/07-dishes-audit.md) | 空状态为英文、空数据仍显示分页条，需改中文空状态并隐藏分页。 |
| admin | 08-tutorials-manage | [admin/08-tutorials-manage.md](admin/08-tutorials-manage.md) | 统计卡整块渐变、ID 列裁切、删除与编辑同权，需统一概览卡并分离危险动作。 |
| admin | 09-tutorials-review | [admin/09-tutorials-review.md](admin/09-tutorials-review.md) | ID 折行导致行高翻倍、操作按钮无文字也无 tooltip，需补列宽并明确动作含义与行内反馈。 |
| admin | 10-topics | [admin/10-topics.md](admin/10-topics.md) | 日期格式与 03/05/06 等清单页不一致，需统一为同一格式。 |
| admin | 11-announcements | [admin/11-announcements.md](admin/11-announcements.md) | 表头「阅…」被裁切、Total 统计与实际不符、操作列过密，需修列宽与分页并收敛动作数量。 |
| admin | 12-withdrawals | [admin/12-withdrawals.md](admin/12-withdrawals.md) | 提现列表被趋势图压在下方、金额红色、趋势图缺空态，需把列表上提首屏并修金额与空态。 |
| admin | 13-recharges | [admin/13-recharges.md](admin/13-recharges.md) | 充值时间列裁切、金额红色、Total 统计不符，需修列宽并统一金额与分页统计。 |
| admin | 14-refunds | [admin/14-refunds.md](admin/14-refunds.md) | 退款 ID 折行、申请时间 ISO、「详情」灰色像禁用，需修列宽与时间并把详情改为常规链接色。 |
| admin | 15-settings | [admin/15-settings.md](admin/15-settings.md) |「基本设置」下方红色横线突兀、侧栏「系统配置/系统设置」命名不一，需改中性分隔并统一命名。 |
| admin | 16-profile | [admin/16-profile.md](admin/16-profile.md) | 创建时间仍为 ISO 未格式化，需统一为 `YYYY-MM-DD HH:mm`。 |
| admin | 17-password | [admin/17-password.md](admin/17-password.md) | 改密卡宽度与设置页不一致、右半屏空置，需统一设置域卡宽。 |
| admin | 18-roles | [admin/18-roles.md](admin/18-roles.md) |「创建时间」列裁切、删除动作无二次确认，需修列宽并补危险确认。 |
| admin | 19-permissions | [admin/19-permissions.md](admin/19-permissions.md) |「新建权限/刷新」未放在表格卡头右侧，需移入卡头以统一新增按钮位置。 |
| admin | 20-logs | [admin/20-logs.md](admin/20-logs.md) | 六张六色实色图标卡、操作时间列裁切、空状态为英文，需统一概览卡并修列宽与空态。 |
| admin | 21-statistics | [admin/21-statistics.md](admin/21-statistics.md) | 图表出现贴底直线 + 大面积空白、缺加载过渡与空态，需补图表空态与加载反馈。 |

## 二、跨页共性问题（按频次降序）

频次口径：一份页评审只要在「层级与构图 / 色彩与字体 / 与相邻页的一致性 / 改造建议」中明确提出该类问题，就计 1 次；不含仅把某色用于图表序列或非问题性提及。页码用 `role/slug` 表示，完整链接见上表。

1. **首屏被统计、筛选或控制区挤占：37 份。**
   - 问题：页面第一屏先出现 KPI、日期筛选、状态 tabs、刷新/导出按钮，真正的待办列表或主内容被推到首屏外；零值时统计卡仍然占满一屏。
   - 涉及页面：admin/01、admin/03、admin/08、admin/09、admin/11、admin/12、admin/21；merchant/01–merchant/12、merchant/14；user/01、user/02、user/03、user/08、user/09、user/10、user/11、user/12、user/13、user/16、user/23、user/26、user/28、user/30、user/31、user/32、user/33。
   - 收敛方向：首屏规则固定为「页头 → 待办/主内容 → 统计与筛选」；列表页统计数据压缩为一条摘要；0 数据时收起无意义统计与筛选，把空状态上移到筛选区正下方。见 `DESIGN-BASELINE.md` 4.5、4.8。

2. **蓝色控件、默认蓝与品牌主色冲突：36 份达到冲突级。**
   - 问题：Element Plus 默认蓝出现在主按钮、筛选选中态、链接、开关、侧栏选中态或消息气泡，和既有暖橙品牌色并存；同一页面还会出现绿、红、黄、蓝多套强调色。这里统计的是“蓝色已成为第二主色或第二状态语言”的高频冲突级页面；下列页面还包含同源默认蓝或蓝色强调的轻度命中，所以名单大于 36 份。
   - 涉及页面（冲突级 + 同源轻度命中）：public/04；merchant/01–merchant/16 全部；admin/01、admin/08、admin/09、admin/14、admin/15、admin/17、admin/18、admin/20、admin/21；user/04、user/05、user/08、user/10、user/11、user/13、user/17、user/18、user/19、user/20、user/22、user/23、user/24、user/25、user/26、user/27、user/28、user/29、user/31、user/32、user/33 等。
   - 收敛方向：`--brand-500 #ED6A42` 是全站唯一主操作色；蓝色只保留信息语义标签、消息气泡与必要的次级文字链接，不得做实心主按钮、普通筛选选中态或正文链接强调色。见 `DESIGN-BASELINE.md` 2、3.1、4.1、7。

3. **KPI / 统计卡形态分裂：32 份。**
   - 问题：有的页面用渐变、六色实色图标块或彩色边框，有的页面只有纯数字；4+1、5 列、6 张一行、数值与标签隔一整张卡等布局并存，统计卡还会抢走列表主内容。
   - 涉及页面：admin/01、admin/08、admin/20 等 13 份；merchant/01、merchant/02、merchant/03、merchant/05、merchant/10、merchant/12、merchant/14、merchant/15 等 11 份；user/06、user/07、user/09、user/14、user/22、user/23、user/33、user/34 等 8 份。
   - 收敛方向：统一为浅底 + Element Plus 线性图标 + `--font-num` 数值 + 标签；4 个一行，5 个改「主指标 + 次级条」；禁止渐变图标块、六色图标与 4+1 空网格。见 `DESIGN-BASELINE.md` 4.8。

4. **金额、价格的颜色与数字字体不统一：25 份。**
   - 问题：金额被染成红/绿/蓝，或用衬线数字；收入、支出、退款同时用三种颜色编码，优惠、原价、实付的关系不清楚。
   - 涉及页面：admin/03、admin/04、admin/05、admin/06、admin/12、admin/13、admin/14、admin/17、admin/18、admin/21；merchant/02、merchant/07、merchant/08、merchant/11、merchant/16；user/01、user/04、user/05、user/11、user/12、user/13、user/14、user/22、user/23、user/28。
   - 收敛方向：金额统一中性深色，正负用 `+`/`-`，方向用「收入 / 支出 / 退款」文字角色；金额与数值使用 `--font-num` + `tabular-nums`、右对齐，禁止红绿蓝三色同时编码。见 `DESIGN-BASELINE.md` 5.2。

5. **表格行操作拥挤、列溢出与时间折行：22 份。**
   - 问题：查看、编辑、禁用、删除同权同色；操作列过密；ID、时间、金额列折行导致行高翻倍；标题或表头被裁切；空数据仍显示分页，Total 与实际行数不符。
   - 涉及页面：admin/02、admin/03、admin/04、admin/05、admin/06、admin/08、admin/09、admin/10、admin/11、admin/12、admin/13、admin/14、admin/15、admin/18、admin/19、admin/20；public/04；user/12。
   - 收敛方向：行操作最多「查看 + 更多」，危险动作进入「更多」并二次确认；ID、时间、金额列不折行；空数据隐藏分页且 Total 等于实际行数。见 `DESIGN-BASELINE.md` 4.3。

6. **同一状态跨页颜色不一致：21 份。**
   - 问题：已完成在列表用绿色、详情用蓝色；待审核、拒绝、退款、营业等状态在列表、详情、KPI、进度条之间各用一套颜色；状态色还被当作金额或装饰色使用。
   - 涉及页面：merchant/01、merchant/02、merchant/03、merchant/04、merchant/07、merchant/11、merchant/16；user/03、user/04、user/07、user/08、user/10、user/11、user/12、user/29、user/30、user/33；admin/02、admin/03、admin/05、admin/14。
   - 收敛方向：全站唯一状态表：待处理黄、进行中蓝、已完成绿、已取消红、草稿灰；同一状态在列表、详情、进度节点和反馈中必须同色。见 `DESIGN-BASELINE.md` 3.1。

7. **页头与标题条语言分裂：20 份。**
   - 问题：merchant 同时存在深绿标题条、白卡标题区、无标题裸文字；admin/user 也有面包屑、重复标题、侧栏名称与页面标题不一致的问题，页面切换像换了一套产品。
   - 涉及页面：merchant/01–merchant/13 多数页面；admin/08、admin/09、admin/15；user/26、user/32、user/34。
   - 收敛方向：统一「标题 + 可选描述 + 右侧动作区」的标题容器；merchant/admin 用紧凑版；侧栏名称与页面标题严格一致；不得再用深绿整条标题条。见 `DESIGN-BASELINE.md` 4.7。

8. **图片失败、占位与封面缺失：20 份。**
   - 问题：菜品图、商家头像、商品缩略图、教程封面加载失败时出现破图、空白、灰块或裁掉关键文字；不同列表没有一致的默认图与图片比例。
   - 涉及页面：merchant/07、merchant/08、merchant/13；user/01、user/03、user/04、user/05、user/10、user/11、user/13、user/28、user/29、user/31、user/32、user/33；admin/02、admin/07、admin/16。
   - 收敛方向：所有业务图片固定比例与 `object-fit: cover`，失败统一显示中性占位图 + 线性图标；菜品卡 4:3、头像 1:1、教程封面 16:9。见 `DESIGN-BASELINE.md` 4.2。

9. **空状态缺失、英文或没有替代动作：18 份。**
   - 问题：出现 `No Data`、纯空白卡、居中小字或「暂无数据」，没有解释为什么空、下一步做什么；空状态与 0 值统计卡、筛选条重复堆叠。
   - 涉及页面：admin/01、admin/07、admin/20；merchant/01、merchant/03、merchant/05、merchant/14、merchant/15；user/06、user/07、user/09、user/15、user/16、user/17、user/19、user/29、user/31、user/34。
   - 收敛方向：统一为「线性图标 + 中文说明 + 一个替代动作」；0 数据时先收起统计和筛选；列表内空状态不超过 240px。见 `DESIGN-BASELINE.md` 4.5。

10. **表单宽度失衡与右侧大面积空白：17 份。**
    - 问题：认证表单铺满全宽、输入框与主按钮不同宽；设置/编辑表单又缩在左侧三分之一，右侧永远空白；三个保存动作并列，用户不知道哪个才是主操作。
    - 涉及页面：public/04；merchant/06、merchant/08、merchant/13、merchant/16；admin/17 等；user/17、user/24、user/27、user/32。
    - 收敛方向：认证类表单固定 `--form-width-auth: 400px` 居中；设置/编辑类使用 `--form-width-settings: 720px` 或两栏铺满；长表单按业务分组，只保留一个主提交动作。见 `DESIGN-BASELINE.md` 4.4。

11. **同类卡片字段骨架与高度不统一：11 份。**
    - 问题：同一列表内的推荐卡、食谱卡、订单卡、收藏卡字段顺序、图片裁切、卡片高度和操作区位置不同，用户只能逐张重新找信息。
    - 涉及页面：user/01、user/08、user/10、user/11、user/15、user/21、user/29；merchant/07、merchant/08、merchant/15；public/03。
    - 收敛方向：列表卡固定为「图片 → 标题 → 状态 → 关键数值 → 次要信息 → 操作区」；同组卡片等高，缺项留空位或省略，不改变字段相对顺序。见 `DESIGN-BASELINE.md` 4.2。

12. **结构性 emoji 与线性图标混用：9 份。**
    - 问题：emoji 被用于统计卡、菜单、字段标签、消息类型和钱包入口，跨系统渲染不一致，且和后台资金/经营场景的专业度冲突。
    - 涉及页面：merchant/01、merchant/04、merchant/06、merchant/08、merchant/13；user/14、user/20、user/22、user/27。
    - 收敛方向：结构性图标全部替换为 `@element-plus/icons-vue` 线性图标，同一语义四端同图标；保留 Logo 与头像，emoji 不参与信息架构。见 `DESIGN-BASELINE.md` 4.9。

13. **时间与日期格式不统一：8 份。**
    - 问题：清单页混用 ISO `T`、斜杠日期、中文日期和不带时分的相对时间；同一时间列在相邻页面格式不同。
    - 涉及页面：admin/03、admin/05、admin/06、admin/10、admin/14、admin/16；user/15、user/34。
    - 收敛方向：需要时分统一 `YYYY-MM-DD HH:mm`，只需日期统一 `YYYY-MM-DD`；禁止 ISO 的 `T`。见 `DESIGN-BASELINE.md` 6.1。

14. **浮层遮挡内容热区或末行：3 份。**
    - 问题：浮动购物车遮住菜品与分类热区，聊天/输入类浮层压住末行，用户需要额外滚动或误点。
    - 涉及页面：user/04、user/17；admin/18 的弹层遮挡属于同一边界问题。
    - 收敛方向：浮动控件改为底部固定栏或预留安全距离，禁止覆盖可点击内容与最后一行；打开弹层时锁定背景滚动。见 `DESIGN-BASELINE.md` 4.6。

## 三、角色之间的一致性问题（去重归并）

四个评审 agent 的页面级问题在跨角色尺度上可归并为以下 11 组。每组都给出一句最终裁决；实现时以 `DESIGN-BASELINE.md` 的 token 与组件约定为准。

1. **主色与按钮层级：public 暖橙、merchant 深绿、admin 默认蓝并不是三种品牌，只是三套遗留值。**
   - 差异：public/01–03 用暖橙；public/04 用绿底 + 默认蓝；merchant/01 用深绿编辑按钮 + 蓝色链接；admin 侧栏和主按钮沿用 Element 蓝；部分页面同屏出现多个实心主按钮。
   - 裁决：保留暖橙 `--brand-500` 作为全站唯一品牌色和唯一主操作色；绿/红/黄/蓝只做状态语义；每屏最多一个实心主按钮，危险动作独立且二次确认。

2. **状态与反馈色：同一业务状态在不同端被重新解释。**
   - 差异：merchant/11 已完成在列表绿、详情蓝；admin/14 退款出现橙、灰、浅红、绿、浅紫蓝五种标签；user/11、user/26 用蓝/绿边框表达筛选或默认态，却没有图例。
   - 裁决：状态色是一套跨角色词典，必须按统一状态表映射；筛选选中态用品牌橙，默认/选中不用绿蓝边框暗猜；反馈色只在操作结果中出现。

3. **金额与数字排版：公共端没有明确规则，user 用交易角色分色，admin 用红色和衬线数字。**
   - 差异：user/14、user/23 用红绿蓝编码收入/支出/退款；admin/05、admin/06、admin/12、admin/13 金额红色且为衬线；merchant/08 总卡路里也用红色。
   - 裁决：金额和统计数值统一 `--font-num` + `tabular-nums`、中性深色、右对齐；正负用符号，收入/支出/退款用文字角色，不给数字本身染色。

4. **表格与列表行操作：admin 把动作全铺开，merchant 把危险动作混进普通按钮，user 卡片又把操作放在不同角落。**
   - 差异：admin/02 查看/禁用/删除同权；admin/04、admin/18 删除或拒绝无二次确认；admin/19 新增按钮不固定；merchant/07 下架/编辑/菜单关联/删除同款浅蓝；user 各列表卡的「详情/收藏/去下单」位置不一致。
   - 裁决：列表操作只保留「查看 + 更多」两个可见入口；新增按钮固定在卡头右侧；危险动作收进「更多」并二次确认；卡片操作区固定在右下或卡片底部同一位置。

5. **空状态：有的页面英文、有的纯空白、有的把 0 值统计放在空状态上方。**
   - 差异：admin/01 出现 `No Data`，admin/07、admin/20 无中文引导；merchant/03、merchant/05、merchant/14、merchant/15 零值统计仍占据大面积；user/06、user/07、user/09、user/34 空卡过高或没有替代动作。
   - 裁决：统一「图标 + 中文说明 + 一个替代动作」；0 数据时先收起统计与筛选，空状态上移；禁止英文无信息量文案与重复统计。

6. **日期与数字格式：admin 清单页保留 ISO，user 消息/热点又不显示完整时间。**
   - 差异：admin/03、admin/05、admin/06、admin/14、admin/16 出现 `T`；admin/10 与相邻页面不一致；user/15、user/34 缺本地化或完整时间。
   - 裁决：需要时分统一 `YYYY-MM-DD HH:mm`；只需日期统一 `YYYY-MM-DD`；同一业务字段在四端使用同一格式。

7. **页头与命名：merchant 深处有三套标题语言，admin 有侧栏命名不一致，user 有重复标题与裸标题。**
   - 差异：merchant/01–13 混用深绿标题条、白卡标题区、无标题；merchant/09、admin/15 侧栏和页面名称不一致；user/26、user/32、user/34 标题层级不完整。
   - 裁决：统一标题容器，侧栏标签与页面标题一致；后台紧凑、前台舒展，结构不换；不得使用深绿整条标题条或重复标题。

8. **卡片骨架与图片：蔬菜图、商家头像、教程封面和订单缩略图在四端各自裁切、各自失败。**
   - 差异：user/01、user/10、user/11、user/29 字段顺序和图片比例不同；merchant/07、merchant/08 卡片过高；admin/02、admin/07 头像/封面无回退；部分页面加载失败只剩破图。
   - 裁决：卡片字段顺序、图片比例、缺失占位和操作区位置全站一致；失败显示中性占位图 + 线性图标；同组卡片等高。

9. **图标体系：后台用 Element 线性图标，部分商家和用户页用 emoji，教程/钱包又用彩色插画，形成三套语言。**
   - 差异：merchant/01、merchant/04、merchant/06、merchant/08、merchant/13 和 user/14、user/20、user/22、user/27 用 emoji 承担结构性职责；admin/01、admin/08、admin/20 用彩色渐变图标块。
   - 裁决：结构性图标统一 Element Plus 线性图标，同一语义四端同图标；渐变图标块删除；品牌 Logo 与头像例外。

10. **表单宽度与密度：认证页太宽、设置页太窄、后台编辑页右侧空白，public 04 还混入另一套控件语言。**
    - 差异：public/01–03 表单接近全宽，public/04 白卡表单语言不同；merchant/06、merchant/08 单列窄表单留半屏空白；merchant/13、admin/17 卡宽不一致；user/24、user/27 小表单贴左上。
    - 裁决：只保留 `--form-width-auth` 与 `--form-width-settings` 两档宽度；认证居中窄列，设置/编辑限宽或两栏铺满；标签、输入框、主按钮同列宽，长表单分组。

11. **反馈与浮层：四端都强调 hover/点击，但缺少统一的 focus、loading、禁用和遮挡边界。**
    - 差异：admin/01 概览卡无 hover；admin/21 图表无加载过渡；user/04、user/17 浮层遮挡内容；多数页面静态截图无法验证键盘焦点与 reduced-motion。
    - 裁决：所有可点击元素必须有 hover、focus-visible、active、disabled 四态；提交类动作有 loading 与防重复；异步列表有骨架屏；动效只用本基线 token，并在 `prefers-reduced-motion` 下落终值。
