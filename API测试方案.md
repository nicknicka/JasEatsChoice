# API测试方案

生成时间：2026-04-15
适用项目：佳食宜选后端接口、桌面端接口封装、小程序接口封装
接口基线：`后端API文档.md`
字段核对基线：`前后端字段对接分析报告.md`

## 一、测试目标

1. 覆盖后端已发布的全部接口，验证接口可用性、字段正确性、状态码与错误提示是否符合预期。
2. 覆盖正常场景、边界场景、异常场景、鉴权场景，确保前后端字段名称、类型、必填规则一致。
3. 重点验证当前报告中已发现的高风险问题：用户资料、地址、订单、评价、通知、优惠券、小程序旧接口残留。

## 二、测试范围

### 2.1 后端接口范围

- 用户与认证
- 商家与商户资料
- 菜品、菜单、分类、收藏、心愿单
- 订单、支付、钱包、优惠券
- 地址、通知、消息、聊天、群组、拼单
- 评价、节日推荐、AI能力、定位、天气
- 管理端、系统配置、日志、统计、定时任务

### 2.2 覆盖原则

- 覆盖 `后端API文档.md` 中全部接口
- 每个接口至少覆盖 1 个正常用例
- 每个有必填参数的接口至少覆盖 1 个缺参用例
- 每个有身份要求的接口至少覆盖 1 个未登录用例
- 每个有状态流转的接口至少覆盖 1 个非法状态用例
- 每个文件上传接口至少覆盖 1 个格式错误或大小超限用例

## 三、测试环境建议

### 3.1 开发环境

- 服务地址：`http://localhost:7777/api`
- 数据库：`jia_shi_yi_xuan`
- Redis：`localhost:6379`
- 文件目录：`/Users/nickxiao/JasEatsChoice/uploads/`
- 建议使用独立测试账号，避免污染真实开发数据

### 3.2 测试环境配置建议

- 测试库需先执行拼单数据模型迁移：`V2026041701__create_group_order_member_table.sql`
- 单独测试库、单独 Redis 库编号、单独上传目录
- 关闭真实短信、真实邮件、真实支付回调，统一使用模拟开关或测试配置
- 拼单场景中：
  - 余额支付走真实闭环
  - 微信支付、支付宝支付仅走模拟流程
- AI接口建议分为两套：
  - 基础连通性测试：走真实服务
  - 回归稳定性测试：走模拟响应
- 每日回归前执行测试数据初始化脚本，保证订单状态、钱包余额、通知数量可预测

### 3.3 测试数据准备

- 普通用户：已注册、未注册、已绑商家、未绑商家
- 商家：正常营业、停业、无菜品、有菜品
- 订单：待支付、待接单、制作中、已完成、已取消
- 地址：无默认地址、有默认地址、多个地址
- 钱包：余额充足、余额不足
- 优惠券：可用、已用、已释放、过期
- 评价：未评价订单、已评价订单、已回复评价、未回复评价
- 拼单：
  - 草稿拼单、待确认拼单、已锁单待支付拼单、已全部支付拼单
  - 发起人成员、普通成员、已退出成员、未加入用户
  - `t_group_order_member` 中存在有效成员、已退出成员、已支付成员
  - `t_group_order.max_participants`、`locked`、`confirmed_time` 有明确预置值

## 四、测试方法

### 4.1 手动测试

适用场景：
- 新接口首轮验证
- 复杂业务链路联调
- 错误提示、字段语义、前后端页面联动验证

执行方式：
- 以 `后端API文档.md` 为接口清单逐项执行
- 使用接口测试工具构造请求
- 同步核对数据库、Redis、文件目录、副作用通知
- 对照 `前后端字段对接分析报告.md` 验证高风险接口
- 拼单接口除接口返回外，必须额外核对：
  - `t_group_order_member`
  - `t_group_order.max_participants`
  - `t_group_order.locked`
  - `t_group_order.confirmed_time`

### 4.2 接口自动化测试

适用场景：
- 回归测试
- 每次提交后的冒烟验证
- 状态流转、边界值、鉴权、异常处理稳定性验证

建议分层：
- 第一层：冒烟集
  - 用户登录、获取用户信息、地址列表、创建订单、取消订单、提交评价、获取通知
- 第二层：核心业务回归集
  - 用户、订单、支付、钱包、地址、评价、收藏、商家、管理端
- 第三层：扩展能力回归集
  - AI、节日推荐、拼单、消息、定时任务、统计报表

