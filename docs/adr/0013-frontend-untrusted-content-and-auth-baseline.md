# 0013 — 前端安全基线：不可信内容不拼 HTML、密码不落盘、非幂等请求不重试、解析失败即拒绝

## 背景

#567 逐条确认了渲染进程里的四处「最省事」选择，它们单独看都不起眼，叠在一起就是可被远程触发的账号接管链：

1. 聊天搜索把消息原文 `replace(new RegExp(keyword), '<mark>$1</mark>')` 拼成 HTML，再交给 `v-html`。对端发一条含 `<img onerror>` 的消息，搜索关键词的一方就会执行脚本，而 `token`、`admin_token` 就在同一份 `localStorage` 里。
2. 关键词未做正则转义，输入 `(` 之类字符会让 `new RegExp` 抛异常，构造病态正则还能卡住渲染进程。
3. 「记住密码」把登录密码明文写进 `savedAccounts`；Web 版的落点就是 `localStorage`。
4. `utils/api.js` 的重试条件不区分请求方法，POST 超时后自动重发，下单、支付可能重复成单。
5. 路由守卫 `isSessionUsable` 在 token 解析失败时返回 `true`，注释写着「交给后端 401 兜底」；后端当前不拦（见 #564），于是手写 `localStorage.admin_token='x'` 就能进 `/admin/*`。

## 决策

1. **不可信内容一律以文本渲染。** 消息原文、搜索结果只允许切分成文本片段交给组件渲染；高亮用 `<mark>` 组件包裹命中片段，不生成 HTML 字符串，`MessageSearchPanel` 不再使用 `v-html`。本决策只覆盖本次改动涉及的渲染路径，不等于全仓 `v-html` 审计结论。
2. **正则关键词必须转义。** 关键词进入 `new RegExp` 前转义元字符，保证 `(`、`*`、`\` 等输入不抛异常。
3. **密码不持久化。** 「记住密码」选项从用户登录页移除；`savedAccounts` 只保留手机号（`{ phone }`），读到历史明文条目时立即回写清掉，且不再回填密码。
4. **只重试幂等请求。** 请求库的重试只对 `GET`/`HEAD` 生效；`POST`/`PUT`/`PATCH`/`DELETE` 一律不自动重发。
5. **解析失败的 token 视为无效。** 路由守卫的 `isSessionUsable` 对缺失、非三段式、payload 非法、缺少 `exp` 或已过期的 token 都返回 `false`，不再依赖后端兜底。

## 替代方案

- **保留 `v-html` + DOMPurify 净化**：消息本身就是纯文本，不存在需要保留的富文本；净化的攻击面比「根本不生成 HTML」更大，还要维护白名单。
- **加密后再存密码**：解密密钥只能放在渲染进程，任何一次 XSS 都能同时拿到密文和密钥，等同于不加密。
- **给 POST 加幂等键再重试**：当前后端没有幂等键契约，改动会越过前端范围；在契约建立前，不重试是唯一安全的默认。
- **解析失败继续放行、只靠后端 401**：后端目前不校验（#564），这条路等于不设防；前端守卫本就该拒绝无法确认的会话。

## 后果

- 聊天搜索此前从未把输入框关键词传进 `useMessageActions`（内部 `searchKeyword` 无人赋值），搜索结果永远为空；本次顺手把 `@search` 的关键词接进 `searchMessages`，高亮修复才真正可被触发与验证。这是行为修复，不是新增功能。
- `16-admin-ai-release` 用例伪造的 `admin_token` 是带 `exp` 的合法三段式 JWT，严格化后仍能通过守卫，因此该 spec 无需改动；已用单测覆盖「合法 JWT 放行、解析失败拒绝」。
- 新增 `utils/searchHighlight.js`、`utils/savedAccounts.js`、`utils/apiRetry.js` 三个纯函数模块，`isSessionUsable` 收敛到 `utils/tokenCleaner.js`，由 `apps/client/scripts/test-renderer-security.mjs` 直接单测。
- 全仓其余 `v-html`（教程 Markdown、AI 卡片等）不因本决策自动获得授权，需要时各自单独评估。
