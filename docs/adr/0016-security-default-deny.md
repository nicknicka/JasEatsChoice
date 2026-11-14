# 0016 鉴权默认值改为拒绝：公开接口显式登记，漏加注解的结果是 401

- 状态：已采纳
- 日期：2026-11-14
- 关联：issue #576、#564（身份只认服务端）、#573（商家写接口鉴权）、前置 #565/#569

## 背景

`#564` 修掉了当前已知的四条信任链（身份、角色、金额、充值），但 `SecurityConfig` 的默认姿态一直是「全放行」：

```java
.requestMatchers("/**").permitAll()
.anyRequest().permitAll();
```

鉴权完全依赖方法上的 `@PreAuthorize`。全仓 82 个 Controller、480 条路由里只有 16 个 Controller 用了
`hasRole('ADMIN')` / `hasAuthority(...)`。**任何新接口只要忘了加注解，就是匿名可调**——`#564` 里的
`POST /v1/wallet/recharge` 正是这么漏出来的。

这不是「某个接口没加注解」的单点问题，而是默认值选错了方向：漏加注解的结果应当是 401/403，
而不是 200。`#564`/`#573` 的修法都是「发现一个补一个」，无法阻止下一个同类入口出现
（`#0015` 结尾已把这一条记入本 issue）。

## 决策

1. **`SecurityConfig` 默认改为 `anyRequest().authenticated()`。** 需要匿名的接口逐个 `requestMatchers(...).permitAll()`
   显式列出，每条带注释说明用途。新增接口忘记加注解时，失败的形态是 401，而不是匿名 200。
2. **公开集合取「最小可行集」**，只放三端**确实**需要匿名访问的：
   - 认证类：`/v1/users/{login,register,send-sms-code,send-email-code,wechat-login,reset-password}`、
     `/v1/merchant/{login,register}`、`/admin/login`、`/v1/captcha/**`
   - 运维联调类：`/actuator/health`（compose 健康检查、`wait-healthy.sh`）、`/v3/api-docs/**`、
     `swagger-ui`、`/doc.html`、`/webjars/**`（`check_routes.py`）、`/error`
   - 图片直出类：`/uploads/**`、`/files/**`、`/synthetic-assets/**`
   - 食谱只读：`GET /v1/recipe/**`（小程序「食谱」是 tabBar 页，未登录可达）
3. **图片直出单独说明。** `<img>` 标签无法携带 `Authorization`，头像/菜品/聊天图片只能匿名读。
   放行的只是**读静态资源**；上传与写入接口仍受保护。`/synthetic-assets/**` 是 V15 迁移后演示账号头像的
   落点，容易漏；`WebConfig` 还有一条根级 `/*.png` 兼容映射，**不**放行（见替代方案）。
4. **放行可以是「方法级」的。** `GET /v1/recipe/**` 放行只覆盖读；`POST /v1/recipe`（新建）、
   `PUT /v1/recipe/toggle-favorite/{id}` 等写接口仍要求登录。归档脚本
   `scripts/gen_route_auth_matrix.py` 因此按「方法 + 路径」而非仅路径统计——
   只按路径会把写接口误报成公开（见后果）。
5. **前端白名单与后端清单同源。** 桌面端 `authSession.js` 的 `PUBLIC_API_PATHS`、小程序 `request.js`
   的 `AUTH_WHITELIST` 与本表三处对齐，写成 `docs/technical/public-routes.md`，任一端新增公开接口必须同时改三处。
6. **业务层内部仍要求登录的接口，保持控制器自行返回 401。** 例如 `/v1/merchant/register` 在
   `SecurityConfig` 里放行（否则登录页拿不到「请先登录」的明确文案），但控制器仍校验
   `Authorization` 并返回 `code=401`。放行的是「鉴权层的拦截」，不是「业务上的匿名可用」。

## 替代方案

- **维持全放行，靠 review 保证每个接口都加注解**：已经失效一次（`#564` 的充值接口），
  且违反「默认安全」的方向；漏掉时的失败形态（200）比拦截时的失败形态（401）危险得多。
- **默认拒绝 + 放行根级 `/*.png` 等图片后缀**：`WebConfig` 的该映射配合
  `FileAccessController.accessFileCompat`，等价于**按文件名匿名遍历整个上传目录**。
  虽然两处都有 canonical path 校验挡住目录穿越，但「知道/猜到文件名就能读」已超出图片直出的必要范围
  （页面渲染用的都是带目录的 `/uploads/...`）。因此不放行，需要时再单独登记。
- **把 `/v1/ai/capabilities`、`/agent/supervisor/**` 一并放行**：这三处原本有测试断言「匿名可读」，
  默认拒绝后会 401。取证后判定是历史随手断言而非产品需求——桌面端路由守卫对 `/user/**`、`/merchant/**`
  强制登录，`capabilities` 只在登录后的 AI 页调用；`SupervisorAgentController` 虽有「匿名→客服助手」
  的降级分支，但该分支属直连调试路径，产品侧没有未登录客服入口。因此改测试为「需登录」，
  而不是为了迁就测试扩大公开面。