自动化建议：
- 后端接口集成测试可使用 Java 接口测试框架
- 独立接口回归可使用接口测试平台或命令行集合执行
- 测试结果需输出：通过数、失败数、失败接口、失败原因、耗时、环境标识

## 五、测试优先级

### P0

- 注册、登录、发送验证码、重置密码
- 获取用户信息、更新用户资料、上传头像、修改密码
- 地址列表、默认地址、创建地址、更新地址、删除地址、设默认地址
- 创建订单、获取订单详情、订单状态更新、取消订单、支付、再来一单
- 提交评价、查看订单评价、商家回复评价、评价统计
- 通知列表、未读数、标记已读、全部已读
- 当前报告中的所有 P0 不一致项

### P1

- 商家资料、商家公告、商家统计、商家评价
- 收藏、心愿单、节日推荐、定位、优惠券、钱包
- 拼单、聊天、消息、AI聊天历史
- 管理端用户、商家、订单、菜品、配置、日志

### P2

- 教程、缓存监控、定时任务、天气、文件访问、热点管理等辅助能力

## 六、测试结果判定标准

### 6.1 单接口通过标准

- HTTP状态正确
- 业务响应结构符合统一格式：`success`、`code`、`message`、`data`
- 字段名称、类型、是否必填与文档一致
- 成功场景返回值正确
- 异常场景返回错误码和提示正确
- 数据库、Redis、文件、副作用通知符合预期
- 拼单接口还需满足：
  - 成员表记录与接口返回的 `members/participants/currentCount` 一致
  - 锁单状态与 `locked/confirmedTime/canEdit/canLeave/canConfirm/canPay` 一致
  - 支付结果与 `currentUserPaid/payStatus/paidAmount` 一致

### 6.2 回归通过标准

- P0 用例通过率 100%
- P1 用例通过率不低于 95%
- 不允许出现新增的字段名不一致、类型不一致、路径不存在、请求方式错误
- 不允许出现订单、支付、钱包、评价状态流转错误
- 不允许出现拼单成员表、锁单状态、支付状态与接口返回不一致

## 七、按接口分类的测试用例

## 7.1 用户与认证

| 用例编号 | 接口 | 场景 | 输入 | 预期输出 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| U-01 | `POST /api/v1/users/register` | 正常注册 | 合法 `phone/password/captcha/checkCodeKey/nickname/email` | `success=true`，返回“注册成功” | P0 |
| U-02 | `POST /api/v1/users/register` | 缺验证码 | 缺 `captcha` 或 `checkCodeKey` | `code=400`，提示验证码不能为空 | P0 |
| U-03 | `POST /api/v1/users/login` | 密码登录成功 | 正确 `phone/password/captcha/checkCodeKey` | 返回 `token`、`user`、`userInfo` | P0 |
| U-04 | `POST /api/v1/users/login` | 验证码错误 | 错误 `code` | `code=400`，提示验证码错误 | P0 |
| U-05 | `GET /api/v1/users/{userId}` | 获取用户详情 | 合法 `userId` | 返回用户对象，且包含钱包信息 | P0 |
| U-06 | `PUT /api/v1/users/{userId}/password` | 修改密码 | `oldPassword/newPassword` | 成功返回“密码修改成功” | P0 |
| U-07 | `PUT /api/v1/users/{userId}/avatar/base64` | 上传头像 | `avatarBase64` | 返回新的 `avatarBase64` | P1 |

## 7.2 地址

| 用例编号 | 接口 | 场景 | 输入 | 预期输出 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| A-01 | `GET /api/v1/addresses/user` | 获取地址列表 | `userId` 合法 | 返回该用户地址数组 | P0 |
| A-02 | `GET /api/v1/addresses/default` | 获取默认地址 | `userId` 合法 | 返回默认地址或空 | P0 |
| A-03 | `POST /api/v1/addresses` | 新增地址 | `userId/receiverName/receiverPhone/province/city/district/detail/isDefault` | 创建成功 | P0 |
| A-04 | `PUT /api/v1/addresses/{id}` | 更新地址 | 合法地址ID + 更新字段 | 更新成功 | P0 |
| A-05 | `DELETE /api/v1/addresses/{id}` | 删除地址 | `id + userId` | 删除成功 | P0 |
| A-06 | `PUT /api/v1/addresses/{id}/default` | 设默认地址 | `id + userId` | 仅该地址为默认 | P0 |
| A-07 | `POST /api/v1/addresses` | 字段名错误 | 传 `name/phone` 替代 `receiverName/receiverPhone` | 应识别为字段不对齐并记录问题 | P0 |

