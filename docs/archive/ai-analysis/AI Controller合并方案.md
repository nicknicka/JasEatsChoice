# AIStreamController 与 AIController 合并方案

## 📋 当前状态分析

### AIStreamController
- **路由**: `/v1/ai/stream`
- **功能**: 仅聊天（流式）
- **Agent**: StreamingIntelligentAssistantAgent（L2智能调度）
- **返回**: SseEmitter（流式）

### AIController
- **路由**: `/v1/ai`
- **功能**: 菜品识别、营养分析、食谱推荐、食谱优化、聊天（旧版）
- **Agent**: NutritionAiAgent、RecommendationAiAgent（旧版）
- **返回**: ResponseResult（非流式）

---

## 🎯 合并建议

### 方案1：简单合并（推荐）⭐

**架构**: 保持两个Agent，但统一到一个Controller

**文件名**: `AIController.java`（合并后）

**路由设计**:
```
/v1/ai/stream/chat     → 流式聊天（使用L2智能调度）
/v1/ai/chat            → 非流式聊天（兼容旧接口，可选）
/v1/ai/dish-recognize  → 菜品识别
/v1/ai/nutrient        → 营养分析
/v1/ai/recipe           → 食谱推荐
/v1/ai/recipe-upload    → 食谱优化
```

**优点**:
- ✅ 改动最小，风险低
- ✅ 向后兼容
- ✅ 保持两个Agent并行

**缺点**:
- ⚠️ 仍使用旧Agent（部分功能）

---

### 方案2：架构升级（理想方案）🚀

**架构**: 统一使用L2智能调度Agent，删除旧Agent

**实现步骤**:
1. 将所有功能迁移到L2智能调度Agent的工具类
2. 删除旧Agent（NutritionAiAgent、RecommendationAiAgent）
3. 统一Controller

**路由设计**:
```
/v1/ai/stream/chat     → 流式聊天（L2智能调度）
/v1/ai/stream/dish-recognize  → 流式菜品识别
/v1/ai/stream/nutrient        → 流式营养分析
/v1/ai/stream/recipe           → 流式食谱推荐
```

**优点**:
- ✅ 架构统一（全L2→L1）
- ✅ 全部支持流式响应
- ✅ 删除旧代码，减少维护

**缺点**:
- ⚠️ 改动较大
- ⚠️ 需要迁移旧功能到工具类

---

## 💡 推荐实现方案（方案1优化版）

### 文件结构

```java
@RestController
@RequestMapping("/v1/ai")
public class AIController {

    // ==================== L2 智能调度Agent（流式） ====================
    @Resource
    private StreamingIntelligentAssistantAgent streamingIntelligentAssistantAgent;

    // ==================== 旧版Agent（保留兼容） ====================
    @Resource
    private NutritionAiAgent nutritionAiAgent;

    @Resource
    private RecommendationAiAgent recommendationAiAgent;

    // ==================== 服务层 ====================
    @Resource
    private ZhipuAIService zhipuAIService;

    @Resource
    private FileUploadConfig fileUploadConfig;

    // ==================== 流式聊天接口 ====================
    /**
     * SSE流式聊天接口（主流接口）
     *
     * 路由: /v1/ai/stream/chat
     * Agent: StreamingIntelligentAssistantAgent (L2智能调度)
     * 返回: SseEmitter (流式响应)
     */
    @PostMapping("/stream/chat")
    public SseEmitter streamChat(@RequestBody Map<String, Object> params) {
        // 现有 AIStreamController 的实现
        // ...
    }

    // ==================== 非流式聊天接口（兼容） ====================
    /**
     * 非流式聊天接口（兼容旧接口）
     *
     * 路由: /v1/ai/chat
     * Agent: NutritionAiAgent (旧版)
     *
     * @deprecated 建议使用 /stream/chat 流式接口
     */
    @Deprecated(since = "2026-04-02", forRemoval = true)
    @PostMapping("/chat")
    public ResponseResult<?> chat(@RequestBody Map<String, Object> params) {
        // 现有 AIController 的实现
        // ...
    }

    // ==================== 菜品识别 ====================
    /**
     * AI菜品识别
     *
     * 路由: /v1/ai/dish-recognize
     * 返回: ResponseResult (识别结果)
     */
    @PostMapping(value = "/dish-recognize", consumes = "multipart/form-data")
    public ResponseResult<?> dishRecognize(
        @RequestParam("image") MultipartFile image,
        @RequestParam(value = "userId", required = false) String userId
    ) {
        // 现有 AIController 的实现
        // ...
    }

    // ==================== 营养分析 ====================
    /**
     * AI营养分析接口
     *
     * 路由: /v1/ai/nutrient
     * Agent: NutritionAiAgent (旧版)
     *
     * @deprecated 建议将功能迁移到L2智能调度Agent的工具类
     */
    @Deprecated(since = "2026-04-02")
    @PostMapping("/nutrient")
    public ResponseResult<?> nutrient(@RequestBody Map<String, Object> params) {
        // 现有 AIController 的实现
        // ...
    }

    // ==================== 食谱推荐 ====================
    /**
     * AI食谱推荐接口
     *
     * 路由: /v1/ai/recipe
     * Agent: RecommendationAiAgent (旧版)
     *
     * @deprecated 建议将功能迁移到L2智能调度Agent的工具类
     */
    @Deprecated(since = "2026-04-02")
    @PostMapping("/recipe")
    public ResponseResult<?> recipe(@RequestBody Map<String, Object> params) {
        // 现有 AIController 的实现
        // ...
    }

    // ==================== 食谱优化 ====================
    /**
     * AI食谱优化
     *
     * 路由: /v1/ai/recipe-upload
     */
    @PostMapping("/recipe-upload")
    public ResponseResult<?> recipeUpload(@RequestBody Map<String, Object> params) {
        // 现有 AIController 的实现
        // ...
    }

    // ==================== 健康检查 ====================
    /**
     * 健康检查接口
     *
     * 路由: /v1/ai/health
     */
    @GetMapping("/health")
    public ResponseResult<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "AI Assistant (Unified)");
        health.put("version", "2.0.0");
        health.put("features", new String[]{
            "流式聊天 (L2智能调度)",
            "菜品识别",
            "营养分析",
            "食谱推荐",
            "食谱优化"
        });
        return ResponseResult.success(health);
    }
}
```

