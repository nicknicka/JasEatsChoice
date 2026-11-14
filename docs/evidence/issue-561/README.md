# #561 / #562 断网态取证

## 为什么用 H5 产物而不是微信模拟器

`#561` 的验收标准里有一条「内容提取、评价提交两个高风险页在 PR 中附断网截图」。
本机**没有安装微信开发者工具**（`/Applications` 下只有微信聊天客户端），
且该工具首次启动强制微信扫码登录，无法在自动化环境里产出模拟器截图。

因此改用 **uni-app 官方 H5 产物**（同一份 `src/` 组件代码，经官方编译器产出）
在 Chromium 移动端视口（390×844 @2x）中渲染，并把后端 API 全部 `abort`
（`net::ERR_INTERNET_DISCONNECTED`，等价于断网）。

**证据强度**：证明的是「同一份组件代码在接口失败时不再回填假数据、改为空态/错误态」。
它不宣称是微信模拟器的像素级截图。

## 复现方式

```bash
cd apps/miniapp
# 1) 产出 H5 产物（需临时在 devDependencies 声明 @dcloudio/uni-h5 与 @dcloudio/uni-h5-vite，
#    当前 package.json 未声明这两个 H5 平台包，属独立问题，未在本 PR 改）
npm run build:h5
# 2) 静态服务
python3 -m http.server 8931 --directory dist/build/h5
# 3) 用 Playwright 打开各页，route abort 掉所有后端域名，截图
```

## 截图清单

| 截图 | 页面 | 断网时呈现 | 判定 |
|---|---|---|---|
| `screenshots/01-content-extract-offline.png` | 内容提取【高风险】 | 「网络连接失败」+「重新提取」按钮 | ✅ |
| `screenshots/01b-content-extract-offline-full.png` | 同上（整页） | 无结果卡片，零假数据 | ✅ |
| `screenshots/help.png` | 帮助中心 | 「网络连接失败」+「重试」 | ✅ |
| `screenshots/merchant-list.png` | 商家列表 | 空态（不再 `generateMockData`） | ✅ |
| `screenshots/ai-advanced.png` | AI 智能分析 | 「网络连接失败」+「重试」 | ✅ |
| `screenshots/order-confirm.png` | 订单确认 | 「未获取到订单商品」+「返回选购」 | ✅ |
| `screenshots/merchant-statistics.png` | 营业统计 | 「未获取到商家信息，请重新登录」+「重试」 | ✅ |
| `screenshots/merchant-finance.png` | 账户流水 | 余额全 0 +「网络连接失败」+「重试」 | ✅ |
| `screenshots/merchant-comment.png` | 评价管理 | 「评价统计加载失败，下拉可重试」 | ✅ |
| `screenshots/merchant-wishlist.png` | 需求池 | 空态「暂无需求」 | ✅ |
| `screenshots/merchant-chat.png` | 消息中心 | 无假系统通知；空态 | ✅ |
| `screenshots/conversation-list.png` | 会话列表 | 空态 | ✅ |
| `screenshots/review-submit.png` | 提交评价【高风险】 | 见下方说明 | ⚠️ |

## ⚠️ review-submit 的 H5 渲染限制（非本 PR 缺陷）

`review-submit` 的「评价对象加载失败 + 重试」块在 H5 下**渲染不出来**，控制台报：

```
TypeError: Cannot assign to read only property '_' of object '#<Object>'
  at updateSlots (@dcloudio/uni-h5-vue/dist/vue.runtime.esm.js:5820)
```

**根因**：`@dcloudio/uni-h5-vue` 内嵌的是 **Vue 3.4.21** 运行时，
而项目 `package.json` 把 Vue 全家桶锁在 **3.5.13**（含 `overrides`）。
两者 slot 归一化实现不一致，导致带 `v-if`/`v-else-if` 兄弟链的块在更新时抛错、整棵子树不渲染。

**与本次改动无关的决定性证据**：
把该页换成**改动前**的版本（接口失败会回填「宫保鸡丁 / 老王家常菜」的那份）重新构建，
同一位置**同样渲染不出** `.target-info` —— 连假数据都显示不出来。
即该块在 H5 下从一开始就渲染失败，不是「因为我删了假数据才空」。

**目标平台（微信小程序）产物是正确的**，见下节：`index.wxml` 里
`wx:elif="{{f}}"` 错误态分支存在，且产物中「宫保鸡丁/老王家常菜」计数为 0。

## 目标平台（mp-weixin）产物级证据

`artifact-report.json`：逐页核对**源码**与**微信小程序产物**中的假数据标记
（`宫保鸡丁 / 老王家常菜 / 2026-03-17 / generateMock / 模拟数据 / 15860`），
以及产物中错误态/空态分支是否存在。

结果：18 条全部 `PASS`（源码计数 0、产物计数 0、错误态分支存在）。

## 本轮补修（截图验证的副产物）

批量截图暴露出上一轮**漏改的页头统计**——列表清了，但页头数字仍是写死的：

| 页面 | 漏改内容 |
|---|---|
| `pages-merchant/comment/index.vue` | `ratingStats` 写死 `4.8分 / 156条 / 96% / 142 / 14` + 星级分布 `120/25/8/2/1`；`filterTabs` 写死 count `156/14/142/89/3` |
| `pages-merchant/chat/index.vue` | 系统通知写死「您有新的订单待处理 / 5分钟前 / 未读 3」；`filterTabs` 写死 unread `5/2` |

修复后：

- `comment`：初值全归零；统计只在接口成功后渲染（`statsLoaded` 守卫），
  失败显示「评价统计加载失败，下拉可重试」，不再显示任何数字；
  `filterTabs` 计数改由接口回填，失败归零。
- `chat`：无真实通知数据时整块不渲染；`filterTabs` 未读初值归零，
  失败时归零（`updateUnreadCounts` 本就从真实列表算，保留）。

复验：目标平台产物中 `156 / 4.8 / 96 / 142` 与「您有新的订单待处理 / 5分钟前」
计数均为 **0**；H5 断网复验截图确认两页不再显示任何写死数字。

## 未纳入本轮的同类问题（清单外）

以下页面也有写死业务值，但**不在 #561 / #562 的验收清单内**，本轮未动：

`pages-merchant/profile/index.vue`（`rating: 4.8`、`reviewCount: 156`、余额 `5,280.00`）、
`pages-merchant/home/analytics.vue`（`avgRating = ref(4.8)`）、
`pages-user/dish/detail/index.vue`、`pages-user/merchant/detail/index.vue`、
以及 `order/detail`、`search`、`group-order/{index,detail,settle}`、
`profile/{edit,shop}`、`wishlist/audit`、`calorie/record`、`dish/customize` 等。
