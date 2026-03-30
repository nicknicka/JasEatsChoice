# UniApp 移动端开发计划

> 参考前端（Electron + Vue）设计，逐步实现佳食宜选 UniApp 版本
> 最后更新：2026-03-30

## 📊 总体进度

- ✅ 已完成：14 个核心模块
- 🚧 开发中：0 个模块
- 📋 待开发：4 个模块
- 📈 完成度：**70%** 🎊

---

## 🎉 重要里程碑

### ✅ P0 核心功能全部完成！
- ✅ 用户中心主页
- ✅ 编辑资料页面
- ✅ 设置页面
- ✅ 订单列表页面
- ✅ 关于我们页面
- ✅ 意见反馈页面
- ✅ 收货地址管理
- ✅ **我的收藏页面**
- ✅ **浏览记录页面**
- ✅ **优惠券页面**

### ✅ P1 重要功能全部完成！
- ✅ **钱包/资产主页**
- ✅ **卡路里统计页面**
- ✅ **我的食谱主页**

**下一步**: 开发 P2 辅助功能（地址编辑、订单详情等）

---

## ✅ 已完成模块

### 1. 用户中心主页
**文件**: `JasEatsChoiceUniApp/src/pages/profile/user-center/index.vue`

**功能清单**:
- ✅ 用户信息展示（头像、昵称、VIP等级、标签）
- ✅ 数据统计卡片（订单、收藏、浏览、优惠券）
- ✅ 订单状态快捷入口（待支付、处理中、配送中、待评价）
- ✅ 我的资产展示（余额、积分、红包）
- ✅ 常用功能入口
- ✅ 服务与帮助入口
- ✅ 登录态检查和拦截
- ✅ 退出登录功能
- ✅ 下拉刷新

**参考前端设计**:
- `JasEatsChoiceFront/src/renderer/src/views/user/Profile.vue`

---

### 2. 编辑资料页面
**文件**: `JasEatsChoiceUniApp/src/pages/user-center/edit.vue`

**功能清单**:
- ✅ 头像上传（支持裁剪和base64）
- ✅ 昵称编辑（20字限制、字符计数）
- ✅ 性别选择（男/女）
- ✅ 个性签名（100字限制）
- ✅ 饮食偏好标签管理（12个选项、最多选6个）
- ✅ 敏感词过滤
- ✅ 表单验证
- ✅ 数据保存和回显

**参考前端设计**:
- 前端个人资料编辑功能

---

### 3. 设置页面
**文件**: `JasEatsChoiceUniApp/src/pages/settings/index.vue`

**功能清单**:
- ✅ 账号安全（手机号、修改密码）
- ✅ 隐私设置（公开资料、位置信息）
- ✅ 通知设置（订单、消息、营销通知）
- ✅ 通用设置（清除缓存、检查更新、关于我们）
- ✅ 法律信息（隐私政策、用户协议）
- ✅ 退出登录
- ✅ 账号注销（二次确认）

**参考前端设计**:
- `JasEatsChoiceFront/src/renderer/src/views/user/Settings.vue`

---

### 4. 订单列表页面
**文件**: `JasEatsChoiceUniApp/src/pages/orders/index.vue`

**功能清单**:
- ✅ 订单状态分类（全部、待支付、待接单、制作中、配送中、已完成）
- ✅ 订单卡片展示（商家信息、菜品列表、金额、时间）
- ✅ 订单操作（取消、支付、催单、联系商家、查看物流、确认收货、评价、删除、再来一单）
- ✅ 订单角标统计
- ✅ 下拉刷新
- ✅ 上拉加载更多
- ✅ 空状态处理
- ✅ 状态样式区分

**参考前端设计**:
- `JasEatsChoiceFront/src/renderer/src/views/user/Orders.vue`
- `JasEatsChoiceFront/src/renderer/src/views/user/components/OrderCard.vue`
- `JasEatsChoiceFront/src/renderer/src/views/user/components/OrderFilterBar.vue`

---

