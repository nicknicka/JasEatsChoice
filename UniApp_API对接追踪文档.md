# UniApp 后端API对接追踪文档

> **项目**：佳食宜选 UniApp小程序
> **创建时间**：2026-03-19
> **文档目的**：追踪所有需要对接的后端API接口，确保前端功能完整实现

---

## 📊 整体进度统计

| 模块 | 已定义API | 已对接页面 | 完成度 | 优先级 |
|------|-----------|-----------|--------|--------|
| 用户模块 | 13个 | 0% | 0% | P0 |
| 菜品模块 | 10个 | 0% | 0% | P0 |
| 订单模块 | 11个 | 0% | 0% | P0 |
| 商家模块 | 14个 | 0% | 0% | P0 |
| 食谱模块 | 11个 | 0% | 0% | P1 |
| 地址模块 | 8个 | 0% | 0% | P0 |
| AI模块 | 8个 | 0% | 0% | P1 |
| 聊天模块 | 13个 | 0% | 0% | P0 |
| 优惠券模块 | 8个 | 0% | 0% | P1 |
| 评价模块 | 0个 | 0% | 0% | P0 |
| 收藏模块 | 0个 | 0% | 0% | P0 |
| 浏览历史模块 | 0个 | 0% | 0% | P2 |
| 钱包模块 | 0个 | 0% | 0% | P1 |
| 心愿单模块 | 0个 | 0% | 0% | P1 |
| 团购模块 | 0个 | 0% | 0% | P2 |
| 通知模块 | 0个 | 0% | 0% | P2 |
| 反馈模块 | 0个 | 0% | 0% | P2 |

---

## 🔴 P0 优先级（核心功能）

### 1. 用户模块 (user.js)

**API文件路径**：`src/api/modules/user.js`

#### 已定义API列表
- ✅ `login` - 用户登录（验证码）
- ✅ `register` - 用户注册
- ✅ `sendCode` - 发送验证码
- ✅ `wechatLogin` - 微信授权登录
- ✅ `getUserInfo` - 获取用户信息
- ✅ `updateUserInfo` - 更新用户信息
- ✅ `changePassword` - 修改密码
- ✅ `resetPassword` - 重置密码
- ✅ `uploadAvatar` - 上传头像
- ✅ `getUserStats` - 获取用户统计数据
- ✅ `completeProfile` - 完善身体数据
- ✅ `getUserGoals` - 获取用户饮食目标
- ✅ `deleteUser` - 删除用户

#### 需要对接的页面
- [ ] `pages/login/index.vue` - 登录页面
  - [ ] 对接 `login` API
  - [ ] 对接 `sendCode` API
  - [ ] 对接 `wechatLogin` API
- [ ] `pages/register/index.vue` - 注册页面
  - [ ] 对接 `register` API
  - [ ] 对接 `sendCode` API
- [ ] `pages-user/profile/index.vue` - 用户中心
  - [ ] 对接 `getUserInfo` API
  - [ ] 对接 `getUserStats` API
- [ ] `pages-user/profile/edit/index.vue` - 编辑资料
  - [ ] 对接 `updateUserInfo` API
  - [ ] 对接 `uploadAvatar` API
  - [ ] 对接 `completeProfile` API

#### 待补充API
- [ ] `logout` - 用户登出
- [ ] `bindPhone` - 绑定手机号
- [ ] `verifyPassword` - 验证密码

---

### 2. 菜品模块 (dish.js)

**API文件路径**：`src/api/modules/dish.js`

#### 已定义API列表
- ✅ `getList` - 获取菜品列表
- ✅ `getDetail` - 获取菜品详情
- ✅ `getMerchantDishes` - 获取商家菜品列表
- ✅ `getRecommend` - 获取推荐菜品
- ✅ `search` - 搜索菜品
- ✅ `getCategories` - 获取菜品分类
- ✅ `getIngredients` - 获取菜品食材
- ✅ `getNutrition` - 获取菜品营养信息
- ✅ `create` - 创建菜品（商家端）
- ✅ `update` - 更新菜品（商家端）
- ✅ `delete` - 删除菜品（商家端）
- ✅ `setAvailability` - 上架/下架菜品（商家端）

