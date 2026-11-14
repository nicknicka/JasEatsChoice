# 公开接口清单（SecurityConfig 默认拒绝）

生成方式：`python3 scripts/check_routes.py --source static` 吐出 480 条后端路由，
按 Controller 与路径前缀归入下表；改路由后重跑即得新清单。

`SecurityConfig` 的默认规则是 `anyRequest().authenticated()`。**只有**下表列出的
接口允许匿名；其余全部需要登录，`/admin/**` 与带 `hasAuthority('admin:*')` 的接口
还需要管理员权限。新增接口忘记加 `@PreAuthorize` 时的结果是 401，不是匿名可调。

## 公开（匿名可访问，显式登记在 SecurityConfig）

| 路径 | 用途 |
|---|---|
| `/v1/users/login` | 用户登录 |
| `/v1/users/register` | 用户注册 |
| `/v1/users/send-sms-code` | 发送短信验证码 |
| `/v1/users/send-email-code` | 发送邮箱验证码 |
| `/v1/users/wechat-login` | 微信登录 |
| `/v1/users/reset-password` | 重置密码 |
| `/v1/merchant/login` | 商家登录 |
| `/v1/merchant/register` | 商家注册 |
| `/admin/login` | 管理员登录 |
| `/v1/captcha/**` | 图形验证码 |
| `/actuator/health`、`/actuator/health/**` | 健康检查（compose、`wait-healthy.sh`） |
| `/v3/api-docs/**`、`/swagger-ui/**`、`/swagger-ui.html`、`/doc.html`、`/webjars/**` | API 文档与联调（`check_routes.py`） |
| `/error` | 容器错误页 |
| `/uploads/**`、`/files/**`、`/synthetic-assets/**` | 图片直出（头像/菜品/聊天图片）。`<img>` 无法带令牌，故只读放行；写入与上传接口仍受保护 |
| `GET /v1/recipe/**` | 食谱只读。小程序「食谱」是 tabBar 页（`pages/recipe/index`），未登录可达且进页即拉今日推荐；点「一键订餐」时才在小程序侧要求登录。`POST /v1/recipe`（新建）仍要求登录 |

## 明确**不**放行的近似路径

| 路径 | 为何不放 |
|---|---|
| `/*.png`、`/*.jpg` 等根级文件名 | `WebConfig` 有「直接按文件名」兼容映射，`FileAccessController.accessFileCompat` 会匿名遍历上传目录。等价于按文件名枚举全站上传内容，超出最小公开集 |
| `/synthetic-assets` 下的文档类后缀 | 仅图片素材需要 `<img>` 直出；pdf/doc/docx 不参与图片渲染 |

## 需登录（默认拒绝覆盖，匿名返回 401）

除上表外的一切，含但不限于：

- 用户数据：`/v1/wallet/**`、`/v1/orders/**`、`/v1/consume-history`、
  `/v1/users/**`（登录注册类除外）、`/v1/address/**`、`/v1/collection/**`
- 浏览与推荐：`/v1/merchant/**`、`/v1/dishes/**`、`/v1/recommendations/**`、
  `/v1/categories`、`/v1/banner/**`（前端路由守卫同样要求先登录才能进首页）
- AI 与内容：`/v1/ai/**`（含 `/v1/ai/capabilities`）、`/v1/content-extraction/**`、`/v1/feedback/**`、
  `/agent/supervisor/**`、`/agent/supervisor-sse/**`（AI 页与聊天入口均在登录后的路由下）
- 食谱写入：`POST /v1/recipe`（读已在公开清单，见上）
- 群订单：`/v1/group-orders/**`、`/v1/group-order-chat/**`

## 管理员（需 `ROLE_ADMIN` 或 `admin:*` 权限）

- `/admin/**`、`/v1/admin/**`、`/v1/tutorial/admin/**`
- 方法级 `@PreAuthorize("hasRole('ADMIN')")` / `hasAnyAuthority('admin:*')`

## 与前端白名单的对齐

| 端 | 位置 | 公开项 |
|---|---|---|
| 桌面/Web | `apps/client/src/renderer/src/utils/authSession.js` `PUBLIC_API_PATHS` | 登录/注册/验证码/重置密码/微信登录/邮箱验证码 |
| 小程序 | `apps/miniapp/src/utils/request.js` `AUTH_WHITELIST` | 同上（用户/商家/管理员登录、注册、验证码、重置密码） |

三处清单同源，改动需同步：任一端新增公开接口，必须同时改 `SecurityConfig` 与本表。
