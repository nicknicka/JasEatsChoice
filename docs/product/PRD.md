# 佳食宜选 - 餐饮下单系统PRD补充文档
**文档版本**：1.1
**生成日期**：2025-11-26
**参考文件**：《产品需求说明书（PRD）.md》、系统现有功能实现

---

## 一、文档说明
本文档基于系统现有功能实现，对原PRD进行补充和完善，确保PRD与实际实现保持一致。

---

## 二、已实现功能补充

### 2.1 新增功能模块与页面

#### 2.1.1 商家端 - 评价中心
- **页面**：`/merchant/home/comments`
- **功能**：
  - 评价统计概览（平均评分、总评价数、已回复/未回复数量）
  - 评分分布可视化展示
  - 评价筛选（状态：全部/已回复/未回复；评分：1-5分）
  - 评价搜索（支持订单号、用户名称、菜品名称）
  - 评价回复与修改
- **实现状态**：✅ 已完成

#### 2.1.2 用户端 - 我的收藏
- **页面**：`/user/home/my-collection`
- **功能**：
  - 收藏商家和菜品
  - 收藏列表管理
  - 快速跳转到商家详情或菜品页面
- **实现状态**：✅ 已完成

#### 2.1.3 用户端 - 制作教程与指南
- **页面**：`/user/home/tutorials`
- **功能**：
  - 展示食谱制作教程
  - 图文并茂的步骤说明
  - 搜索和筛选功能
- **实现状态**：✅ 已完成

#### 2.1.4 商家端 - 今日订单
- **页面**：`/merchant/home/today-orders`
- **功能**：
  - 今日订单列表
  - 订单状态实时更新
  - 快速操作订单
- **实现状态**：✅ 已完成

---

## 三、界面实现与设计补充

### 3.1 用户端页面实现状态

| 页面名称 | 实现路径 | 设计匹配度 | 实现状态 |
| --- | --- | --- | --- |
| AI聊天助手 | `user/AI.vue` | 100% | ✅ |
| 查看所有订单 | `user/Orders.vue` | 100% | ✅ |
| 管理地址 | `user/Address.vue` | 100% | ✅ |
| 今日食谱 | `user/TodayRecipe.vue` | 100% | ✅ |
| 卡路里统计 | `user/Calorie.vue` | 100% | ✅ |
| 联系客服 | `user/Contact.vue` | 100% | ✅ |
| 商家查找 | `user/MerchantList.vue` | 100% | ✅ |
| 设置页面 | `user/Settings.vue` | 100% | ✅ |
| 我的食谱 | `user/MyRecipe.vue` | 100% | ✅ |
| 系统通知详情 | `user/SystemNotification.vue` | 100% | ✅ |
| 消费记录 | `user/ConsumeHistory.vue` | 100% | ✅ |
| 消息中心 | `user/MessageCenter.vue` | 100% | ✅ |
| 用户中心 | `user/Profile.vue` | 100% | ✅ |
| 用户端首页 | `user/Home.vue` | 100% | ✅ |
| 我的收藏 | `user/MyCollection.vue` | 95% | ✅ |
| 制作教程与指南 | `user/Tutorials.vue` | 90% | ✅ |

### 3.2 商家端页面实现状态

| 页面名称 | 实现路径 | 设计匹配度 | 实现状态 |
| --- | --- | --- | --- |
| 菜单管理 | `merchant/Menu.vue` | 100% | ✅ |
| 菜品管理 | `merchant/DishManagement.vue` | 100% | ✅ |
| 今日订单 | `merchant/TodayOrders.vue` | 100% | ✅ |
| 商家中心 | `merchant/Home.vue` | 100% | ✅ |
| 我的店铺 | `merchant/MyShop.vue` | 100% | ✅ |
| 营业统计 | `merchant/Statistics.vue` | 100% | ✅ |
| 评价中心 | `merchant/Comments.vue` | 100% | ✅ |

---

## 四、技术实现调整与优化

### 4.1 路由结构优化
- 采用嵌套路由结构，将用户端和商家端页面统一管理
- 增强路由导航守卫，实现自动角色识别与切换
- 实现路由变化自动更新角色的功能

### 4.2 组件复用与统一
- 实现CommonHome组件，统一用户端和商家端的布局结构
- 规范组件命名和样式风格
- 实现响应式设计，适配不同屏幕尺寸

### 4.3 状态管理与数据处理
- 采用Vue 3 Composition API实现组件内部状态管理
- 使用Element Plus组件库实现统一的UI风格
- 接口失败或返回空时一律显示空态/错误态并提供重试入口，禁止回填 mock 数据（依据 `docs/technical/功能真实化改造计划.md` M5 决策）

### 4.4 基于阿里云平台的短信发送服务
- **功能**：实现手机验证码发送功能，用于用户修改手机号时的身份验证
- **技术**：阿里云短信服务API
- **配置**：通过application.yml配置短信签名、模板CODE和超时时间
- **参数**：
  - `phone`：接收短信的手机号
  - `code`：6位随机验证码