#### 需要对接的页面
- [ ] `pages-user/home/index.vue` - 首页
  - [ ] 对接 `getRecommend` API（推荐菜品）
- [ ] `pages-user/search/index.vue` - 搜索页面
  - [ ] 对接 `search` API
- [ ] `pages-user/merchant/detail/index.vue` - 商家详情
  - [ ] 对接 `getMerchantDishes` API
  - [ ] 对接 `getCategories` API
- [ ] `pages-user/dish/detail/index.vue` - 菜品详情
  - [ ] 对接 `getDetail` API
  - [ ] 对接 `getIngredients` API
  - [ ] 对接 `getNutrition` API
- [ ] `pages-merchant/dish/index.vue` - 商家端菜品管理
  - [ ] 对接 `getMerchantDishes` API
  - [ ] 对接 `delete` API
  - [ ] 对接 `setAvailability` API
- [ ] `pages-merchant/dish/add.vue` - 商家端添加菜品
  - [ ] 对接 `create` API
- [ ] `pages-merchant/dish/edit.vue` - 商家端编辑菜品
  - [ ] 对接 `update` API

#### 待补充API
- [ ] `batchDelete` - 批量删除菜品
- [ ] `getReviews` - 获取菜品评价
- [ ] `updateStock` - 更新库存

---

### 3. 订单模块 (order.js)

**API文件路径**：`src/api/modules/order.js`

#### 已定义API列表
- ✅ `create` - 创建订单
- ✅ `getByUser` - 获取用户订单列表
- ✅ `getByMerchant` - 获取商家订单列表
- ✅ `getDetail` - 获取订单详情
- ✅ `getDishes` - 获取订单的菜品列表
- ✅ `updateStatus` - 更新订单状态
- ✅ `cancel` - 取消订单
- ✅ `confirm` - 确认收货
- ✅ `refund` - 申请退款
- ✅ `getStats` - 订单统计
- ✅ `reorder` - 再来一单
- ✅ `getCount` - 获取订单数量统计

#### 需要对接的页面
- [ ] `pages-user/cart/index.vue` - 购物车
  - [ ] 对接 `create` API（创建订单）
- [ ] `pages-user/order/confirm/index.vue` - 确认订单
  - [ ] 对接 `create` API
- [ ] `pages-user/order/detail/index.vue` - 订单详情
  - [ ] 对接 `getDetail` API
  - [ ] 对接 `getDishes` API
  - [ ] 对接 `cancel` API
  - [ ] 对接 `confirm` API
  - [ ] 对接 `refund` API
- [ ] `pages-user/order/progress/index.vue` - 订单进度
  - [ ] 对接 `getDetail` API
  - [ ] 对接 `updateStatus` API（WebSocket实时更新）
- [ ] `pages-user/orders/index.vue` - 我的订单
  - [ ] 对接 `getByUser` API
  - [ ] 对接 `getCount` API
- [ ] `pages-merchant/order/index.vue` - 商家端订单管理
  - [ ] 对接 `getByMerchant` API
  - [ ] 对接 `getStats` API
- [ ] `pages-merchant/order/detail.vue` - 商家端订单详情
  - [ ] 对接 `getDetail` API
  - [ ] 对接 `updateStatus` API
- [ ] `pages-merchant/order/today.vue` - 今日订单
  - [ ] 对接 `getByMerchant` API（今日筛选）

#### 待补充API
- [ ] `estimateTime` - 估价/预计送达时间
- [ ] `applyCoupon` - 使用优惠券
- [ ] `getDeliveryFee` - 获取配送费

---

### 4. 商家模块 (merchant.js)

**API文件路径**：`src/api/modules/merchant.js`