---

## 🔧 实施步骤

### Step 1: 备份现有文件

```bash
cp AIStreamController.java AIStreamController.java.bak
cp AIController.java AIController.java.bak
```

### Step 2: 合并Controller

**方案A：将AIStreamController的内容合并到AIController**

1. 删除 `AIStreamController.java`
2. 将 `streamChat()` 方法移到 `AIController.java`
3. 更新路由：`/v1/ai/stream/chat`
4. 添加必要的依赖注入

**方案B：反向合并**

1. 将AIController的非聊天功能移到 `AIStreamController.java`
2. 重命名为 `AIController.java`
3. 统一所有AI功能到这个Controller

**推荐：方案B**（因为Streaming是主要功能）

### Step 3: 测试验证

```bash
# 测试流式聊天
curl -X POST http://localhost:8080/v1/ai/stream/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"你好","userId":"test"}'

# 测试菜品识别
curl -X POST http://localhost:8080/v1/ai/dish-recognize \
  -F "image=@dish.jpg"

# 测试营养分析
curl -X POST http://localhost:8080/v1/ai/nutrient \
  -H "Content-Type: application/json" \
  -d '{"foodName":"番茄炒蛋"}'
```

### Step 4: 更新前端调用

**前端需要更新的接口**:

```javascript
// 原来
POST /v1/ai/stream/chat  // 保持不变
POST /v1/ai/chat            // 已废弃，使用上面的

// 其他接口保持不变
POST /v1/ai/dish-recognize
POST /v1/ai/nutrient
POST /v1/ai/recipe
POST /v1/ai/recipe-upload
```

---

## ⚠️ 注意事项

### 1. 路由冲突

```
旧:
- /v1/ai/stream/chat (AIStreamController)
- /v1/ai/chat (AIController)

合并后:
- /v1/ai/stream/chat (主聊天接口)
- /v1/ai/chat (保留或废弃)
```

**建议**：
- 保留 `/v1/ai/stream/chat` 作为主流式接口
- `/v1/ai/chat` 标记为 `@Deprecated`，引导使用流式接口

### 2. Bean命名

合并后可能需要重命名Bean：

```java
@Resource
private StreamingIntelligentAssistantAgent streamingIntelligentAssistantAgent;

@Resource
private StreamingMerchantAssistantAgent streamingMerchantAssistantAgent;
```

### 3. Swagger文档

合并后确保Swagger标签清晰：

```java
@Api(tags = "AI助手（统一接口）")
@RestController
@RequestMapping("/v1/ai")
public class AIController {

    @ApiOperation("流式聊天（推荐）", notes = "使用L2智能调度Agent")
    @PostMapping("/stream/chat")

    @ApiOperation("菜品识别", notes = "AI识别菜品名称")
    @PostMapping("/dish-recognize")
}
```

---

## 🎯 最终建议

### 推荐方案：**渐进式合并**

**阶段1**（立即执行）:
1. 合并两个Controller到一个文件
2. 保持所有现有接口不变
3. 添加清晰的注释说明

**阶段2**（后续优化）:
1. 标记旧接口为 `@Deprecated`
2. 在响应中添加推荐使用新接口的提示

**阶段3**（架构升级）:
1. 将非聊天功能迁移到L2智能调度Agent的工具类
2. 删除旧版Agent
3. 全部统一到L2→L1架构

---

## 📝 代码示例

### 合并后的Controller结构

```java
@RestController
@RequestMapping("/v1/ai")
@Api(tags = "AI助手（统一接口）")
public class AIController {

    // ==================== 流式聊天（主流） ====================
    @PostMapping("/stream/chat")
    public SseEmitter streamChat(@RequestBody Map<String, Object> params) {
        // 实现代码...
    }

    // ==================== 菜品识别 ====================
    @PostMapping("/dish-recognize")
    public ResponseResult<?> dishRecognize(MultipartFile image, String userId) {
        // 实现代码...
    }

    // ==================== 营养分析 ====================
    @PostMapping("/nutrient")
    public ResponseResult<?> nutrient(@RequestBody Map<String, Object> params) {
        // 实现代码...
    }

    // ==================== 食谱推荐 ====================
    @PostMapping("/recipe")
    public ResponseResult<?> recipe(@RequestBody Map<String, Object> params) {
        // 实现代码...
    }

    // ==================== 食谱优化 ====================
    @PostMapping("/recipe-upload")
    public ResponseResult<?> recipeUpload(@RequestBody Map<String, Object> params) {
        // 实现代码...
    }
}
```

---

## ✅ 总结

**合并优势**:
- ✅ 统一管理所有AI功能
- ✅ 减少Controller数量
- ✅ 更好的代码组织
- ✅ 便于维护和扩展

**实施建议**:
1. 先做简单合并（方案1）
2. 保持向后兼容
3. 逐步迁移到新架构

**时间估算**: 2-3小时（简单合并）

需要我帮您执行合并吗？