## 7.3 订单与支付

| 用例编号 | 接口 | 场景 | 输入 | 预期输出 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| O-01 | `POST /api/v1/orders` | 创建订单 | 合法 `order + dishes[]` | 返回订单ID | P0 |
| O-02 | `GET /api/v1/orders/{orderId}` | 获取订单详情 | 合法 `orderId` | 返回订单对象 | P0 |
| O-03 | `PUT /api/v1/orders/{orderId}/status` | 更新订单状态 | 查询参数 `status=1/2/3/4` | 状态更新成功 | P0 |
| O-04 | `PUT /api/v1/orders/{orderId}/status` | 错误传参方式 | 将 `status` 放请求体 | 后端不应收到状态，需记录联调风险 | P0 |
| O-05 | `PUT /api/v1/orders/{orderId}/cancel` | 取消待支付订单 | `reason` 查询参数 | 返回“订单已取消” | P0 |
| O-06 | `PUT /api/v1/orders/{orderId}/cancel` | 非法状态取消 | 已完成订单 | `code=400`，提示不可取消 | P0 |
| O-07 | `POST /api/v1/orders/{orderId}/pay` | 支付成功 | `userId/paymentMethod` | 返回“支付成功” | P0 |
| O-08 | `POST /api/v1/orders/{orderId}/reorder` | 再来一单 | 合法 `orderId` | 返回复购结果 | P1 |

## 7.4 评价

| 用例编号 | 接口 | 场景 | 输入 | 预期输出 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| R-01 | `POST /api/v1/reviews` | 提交评价 | `orderId/merchantId/rating/content/images` | 返回评价对象 | P0 |
| R-02 | `POST /api/v1/reviews` | 缺订单ID | 缺 `orderId` | `code=400` | P0 |
| R-03 | `GET /api/v1/reviews/order/{orderId}` | 获取订单评价 | 合法 `orderId` | 返回评价详情与回复列表 | P0 |
| R-04 | `POST /api/v1/reviews/{reviewId}/additional` | 追加评价 | `content/images` | 返回追评记录 | P1 |
| R-05 | `POST /api/v1/reviews/{reviewId}/reply` | 商家回复 | `merchantId/content` | 返回“回复成功” | P0 |
| R-06 | `GET /api/v1/reviews/merchant/{merchantId}/statistics` | 评价统计 | 合法 `merchantId` | 返回总数、均分、回复数等 | P1 |

## 7.5 通知

| 用例编号 | 接口 | 场景 | 输入 | 预期输出 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| N-01 | `GET /api/notifications/user/{userId}` | 获取通知列表 | 合法 `userId` | 返回通知数组 | P0 |
| N-02 | `GET /api/notifications/unread-count` | 获取未读数 | `userId` | 返回整数 | P0 |
| N-03 | `PUT /api/notifications/{notificationId}/read` | 标记已读 | 合法 `notificationId` | 返回“标记成功” | P0 |
| N-04 | `PUT /api/notifications/all-read` | 全部已读 | `userId` | 返回“所有消息已标记为已读” | P0 |
| N-05 | `DELETE /api/notifications/batch` | 批量删除 | ID数组 | 删除成功 | P1 |

## 7.6 收藏与心愿单

| 用例编号 | 接口 | 场景 | 输入 | 预期输出 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| F-01 | `GET /api/v1/favorites` | 获取收藏列表 | `userId/type` | 返回收藏列表 | P1 |
| F-02 | `POST /api/v1/favorites/dishes` | 收藏菜品 | `userId/collectableId` | 返回收藏对象 | P1 |
| F-03 | `DELETE /api/v1/favorites/dishes/{dishId}` | 取消收藏 | `userId + dishId` | 成功 | P1 |
| W-01 | `POST /api/v1/wish-list/item` | 创建想吃项 | DTO 合法 | 返回 `itemId` | P1 |
| W-02 | `GET /api/v1/wish-list/items` | 获取我的想吃列表 | 登录态 | 返回列表 | P1 |
| W-03 | `GET /api/v1/wish-list/merchant/pending` | 获取商家待审核列表 | 商家登录态 | 返回待审核列表 | P1 |
| W-04 | `POST /api/v1/wish-list/audit` | 审核想吃项 | 审核 DTO | 返回布尔结果 | P1 |