#### 已定义API列表
- ✅ `getList` - 获取商家列表
- ✅ `getDetail` - 获取商家详情
- ✅ `getNearby` - 获取附近商家
- ✅ `login` - 商家登录
- ✅ `getInfo` - 获取商家信息（当前登录商家）
- ✅ `updateInfo` - 更新商家信息
- ✅ `getCoupons` - 获取商家优惠券列表
- ✅ `getReviews` - 获取商家评价列表
- ✅ `getStatistics` - 获取商家统计数据
- ✅ `getFinance` - 获取商家财务数据
- ✅ `withdraw` - 商家提现申请
- ✅ `favorite` - 收藏商家
- ✅ `unfavorite` - 取消收藏商家
- ✅ `checkFavorite` - 检查是否收藏商家

#### 需要对接的页面
- [ ] `pages-user/home/index.vue` - 首页
  - [ ] 对接 `getNearby` API（附近商家）
- [ ] `pages-user/home/merchant-list.vue` - 商家列表
  - [ ] 对接 `getList` API
- [ ] `pages-user/merchant/detail/index.vue` - 商家详情
  - [ ] 对接 `getDetail` API
  - [ ] 对接 `getCoupons` API
  - [ ] 对接 `getReviews` API
  - [ ] 对接 `checkFavorite` API
  - [ ] 对接 `favorite` / `unfavorite` API
- [ ] `pages-merchant/home/index.vue` - 商家首页
  - [ ] 对接 `getInfo` API
  - [ ] 对接 `getStatistics` API
- [ ] `pages-merchant/home/statistics.vue` - 数据统计
  - [ ] 对接 `getStatistics` API
- [ ] `pages-merchant/home/analytics.vue` - 数据分析
  - [ ] 对接 `getStatistics` API
  - [ ] 对接 `getFinance` API
- [ ] `pages-merchant/profile/shop.vue` - 店铺信息
  - [ ] 对接 `getInfo` API
  - [ ] 对接 `updateInfo` API
- [ ] `pages-merchant/profile/finance.vue` - 财务管理
  - [ ] 对接 `getFinance` API
- [ ] `pages-merchant/profile/withdraw.vue` - 提现
  - [ ] 对接 `withdraw` API

---

### 5. 地址模块 (address.js)

**API文件路径**：`src/api/modules/address.js`

#### 已定义API列表
- ✅ `getList` - 获取地址列表
- ✅ `getDefault` - 获取默认地址
- ✅ `getDetail` - 获取地址详情
- ✅ `create` - 新增地址
- ✅ `update` - 更新地址
- ✅ `delete` - 删除地址
- ✅ `setDefault` - 设置默认地址

#### 需要对接的页面
- [ ] `pages-user/address/index.vue` - 收货地址
  - [ ] 对接 `getList` API
  - [ ] 对接 `delete` API
  - [ ] 对接 `setDefault` API
- [ ] `pages-user/address/edit/index.vue` - 编辑地址
  - [ ] 对接 `getDetail` API（编辑模式）
  - [ ] 对接 `create` API（新增模式）
  - [ ] 对接 `update` API（更新模式）
- [ ] `pages-user/order/confirm/index.vue` - 确认订单
  - [ ] 对接 `getDefault` API
  - [ ] 对接 `getList` API

---

### 6. 聊天模块 (chat.js)

**API文件路径**：`src/api/modules/chat.js`

#### 已定义API列表
- ✅ `getConversations` - 获取会话列表
- ✅ `getConversation` - 获取会话详情
- ✅ `getMessages` - 获取消息列表
- ✅ `sendMessage` - 发送消息
- ✅ `sendImage` - 发送图片消息
- ✅ `sendDishCard` - 发送菜品卡片
- ✅ `sendOrderCard` - 发送订单卡片
- ✅ `markRead` - 标记消息已读
- ✅ `recallMessage` - 撤回消息
- ✅ `createConversation` - 创建会话
- ✅ `createGroup` - 创建群聊
- ✅ `getUnreadCount` - 获取未读消息数
- ✅ `deleteConversation` - 删除会话
- ✅ `getQuickReplies` - 获取快捷回复