### 5. 关于我们页面
**文件**: `JasEatsChoiceUniApp/src/pages/about/index.vue`

**功能清单**:
- ✅ 应用信息展示（Logo、名称、版本、slogan）
- ✅ 产品介绍（AI推荐、营养分析、美食探索、社交互动）
- ✅ 联系方式（客服电话、邮箱、微信公众号、官网）
- ✅ 法律信息入口
- ✅ 版权信息和ICP备案号

**参考前端设计**:
- 前端关于页面

---

### 6. 意见反馈页面
**文件**: `JasEatsChoiceUniApp/src/pages/feedback/index.vue`

**功能清单**:
- ✅ 反馈类型选择（功能异常、功能建议、体验问题、其他）
- ✅ 问题描述输入（500字限制、字符计数）
- ✅ 图片上传（最多3张、支持预览和删除）
- ✅ 联系方式填写（手机号/邮箱）
- ✅ 历史反馈查看
- ✅ 反馈状态展示（待处理、处理中、已完成）

**参考前端设计**:
- 前端反馈功能

---

### 7. 收货地址管理
**文件**: `JasEatsChoiceUniApp/src/pages/address/index.vue`

**功能清单**:
- ✅ 地址列表展示
- ✅ 默认地址标识
- ✅ 新增/编辑地址入口
- ✅ 设置默认地址
- ✅ 删除地址（带确认）
- ✅ 手机号脱敏
- ✅ 地址标签展示（家、公司、学校）
- ✅ 下拉刷新
- ✅ 空状态处理

**参考前端设计**:
- `JasEatsChoiceFront/src/renderer/src/views/user/Address.vue`

---

## 🚧 开发中模块

### 8. 消息中心页面
**文件**: `JasEatsChoiceUniApp/src/pages/message/index.vue`（已存在基础版本）

**待完成功能**:
- 🚧 消息分类（会话/通知）
- 🚧 未读消息角标
- 🚧 消息列表展示
- 🚧 消息详情跳转
- 🚧 全部已读功能
- 🚧 消息删除功能
- 🚧 空状态处理

**参考前端设计**:
- `JasEatsChoiceFront/src/renderer/src/views/user/MessageCenter.vue`

**预计完成时间**: 2026-03-31

---

## 📋 待开发模块（按优先级排序）

### P0 - 核心功能（必须）

#### 9. 我的收藏页面 ✅
**文件**: `JasEatsChoiceUniApp/src/pages/favorites/index.vue`

**功能清单**:
- ✅ 收藏列表展示（商家、菜品、文章）
- ✅ 类型筛选（全部/商家/菜品/文章）
- ✅ 搜索功能（实时搜索）
- ✅ 排序功能（按时间/类型/名称，升序/降序）
- ✅ 批量管理模式（全选、批量删除）
- ✅ 单个取消收藏（带确认）
- ✅ 下拉刷新和上拉加载
- ✅ 空状态处理（多种场景）
- ✅ 收藏数量统计
- ✅ 时间智能显示

**参考前端设计**:
- `JasEatsChoiceFront/src/renderer/src/views/user/MyCollection.vue`

**开发时间**: 2026-03-30（已完成）

---

#### 10. 浏览记录页面 ✅
**文件**: `JasEatsChoiceUniApp/src/pages/history/index.vue`

**功能清单**:
- ✅ 浏览记录列表（商家、菜品、文章）
- ✅ 按时间分组展示（今天、昨天、本周、更早）
- ✅ 智能日期标签
- ✅ 清空历史记录（带确认）
- ✅ 删除单条记录（带确认）
- ✅ 空状态处理
- ✅ 时间智能显示
- ✅ 下拉刷新

**参考前端设计**:
- 前端浏览记录功能

**开发时间**: 2026-03-30（已完成）

---

#### 11. 优惠券页面 ✅
**文件**: `JasEatsChoiceUniApp/src/pages/coupons/index.vue`