## 7.7 商家与管理端

| 用例编号 | 接口 | 场景 | 输入 | 预期输出 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| M-01 | `POST /api/v1/merchant/register` | 商家注册 | 合法注册数据 | 返回商家对象 | P1 |
| M-02 | `GET /api/v1/merchant/{merchantId}` | 商家详情 | 合法 `merchantId` | 返回商家详情 | P1 |
| M-03 | `GET /api/v1/merchant/{merchantId}/statistics` | 商家统计 | 合法 `merchantId` | 返回统计数据 | P1 |
| A-ADMIN-01 | `POST /api/admin/login` | 管理员登录 | 正确账号密码 | 登录成功 | P1 |
| A-ADMIN-02 | `GET /api/admin/orders` | 管理端订单列表 | 合法筛选参数 | 返回分页数据 | P1 |
| A-ADMIN-03 | `PUT /api/admin/merchants/{merchantId}/audit` | 商家审核 | 正确审核参数 | 审核成功 | P1 |

## 7.8 AI与扩展能力

| 用例编号 | 接口 | 场景 | 输入 | 预期输出 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| AI-01 | `POST /api/v1/ai/recipe` | AI食谱生成 | 合法文本 | 返回食谱结果 | P1 |
| AI-02 | `POST /api/v1/ai/dish-recognize` | 菜品识别 | 合法图片 | 返回识别结果 | P1 |
| AI-03 | `GET /api/v1/ai/chat/history` | 获取聊天历史 | `userId` | 返回历史消息 | P1 |
| AI-04 | `POST /api/agent/supervisor/chat` | 非流式对话 | 合法请求体 | 返回完整回答 | P2 |
| AI-05 | `POST /api/agent/supervisor-sse/chat` | 流式对话 | 合法请求体 | 按事件流返回内容 | P2 |

## 7.9 拼单专项