#### 需要对接的页面
- [ ] `pages-common/chat/conversation-list.vue` - 消息列表
  - [ ] 对接 `getConversations` API
  - [ ] 对接 `getUnreadCount` API
- [ ] `pages-common/chat/chat-room.vue` - 聊天室
  - [ ] 对接 `getMessages` API
  - [ ] 对接 `sendMessage` API
  - [ ] 对接 `sendImage` API
  - [ ] 对接 `markRead` API
  - [ ] 对接 `recallMessage` API
  - [ ] WebSocket连接实时消息
- [ ] `pages-user/message/index.vue` - 消息中心
  - [ ] 对接 `getUnreadCount` API
- [ ] `pages-merchant/chat/index.vue` - 商家端消息中心
  - [ ] 对接 `getConversations` API
  - [ ] 对接 `getUnreadCount` API
- [ ] `pages-merchant/chat/detail.vue` - 商家端聊天详情
  - [ ] 对接 `getMessages` API
  - [ ] 对接 `sendMessage` API

#### 待补充API
- [ ] `uploadVoice` - 上传语音消息
- [ ] `sendVoice` - 发送语音消息
- [ ] `blockUser` - 拉黑用户

---

### 7. 评价模块（待创建）

**API文件路径**：`src/api/modules/review.js`（待创建）

#### 待创建API列表
- [ ] `create` - 发表评价
- [ ] `getDishReviews` - 获取菜品评价列表
- [ ] `getMerchantReviews` - 获取商家评价列表
- [ ] `getUserReviews` - 获取用户评价列表
- [ ] `getDetail` - 获取评价详情
- [ ] `reply` - 商家回复评价
- [ ] `delete` - 删除评价
- [ ] `uploadImages` - 上传评价图片
- [ ] `like` - 点赞评价
- [ ] `getStats` - 获取评价统计

#### 需要对接的页面
- [ ] `pages-user/review/submit/index.vue` - 发表评价
  - [ ] 对接 `create` API
  - [ ] 对接 `uploadImages` API
- [ ] `pages-user/review/list/index.vue` - 全部评价
  - [ ] 对接 `getDishReviews` API
  - [ ] 对接 `getMerchantReviews` API
- [ ] `pages-merchant/comment/index.vue` - 评价中心
  - [ ] 对接 `getMerchantReviews` API
  - [ ] 对接 `getStats` API
- [ ] `pages-merchant/comment/detail.vue` - 评价详情
  - [ ] 对接 `getDetail` API
- [ ] `pages-merchant/comment/reply.vue` - 回复评价
  - [ ] 对接 `reply` API

---

### 8. 收藏模块（待创建）

**API文件路径**：`src/api/modules/favorite.js`（待创建）

#### 待创建API列表
- [ ] `addDish` - 收藏菜品
- [ ] `removeDish` - 取消收藏菜品
- [ ] `checkDish` - 检查是否收藏菜品
- [ ] `getDishList` - 获取收藏菜品列表
- [ ] `addMerchant` - 收藏商家（已在merchant.js中）
- [ ] `removeMerchant` - 取消收藏商家（已在merchant.js中）
- [ ] `checkMerchant` - 检查是否收藏商家（已在merchant.js中）
- [ ] `addRecipe` - 收藏食谱
- [ ] `removeRecipe` - 取消收藏食谱
- [ ] `getRecipeList` - 获取收藏食谱列表

#### 需要对接的页面
- [ ] `pages-user/collection/index.vue` - 我的收藏
  - [ ] 对接 `getDishList` API
  - [ ] 对接 `getRecipeList` API

---

## 🟡 P1 优先级（重要功能）

### 9. 食谱模块 (recipe.js)

**API文件路径**：`src/api/modules/recipe.js`

