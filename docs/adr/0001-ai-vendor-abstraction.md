# 0001 — AI 供应商抽象（以 LangChain4j 标准接口为边界）

将 AI 模型供应商从智谱单一依赖解耦为可配置、可切换、可降级的架构。业务代码和 Agent 系统只依赖 LangChain4j 的标准接口（`ChatLanguageModel`、`StreamingChatLanguageModel`），具体供应商实现通过配置切换。

## 背景

当前 AI 能力完全绑定智谱：模型名（`glm-4-flash`、`glm-4.1v-thinking-flash`）硬编码在 `application.yml` 和 `LangChain4jConfig` 中；`ZhipuAIService` 直接调用智谱 SDK；虽然已有 `ZhipuDisabledChatModel` 作为空 key 的降级占位，但换供应商的话需要改动的地方不止配置。

## 决策

1. **抽象层用 LangChain4j 的标准接口**，不自己造轮子。`ChatLanguageModel`、`StreamingChatLanguageModel` 本身就是供应商无关的，智谱、OpenAI、通义、Ollama 都有实现或可以适配。
2. **Agent 层保留 LangChain4j 的 `@Agent` + Tool 机制**。Agent 框架替换的概率远低于模型供应商替换，不值得额外抽象。
3. **模型选择全部外部化到配置**：`application.yml` 里只配置"用哪个供应商"和"模型名"，业务代码不出现具体供应商名称。通过 Spring Profile 或配置属性切换实现供应商切换。
4. **统一的能力降级机制**：扩展 `ZhipuDisabledChatModel` 的思路，抽象为 `NoopChatModel` / `UnsupportedCapabilityException`——当当前供应商不支持某能力（如视觉理解）时，返回友好提示而不是报错。
5. **新增能力探测接口**：后端暴露当前模型的能力清单（文本/视觉/流式/函数调用），前端根据能力清单动态调整 UI。

## 考虑过的替代方案

- **自己定义 AiService 接口**：放弃。LangChain4j 的接口设计已经足够好，自己包一层没有新增价值，反而增加维护成本。
- **连 Agent 框架也抽象**：放弃。换 Agent 框架的概率极低，且 LangChain4j 的 Tool / Memory / Agent 抽象已经够用。

## 后果

- 换供应商只需改配置，不需要改业务代码
- `AI_API_KEY` 留空也能启动的模式扩展为"任何供应商缺失都能启动"
- 后续接入本地模型（Ollama）做离线降级时，只需加一个配置 profile
- 代码改动集中在 `agent/config/` 和 `application.yml`，Agent 和 Tool 层基本不动