| 用例编号 | 接口 | 场景 | 输入 | 预期输出 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| GO-01 | `POST /api/v1/group-orders/group-orders` | 创建拼单并持久化人数上限 | 合法 `initiatorId/groupId/merchantId/addressId/maxParticipants` | 返回拼单ID，`t_group_order.max_participants` 正确落库，`locked=false`，`confirmed_time=NULL` | P0 |
| GO-02 | `POST /api/v1/group-orders/group-orders` | 创建拼单后初始化成员表 | 创建成功后查询数据库 | `t_group_order_member` 至少存在发起人一条有效记录，`role=initiator`，`leave_status=0` | P0 |
| GO-03 | `POST /api/v1/group-orders/join` | 按订单码加入 | 合法 `orderCode/userId` | 返回 `groupOrderId/orderCode/currentCount/maxParticipants/joined/joinable`，成员表新增或恢复有效成员记录 | P0 |
| GO-04 | `GET /api/v1/group-orders/group-orders/{groupOrderId}` | 详情真值回显 | 携带 `userId` | `members/participants/currentCount/maxParticipants/currentUserJoined/currentUserPaid/canEdit/canLeave/canConfirm/canPay` 与数据库一致 | P0 |
| GO-05 | `GET /api/v1/group-orders/users/{userId}/orders` | 用户拼单列表真值回显 | 合法 `userId` | 列表项中的 `members/participants/currentCount/maxParticipants/locked` 与详情口径一致 | P0 |
| GO-06 | `POST /api/v1/group-orders/group-orders/{groupOrderId}/selections` | 保存选菜 | 合法 `userId/dishes[]` | 返回当前用户选菜记录，详情中的成员金额、菜品汇总同步更新 | P0 |
| GO-07 | `POST /api/v1/group-orders/group-orders/{groupOrderId}/selections` | 已锁单后改菜 | 已确认成团的拼单 + 合法菜品 | 返回失败，提示不能继续修改选菜 | P0 |
| GO-08 | `GET /api/v1/group-orders/group-orders/{groupOrderId}/selections/{userId}` | 获取用户选菜 | 合法拼单ID与用户ID | 返回该用户菜品列表，字段完整正确 | P0 |
| GO-09 | `POST /api/v1/group-orders/group-orders/{groupOrderId}/confirm` | 发起人确认成团 | `userId=initiatorId` 且至少有一份菜品 | 返回 `locked=true`、`confirmedTime` 非空、`canConfirm=false`、`canEdit=false` | P0 |
| GO-10 | `POST /api/v1/group-orders/group-orders/{groupOrderId}/confirm` | 非发起人确认成团 | 普通成员 `userId` | `code=403`，提示仅发起人可确认 | P0 |
| GO-11 | `POST /api/v1/group-orders/group-orders/{groupOrderId}/confirm` | 空拼单确认 | 无任何选菜 | `code=400`，提示至少选择一份菜品 | P0 |
| GO-12 | `DELETE /api/v1/group-orders/group-orders/{groupOrderId}/leave` | 未确认前退出 | 普通成员 `userId` | 返回退出成功，成员表 `leave_status=1`、`leave_time` 非空，用户菜品被清理，`currentCount` 减少 | P0 |
| GO-13 | `DELETE /api/v1/group-orders/group-orders/{groupOrderId}/leave` | 发起人退出 | 发起人 `userId` | `code=400`，提示发起人不能直接退出 | P0 |
| GO-14 | `DELETE /api/v1/group-orders/group-orders/{groupOrderId}/leave` | 已支付成员退出 | 已支付普通成员 `userId` | `code=400`，提示已支付成员暂不支持退出拼单 | P0 |
| GO-15 | `DELETE /api/v1/group-orders/group-orders/{groupOrderId}/leave` | 已确认后退出 | 已锁定拼单 | `code=400`，提示不能退出 | P0 |
| GO-16 | `POST /api/v1/group-orders/group-orders/{groupOrderId}/pay` | 单人余额支付 | `paymentType=single,paymentMethod=balance` | 支付成功，成员表该用户 `pay_status=paid`、`paid_amount` 正确，详情 `currentUserPaid=true` | P0 |
| GO-17 | `POST /api/v1/group-orders/group-orders/{groupOrderId}/pay` | 发起人统一余额支付 | `paymentType=all,paymentMethod=balance` | 全部有效成员 `pay_status=paid`，拼单状态流转到 `1`，`locked=true` | P0 |
| GO-18 | `POST /api/v1/group-orders/group-orders/{groupOrderId}/pay` | 未确认直接支付 | `paymentMethod=balance` | `code=400`，提示先确认成团 | P0 |
| GO-19 | `POST /api/v1/group-orders/group-orders/{groupOrderId}/pay` | 无金额可支付 | 当前用户无应付金额或已支付 | `code=400`，提示当前没有可支付的拼单金额 | P0 |
| GO-20 | `POST /api/v1/group-orders/group-orders/{groupOrderId}/pay` | 微信支付模拟 | `paymentMethod=wechat` | 返回 `paymentNo/status/paymentParams`，且 `paymentParams.type=wechat`，不校验真实商户回调 | P1 |
| GO-21 | `POST /api/v1/group-orders/group-orders/{groupOrderId}/pay` | 支付宝支付模拟 | `paymentMethod=alipay` | 返回 `paymentNo/status/paymentParams`，且 `paymentParams.type=alipay`，不校验真实商户回调 | P1 |
| GO-22 | `POST /api/v1/group-orders/group-orders/{groupOrderId}/invite` | 获取分享信息 | 可选 `friendIds[]` | 返回 `orderCode/shareTitle/shareText/qrcodeContent/inviteeCount` | P1 |

### 7.9.1 拼单数据库校验矩阵

| 校验对象 | 校验点 | 通过标准 |
| --- | --- | --- |
| `t_group_order_member` | 成员增删、角色、支付状态、退出状态 | 与接口返回的 `members/participants` 完全一致 |
| `t_group_order.max_participants` | 创建、详情、列表人数上限 | 与接口返回的 `maxParticipants` 一致 |
| `t_group_order.locked` | 确认成团、全部支付后状态 | 与接口返回的 `locked/canEdit/canLeave/canConfirm/canPay` 一致 |
| `t_group_order.confirmed_time` | 确认成团后写入时间 | 与接口返回的 `confirmedTime` 一致，且锁单前为空 |
| `t_payment_record` | 单人支付、统一支付、模拟支付 | 余额支付写真实成功记录；微信/支付宝仅校验模拟返回结构 |

## 八、执行节奏建议

1. 每次后端接口变更后，先更新 `后端API文档.md`。
2. 每次前端接口层变更后，重新执行字段对齐检查，更新 `前后端字段对接分析报告.md`。
3. 每日构建执行 P0 冒烟；每周执行一次全量回归。
4. 缺陷关闭前必须包含：接口请求报文、响应报文、环境、复现步骤、数据库核对结果。