#### 已定义API列表
- ✅ `getToday` - 获取今日食谱推荐
- ✅ `getMyRecipes` - 获取我的食谱
- ✅ `getDetail` - 获取食谱详情
- ✅ `getSteps` - 获取食谱制作步骤
- ✅ `getIngredients` - 获取食谱食材列表
- ✅ `getNutrition` - 获取食谱营养信息
- ✅ `search` - 搜索食谱
- ✅ `favorite` - 收藏食谱
- ✅ `unfavorite` - 取消收藏食谱
- ✅ `getCategories` - 获取食谱分类
- ✅ `getRecommend` - 获取推荐食谱

#### 需要对接的页面
- [ ] `pages-user/recipe/today.vue` - 今日食谱
  - [ ] 对接 `getToday` API
- [ ] `pages-user/recipe/my.vue` - 我的食谱
  - [ ] 对接 `getMyRecipes` API
- [ ] `pages-user/recipe/detail/index.vue` - 食谱详情
  - [ ] 对接 `getDetail` API
  - [ ] 对接 `getSteps` API
  - [ ] 对接 `getIngredients` API
  - [ ] 对接 `getNutrition` API

---

### 10. AI模块 (ai.js)

**API文件路径**：`src/api/modules/ai.js`

#### 已定义API列表
- ✅ `chat` - AI对话
- ✅ `getHistory` - 获取对话历史
- ✅ `getConversation` - 获取对话详情
- ✅ `extractContent` - 内容提取
- ✅ `analyzeNutrition` - 营养分析
- ✅ `generateRecipe` - 食谱生成
- ✅ `getQuickQuestions` - 获取快捷提问
- ✅ `clearHistory` - 清除对话历史

#### 需要对接的页面
- [ ] `pages-user/ai/index.vue` - AI助手
  - [ ] 对接 `chat` API
  - [ ] 对接 `getQuickQuestions` API
  - [ ] 对接 `clearHistory` API
- [ ] `pages-user/ai/advanced.vue` - AI智能分析
  - [ ] 对接 `analyzeNutrition` API
  - [ ] 对接 `generateRecipe` API
- [ ] `pages-user/ai/content-extract.vue` - 内容提取
  - [ ] 对接 `extractContent` API

---

### 11. 优惠券模块 (coupon.js)

**API文件路径**：`src/api/modules/coupon.js`

#### 已定义API列表
- ✅ `getMyCoupons` - 获取我的优惠券
- ✅ `getDetail` - 获取优惠券详情
- ✅ `receive` - 领取优惠券
- ✅ `getAvailable` - 获取可领取的优惠券
- ✅ `getMerchantCoupons` - 获取商家优惠券
- ✅ `validate` - 校验优惠券
- ✅ `getUsageHistory` - 获取优惠券使用记录

#### 需要对接的页面
- [ ] `pages-user/coupon/index.vue` - 我的优惠券
  - [ ] 对接 `getMyCoupons` API
- [ ] `pages-user/merchant/detail/index.vue` - 商家详情
  - [ ] 对接 `getMerchantCoupons` API
  - [ ] 对接 `receive` API
- [ ] `pages-user/order/confirm/index.vue` - 确认订单
  - [ ] 对接 `validate` API

---

### 12. 钱包模块（待创建）

**API文件路径**：`src/api/modules/wallet.js`（待创建）

#### 待创建API列表
- [ ] `getBalance` - 获取余额
- [ ] `getTransactions` - 获取交易记录
- [ ] `recharge` - 充值
- [ ] `withdraw` - 提现
- [ ] `getStatistics` - 获取收支统计
- [ ] `getDetail` - 获取交易详情

#### 需要对接的页面
- [ ] `pages-user/wallet/index.vue` - 我的钱包
  - [ ] 对接 `getBalance` API
  - [ ] 对接 `getTransactions` API
  - [ ] 对接 `getStatistics` API

---

### 13. 心愿单模块（待创建）

**API文件路径**：`src/api/modules/wishlist.js`（待创建）

