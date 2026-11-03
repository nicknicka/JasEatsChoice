# RAG 方案

> 生成日期：2026-10-31
> 来源：Codex 归档会话 `01a182f3-cdb7-7c23-aaf1-dcdfcab2d1bc`，标题为“这个项目你觉得可以加入 rag 去使用吗？有什么方面可以加入”
> 状态：方案草案，未开始实现
> 规格 Issue：[#144 公告 RAG：AI 助手接入公告语义检索](https://github.com/nicknicka/JasEatsChoice/issues/144)
> 首期范围：公告 RAG；第二阶段接入教程 RAG；后续再评估菜品描述、商家介绍、节日活动和热门话题

## 一、结论与边界

项目继续走 **Agentic tool-call** 主线。结构化数据用工具查实时库，RAG 只负责非结构化的自然语言知识，不替代订单、优惠券、健康目标、营养数据、用户偏好等现有查询逻辑。

RAG 的落地目标：

1. 用户问“这家店现在有什么优惠”“满减规则是什么”“退款怎么处理”时，Agent 能检索公告或教程原文，再基于原文回答。
2. 检索必须受平台/商家/目标群体/有效期约束，不能把其他商家的私有公告召回给当前上下文。
3. 已过期、已下线、已删除的内容不能继续被 AI 引用。
4. AI 服务地址/密钥缺失，或 Qdrant 不可用时，项目仍然能启动；只有 RAG 检索能力降级，不能让整个 Spring 上下文起不来。
5. RAG 只接入 AI 聊天。商家详情页、通知中心、群公告等结构化展示继续走原 SQL 查询，两种读取路径不互相替代。

明确不做：

- 不把订单、优惠券、营养记录、用户偏好等结构化数据放入向量库。
- 不把 RAG 接入公告列表、通知中心和商家详情页。
- 首期不做 BM25 + 向量混合检索，不做 rerank 模型，不做多查询改写。
- 不为 RAG 单独新建一个完整公告 Agent。检索只提供一个工具能力。

## 二、沿用并细化的四项决策

| 决策项 | 已确认方案 | 本方案细化 |
|---|---|---|
| 索引结构 | 单个 Qdrant collection，用 payload `sourceType` 区分内容 | 首期 collection 名使用 `jaseats_knowledge_v1`；不同内容源共用一个 collection，按 payload 过滤 |
| 平台级表示 | `merchantId = "platform"` | 公告表的 `merchantId = null` 在向量 metadata 中转为 `"platform"`，避免空字符串和 null 混用 |
| 检索策略 | 先纯向量检索 | 先取候选后去重，默认向 Agent 返回 3 条；记录相似度，达到数据量或精度瓶颈后再评估混合检索 |
| 同步方式 | Spring 领域事件统一同步向量 | 内容事务提交后处理，失败不回滚内容写；首期记录错误并提供重建入口，不先引入 outbox |

补充决策：

- Embedding 使用 OpenAI-compatible 的 `OpenAiEmbeddingModel`，与聊天模型共用同一适配器边界。
- 向量库优先使用与当前 LangChain4j 版本匹配的 `dev.langchain4j:langchain4j-qdrant:1.12.1-beta21`，直接复用 `QdrantEmbeddingStore` 对 metadata filter、自定义 point ID 和删除的支持。
- `langchain4j-qdrant:1.12.1-beta21` 传递依赖 `io.qdrant:client:1.17.0`；首期 Qdrant 容器使用 `v1.17.0` 与之对齐。若要升级到更高版本，必须单独做兼容性冒烟并记录。
- 向量维度一旦变化，必须新建 collection 并全量重建，不能原地修改维度。

## 三、总体架构

### 3.1 写入链路

```text
管理员端 / 商家端 / 审核 / 定时任务
        |
        v
AnnouncementService / TutorialService
        |
        | 事务提交前发布领域事件
        v
@TransactionalEventListener(AFTER_COMMIT)
        |
        v
RagIndexService
        |
        | 组装文本 + metadata，按规则分块
        v
EmbeddingModel（OpenAI-compatible embedding 模型）
        |
        v
QdrantVectorStore.upsert / deleteByDocument
```

### 3.2 读取链路

```text
用户消息
   |
   v
SupervisorAgent
   |
   v
知识客服 L1 Agent / 商家信息 L1 Agent
   |
   v
RagKnowledgeTools.searchKnowledge(...)
   |
   | 读取可信请求上下文：userId / audience / merchantId
   | 构造 payload filter：sourceType / merchantId / targetAudience / status / 时间
   v
RagRetrievalService
   |
   | query embedding + Qdrant search + 去重 + 截断
   v
格式化检索结果
   |
   v
Agent 只基于检索文本组织回答
```

### 3.3 组件与文件落点

| 组件 | 建议路径 | 职责 |
|---|---|---|
| RAG 配置 | `config/RagProperties.java` | Qdrant、collection、topK、chunk 大小、超时等配置 |
| Embedding Bean | `agent/config/LangChain4jConfig.java` 或独立 `RagConfig.java` | 构建 `EmbeddingModel`；缺 key 时返回 `DisabledEmbeddingModel` |
| Qdrant 适配层 | `rag/store/RagVectorStore.java` | 基于 `QdrantEmbeddingStore` 封装 collection、幂等 upsert、按 metadata 删除和 vector search；collection 创建/payload index 可使用其传递的 Qdrant client |
| 分块器 | `rag/RagChunker.java` | 公告/教程文本规范化、chunk、overlap、内容 hash |
| 索引服务 | `rag/RagIndexService.java` | 内容源到文档模型的转换、幂等 upsert/delete |
| 检索服务 | `rag/RagRetrievalService.java` | 过滤条件、embedding、检索、去重、格式化 |
| 领域事件 | `rag/event/RagDocumentChangedEvent.java` | 只携带 sourceType、contentId、changeType，不在事件中塞大对象 |
| 事件监听 | `rag/RagIndexEventListener.java` | 事务提交后读取最新内容并同步向量 |
| Agent 工具 | `agent/tools/rag/RagKnowledgeTools.java` | 暴露给 LangChain4j Agent 的 `@Tool` 方法 |
| 重建入口 | `controller/AdminRagController.java` | 仅管理员可用的全量/按来源重建入口 |

## 四、Qdrant 索引设计

### 4.1 Collection

- 名称：`jaseats_knowledge_v1`
- 距离函数：`Cosine`
- 向量维度：由实际 Embedding 模型决定，通过 `AI_EMBEDDING_DIMENSIONS` 显式配置；配置值必须与真实响应和现有 collection 一致
- 写入方式：按文档分块后，每块一个 point
- point ID：使用 `UUID.nameUUIDFromBytes(documentId + ":" + chunkIndex)` 生成稳定的 UUID，再调用 `QdrantEmbeddingStore.addAll(ids, embeddings, segments)` 写入，保证重复写入是幂等 upsert

### 4.2 Payload

```json
{
  "sourceType": "announcement",
  "contentId": "123456",
  "documentId": "announcement:123456",
  "chunkIndex": 0,
  "chunkCount": 1,
  "title": "周末满减活动",
  "text": "本周六周日满 30 减 5，仅限到店自取……",
  "contentHash": "sha256:...",
  "merchantId": "platform",
  "targetAudience": "customer",
  "status": "active",
  "startAt": "2026-10-31T00:00:00",
  "endAt": "2026-11-02T23:59:59",
  "updatedAt": "2026-10-31T10:00:00",
  "audienceTags": ["customer", "all"]
}
```

字段规则：

- `merchantId`：平台内容固定为 `"platform"`，商家内容使用真实 `merchantId`。
- `targetAudience`：保留源数据原始值；`audienceTags` 用于查询时统一匹配 `all/customer/merchant`。
- `status`：公告使用 `active/draft/inactive`；教程只有 `PUBLISHED` 才进入索引。
- `startAt/endAt`：公告有效期；无起止时间时存空字符串，不写 null。
- `contentHash`：用于重建和调试；首期仍需重新计算嵌入，不把它当成避免 embedding 的强一致缓存。
- `text`：真正参与 embedding 的文本，同时作为返回给 Agent 的原文。

需要创建 payload 索引的字段：

- `sourceType`
- `merchantId`
- `targetAudience`
- `audienceTags`
- `status`
- `contentId`
- `startAt`
- `endAt`

### 4.3 分块规则

- 公告通常较短：标题 + 正文合并后不超过 800 个中文字符时，只生成一个 chunk。
- 教程较长：按 500 个中文字符切块，保留 80 个字符 overlap。
- 优先按段落切分，段落过长再按字符切分。
- 每个 chunk 前部带上标题，避免后续块丢失文档语义。
- 首期不做语义分块，不做 LLM 总结式分块。

## 五、租户、目标群体与可见性

### 5.1 查询范围

| 当前上下文 | 可检索内容 |
|---|---|
| 用户端，未指定商家 | 平台内容 + `targetAudience in (all, customer)` |
| 用户端，明确指定商家 | 平台内容 + 该商家的 `customer/all` 内容；不能扩展到其他商家 |
| 商家端，查看自家公告 | 平台 `merchant/all` 内容 + 当前 `merchantId` 的公告 |
| 商家端，未确定自家 `merchantId` | 失败关闭，只返回平台 `merchant/all` 内容 |
| 管理员端 | 首期不接入 RAG 聊天；如需接入，单独定义权限，不能默认放开全商家 |

### 5.2 查询过滤条件

所有检索至少包含：

1. `sourceType in [...]`
2. `status = active`，教程为 `PUBLISHED`
3. `audienceTags` 包含当前 `audience`
4. `merchantId in ["platform", currentMerchantId]`
5. `startAt empty OR startAt <= now`
6. `endAt empty OR endAt >= now`

未指定商家时，`merchantId = "platform"`，不能退化成“检索所有商家”。

### 5.3 上下文可信来源

`audience`、`merchantId`、`userId` 必须来自请求上下文或认证信息，不能让模型自己声明权限。

当前 AgenticScope 只在 `SSEAgentListener.afterAgenticScopeCreated` 写入了 `userId`。实现 RAG 时需要补齐统一作用域初始化：

- 用户端聊天写入 `audience=customer`。
- 商家端聊天写入 `audience=merchant` 和当前 `merchantId`。
- 同步 Supervisor 入口也需要同样的作用域初始化，不能只在 SSE 入口工作。
- Agent 工具从 `AgenticScope` 读取这些字段；缺失时执行失败关闭。

如果用户在聊天中提到商家名称，工具可以在内部做一次保守的商家解析；匹配到唯一商家后才允许加入该商家过滤条件，不能把模型生成的 `merchantId` 直接当成可信参数。

## 六、Embedding 设计

### 6.1 依赖与模型

`apps/server/pom.xml` 使用 `langchain4j-open-ai` 提供的 `OpenAiEmbeddingModel`，不引入供应商专属 SDK。

当前配置位于 `announcement-rag.embedding.*`：

```yaml
announcement-rag:
  embedding:
    provider: ${AI_EMBEDDING_PROVIDER:openai-compatible}
    model: ${AI_EMBEDDING_MODEL:}
    dimensions: ${AI_EMBEDDING_DIMENSIONS:0}
    base-url: ${AI_EMBEDDING_BASE_URL:}
```

`AI_EMBEDDING_BASE_URL` 必须是 OpenAI-compatible 服务根地址（通常以 `/v1` 结尾），不能填写 `/chat/completions`。旧配置若带有该后缀，启动时会迁移为根地址；无法安全转换时清空并提示管理员重新填写。

### 6.2 降级

- 服务地址、模型名或维度不完整时，embedding 返回占位实现并标记 `NOT_CONFIGURED`。key 解析顺序为运行时 Embedding key > `AI_API_KEY`；本地无鉴权的兼容端点可以只配置 `AI_EMBEDDING_BASE_URL`。
- 应用启动不因缺 key 失败。
- RAG 工具被调用时返回“知识检索暂不可用，请基于现有工具结果回答”，不能编造平台规则。
- 单次 embedding 调用失败时，索引同步记录错误；内容写事务已经提交，不回滚。
- 首期不引入本地 embedding fallback，避免维度不一致。

### 6.3 成本与批处理

- embedding 请求按批处理，建议每批 16～32 个 chunk。
- 单次重建限制每批最大文档数，避免一次重建耗尽外部 Embedding 服务额度。
- 日志只记录 sourceType、contentId、chunk 数、耗时、成功失败，不打印完整公告内容。

## 七、索引同步设计

### 7.1 事件

```java
public record RagDocumentChangedEvent(
    RagSourceType sourceType,
    String contentId,
    RagChangeType changeType,
    Instant occurredAt
) {}

public enum RagChangeType {
    UPSERT,
    DELETE
}
```

事件只携带标识，不携带完整实体。监听器在事务提交后重新读取数据库，以数据库最终状态为准。

监听器：

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onDocumentChanged(RagDocumentChangedEvent event) {
    ragIndexService.sync(event.sourceType(), event.contentId());
}
```

发布事件的 Service 写方法必须处于 Spring 事务中。`@TransactionalEventListener(AFTER_COMMIT)` 默认只在存在活动事务时执行；不能在没有事务的 Controller 里直接发布事件后期待它被消费。确实无法加事务的入口才考虑 `fallbackExecution = true`，并单独说明原因。

`sync` 的规则：

- 内容不存在、草稿、已删除、已下线、已拒绝、已过期：`deleteByDocument(...)`。
- 内容可见：重新构造文档、分块、embedding、upsert。
- 同一文档重复处理必须幂等。
- 向量同步失败不能影响原内容写操作。

### 7.2 公告写操作覆盖

现有公告写入入口：

- `AdminAnnouncementController`：创建、编辑、删除、批量删除、状态切换。
- `MerchantController`：创建、编辑、状态切换、删除。
- `AnnouncementScheduler`：过期公告批量下线。

实现方式建议：

- 在 `AnnouncementServiceImpl` 统一覆盖 `save`、`updateById`、`removeById`、`updateBatchById`、`removeBatchByIds`。
- 写库成功后发布 `RagDocumentChangedEvent`。
- 控制器和调度器不需要感知 RAG。
- 即使未来新增公告写入入口，只要仍走 `AnnouncementService`，就会自动同步。
- `AnnouncementScheduler` 批量置 inactive 时必须触发删除向量。

如果后续发现公告的阅读量更新也频繁走 `updateById`，再由 `RagIndexService` 计算内容 hash 后跳过重复嵌入；首期不要为这个不存在的问题增加复杂状态表。

### 7.3 教程写操作覆盖

教程已经有明确的 Service 方法，建议在这些方法写成功后发布事件：

- `createByAdmin`
- `approveTutorial`
- `rejectTutorial`
- `createByMerchant`
- `updateByMerchant`
- `submitForReview`
- `createByUser`
- `generateByAI`
- `removeById` 对应的删除入口

只有 `PUBLISHED` 状态进入索引。状态从 `PUBLISHED` 变成其他状态时删除向量。

### 7.4 失败与重建

首期不引入 outbox。原因：当前数据量只有几千条，失败时通过日志和重建入口恢复比新增一张同步状态表更简单。

- 同步失败记录 `ERROR` 日志和指标。
- 提供管理员重建入口：按 `sourceType`、`contentId` 或全量重建。
- 重建接口必须要求管理员权限，且不能在 `GET` 请求里做全量重建。
- 如果以后出现持续丢事件、需要跨重启可靠补偿，再评估 transaction outbox。

## 八、检索与工具契约

### 8.1 检索参数

首期默认值：

| 参数 | 默认值 | 说明 |
|---|---:|---|
| `candidateLimit` | 10 | Qdrant 初筛数量 |
| `topK` | 3 | 返回给 Agent 的文档数 |
| `maxContextChars` | 2000 | 注入 Agent 的文本总上限 |
| `maxChunkChars` | 600 | 单块返回上限 |
| `scoreThreshold` | 暂不固定 | 先记录分数，用真实问题集调优后再固化 |

检索后处理：

1. 按 `score` 降序。
2. 同一 `documentId` 只保留最高分 chunk，避免一条公告占满上下文。
3. 超出 `maxContextChars` 时截断。
4. 无结果时返回明确的“无匹配知识”，不返回空 JSON 让模型猜。

### 8.2 Tool 方法

建议文件：`apps/server/src/main/java/com/xx/jaseatschoicejava/agent/tools/rag/RagKnowledgeTools.java`

```java
@Tool("""
    从平台公告、商家公告和平台教程中检索与用户问题相关的原文。

    何时使用：
    - 用户询问平台规则、退款、用券、活动、满减、公告、教程
    - 用户询问某个商家的活动或公告

    何时不要使用：
    - 订单状态、优惠券余额、用户资料等实时结构化数据，继续使用已有查询工具

    返回结果只能作为回答依据，不能补充检索结果中不存在的信息。
    """)
public String searchKnowledge(
    @P("用户原始问题，保留金额、日期和活动名称") String query,
    @P("用户提到的商家名称；未提到时传空字符串") String merchantName,
    @P("最多返回文档数，默认3，范围1-5") Integer limit,
    AgenticScope scope
);
```

约束：

- `audience`、`merchantId`、`userId` 从 `scope` 读取，不从 Tool 参数读取权限。
- `merchantName` 只作为“待解析名称”，解析结果必须唯一且满足当前 `audience` 限制。
- 没有作用域上下文时按失败关闭处理，只允许平台内容或直接返回不可用。
- 工具内部不能让模型传完整 SQL、Qdrant filter 或任意 metadata。

### 8.3 返回格式

返回给 LLM 的不是裸向量，也不是只有一串正文，而应是可审计、可截断的 JSON 字符串：

```json
{
  "available": true,
  "query": "周末满减活动是什么",
  "items": [
    {
      "sourceType": "announcement",
      "title": "周末满减活动",
      "content": "本周六周日满30减5，仅限到店自取。",
      "effectiveRange": "2026-10-31 至 2026-11-02",
      "score": 0.83
    }
  ],
  "guidance": "只能依据 items 回答；没有覆盖的问题要明确说未查询到。"
}
```

不返回 `merchantId`、`contentId`、Qdrant point ID 给最终用户。内部日志可以记录这些字段用于排查。

### 8.4 注入哪个 Agent

归档里的原表述是“做成 tool，挂到 Supervisor 层”。继续核对当前 LangChain4j `1.12.1-beta21` 后，发现需要修正：

- `SupervisorAgentService` 只有 `subAgents(...)`，没有 `tools(...)`。
- 因此不能字面上把 Tool 直接挂到 Supervisor。
- 首期推荐把现有 `CustomerServiceAgent` 从独立 `AiServices` 实例提升为 Supervisor 的 L1 知识客服 Agent，并在它上面挂 `RagKnowledgeTools`。
- `MerchantInfoAgent` 如确实需要回答商家公告，也可以挂同一个无状态 Tool；但不要为公告单独新建一个 Agent。
- `SupervisorAgentFactory.createSupervisorContext()` 增加路由：
  - 平台规则、退款、用券、活动、公告、教程 → `CustomerServiceAgent`
  - 商家结构化信息 → `MerchantInfoAgent`
  - 两者复合时，先查结构化商家信息，再由客服 Agent 组织知识回答。

这个修正保留“方式是 Tool”的原决策，只调整 Tool 的挂载宿主。

### 8.5 Prompt 约束

知识客服 Agent 的 SystemMessage 增加：

- 回答平台规则类问题前必须调用知识检索工具。
- 只允许使用工具返回的原文。
- 检索为空时明确回答“当前没有查询到相关有效内容”，不能从训练知识补充平台规则。
- 不能向用户泄露内部 ID、向量分数、过滤条件。
- 有效期相关问题必须引用 `effectiveRange`，不能把过期活动说成当前有效。

## 九、部署与配置

### 9.1 Qdrant 容器

`deploy/compose.yml` 增加：

- 服务名：`qdrant`
- 镜像：`qdrant/qdrant:v1.17.0`，与 `langchain4j-qdrant:1.12.1-beta21` 传递的 Java 客户端版本对齐
- 网络：`jaseats-net`
- 数据卷：`qdrant-data:/qdrant/storage`
- 应用依赖：`spring-boot.depends_on.qdrant`

开发环境可按需暴露 `6333:6333`；生产环境默认只走容器网络，不直接对外暴露。

### 9.2 应用依赖

`apps/server/pom.xml` 增加：

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-qdrant</artifactId>
    <version>1.12.1-beta21</version>
</dependency>
```

如果需要直接创建 collection、payload index 或执行 Qdrant 管理操作，使用该依赖传递的 `io.qdrant:client:1.17.0`，不要再声明一个不同版本，避免客户端混用。

### 9.3 环境变量

`deploy/.env.example` 增加占位配置：

```dotenv
# ---- RAG / Qdrant ----
ANNOUNCEMENT_RAG_ENABLED=true
QDRANT_HOST=qdrant
QDRANT_PORT=6334
QDRANT_COLLECTION=jaseats_knowledge_v1
AI_EMBEDDING_PROVIDER=openai-compatible
AI_EMBEDDING_MODEL=
AI_EMBEDDING_DIMENSIONS=0
AI_EMBEDDING_BASE_URL=
```

不新增真实密钥。所有 AI key 统一使用 `AI_API_KEY`，只放 `deploy/.env`。

### 9.4 启动降级

- Qdrant 连接失败不能让 Spring 启动失败。
- Collection 初始化使用懒加载或启动后幂等检查。
- `RAG_ENABLED=false` 时，工具返回“RAG 未启用”。
- 缺少 embedding key 时，工具返回“知识检索未配置”。
- Qdrant 检索超时设置上限，不能拖垮整轮聊天。

## 十、实施顺序

### 阶段 1：基础设施与降级

改造：

- 增加 Qdrant 依赖、容器、配置。
- 增加 `EmbeddingModel` Bean 和禁用降级。
- 增加 Qdrant client、collection 初始化、健康检查。
- 不接业务写操作。

验证：

- 无 `AI_API_KEY` 时 Spring 正常启动。
- 无 Qdrant 时 Spring 正常启动，聊天不受影响。
- Qdrant 启动后能创建 collection 和 payload index。
- `RAG_ENABLED=false` 时工具不可用且提示明确。

### 阶段 2：公告索引

改造：

- 完成 chunk、payload、point ID 规则。
- 公告 Service 写操作发布事件。
- AFTER_COMMIT 监听器执行 upsert/delete。
- 增加管理员重建入口。

验证：

- 新建 active 公告后能检索到。
- 编辑后旧 chunk 消失，新内容可检索。
- 状态改为 inactive 后不再召回。
- 删除后按 `documentId` 无残留 point。
- 调度器把公告置 inactive 后向量同步删除。
- 重复重建不产生重复 point。

### 阶段 3：Agent 检索工具

改造：

- 完成 `RagRetrievalService` 和 `RagKnowledgeTools`。
- 补齐 Supervisor 请求作用域中的 `audience` 和 `merchantId`。
- 将 `CustomerServiceAgent` 提升为 L1 Agent，并挂 RAG Tool。
- 更新 Supervisor 路由和提示词。

验证：

- 用户端未指定商家时只能检索平台内容。
- 指定商家后能检索平台 + 该商家的 `customer/all` 内容。
- 商家端只能检索平台 `merchant/all` + 自家内容。
- 没有作用域时失败关闭，不返回其他商家内容。
- 无检索结果时不编造平台规则。

### 阶段 4：教程索引

改造：

- 给教程发布/审核/编辑/下架/删除路径增加事件。
- 只索引 `PUBLISHED` 教程。
- 接入同一检索工具，payload 使用 `sourceType=tutorial`。

验证：

- 草稿、待审核、拒绝教程不进索引。
- 审核通过后立即可检索。
- 下架或删除后立即删除向量。

### 阶段 5：扩展内容源

按收益排序：

1. 菜品描述：`sourceType=dish`
2. 商家介绍：`sourceType=merchant`
3. 节日活动：`sourceType=festival`
4. 热门话题：`sourceType=hot_topic`

每个内容源都必须先定义：

- 哪个字段进入 `text`
- `merchantId` 和 `targetAudience` 怎么映射
- 哪些状态可见
- 哪些写操作触发事件
- 删除和状态变更如何清理向量

## 十一、验收标准

功能验收：

- AI 能正确回答平台公告、商家公告、教程中的问题，并且答案内容可以在原文中找到。
- 已过期、已下线、已删除、其他商家私有的内容不会被召回。
- 用户未指定商家时，不会检索任何商家的私有公告。
- 商家端不会检索其他商家公告。
- RAG 不可用时，原 Agent 工具仍可正常工作，不出现 500。

工程验收：

- `make check` 通过。
- 新增单元测试覆盖：分块、point ID、metadata 映射、过滤条件、空作用域失败关闭。
- 集成验证覆盖：upsert、update、delete、status inactive、过期时间过滤。
- 不在代码、YAML 默认值、测试和文档中写入真实 API key。
- `tests/progress.txt` 不作为提交内容。

质量验收：

- 建立至少 20 个真实问题的小型评测集，覆盖平台规则、商家活动、无效活动、跨商家隔离。
- 首期统计 `Top-1 命中率`、`Top-3 命中率` 和 `无答案误答率`。
- 如果 Top-3 命中率不达标，再评估混合检索；不要在没有评测数据前提前引入 BM25 和 rerank。

## 十二、GitHub 开源实现参考

以下项目用于验证接入模式。官方示例优先作为接口用法依据，社区项目只作为工程经验参考。

| 项目 | 主要借鉴点 |
|---|---|
| [langchain4j/langchain4j-examples](https://github.com/langchain4j/langchain4j-examples) | 同时覆盖自动 RAG 和低层 RAG。`AiServices.contentRetriever(...)` 适合固定知识问答；`EmbeddingStoreContentRetriever.dynamicFilter(...)` 支持从查询上下文生成过滤；[QdrantEmbeddingStoreExample](https://github.com/langchain4j/langchain4j-examples/blob/main/qdrant-example/src/main/java/QdrantEmbeddingStoreExample.java) 使用官方 `QdrantEmbeddingStore` |
| [spring-petclinic/spring-petclinic-langchain4j](https://github.com/spring-petclinic/spring-petclinic-langchain4j) | `ApplicationStartedEvent` 启动时把数据库内容灌入 embedding store；`DefaultRetrievalAugmentor` + 自定义 `QueryRouter` 决定是否检索；`@AiService` + SSE 对聊天暴露 |
| [spring-projects/spring-ai-examples](https://github.com/spring-projects/spring-ai-examples) | `rag-with-kotlin` 从数据库读取后先灌入 `VectorStore`，再通过 `QuestionAnswerAdvisor` 接到 `ChatClient`；适合对比“自动检索注入”路线 |
| [Azure-Samples/azure-openai-rag-workshop-java](https://github.com/Azure-Samples/azure-openai-rag-workshop-java) | 写入侧 `ingestion` 与读取侧 `backend` 分开部署；ingestion 负责建 collection 和写向量，backend 只读。当前项目不需要拆服务，但说明写入和查询职责分离是常见做法 |
| [UMC-POP-IT/Pop-It-BE](https://github.com/UMC-POP-IT/Pop-It-BE) | 领域写事务提交后，用 `@Async` + `@TransactionalEventListener(AFTER_COMMIT)` 生成向量；注释明确解释了事务未提交时查不到新记录、网络调用会拉长事务 |
| [gmjneko/JCFeed](https://github.com/gmjneko/JCFeed) | 事件监听用 `AFTER_COMMIT`，向量生成失败不影响内容发布；另设定时回填任务兜住“事件丢失、单次失败、历史数据”三类缺口 |
| [baekgwa-blog/server](https://github.com/baekgwa-blog/server) | 事件处理失败写入独立的 failure 表，定时任务按 CREATE/DELETE/UPDATE 重试；比只记日志更适合需要可靠补偿的场景 |
| [kezhenxu94/spring-agent](https://github.com/kezhenxu94/spring-agent) | `KnowledgeScope` 从 ToolContext 读取用户/群/租户，`KnowledgeScopeFilter` 在查询侧构造可见范围；空 scope 不能当通配符，没有身份时失败关闭。这个模式和本项目的租户过滤最接近 |
| [ThomasVitale/modular-rag](https://github.com/ThomasVitale/modular-rag) | 知识检索作为 `@Tool`；每个领域工具固定自己的 metadata filter、topK 和 similarity threshold，模型只能选择工具和查询词，不能改权限过滤 |
| [criew/opaa](https://github.com/criew/opaa) | `SearchKnowledgeTool` 是知识检索的唯一入口；搜索范围和 metadata filter 只来自运行上下文，明确不让模型参数决定“搜哪里”，工具异常不把内部细节回灌给模型 |
| [nageoffer/ragent](https://github.com/nageoffer/ragent) | 工具错误统一返回用户可理解的信息，不把异常、SQL、内部路径带进模型上下文；工具只暴露 `query`，保持只读 |
| [JetBrains/koog](https://github.com/JetBrains/koog) | `KnowledgeBaseTools` 直接把 `VectorStore.similaritySearch(query, topK, threshold)` 暴露给 Agent，说明“知识检索工具”已经是主流 Agent 接法 |

从这些项目可以归纳出四种高置信模式：

1. **固定知识问答用自动注入，混合业务 Agent 用 Tool。** 纯知识库问答常把 `ContentRetriever` / `RetrievalAugmentor` / `QuestionAnswerAdvisor` 直接挂到 AiService；既有结构化工具又有知识库时，更常见的是 `search_knowledge` Tool，由模型判断是否调用。
2. **权限和租户过滤绝不能由模型决定。** 工具参数只允许模型传查询词和条数，`audience`、`merchantId`、用户/群/租户范围必须来自运行上下文，并在检索层统一构造过滤条件。
3. **业务写和向量同步解耦。** 写入事务完成后触发 embedding 是主流做法；同步失败不能回滚已经成功的内容写入。小规模项目常用日志 + 重建/补偿任务，可靠性要求更高时增加 failure 表或 outbox。
4. **Qdrant 接入优先使用成熟适配层。** 官方示例直接使用 `QdrantEmbeddingStore`，collection 由启动配置或独立 ingestion 服务创建；官方适配层支持自定义 point ID、metadata filter 删除和带 filter 的搜索，没必要先自己实现一套 store。

对本方案的直接修正：

- 采用 `langchain4j-qdrant:1.12.1-beta21`，不再自己封装 Qdrant Java store。
- `RagKnowledgeTools` 只接收 `query`、可选 `merchantName`、`limit`；租户、受众和权限过滤从 AgenticScope/请求上下文读取，保持失败关闭。
- 首期保留 `AFTER_COMMIT` + 重建入口；如果实际出现持续丢事件，再按 `baekgwa` 模式增加 failure 表，不要现在直接上完整 outbox。
- 模型生成 metadata filter 的能力只用于内容类型、时间等非权限筛选，不能用于 `merchantId` / `audience` / `tenantId`。

## 十三、待确认项

以下问题不阻塞阶段 1，但开始阶段 3 前必须确定：

1. 商家端 AI 聊天请求里当前是否已有可信的 `merchantId` 来源；如果没有，需要先补认证上下文。
2. `CustomerServiceAgent` 提升为 L1 后，是否保留原来的独立快速通道；建议保留，但 RAG 只在 Supervisor 路径启用。
3. 商家名称解析是否首期只支持“唯一匹配”；如果多商家同名，是否需要城市或地址辅助。
4. Embedding 模型实际返回维度以真实连通性校验结果为准；首次配置时应据此填写 `AI_EMBEDDING_DIMENSIONS` 并创建 collection。
5. 生产环境是否需要给 Qdrant 配置 API key；如果只在内部 Docker 网络访问，首期可以不启用鉴权。