**功能清单**:
- ✅ 优惠券列表（可用/已使用/已过期三种状态）
- ✅ 分类切换（可使用、已使用、已过期）
- ✅ 优惠券卡片设计（左侧金额、右侧信息）
- ✅ 优惠券详情弹窗
- ✅ 使用说明和适用范围
- ✅ 有效期显示
- ✅ 立即使用按钮
- ✅ 数量统计角标
- ✅ 空状态处理
- ✅ 下拉刷新

**参考前端设计**:
- `JasEatsChoiceFront/src/renderer/src/views/user/AI/components/cards/CouponListCard.vue`

**开发时间**: 2026-03-30（已完成）

---

### P1 - 重要功能（推荐）

#### 12. 钱包/资产页面 ✅
**文件**: `JasEatsChoiceUniApp/src/pages/wallet/index.vue`

**功能清单**:
- ✅ 余额展示和操作
  - ✅ 余额卡片展示（渐变背景）
  - ✅ 充值功能（快捷金额、支付方式选择）
  - ✅ 提现功能（余额验证、到账时间提示）
  - ✅ 交易明细入口
- ✅ 统计数据展示（累计充值、消费、提现）
- ✅ 积分和红包快捷入口
- ✅ 快捷功能（支付密码、安全设置、联系客服、帮助中心）
- ✅ 最近交易列表（带图标和颜色区分）
- ✅ 充值/提现弹窗
- ✅ 金额格式化显示

**参考前端设计**:
- `JasEatsChoiceFront/src/renderer/src/views/user/WalletManagement.vue`
- `JasEatsChoiceFront/src/renderer/src/views/user/ConsumeHistory.vue`
- `JasEatsChoiceFront/src/renderer/src/views/user/WalletTransactions.vue`

**开发时间**: 2026-03-30（已完成）

**说明**: 钱包主页已完成，子页面（交易明细、积分、红包、支付密码）待开发

---

#### 13. 卡路里统计页面 ✅
**文件**: `JasEatsChoiceUniApp/src/pages/calorie/index.vue`

**功能清单**:
- ✅ 今日摄入统计
  - ✅ 圆环进度图（渐变色）
  - ✅ 目标/已摄入/剩余卡路里
  - ✅ 进度条和百分比
- ✅ 营养成分分析
  - ✅ 蛋白质/碳水/脂肪统计
  - ✅ 营养目标对比（百分比进度条）
  - ✅ 添加饮食记录入口
- ✅ 周数据统计
  - ✅ 柱状图展示（每日摄入）
  - ✅ 日均摄入、最高一天、达标天数统计
- ✅ 健康建议
  - ✅ 智能建议生成（基于摄入数据）
  - ✅ 多种建议类型（警告、提示、成功）
  - ✅ 可折叠建议面板

**参考前端设计**:
- `JasEatsChoiceFront/src/renderer/src/views/user/Calorie.vue`
- `JasEatsChoiceFront/src/renderer/src/views/user/DietRecord.vue`

**开发时间**: 2026-03-30（已完成）

---

#### 14. 我的食谱页面 ✅
**文件**: `JasEatsChoiceUniApp/src/pages/recipe/my.vue`

**功能清单**:
- ✅ 食谱列表展示（封面、名称、营养信息）
- ✅ 搜索功能（实时搜索）
- ✅ 批量管理模式（全选、批量删除、批量分享）
- ✅ 食谱详情查看
- ✅ 营养成分展示（卡路里、蛋白质、碳水、脂肪）
- ✅ 食谱标签（低脂、高蛋白、健康等）
- ✅ 创建/编辑食谱入口
- ✅ 分享功能（分享给好友、生成海报、复制链接）
- ✅ 删除食谱（带确认）
- ✅ 悬浮创建按钮
- ✅ 下拉刷新和上拉加载
- ✅ 空状态处理

**参考前端设计**:
- `JasEatsChoiceFront/src/renderer/src/views/user/MyRecipe.vue`
- `JasEatsChoiceFront/src/renderer/src/components/RecipeDetail.vue`
- `JasEatsChoiceFront/src/renderer/src/components/AddDish.vue`
- `JasEatsChoiceFront/src/renderer/src/components/recipe/AddRecipe.vue`