#### 待创建API列表
- [ ] `create` - 创建心愿单
- [ ] `getMyWishlists` - 获取我的心愿单
- [ ] `getDetail` - 获取心愿单详情
- [ ] `addItem` - 添加菜品到心愿单
- [ ] `removeItem` - 从心愿单移除菜品
- [ ] `update` - 更新心愿单
- [ ] `delete` - 删除心愿单
- [ ] `share` - 分享心愿单
- [ ] `getPublicWishlists` - 获取公开心愿单列表
- [ ] `follow` - 关注心愿单
- [ ] `audit` - 商家审核心愿单
- [ ] `getMerchantPending` - 获取待审核心愿单（商家端）

#### 需要对接的页面
- [ ] `pages/wishlist/index.vue` - 心愿单列表
  - [ ] 对接 `getMyWishlists` API
  - [ ] 对接 `getPublicWishlists` API
- [ ] `pages/wishlist/add.vue` - 创建心愿单
  - [ ] 对接 `create` API
- [ ] `pages/wishlist/detail.vue` - 心愿单详情
  - [ ] 对接 `getDetail` API
- [ ] `pages/wishlist/select-dishes.vue` - 选择菜品
  - [ ] 对接 `addItem` API
- [ ] `pages-merchant/wishlist/index.vue` - 商家端心愿单审核
  - [ ] 对接 `getMerchantPending` API
- [ ] `pages-merchant/wishlist/audit.vue` - 审核心愿单
  - [ ] 对接 `audit` API

---

## 🟢 P2 优先级（辅助功能）

### 14. 浏览历史模块（待创建）

**API文件路径**：`src/api/modules/history.js`（待创建）

#### 待创建API列表
- [ ] `getList` - 获取浏览历史
- [ ] `add` - 添加浏览记录
- [ ] `delete` - 删除浏览记录
- [ ] `clear` - 清空浏览历史

#### 需要对接的页面
- [ ] `pages-user/history/index.vue` - 浏览历史
  - [ ] 对接 `getList` API
  - [ ] 对接 `clear` API

---

### 15. 团购模块（待创建）

**API文件路径**：`src/api/modules/groupOrder.js`（待创建）

#### 待创建API列表
- [ ] `create` - 发起团购
- [ ] `join` - 参与团购
- [ ] `getList` - 获取团购列表
- [ ] `getDetail` - 获取团购详情
- [ ] `cancel` - 取消团购
- [ ] `settle` - 结算团购

#### 需要对接的页面
- [ ] `pages/group-order/index.vue` - 团购列表
- [ ] `pages/group-order/create.vue` - 发起团购
- [ ] `pages/group-order/detail.vue` - 团购详情
- [ ] `pages/group-order/select-dishes.vue` - 选择菜品
- [ ] `pages/group-order/settle.vue` - 结算

---

### 16. 通知模块（待创建）

**API文件路径**：`src/api/modules/notification.js`（待创建）

#### 待创建API列表
- [ ] `getList` - 获取通知列表
- [ ] `getUnreadCount` - 获取未读通知数
- [ ] `markRead` - 标记通知已读
- [ ] `markAllRead` - 全部标记已读
- [ ] `delete` - 删除通知
- [ ] `getSettings` - 获取通知设置
- [ ] `updateSettings` - 更新通知设置

#### 需要对接的页面
- [ ] `pages/notification/index.vue` - 通知列表
- [ ] `pages/notification/detail.vue` - 通知详情

---

### 17. 反馈模块（待创建）

**API文件路径**：`src/api/modules/feedback.js`（待创建）

#### 待创建API列表
- [ ] `submit` - 提交反馈
- [ ] `getList` - 获取反馈列表
- [ ] `getDetail` - 获取反馈详情
- [ ] `uploadImage` - 上传反馈图片
- [ ] `getTypes` - 获取反馈类型

#### 需要对接的页面
- [ ] `pages-user/feedback/index.vue` - 意见反馈
  - [ ] 对接 `submit` API
  - [ ] 对接 `getTypes` API

---

## 🔧 待创建的API模块文件

### 1. 评价模块
**文件路径**：`src/api/modules/review.js`
**优先级**：P0
**包含功能**：
- 发表评价
- 获取评价列表
- 商家回复评价
- 评价统计

