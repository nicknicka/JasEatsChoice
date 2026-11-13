# 0014 — 身份与金额只认服务端：不再采信 X-User-Id，订单金额由菜品表重算，充值收口到管理端

## 背景

#564 复现出一条完整攻击链，公开仓库任何人都能照着走：

1. 任意有效 token（注册一个普通用户即可）+ 请求头 `X-User-Id: <受害者ID>`；
2. `JwtAuthenticationFilter` 对任何通过验签的 token 都无条件追加 `ROLE_ADMIN`；
3. `POST /v1/wallet/recharge` 既无方法注解也无登录要求，`userId`、`amount` 全部来自 query；
4. `POST /v1/orders` 把请求体里的 `Order` 原样 `save()`，单价与 `totalAmount` 也来自请求体。

每一步单独看都只是「少一个注解」，串起来的结果是：伪造身份 → 拿到管理员角色 → 给任意账户造任意余额 → 用 0.01 元下单。金额、身份、角色三条信任链全部指向客户端。

## 决策

1. **身份只有一个来源。** `UserIdentityUtil` 只读 `JwtAuthenticationFilter` 验签后写入的请求属性 `jaseats.verifiedUserId`，删除 `X-User-Id` 请求头兜底与工具类内的二次令牌解析；未登录统一 401。两端前端同步停止发送 `X-User-Id`，避免后来者误以为它有效。
2. **角色由令牌声明决定。** `ROLE_ADMIN` 只在令牌的 `permissions` 含 `admin:*` 时授予（管理端令牌由 `AdminServiceImpl` 签发，权限一律该前缀）；普通用户令牌只拿到 `ROLE_USER`。
3. **订单金额由服务端重算。** 下单人取令牌身份，`OrderPricing` 按 `mch_dish.price × quantity` 计算总额并回写每条 `OrderDish.price`；请求体的 `totalAmount` 与 `price` 直接丢弃。跨商家、已下架、数量非 1–99、菜品不存在一律 400。优惠券仍在支付环节由服务端核销，不在下单时信任客户端折后价。
4. **充值只允许管理端入账。** `/v1/wallet/recharge` 加 `@PreAuthorize("hasRole('ADMIN')")`；用户侧（桌面端钱包页、小程序钱包页）不再调用该接口，只提示「在线充值通道暂未开放，请联系客服」。真实支付渠道接入前，用户侧没有可验证的资金到账凭据，放开自助充值等于凭空造钱。
5. **动钱的操作只能对自己做。** 钱包读接口本人或管理员，提现只允许本人；`POST /v1/orders` 与 `POST /v1/payment/create` 校验订单归属，`/{orderId}/pay` 同样拒绝代付他人订单。路径与 query 里的 `userId` 只为兼容旧前端保留，不参与鉴权。

## 替代方案

- **保留 `X-User-Id` 但加签名**：等于再造一套会话机制，与 JWT 重复且更难审；直接删掉更小的攻击面。
- **按角色表实时查库判定管理员**：每次请求多一次查询，而令牌内的 `admin:*` 权限本就由服务端签发、可吊销（`AdminServiceImpl` 已有权限表）。当前选择信任签名内的声明。
- **下单时信任客户端金额，只在支付时校验**：支付金额取自 `ord_order.total_amount`，一旦下单可写 0.01，支付环节校验也只会校验这个 0.01。
- **把充值改成「模拟支付回调」**：没有真实支付渠道时，回调本身就是伪造入口，不成立。
- **`SecurityConfig` 立刻改成 `anyRequest().authenticated()`**：方向正确，但公开接口清单尚未盘点（小程序匿名页、健康检查、OpenAPI、Agent 回调），一次改完等于用三个客户端的可用性做赌注。本题交给后续 issue 按「先清单、再默认拒绝」推进，本决策不宣称已具备该防线。

## 后果

- 任何依赖 `X-User-Id` 的旧调用会退化为未登录：`AddDishController` 两个方法改为读可信身份；`BehaviorTrackingUtil` 在匿名请求下不再记录用户维度。
- E2E 需要跟着改：`04-order.spec.js` 的充值改为先用管理员令牌登录再入账，下单相关请求带上登录态；`06-wallet.spec.js` 的余额读取带令牌，充值用例改为断言「提示走客服、余额不变」。`OrderController` / `PaymentController` 的单测同步新签名。
- 新增单测：`OrderPricingTest`（重算与边界）、`OrderControllerTrustTest`（身份与归属）、`JwtAuthenticationFilterTest`（角色授予）、`UserIdentityUtilTest`（伪造头失效）。
- 客户端不再展示「充值即时到账」，这是行为变更而不仅是安全修复：产品侧需要另行安排真实支付渠道。
- `docs/api/后端API文档.md` 是 OpenAPI 快照，需在栈起来后重新生成，本次未手工编辑。
