# 0015 支付密码与商家资源写接口：同一类操作只允许一种鉴权口径

- 状态：已采纳
- 日期：2026-11-14
- 关联：issue #573、#565（支付密码强制校验）、#569（商家相册上传鉴权）、#564（身份只认服务端）

## 背景

`#565` 把订单支付改成「支付密码必填」，`#569` 给商家相册上传补了归属校验。两次修复都只覆盖了被点到的那一个入口，留下两条同类的旁路：

1. `POST /v1/group-orders/{id}/pay` 直接把请求体里的 `userId` 当付款人，`paymentPassword` 虽然被读出来，但要到 `handleGroupOrderWalletPayment` 里才「看一眼」——而那里只有一段注释，没有任何校验，随后照样 `checkBalance` → `deductBalance`。群订单支付因此成为「无密码扣款」的替代入口。
2. `DELETE /v1/merchant/{merchantId}/album` 与上传是同一份数据、同一个路径，`merchantId` 直接来自 path，既没有登录判断也没有归属判断。任意登录用户删除任意商家的相册条目，文件系统里的图片也一并删掉。

两处的共同点不是「漏了一个注解」，而是**同一类操作在不同入口被判定了两套口径**。攻击者不需要破解校验，只需要换入口。

## 决策

1. **动余额的入口，密码校验写在最前面、写在唯一的地方。** 群订单支付在 `payGroupOrder` 里校验：缺失 → `400 请输入支付密码`，`verifyPaymentPassword` 失败 → `400 支付密码错误`。校验位置在 `createPayment` 之前、优惠券核销之前，因此密码错误不会留下失败的支付流水、不会消耗优惠券、不会推进群订单状态。`handleGroupOrderWalletPayment` 不再接收密码参数——传了密码却不校验的签名本身就是下一个漏洞的温床。
2. **付款人只认令牌身份。** 群订单支付的 `userId` 同样改由 `UserIdentityUtil.requireVerifiedUserId(request)` 提供，请求体里的 `userId` 退化为无效字段（保留不读，兼容旧前端）。这与 `#564` 的口径一致：身份只有一个来源。
3. **商家资源的写接口共用同一个判定。** 相册删除复用上传时的 `extractVerifiedUserId` + `canManageMerchant`：未登录 401，非本人且非管理员 403，判定在触达数据库之前完成。
4. **校验失败不得产生副作用。** 两条拒绝路径都以「没有写操作」为验收条件，用单测 `verifyNoInteractions` / 状态断言固定下来，而不是只看返回码。

## 替代方案

- **给群订单支付单独开一个「免密额度」**：需要额外维护用户级配置与风控，而当前群订单页已经有密码输入能力，直接强制更简单也更一致。
- **只在前端加密码框，后端仍宽松**：前端不是信任边界，直接调接口依然免密，等于没改。
- **给相册删除加 `@PreAuthorize("isAuthenticated()")`**：只校验「登录过」，不校验「是不是这家店的」，任何注册用户仍可删别人的相册。
- **一次把 `MerchantController` 所有写接口都补鉴权**（`PUT /{merchantId}`、头像上传、公告 CRUD、接单）：方向正确，但涉及商家端多处调用链，且与 `#576`（`SecurityConfig` 默认拒绝）是同一问题的系统性解法。本决策只收口 issue 点名的两处，其余记入 `#576` 的盘点清单。

## 后果

- 群订单支付从「免密」变为「必填 6 位支付密码」。桌面端 `OrderConfirmation.vue` 与小程序 `settle.vue` 同步加了密码输入，用户在群订单页取消输入即终止支付（不创建流水）。
- 未设置支付密码的用户无法完成群订单支付，与普通订单支付的行为一致；这是产品口径问题，不是本次引入的新限制。
- 新增单测 `GroupOrderPayPasswordTest`（缺失/错误密码、匿名调用均不产生流水与扣款）与 `MerchantAlbumAuthTest`（非商家 403、未登录 401、管理员放行），`ThirdPartyPaymentControllerTest` 同步新签名。
- `MerchantController` 中仍未鉴权的写接口不止相册删除（`PUT /{merchantId}`、`POST /{merchantId}/avatar`、公告增删改、`PUT /{merchantId}/orders/{orderId}/accept`），统一记入 `#576`：先盘公开接口清单，再把 `SecurityConfig` 改成默认拒绝，让「漏加注解」的默认结果是 403 而不是 200。