### 2. 收藏模块
**文件路径**：`src/api/modules/favorite.js`
**优先级**：P0
**包含功能**：
- 收藏/取消收藏菜品
- 收藏/取消收藏食谱
- 获取收藏列表

### 3. 钱包模块
**文件路径**：`src/api/modules/wallet.js`
**优先级**：P1
**包含功能**：
- 余额查询
- 充值提现
- 交易记录

### 4. 心愿单模块
**文件路径**：`src/api/modules/wishlist.js`
**优先级**：P1
**包含功能**：
- 创建心愿单
- 心愿单管理
- 商家审核

### 5. 浏览历史模块
**文件路径**：`src/api/modules/history.js`
**优先级**：P2
**包含功能**：
- 浏览记录
- 清空历史

### 6. 团购模块
**文件路径**：`src/api/modules/groupOrder.js`
**优先级**：P2
**包含功能**：
- 发起团购
- 参与团购

### 7. 通知模块
**文件路径**：`src/api/modules/notification.js`
**优先级**：P2
**包含功能**：
- 通知列表
- 已读管理

### 8. 反馈模块
**文件路径**：`src/api/modules/feedback.js`
**优先级**：P2
**包含功能**：
- 提交反馈
- 反馈记录

---

## 📝 实施计划

### 第一阶段：核心功能对接（P0）
**预计时间**：2-3周

1. **用户模块**（3天）
   - 登录注册页面
   - 用户中心页面
   - 编辑资料页面

2. **商家模块**（3天）
   - 商家列表
   - 商家详情

3. **菜品模块**（4天）
   - 首页推荐
   - 菜品详情
   - 商家菜品管理

4. **订单模块**（5天）
   - 购物车
   - 确认订单
   - 订单详情
   - 订单列表
   - 商家端订单管理

5. **地址模块**（2天）
   - 地址列表
   - 编辑地址

6. **聊天模块**（4天）
   - 消息列表
   - 聊天室
   - WebSocket实时通信

7. **评价模块**（3天）
   - 创建review.js
   - 发表评价
   - 评价列表

8. **收藏模块**（2天）
   - 创建favorite.js
   - 我的收藏

### 第二阶段：重要功能对接（P1）
**预计时间**：1-2周

1. **食谱模块**（3天）
2. **AI模块**（4天）
3. **优惠券模块**（2天）
4. **钱包模块**（3天）
5. **心愿单模块**（4天）

### 第三阶段：辅助功能对接（P2）
**预计时间**：1周

1. **浏览历史**（1天）
2. **团购模块**（2天）
3. **通知模块**（2天）
4. **反馈模块**（1天）

---

## ✅ 完成标准

每个API对接完成的标准：
1. ✅ API已定义或已创建
2. ✅ 页面已对接API
3. ✅ 错误处理已完善
4. ✅ 加载状态已处理
5. ✅ 数据展示正确
6. ✅ 交互逻辑完整
7. ✅ 测试通过

---

## 📌 注意事项

1. **API路径统一**：所有API使用 `/v1/` 或 `/api/` 前缀
2. **错误处理**：统一使用try-catch，错误提示友好
3. **加载状态**：所有异步请求需要loading状态
4. **数据缓存**：合理使用本地缓存，减少请求
5. **Token管理**：请求自动携带token，过期自动刷新
6. **WebSocket**：聊天模块需要WebSocket连接
7. **图片上传**：使用统一的图片上传方法
8. **请求拦截**：统一处理请求和响应
9. **Mock数据**：开发阶段使用Mock数据
10. **环境配置**：开发/测试/生产环境切换

---

## 🔗 相关文档

- [项目配置指南.md](./配置指南.md)
- [佳食宜选.md](./佳食宜选.md)
- [产品需求说明书（PRD）.md](./产品需求说明书（PRD）.md)

---

**更新日志**：
- 2026-03-19：创建文档，梳理所有API模块和页面对接需求
