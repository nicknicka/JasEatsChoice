# 0017 生产 profile 的四项配置修正：fail fast 优先于静默降级

- 状态：已采纳
- 日期：2026-11-14
- 关联：issue #566、#579（父 issue）

## 背景

`application-prod.yml` 有四处配置「看起来对、跑起来不对」。共同点是**失败形态是静默的**：
应用能起来，但短信不发、迁移跳过、nginx 起不来、密码明文可见。生产环境最难排查的正是这一类。

| # | 位置 | 现状 | 后果 |
|---|---|---|---|
| 1 | `application.yml` + `AliyunSMSServiceImpl` | `mockMode: true` 写死在主配置，prod 无覆盖 | 生产不发短信，验证码打日志；能读日志的人可拿任意手机号的码 |
| 2 | `application.yml` | `baseline-on-migrate: true` + `baseline-version: 0` | 已有表无 history 的库：baseline 到 0 → 跑 `V1` 的 `CREATE TABLE` → `table already exists`，起不来 |
| 3 | `prod.conf.template` + `.env.example` | 模板拼 `${SSL_CERT_DIR}/live/${API_DOMAIN}/...`，而 `SSL_CERT_DIR=/etc/letsencrypt/live` | 路径变 `/etc/letsencrypt/live/live/<domain>/...`，nginx 起不来 |
| 4 | `compose.yml` | `redis-cli -a "$REDIS_PASSWORD"`，单 `$` 由宿主侧展开 | 明文密码进容器配置，`docker inspect`/`ps` 可见 |

## 决策

1. **SMS 生产必填，缺则启动即失败。** `application-prod.yml` 增加
   `aliyun.sms.{mock-mode: false, endpoint, sign-name, template-code}` 四项，全部读环境变量；
   `AliyunSMSConfiguration` 用 `StringUtils.hasText` 校验（空串也视为缺失）后抛
   `IllegalArgumentException`。业务实现里的 `mockMode: true` 保留在**非 prod** 环境，
   但生产不存在「悄悄降级成打日志」这条路径。
2. **Flyway 基线定到既有库的实际版本。** 关键观察：`db/init/01_schema.sql` 与
   `V1__init_schema.sql` **结构不等价**（`01_schema.sql` 的 `t_chat_msg` 多一列 `synced`，
   61 张表仍是 `t_` 前缀、未做 V3 重命名），因此 issue 原文设想的「baseline 到 0 再全量迁移」
   在遗留库上必然失败——`V2__seed_data.sql` 的 `INSERT` 列数对不上。
   墓碑版本不能猜成「空库起点」，只能取**「既有库已经到最新」**这个语义，故
   `baseline-version: 24`（当前迁移链的末版本）。空库没有 history，Flyway 正常从 `V1` 全量迁移；
   遗留库有表无 history，baseline 到 24 后跳过全部迁移。
3. **证书目录语义统一为「父目录」。** `.env.example` 的 `SSL_CERT_DIR` 改为 `/etc/letsencrypt`，
   模板负责补 `/live/<domain>/`。验收方式是把 `docker compose config` 的输出作为断言对象，
   看路径里不再出现两次 `live`。
4. **容器内的 `$` 一律写 `$$`。** Compose 的 `$$` 转义为字面 `$`，交给容器内 shell 展开。
   Redis 的 `command` 与 `healthcheck.test` 两处都改成 `"$$REDIS_PASSWORD"`，
   密码不再出现在 `docker inspect` 的配置里。

## 替代方案

- **SMS 保持 mock，靠部署文档提醒填配置**：文档不是执行点。漏填的结果是「验证码只打日志」，
  且日志里有他人手机号的验证码——这是可用性问题叠加安全问题。
- **Flyway `baseline-version: 1`（issue 原文的方向）**：实测失败。
  它隐含「`V1` 可以从空的既有库上安全重放」这个前提，而 `01_schema.sql` 与 `V1` 不等价使该前提不成立。
  真正安全的做法是先让两者结构对齐（或让 `01_schema.sql` 退役），但那是独立的重构工作，
  不该塞进一个配置修正里。本次只把 baseline 定到当前末版本，保证「起得来」。
- **`db/init/01_schema.sql` 直接从仓库删除**：它未被挂载为 `initdb.d`，运行时建表由 Flyway `V1` 负责，
  仅 README 提及。但它的存在本身是「两份 schema 定义」的隐患（正是本次 baseline 判断错误的根源），
  记入后续 issue，不在本次删除。
- **`SSL_CERT_DIR` 改为保留 `/live` 后缀、模板去掉 `/live`**：同样能消除双 `live`，
  但「父目录」语义更符合变量名（`DIR` 指证书根），也让 certbot 的工作目录一眼可推。

## 后果

- 生产启动时若缺 `ALIYUN_SMS_ENDPOINT/SIGN_NAME/TEMPLATE_CODE` 或 `ALIBABA_CLOUD_ACCESS_KEY_*`，
  直接抛异常退出，而不是带着打日志的假短信在线上跑。
- 空库路径（`make reset-db`）验证通过：`V1..V24` 全量迁移成功；
  遗留库（表齐全、无 `flyway_schema_history`）验证通过：输出
  `Successfully baselined schema with version: 24` 且应用启动成功。
- `deploy/.env.example` 新增「Aliyun SMS（生产必填）」块，附获取方式说明。
- 新增单测 `AliyunSMSConfigurationTest`：缺任一必填项（含空串）抛异常、四项齐全时成功创建 client。
- **更正 issue 原文的一处判断**：`01_schema.sql` 的乱码注释是**可无损还原**的（UTF-8 字节被
  cp1252 误读，用完整 cp1252 反向表逐段解码即可）。已还原 `01_schema.sql` 491 行、`V1` 436 行乱码，
  还原后 `grep -c "è\|æ\|ç"` 为 0。此项不是「重新生成注释」，而是**还原既有字节**。