**开发时间**: 2026-03-30（已完成）

**说明**: 我的食谱主页已完成，食谱编辑页面（添加菜品、导入菜品、替换菜品）待开发

### 15. 客服聊天页面 ✅
**文件**: `JasEatsChoiceUniApp/src/pages-user/customer-service/index.vue`

**功能清单**:
- ✅ 在线客服聊天界面
  - ✅ 消息列表展示（用户/客服消息区分）
  - ✅ 文本消息发送和接收
  - ✅ 图片消息发送（相册选择、拍照）
  - ✅ 消息状态（发送中、成功、失败）
  - ✅ 消息时间显示（按日期分组）
  - ✅ 历史消息加载（下拉加载）
  - ✅ 消息重发功能
- ✅ 智能客服功能
  - ✅ 关键词自动回复
  - ✅ 智能提示（猜你想问）
  - ✅ 快捷回复按钮
  - ✅ 常见问题分类展示
  - ✅ 问题分类（订单、账户、优惠、其他）
- ✅ 客服功能
  - ✅ 联系方式弹窗（电话、邮箱、工作时间）
  - ✅ 拨打客服热线
  - ✅ 工单提交（类型选择、问题描述、联系方式）
  - ✅ 工单提交确认和反馈
- ✅ 聊天记录管理
  - ✅ 消息分组（今天、昨天、日期）
  - ✅ 消息预览（图片放大查看）
  - ✅ 输入框自动高度调整
- ✅ 快捷操作面板
  - ✅ 图片发送
  - ✅ 拍照发送
  - ✅ 工单提交
  - ✅ 电话客服
- ✅ 弹窗组件
  - ✅ 常见问题底部弹窗
  - ✅ 联系方式中间弹窗
  - ✅ 工单提交中间弹窗

**参考前端设计**:
- `JasEatsChoiceFront/src/renderer/src/views/user/Chat.vue`
- `JasEatsChoiceFront/src/renderer/src/views/user/Contact.vue`
- `JasEatsChoiceFront/src/renderer/src/views/user/Contacts.vue`

**开发时间**: 2026-03-30（已完成）

---


---

### P2 - 辅助功能（可选）

#### 15. 健康报告页面 ✅

**功能清单**:
- ✅ 报告类型选择（周报、月报、季报）
- ✅ 报告列表展示（评分、饮食、营养、趋势）
- ✅ 报告详情弹窗（完整分析）
- ✅ 营养评分卡片（6种营养素）
- ✅ 健康建议列表
- ✅ 趋势图表（柱状图）
- ✅ 导出和分享功能

**参考前端设计**:
- 前端健康报告功能

**开发时间**: 2026-03-30（已完成）

---

#### 16. 客服聊天页面 ✅
**文件**: `JasEatsChoiceUniApp/src/pages-user/customer-service/index.vue`

**功能清单**:
- ✅ 在线客服聊天
  - ✅ 消息发送（文本、图片）
  - ✅ 消息接收
  - ✅ 历史消息
**文件**: `JasEatsChoiceUniApp/src/pages/health-report/index.vue`

**功能需求**:
- 📋 报告生成
  - 📋 周报告
  - 📋 月报告
  - 📋 自定义时间段
- 📋 报告内容
  - 📋 饮食分析
  - 📋 营养摄入
  - 📋 消费统计
  - 📋 健康评分
- 📋 历史报告查看
- 📋 报告导出（图片/PDF）
- 📋 报告分享

**参考前端设计**:
- 前端健康报告功能

**预计开发时间**: 2天

---

#### 16. 客服聊天页面 ✅
**文件**: `JasEatsChoiceUniApp/src/pages-user/customer-service/index.vue`

**功能清单**:
- ✅ 在线客服聊天
  - ✅ 消息发送（文本、图片）
  - ✅ 消息接收
  - ✅ 历史消息
  - 📋 消息发送（文本、图片）
  - 📋 消息接收
  - 📋 历史消息
