# 0005 — 字段命名统一与 ID 类型策略

对 ADR-0004 里遗留的两项（字段命名统一、ID 类型统一）给出明确决策：**时间字段统一为 `created_at`/`updated_at`，作为独立 PR 分批执行；ID 类型维持现状不做统一，只固化"新表选型规范"**。

## 背景

第一阶段（ADR-0002）只统一了表名，没有动字段名。截至本决策，实际数据的分布是：

**时间字段（108 处，覆盖 74 张表）**

| 列名 | 表数 |
|------|------|
| `create_time` | 60 |
| `update_time` | 48 |
| `created_time` | 4 |
| `updated_time` | 3 |

统一目标 `created_at` / `updated_at` 目前在库里为 0 处。

**ID 类型（65 张表的主键）**

| 类型 | 表数 |
|------|------|
| `varchar(64)` | 24 |
| `bigint`（含 1 张 unsigned） | 20 |
| `varchar(32)` | 17 |
| `varchar(50)` | 3 |
| `int` | 1 |

对应 Entity 侧：`String id` 50 个，`Long id` 16 个。

**逻辑删除字段**：库里 0 处，代码里 0 处 `@TableLogic`。ADR-0002 规划的 `is_deleted` 从未落地。

**索引命名**：301 个二级索引里既有 `idx_admin_id`（字段名）也有 `idx_discount_merchant`（表前缀+字段），两种风格并存。

## 决策

### 1. 时间字段统一为 `created_at` / `updated_at`（执行）

- **命名**：`create_time`/`created_time` → `created_at`；`update_time`/`updated_time` → `updated_at`。
- **数据迁移方式**：用 `ALTER TABLE ... CHANGE COLUMN` 在 Flyway 迁移里保留数据，不做 drop+add。
- **分批**：按领域前缀（`usr_` / `mch_` / `ord_` / …）分迁移脚本，一个域一个脚本，便于 review 与回滚。
- **代码同步**：74 个 Entity 的属性名（`createTime` → `createdAt`）、314 处 getter/setter 调用点、6 个 Mapper XML、前端 128 处 `createTime` + 21 处 `updateTime` 引用、视图定义。
- **为什么值得做**：命名是开发者每天都在读的东西，四种拼写并存会持续造成"这个表到底叫什么"的摩擦；且当前有 107 条 E2E 兜底，迁移风险可控。

### 2. ID 类型不做统一（不执行，只定规范）

- **不改存量**。理由：
  1. 代码里跨表关联全靠 MyBatis-Plus 单表查询（`getById`/`lambdaQuery`），Mapper XML 里 **0 处 JOIN**，实测唯一含 JOIN 的是视图 `v_active_festivals`。ID 类型不匹配导致的隐式转换/索引失效风险在当前架构下不存在。
  2. 改 ID 类型要动全部主键 + 外键列的数据迁移，不可逆，且收益只有"规范好看"。
  3. 风险与收益不成比例，判定为负 ROI。
- **只固化新表选型规范**（见下），避免分布继续扩大。

### 3. 新表选型规范（约束增量）

- **主键**：新表统一用 `bigint`（雪花 ID 或自增，二选一后不再摇摆）。
- **时间戳**：新表统一用 `created_at` / `updated_at`。
- **索引**：新表统一用 `<领域前缀>_idx_<字段>`，如 `ord_order_idx_user_id`。存量 301 个索引不改名。
- 规范写在本 ADR 与 `GLOSSARY.md`，作为 review 检查项。

### 4. 逻辑删除字段（暂不引入）

当前所有删除都是物理删除，没有软删除需求。不为了"ADR-0002 规划过"而加一个永远为 0 的列。等真出现需要软删除的业务场景时再按新表规范引入。

## 考虑过的替代方案

- **时间字段用 `created_at` 但保留 `create_time` 作为视图别名过渡**：放弃。视图别名只会延长混乱期，直接改干净，用 E2E 兜底。
- **ID 统一成 `varchar(32)` 雪花**：放弃。虽然分布式友好，但当前是单体 + 单库 MySQL，`bigint` 更省空间，且存量 `varchar` 改 `bigint` 的迁移成本高于收益。
- **ID 统一成 `bigint` 自增**：同上，存量迁移不可逆，不做。
- **一次迁移改完全部 74 张表**：放弃。单个巨型 `ALTER` 迁移无法 review、失败难定位，按领域拆分。

## 后果

- 时间字段统一后，Entity 属性、前端引用、视图都要同步改；这是一次横跨 DB/后端/前端/视线的改动，单独 PR 承载。
- ID 类型继续多态存在，但通过"新表规范"停止扩散；维护者需接受这是有意的技术债。
- `GLOSSARY.md` 增加命名约定小节，作为唯一事实来源。
- 若未来真出现跨表 JOIN 的性能问题，再重新评估 ID 统一（届时有真实数据支撑）。
