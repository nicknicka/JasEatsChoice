# 0002 — 数据库重构：引入 Flyway + 领域前缀命名规范

对 74 张表进行命名规范化和领域分组，引入 Flyway 管理 schema 迁移，解决当前表名混乱、无版本化迁移、领域边界不清晰的问题。分两阶段执行。

## 背景

当前数据库有 74 张表，存在以下问题：
- 命名不统一：有的表有 `t_` 前缀有的没有（如 `announcement` vs `t_user`）
- 领域边界不可见：74 张表平铺，从表名看不出属于哪个业务域
- 无版本化迁移工具：schema 变更靠手工 SQL 文件管理，`db/migrations/` 仅作追溯不参与启动
- 新环境初始化依赖 `db/init/` 的空卷机制，改种子数据需要删卷重来

## 决策

### 第一阶段：命名 + 迁移工具（机械改动，风险可控）

1. **引入 Flyway** 作为数据库迁移工具。迁移脚本放在 `db/migration/`（Flyway 默认路径），按 `V1__xxx.sql`、`V2__xxx.sql` 编号。
2. **统一领域前缀命名规范**：表名格式为 `<领域前缀>_<实体名>`，全部小写下划线。74 张表按以下领域分组：

| 前缀 | 领域 | 代表表 |
|------|------|--------|
| `usr_` | 用户域 | `usr_user`、`usr_profile`、`usr_preference`、`usr_address` |
| `mch_` | 商家域 | `mch_merchant`、`mch_dish`、`mch_menu`、`mch_review` |
| `ord_` | 订单域 | `ord_order`、`ord_order_dish`、`ord_payment_record`、`ord_refund_record` |
| `wlt_` | 钱包域 | `wlt_wallet`、`wlt_recharge_record`、`wlt_withdraw_record`、`wlt_consume_history` |
| `ai_` | AI 域 | `ai_chat_history`、`ai_chat_session`、`ai_chat_msg`、`ai_recommendation_log` |
| `nut_` | 营养域 | `nut_calorie_record`、`nut_nutrition`、`nut_ingredient_conflict_rule`、`nut_food_nutrition` |
| `soc_` | 社交域 | `soc_group`、`soc_group_order`、`soc_collection`、`soc_wish_list_item` |
| `cnt_` | 内容域 | `cnt_recipe`、`cnt_tutorial`、`cnt_banner`、`cnt_festival` |
| `sys_` | 系统管理域 | `sys_admin`、`sys_role`、`sys_permission`、`sys_config`、`sys_announcement` |
| `ext_` | 外部集成域 | `ext_content_extraction`、`ext_hot_topic`、`ext_oauth_user` |

3. **字段命名统一**：主键统一 `id`，时间戳统一 `created_at` / `updated_at`，逻辑删除统一 `is_deleted`（0=未删, 1=已删）。
4. **索引命名规范**：`<表前缀>_idx_<字段>`（普通索引）、`<表前缀>_uk_<字段>`（唯一索引）。
5. **初始 schema 作为 V1**：把当前 74 张表的建表语句整理为 Flyway 的 `V1__init_schema.sql`，种子数据为 `V2__seed_data.sql`。后续所有 schema 变更都通过新增 migration 脚本进行。

### 第二阶段：结构优化（改动业务逻辑，需谨慎）

第一阶段完成后，再评估以下优化项（不包含在本次决策中，后续单独决策）：
- 冗余表合并或拆分
- 字段类型优化（如金额用 decimal 精度、枚举用 int 还是 varchar）
- 缺失的外键和索引补充
- 大表分区策略

## 考虑过的替代方案

- **用 Liquibase 而不是 Flyway**：放弃。Liquibase 功能更全但复杂度也高，当前团队规模和项目复杂度用 Flyway 完全足够，SQL 优先的方式也符合现有开发习惯。
- **不加领域前缀，靠代码 package 区分**：放弃。74 张表平铺在数据库里，从表名完全看不出领域归属，排查问题和新人上手成本高。前缀是最便宜的可视化方式。
- **保持 `t_` 前缀加领域中缀**（如 `t_usr_user`）：放弃。三层命名太长，且 `t_` 前缀没有实际信息价值。

## 后果

- 改动量大（74 张表 + 75 个 Entity + 72 个 Mapper + 所有 XML + 业务 SQL），但第一阶段本质是机械替换，风险可控
- 后续 schema 变更有版本管理，可以回滚、可以追踪
- 新成员看表名就能大致判断属于哪个领域
- 第一阶段完成后才能评估第二阶段的具体优化项
- MyBatis-Plus 的 `table-underline: true` 配置无需改动，Entity 上的 `@TableName` 注解需要批量更新