- **特点**：即使发送失败也返回成功响应，防止暴力攻击

---

## 五、功能实现状态修正

### 5.1 用户注册页面
- **页面路径**：`/register`
- **接口**：`POST /v1/users/register`
- **实现状态**：✅ 已实现
- **实现位置**：前端 `apps/client/src/renderer/src/views/user/Register.vue`；后端 `apps/server/src/main/java/com/xx/jaseatschoicejava/controller/UserController.java`
- **说明**：前端提供注册表单、验证码和提交逻辑；后端校验验证码与手机号/邮箱唯一性，并将新用户写入数据库。

### 5.2 商家角色搜索功能
- **实现状态**：✅ 已实现
- **实现位置**：前端 `apps/client/src/renderer/src/components/chat/dialogs/MerchantSelectDialog.vue:194`（`searchMerchantsFromApi`）调用 `merchantApi.getMerchants({ keyword })`；后端 `apps/server/src/main/java/com/xx/jaseatschoicejava/controller/MerchantController.java:185` 接收 `keyword` 参数并按商家名/菜品名过滤。
- **说明**：源码中未找到「商家搜索 TODO」标记，原「部分实现（前端代码中有TODO）」结论已不成立。

---

## 六、系统整体状态

### 6.1 已实现功能覆盖率

下表按模块逐条给出「已实现 / 部分实现 / 未实现」，每项均可指到实现位置或缺口跟踪 issue。原「五个模块均 100%」的概括已废止（依据：小程序商家统计页无数据源、多处失败回填假数据、积分体系后端缺失等）。

| 模块 | 状态 | 依据 / 位置 | 缺口 |
| --- | --- | --- | --- |
| 个性化饮食推荐与日常管理 | 部分实现 | 实时推荐已真实推送（`RealtimeRecommendationServiceImpl.sendToUser`）；卡路里统计已改为查真实订单与卡路里记录（`UserStatisticsServiceImpl.getCaloriesStatistics`） | 小程序 `pages-user/calorie/record.vue:252-269` 在接口失败时仍回填写死的餐次与记录（该页不在 #561 清单内，需单独立项） |
| 菜品定制与订单全链路管理 | 部分实现 | 下单、订单列表、状态流转等主链路已实现（见 `tests/specs/04-order.spec.js`） | 小程序 `pages-user/order/confirm` 曾用写死购物车，已由 PR #581 修复；无其它已知缺口 |
| 双角色权限管理 | 已实现 | 嵌套路由 + 角色守卫自动识别切换（见 4.1）；后端鉴权默认拒绝并显式登记公开接口（PR #576） | 无 |
| 外部内容整合与社交互动 | 已实现 | 群订单会话同步消息真实落库并推送成员（`GroupOrderChatServiceImpl.syncMessage`）；聊天历史失败不再回填 mock、内容提取失败改错误态（PR #581） | 无 |
| AI能力集成 | 已实现 | AI 聊天、内容提取、菜品识别、食谱优化等经 `LangChain4jConfig` 供应商抽象接入；`ai/advanced` 整页写死已由 PR #581 修复 | 无 |
| 商家运营工具 | 部分实现 | 商家营业统计已接入 `/v1/merchant/{id}/statistics`（`MerchantController.java:925`，PR #581）；评价中心、菜单管理已实现 | 小程序 `pages-merchant/profile/index.vue:132-147` 商家信息与统计为写死初值，失败分支「保持默认数据」（`:171`、`:191`），未接真实接口 |
| 钱包与积分体系 | 部分实现 | 钱包充值/提现/平台币支付已实现（`WalletService`、`PaymentController`） | **积分体系后端从未实现**：`apps/server` 内无积分实体、字段与接口（`Wallet.java` 无 points/integral 字段）；小程序积分页为「暂未开放」占位（`pages-user/profile/integral.vue`）。与审计清单 M-09 结论一致 |

### 6.2 系统特点
- 界面遵循现代 UI 设计规范
- 核心业务链路（下单、订单、钱包、聊天、AI）已实现
- 代码结构清晰，易于维护
- 双角色权限管理已实现（路由守卫 + 后端鉴权默认拒绝）
- 社交互动与订单协同已实现
- 失败路径统一为空态/错误态，不回填 mock 数据（依据 `docs/technical/功能真实化改造计划.md` M5）

---

## 七、后续迭代建议

1. **优化用户注册流程**：在已实现注册能力的基础上，持续改善异常提示和账号安全性
2. **优化AI推荐算法**：提升个性化推荐的准确性
3. **增强数据统计功能**：增加更多维度的营业统计
4. **完善移动端适配**：确保在移动设备上的良好体验
5. **统一支付渠道口径**：既定决策为只保留平台币支付；微信/支付宝接口保留但固定返回「暂未开放」（`PaymentController.java:201`、`:210`），文档与前端入口均不再宣称支持更多支付渠道

**文档更新说明**：本补充文档将随系统迭代不断更新，确保PRD与实际实现保持一致。