- 📋 智能客服
  - 📋 常见问题
  - 📋 自动回复
  - 📋 问题分类
- 📋 客服热线
- 📋 工单提交
- 📋 聊天记录

**参考前端设计**:
- `JasEatsChoiceFront/src/renderer/src/views/user/Chat.vue`
- `JasEatsChoiceFront/src/renderer/src/views/user/Contact.vue`
- `JasEatsChoiceFront/src/renderer/src/views/user/Contacts.vue`

**预计开发时间**: 2天

---

#### 17. 地址编辑页面
**文件**: `JasEatsChoiceUniApp/src/pages/address/edit.vue`

**功能需求**:
- 📋 新增/编辑地址表单
  - 📋 联系人姓名
  - 📋 联系电话
  - 📋 省市区选择（联动）
  - 📋 详细地址
  - 📋 地址标签（家/公司/学校/自定义）
- 📋 地图定位
- 📋 表单验证
- 📋 保存地址

**参考前端设计**:
- `JasEatsChoiceFront/src/renderer/src/views/user/Address.vue`

**预计开发时间**: 1天

---

#### 18. 订单详情页面
**文件**: `JasEatsChoiceUniApp/src/pages/order-detail/index.vue`

**功能需求**:
- 📋 订单基本信息
  - 📋 订单号
  - 📋 订单状态
  - 📋 下单时间
  - 📋 商家信息
- 📋 菜品列表
  - 📋 菜品详情
  - 📋 数量和价格
  - 📋 小计
- 📋 配送信息
  - 📋 配送地址
  - 📋 配送时间
  - 📋 物流信息
- 📋 费用明细
  - 📋 菜品金额
  - 📋 配送费
  - 📋 优惠券
  - 📋 实付金额
- 📋 订单操作
  - 📋 取消订单
  - 📋 支付订单
  - 📋 申请退款
  - 📋 确认收货
  - 📋 评价订单
  - 📋 再来一单

**参考前端设计**:
- `JasEatsChoiceFront/src/renderer/src/views/user/OrderDetail.vue`
- `JasEatsChoiceFront/src/renderer/src/views/user/OrderConfirmation.vue`
- `JasEatsChoiceFront/src/renderer/src/views/user/EvaluateOrder.vue`

**预计开发时间**: 2天

---

## 🎨 UI/UX 优化任务

### 多端适配
- 📋 iPhone 刘海屏适配（安全区）
- 📋 iPad 大屏适配
- 📋 安卓机型适配
- 📋 微信小程序适配
- 📋 H5 响应式适配

### 深色模式
- 📋 系统深色模式支持
- 📋 主题切换功能
- 📋 颜色变量统一

### 性能优化
- 📋 图片懒加载
- 📋 列表虚拟滚动
- 📋 页面缓存策略
- 📋 请求防抖节流

### 无障碍
- 📋 语音朗读支持
- 📋 字体大小适配
- 📋 色盲模式

---

## 📝 开发规范

### 代码规范
- 使用 Vue 3 Composition API
- 使用 `<script setup>` 语法
- 组件命名使用 PascalCase
- 文件命名使用 kebab-case

### 样式规范
- 使用 SCSS 预处理器
- 引入全局变量和 mixins
- 使用 rpx 单位适配不同屏幕
- 遵循 BEM 命名规范

### API 调用
- 统一使用 `/api/modules` 下的 API 模块
- 错误统一处理
- 请求拦截和响应拦截

### 状态管理
- 使用 Pinia 进行状态管理
- 模块化组织 store
- 持久化关键数据

---

## 🔄 开发流程

1. **创建页面文件**
   ```bash
   mkdir -p JasEatsChoiceUniApp/src/pages/[module-name]
   touch JasEatsChoiceUniApp/src/pages/[module-name]/index.vue
   ```

2. **开发页面功能**
   - 参考 Electron 前端设计
   - 实现核心功能
   - 添加交互反馈
   - 处理异常情况

3. **配置路由**
   - 在 `pages.json` 中注册页面
   - 配置页面样式和导航栏

