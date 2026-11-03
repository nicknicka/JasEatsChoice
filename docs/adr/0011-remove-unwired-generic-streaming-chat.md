# 0011 — 删除未接线的通用对话流式链，收窄「流式」语义

## 背景

仓库里曾同时存在两套「流式」相关实现：

1. **通用对话流式链**：`StreamingResponseAgent` + `LangChain4jStreamingConfig` + `DynamicAiModelRegistry` 的 `STREAMING` 模型槽 + `buildStreamingModel()`。它没有路由入口、没有调用者，也没有当前的接口契约，唯一效果是让后续开发者误以为通用 AI 聊天支持逐 token 输出。
2. **食谱优化流式链**：`POST /v1/ai/recipe/stream` → `buildRecipeStreamingModel` → `StreamingChatModel`。它由前端食谱优化入口真实调用，是当前在用的逐 token 流式能力（见 #221、#229）。

另外，Supervisor 的 SSE 接口虽然使用 `text/event-stream`，但行为是「执行过程推送进度事件，最终回复一次性下发」，并不做逐 token 渲染。它也常被「流式聊天」这个旧称呼误导。

## 决策

1. **删除整条通用对话流式链**，不保留 deprecated 空壳：`StreamingResponseAgent`、`LangChain4jStreamingConfig`、`DynamicAiModelRegistry.ModelKind.STREAMING`、`ActiveModels.streaming`、`ChatModelProvider.buildStreamingModel()`、`AiModelClientFactory.buildStreamingModel()` 两个重载、`OpenAiCompatibleModelClientFactory.buildStreamingModel()`，以及 `AiRuntimeConfigServiceImpl` 激活流程中的两处构建。
2. **保留食谱优化流式链**：`/v1/ai/recipe/stream`、`buildRecipeStreamingModel()` 与 `NoopStreamingChatModel` 不动。
3. **保留 Supervisor SSE 链路**：它推送进度和最终结果，不逐 token 输出。
4. **`AiCapabilitiesDTO.streaming` 不改字段名，只收窄语义**：表示「食谱优化是否支持逐 token 流式输出」。改名会破坏现有接口与前端契约，收益不足。
5. **删除判据**：无调用者、无路由入口、无当前契约、无近期需求。本 ADR 不授权全仓 dead code 扫描。

## 替代方案

- **保留 `@Deprecated` 占位**：没有迁移路径，只会继续出现在依赖注入、激活流程和能力描述里，误导性大于兼容价值。
- **把 `streaming` 字段改名为 `recipeStreaming`**：语义更清楚，但需要同步后端 DTO、前端与测试，且该字段当前只服务食谱优化，暂不值得承担契约破坏成本。
- **把 Supervisor SSE 改成真正逐 token**：需要把 LLM 流式回调、卡片结构化输出和前端渲染重新协同设计；当前需求只要求执行进度可见、最终结果完整，没有证据支持这次扩展。

## 后果

- `DynamicAiModelRegistry.ActiveModels` 从 6 个字段降为 5 个，激活与回滚流程的构造参数同步减少，相关单测同步调整。
- 通用 AI 聊天不再有任何流式客户端或流式 Agent 的痕迹；未来若重新需要，按新需求重新设计，不复活旧代码。
- `AiCapabilitiesDTO.streaming` 仍然存在，但只表示食谱优化的逐 token 能力；Supervisor SSE 统一用「SSE 进度推送」称呼。