- **一次性把 `/v1/scheduled-tasks/**` 也加上管理员权限**：该接口能反射执行任意 Spring Bean 方法，
  目前只要求「登录」确实偏松。但逐接口定权限点是另一个粒度的工作，记入后续 issue。

- **默认拒绝同时收紧 `@PreAuthorize` 的权限粒度**（如 `/v1/scheduled-tasks/**` 这类反射执行任意
  Bean 方法的运维接口，目前只要求「登录」即可）：方向正确，但需要逐接口定权限点，
  与本次「先把默认值改对」是两个不同粒度的工作，记入后续 issue。

## 后果

- 后端：`SecurityConfig` 的公开清单从「无」变为 14 组显式路径；新增集成测试
  `SecurityDefaultDenyIntegrationTest` 固定四类断言——公开端点可达、用户数据端点 401、
  浏览/管理员端点 401、图片直出可匿名读且根级文件名仍 401。
- 测试：默认拒绝后，多个 E2E 用例原本的**匿名数据前置调用**会 401。新增 `tests/utils/auth-token.js`
  （进程内缓存的 `userToken()`/`adminToken()` + `authedFetch`/`authedJson`），
  并把 02/03/05/08/09/15 等 spec 的直连调用改为带令牌；`tests/utils/real-data.js` 统一带上用户令牌，
  一次修好 03/04/05/06/08/09/10 的数据前置。
- **前端：默认拒绝暴露了 2 处「原先靠 permitAll 蒙混」的真实缺陷**（全量 E2E 由此从全绿变成 16 失败，
  修好后 144/144 全绿）。这类调用的共同点是**不走 axios 的全局拦截器**，因此不自动带令牌：
  - `views/user/AI.vue` 的 `loadCapabilities` 用裸 `fetch` 调 `/v1/ai/capabilities` 且**不带 `Authorization`**。
    该接口原 `permitAll`，改「需登录」后 401，被全局拦截器判为「用户态失效」→ 清登录态跳 `/login`
    → AI 页整体卸载。**一处未带令牌的调用会连带炸掉整个页面**（08 的 7 条 + 17 的 5 条同时红）。
    修法：裸 `fetch` 必须自己从 `authStore` 取令牌。
  - `views/admin/Dashboard.vue` 的 `fetchRecentTutorials` 让**管理端**调**用户端**接口
    `/v1/tutorial/list`。管理员会话只持有 `admin_token`，该路径被 `resolveAuthScope` 判为 user scope
    → 取到空的 `auth_token` → 401 → 清管理员态。修法：改用 `/v1/tutorial/admin/list`。
  - 由此得到一条通用验收动作：**默认拒绝改造必须连同「前端裸 fetch / 跨作用域调用」一起盘**。
    排查手段是写一个探针，用**真实令牌**注入 localStorage 后逐页扫 401 并记录最终 URL——
    最终 URL 被改成 `/login` 即说明全局拦截器清了登录态，比逐条读用例断言快得多。
- 运维：`/v1/scheduled-tasks/**` 现在要求登录；触发临时任务的 E2E 用例（15.2）改带管理员令牌。
- 测试替身：`16-admin-ai-release.spec.js` 用**假** admin 令牌 + 全量 stub 跑，原先靠 `/admin/current`
  是 `permitAll`（返回 404 被吞）蒙混；默认拒绝后该请求 401 会把页面弹走，补 stub `**/api/admin/current`。
- 文档：新增 `docs/technical/public-routes.md`，写明清单、生成方式（`check_routes.py --source static`
  的 480 条路由）、与前端两处白名单的对齐关系。
- **验收第 6 条（两端白名单一致）实测抓到一处漂移**：小程序 `AUTH_WHITELIST` 写的是
  `/v1/admin/login`，而后端真实路径是 `/admin/login`（`AdminController` 的 `@RequestMapping("/admin")`），
  多了一层 `v1`。小程序没有任何 admin 页面或 api 模块，这条既是**拼错**也是**死条目**。
  它长期无人发现，是因为漂移的失败形态很隐蔽：小程序端登录管理员时会**不带令牌**发出请求 →
  401 → 全局拦截器判「登录态失效」→ 清 localStorage 跳登录页，现象是「打开页面被弹回登录」，
  很难反查到白名单里的一条拼写错误。已改为 `/admin/login`。
- **新增 `scripts/check_public_route_consistency.py`**（接入 `make check` 的 `check-public-routes`、
  含 8 项单测）：把桌面端 `PUBLIC_API_PATHS` 与小程序 `AUTH_WHITELIST` 的**每条**路径拿去与
  `SecurityConfig` 的 `permitAll` 比对，凡「前端声明公开但后端不放行」即报错。
  只做单向约束——反向（后端放行但某端不涉及，如小程序不渲染 `/uploads/**`）属正常职责差异，
  硬报会产生高频误报而被忽略。路径解析**复用** `gen_route_auth_matrix.public_patterns`，
  避免两处各写一套、其中一处修了 bug 另一处没修而校验静默失效。
- 未纳入本次：`db/init/01_schema.sql` 与 `Flyway V1` 结构不等价（见 `#566` 的 `baseline-version` 讨论），
  属独立历史遗留问题。