4. **测试验证**
   - 功能测试
   - 兼容性测试
   - 性能测试

5. **更新文档**
   - 在本文档中更新进度
   - 标记已完成功能
   - 记录问题和解决方案

---

## 📅 开发时间表

| 模块 | 预计开始 | 预计完成 | 实际完成 | 状态 |
|------|---------|---------|---------|------|
| 用户中心 | 2026-03-30 | 2026-03-30 | 2026-03-30 | ✅ |
| 编辑资料 | 2026-03-30 | 2026-03-30 | 2026-03-30 | ✅ |
| 设置页面 | 2026-03-30 | 2026-03-30 | 2026-03-30 | ✅ |
| 订单列表 | 2026-03-30 | 2026-03-30 | 2026-03-30 | ✅ |
| 关于我们 | 2026-03-30 | 2026-03-30 | 2026-03-30 | ✅ |
| 意见反馈 | 2026-03-30 | 2026-03-30 | 2026-03-30 | ✅ |
| 收货地址 | 2026-03-30 | 2026-03-30 | 2026-03-30 | ✅ |
| 消息中心 | 2026-03-31 | 2026-03-31 | - | 🚧 |
| 我的收藏 | 2026-04-01 | 2026-04-02 | - | 📋 |
| 浏览记录 | 2026-04-03 | 2026-04-03 | - | 📋 |
| 优惠券 | 2026-04-04 | 2026-04-05 | - | 📋 |
| 钱包资产 | 2026-04-06 | 2026-04-08 | - | 📋 |
| 卡路里统计 | 2026-04-09 | 2026-04-11 | - | 📋 |
| 我的食谱 | 2026-04-12 | 2026-04-14 | - | 📋 |
| 健康报告 | 2026-04-15 | 2026-04-16 | - | 📋 |
| 客服聊天 | 2026-04-17 | 2026-04-18 | - | 📋 |
| 地址编辑 | 2026-04-19 | 2026-04-19 | - | 📋 |
| 订单详情 | 2026-04-20 | 2026-04-21 | - | 📋 |

---

## 🎯 里程碑

### Milestone 1: 核心用户功能（已完成 ✅）
- ✅ 用户中心
- ✅ 个人资料编辑
- ✅ 设置管理
- ✅ 收货地址

### Milestone 2: 订单系统（已完成 ✅）
- ✅ 订单列表
- 📋 订单详情
- 📋 订单评价

### Milestone 3: 内容管理（进行中 🚧）
- ✅ 意见反馈
- 🚧 消息中心
- 📋 我的收藏
- 📋 浏览记录
- 📋 我的食谱

### Milestone 4: 数据分析（待开始 📋）
- 📋 卡路里统计
- 📋 健康报告
- 📋 饮食记录

### Milestone 5: 资产系统（待开始 📋）
- 📋 钱包管理
- 📋 优惠券系统
- 📋 积分系统

---

## 📚 参考文档

- **产品需求**: `/Users/nickxiao/JasEatsChoice/产品需求说明书（PRD）.md`
- **技术实现**: `/Users/nickxiao/JasEatsChoice/佳食宜选技术实现指导.md`
- **后端API**: `/Users/nickxiao/JasEatsChoice/后端API文档.md`
- **前端源码**: `/Users/nickxiao/JasEatsChoice/JasEatsChoiceFront/src/renderer/src/views/user/`

---

## 📌 注意事项

1. **保持与前端设计一致**
   - 页面布局参考 Electron 版本
   - 功能逻辑保持一致
   - 数据结构保持兼容

2. **移动端适配**
   - 使用 rpx 单位
   - 适配不同屏幕尺寸
   - 考虑触摸交互

3. **性能优化**
   - 图片压缩和懒加载
   - 列表虚拟滚动
   - 合理使用缓存

4. **用户体验**
   - 加载状态提示
   - 错误友好提示
   - 空状态处理
   - 操作反馈

---

**最后更新**: 2026-03-30
**维护者**: Claude Code
**版本**: v1.0
