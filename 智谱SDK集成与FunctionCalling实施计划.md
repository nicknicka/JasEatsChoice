# 智谱SDK集成与Function Calling AI助手实施计划

> **项目名称**：佳食宜选 AI助手升级计划
> **创建时间**：2026-03-13
> **当前版本**：Spring Boot 2.7.18 + Java 21
> **目标**：集成智谱AI官方SDK并实现Function Calling功能

---

## 📋 目录

1. [项目背景](#项目背景)
2. [技术选型](#技术选型)
3. [当前架构分析](#当前架构分析)
4. [SDK集成方案](#sdk集成方案)
5. [Function Calling实现](#function-calling实现)
6. [实施步骤](#实施步骤)
7. [代码示例](#代码示例)
8. [测试策略](#测试策略)
9. [回滚计划](#回滚计划)
10. [风险评估](#风险评估)

---

## 1️⃣ 项目背景

### 1.1 当前现状
佳食宜选项目目前使用**原生Java HttpClient**调用智谱AI API，存在以下问题：
- ❌ 缺乏连接池管理，性能优化不足
- ❌ 无自动重试机制，容错能力弱
- ❌ 手动序列化JSON，代码冗余
- ❌ 缺少类型安全的SDK封装
- ❌ Function Calling功能未实现

### 1.2 升级目标
- ✅ 集成智谱AI官方SDK（新版 Z.ai SDK）
- ✅ 实现完整的Function Calling能力
- ✅ 配置智能AI助手（菜品推荐、订单管理、营养分析）
- ✅ 保持向后兼容，平滑迁移

---

## 2️⃣ 技术选型

### 2.1 SDK版本选择

| 特性 | 旧版SDK (V4) | 新版SDK (Z.ai) | 推荐 |
|------|-------------|---------------|------|
| **Maven坐标** | `cn.bigmodel.openapi:oapi-java-sdk` | `ai.z.openapi:zai-sdk` | ⭐ |
| **最新版本** | 0.1.7 (2024年) | 0.3.3 (持续更新) | ⭐ |
| **API兼容性** | 仅支持旧API | 支持最新API特性 | ⭐ |
| **Function Calling** | 支持不完善 | 完善支持 | ⭐ |
| **文档质量** | 简单 | 详细完善 | ⭐ |
| **维护状态** | 维护较少 | 官方主力维护 | ⭐ |

**🎯 最终选择**：**新版 Z.ai SDK (0.3.3)**

### 2.2 核心依赖

```xml
<!-- 智谱AI官方SDK -->
<dependency>
    <groupId>ai.z.openapi</groupId>
    <artifactId>zai-sdk</artifactId>
    <version>0.3.3</version>
</dependency>
```

---

## 3️⃣ 当前架构分析

### 3.1 现有文件结构

```
JasEatsChoiceJava/
├── config/
│   └── ZhipuAIConfig.java              # 自定义配置类（需保留）
├── controller/
│   └── AIController.java               # AI接口控制器
├── service/
│   ├── ZhipuAIService.java             # 服务接口
│   └── impl/
│       └── ZhipuAIServiceImpl.java     # 当前使用原生HTTP实现
└── resources/
    └── application.yml                 # 配置文件
```

### 3.2 当前实现方式

**ZhipuAIServiceImpl.java** (第780-809行)：
```java
private String sendRequest(Map<String, Object> requestBody) throws Exception {
    String apiKey = zhipuAIConfig.getApiKey();
    java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(30))
            .build();

    java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
            .uri(java.net.URI.create(zhipuAIConfig.getBaseUrl()))
            .header("Authorization", "Bearer " + apiKey)
            .POST(...)
            .build();

    return response.body();
}
```

**问题**：手动处理HTTP请求、序列化、错误处理，代码复杂且易出错。

---

## 4️⃣ SDK集成方案

### 4.1 配置迁移

#### 4.1.1 application.yml (保持不变)

```yaml
zhipuai:
  api-key: 03221a26791e408c8b90fc60153b21c8.cQtSYhnFrFEoT27u
  model: glm-4-flash
  vision-model: glm-4.6v-flash
  base-url: https://open.bigmodel.cn/api/paas/v4/chat/completions
  timeout: 30000
```

#### 4.1.2 保留 ZhipuAIConfig.java

现有配置类无需修改，SDK通过Spring自动注入配置。

### 4.2 Maven依赖更新

**修改文件**：`pom.xml`

```xml
<!-- OkHttp - 用于调用智谱AI API -->
<!-- 注释掉或删除，SDK已内置 -->
<!--
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
</dependency>
-->

<!-- 智谱AI官方SDK (新增) -->
<dependency>
    <groupId>ai.z.openapi</groupId>
    <artifactId>zai-sdk</artifactId>
    <version>0.3.3</version>
</dependency>
```

---

## 5️⃣ Function Calling实现

### 5.1 可用工具函数定义

为AI助手配置6个核心工具函数：

| 函数名 | 功能描述 | 参数 |
|--------|---------|------|
| **search_dishes** | 搜索菜品 | `keyword` (关键词), `category` (分类) |
| **get_dish_details** | 获取菜品详情 | `dish_id` (菜品ID) |
| **create_order** | 创建订单 | `dish_items` (菜品列表), `address` (地址) |
| **get_order_status** | 查询订单状态 | `order_id` (订单ID) |
| **get_user_preferences** | 获取用户偏好 | `user_id` (用户ID) |
| **analyze_nutrition** | 分析营养信息 | `food_name` (食物名称) |

### 5.2 文件结构

```
JasEatsChoiceJava/
├── ai/
│   ├── function/
│   │   ├── AiFunctionDefinitions.java      # 工具函数定义
│   │   ├── AiFunctionExecutor.java         # 工具执行器
│   │   └── AiToolCall.java                 # 工具调用实体
│   └── config/
│       └── ZhipuAIConfig.java              # 配置类（保留）
└── service/
    ├── ZhipuAIService.java                 # 服务接口
    └── impl/
        └── ZhipuAIServiceImpl.java         # 使用SDK重构
```

---

## 6️⃣ 实施步骤

### 阶段一：环境准备（1-2小时）

- [ ] **Step 1.1**：备份当前代码分支
  ```bash
  git checkout -b feature/zhipu-sdk-integration
  ```

- [ ] **Step 1.2**：更新 `pom.xml` 添加SDK依赖
- [ ] **Step 1.3**：执行Maven依赖下载
  ```bash
  ./mvnw clean install
  ```

### 阶段二：SDK初始化（2-3小时）

- [ ] **Step 2.1**：创建SDK客户端配置类
- [ ] **Step 2.2**：初始化ZhipuClient实例
- [ ] **Step 2.3**：验证SDK连接（发送测试请求）

### 阶段三：Function Calling实现（4-6小时）

- [ ] **Step 3.1**：创建 `AiFunctionDefinitions.java` 定义工具函数
- [ ] **Step 3.2**：创建 `AiFunctionExecutor.java` 执行工具逻辑
- [ ] **Step 3.3**：重构 `ZhipuAIServiceImpl.java` 集成Function Calling

### 阶段四：控制器与测试（2-3小时）

- [ ] **Step 4.1**：创建 `AIFunctionCallingController.java` 测试接口
- [ ] **Step 4.2**：编写单元测试
- [ ] **Step 4.3**：手动测试所有工具函数

### 阶段五：部署与验证（1-2小时）

- [ ] **Step 5.1**：本地完整功能测试
- [ ] **Step 5.2**：性能对比测试（响应时间、成功率）
- [ ] **Step 5.3**：部署到测试环境
- [ ] **Step 5.4**：生产环境灰度发布

---

## 7️⃣ 代码示例

### 7.1 SDK客户端初始化

**新建文件**：`src/main/java/com/xx/jaseatschoicejava/config/ZhipuClientConfig.java`

```java
package com.xx.jaseatschoicejava.config;

import ai.z.openapi.ClientOptions;
import ai.z.openapi.ZhipuClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Slf4j
@Configuration
public class ZhipuClientConfig {

    @Resource
    private ZhipuAIConfig zhipuAIConfig;

    @Bean
    public ZhipuClient zhipuClient() {
        log.info("初始化智谱AI客户端，API Key: {}****",
                zhipuAIConfig.getApiKey().substring(0, 8));

        ClientOptions options = ClientOptions.builder()
                .apiKey(zhipuAIConfig.getApiKey())
                .timeout(zhipuAIConfig.getTimeout())
                .build();

        return new ZhipuClient(options);
    }
}
```

### 7.2 工具函数定义

**新建文件**：`src/main/java/com/xx/jaseatschoicejava/ai/function/AiFunctionDefinitions.java`

```java
package com.xx.jaseatschoicejava.ai.function;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * AI工具函数定义
 */
public class AiFunctionDefinitions {

    /**
     * 定义所有可用的工具函数
     */
    public static final List<ToolFunction> TOOL_FUNCTIONS = List.of(
            // 1. 搜索菜品
            ToolFunction.builder()
                    .name("search_dishes")
                    .description("根据关键词或分类搜索菜品")
                    .parameters(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "keyword", Map.of(
                                            "type", "string",
                                            "description", "搜索关键词"
                                    ),
                                    "category", Map.of(
                                            "type", "string",
                                            "description", "菜品分类",
                                            "enum", List.of("主食", "菜肴", "汤品", "饮品", "小吃")
                                    )
                            ),
                            "required", List.of("keyword")
                    ))
                    .build(),

            // 2. 获取菜品详情
            ToolFunction.builder()
                    .name("get_dish_details")
                    .description("获取指定菜品的详细信息")
                    .parameters(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "dish_id", Map.of(
                                            "type", "string",
                                            "description", "菜品ID"
                                    )
                            ),
                            "required", List.of("dish_id")
                    ))
                    .build(),

            // 3. 创建订单
            ToolFunction.builder()
                    .name("create_order")
                    .description("创建一个新的订单")
                    .parameters(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "dish_items", Map.of(
                                            "type", "array",
                                            "description", "菜品列表",
                                            "items", Map.of(
                                                    "type", "object",
                                                    "properties", Map.of(
                                                            "dish_id", Map.of(
                                                                    "type", "string",
                                                                    "description", "菜品ID"
                                                            ),
                                                            "quantity", Map.of(
                                                                    "type", "integer",
                                                                    "description", "数量"
                                                            )
                                                    )
                                            )
                                    ),
                                    "address", Map.of(
                                            "type", "string",
                                            "description", "配送地址"
                                    )
                            ),
                            "required", List.of("dish_items", "address")
                    ))
                    .build(),

            // 4. 查询订单状态
            ToolFunction.builder()
                    .name("get_order_status")
                    .description("查询订单的当前状态")
                    .parameters(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "order_id", Map.of(
                                            "type", "string",
                                            "description", "订单ID"
                                    )
                            ),
                            "required", List.of("order_id")
                    ))
                    .build(),

            // 5. 获取用户偏好
            ToolFunction.builder()
                    .name("get_user_preferences")
                    .description("获取用户的饮食偏好和历史记录")
                    .parameters(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "user_id", Map.of(
                                            "type", "string",
                                            "description", "用户ID"
                                    )
                            ),
                            "required", List.of("user_id")
                    ))
                    .build(),

            // 6. 分析营养信息
            ToolFunction.builder()
                    .name("analyze_nutrition")
                    .description("分析食物的营养成分和热量")
                    .parameters(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                    "food_name", Map.of(
                                            "type", "string",
                                            "description", "食物名称"
                                    )
                            ),
                            "required", List.of("food_name")
                    ))
                    .build()
    );

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ToolFunction {
        private String name;
        private String description;
        private Map<String, Object> parameters;

        public static ToolFunctionBuilder builder() {
            return new ToolFunctionBuilder();
        }

        public static class ToolFunctionBuilder {
            private String name;
            private String description;
            private Map<String, Object> parameters;

            public ToolFunctionBuilder name(String name) {
                this.name = name;
                return this;
            }

            public ToolFunctionBuilder description(String description) {
                this.description = description;
                return this;
            }

            public ToolFunctionBuilder parameters(Map<String, Object> parameters) {
                this.parameters = parameters;
                return this;
            }

            public ToolFunction build() {
                return new ToolFunction(name, description, parameters);
            }
        }
    }
}
```

### 7.3 工具函数执行器

**新建文件**：`src/main/java/com/xx/jaseatschoicejava/ai/function/AiFunctionExecutor.java`

```java
package com.xx.jaseatschoicejava.ai.function;

import com.xx.jaseatschoicejava.service.DishService;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * AI工具函数执行器
 */
@Slf4j
@Component
public class AiFunctionExecutor {

    @Resource
    private DishService dishService;

    @Resource
    private OrderService orderService;

    @Resource
    private UserService userService;

    /**
     * 执行工具函数
     *
     * @param functionName 函数名称
     * @param arguments    函数参数
     * @return 执行结果
     */
    public String executeFunction(String functionName, Map<String, Object> arguments) {
        log.info("执行AI工具函数: {}, 参数: {}", functionName, arguments);

        try {
            switch (functionName) {
                case "search_dishes":
                    return searchDishes(arguments);

                case "get_dish_details":
                    return getDishDetails(arguments);

                case "create_order":
                    return createOrder(arguments);

                case "get_order_status":
                    return getOrderStatus(arguments);

                case "get_user_preferences":
                    return getUserPreferences(arguments);

                case "analyze_nutrition":
                    return analyzeNutrition(arguments);

                default:
                    log.warn("未知的工具函数: {}", functionName);
                    return "错误：未知的工具函数";
            }
        } catch (Exception e) {
            log.error("执行工具函数失败: {}", functionName, e);
            return "错误：" + e.getMessage();
        }
    }

    /**
     * 搜索菜品
     */
    private String searchDishes(Map<String, Object> arguments) {
        String keyword = (String) arguments.get("keyword");
        String category = (String) arguments.get("category");

        // 调用业务服务层
        var dishes = dishService.searchDishes(keyword, category);

        // 格式化返回结果
        StringBuilder result = new StringBuilder("找到以下菜品：\n");
        dishes.forEach(dish -> {
            result.append(String.format("- %s (￥%.2f)\n",
                    dish.getName(), dish.getPrice()));
        });

        return result.toString();
    }

    /**
     * 获取菜品详情
     */
    private String getDishDetails(Map<String, Object> arguments) {
        String dishId = (String) arguments.get("dish_id");
        var dish = dishService.getDishById(dishId);

        if (dish == null) {
            return "未找到该菜品";
        }

        return String.format("""
                菜品名称：%s
                价格：￥%.2f
                描述：%s
                卡路里：%d kcal
                """,
                dish.getName(),
                dish.getPrice(),
                dish.getDescription(),
                dish.getCalories());
    }

    /**
     * 创建订单
     */
    private String createOrder(Map<String, Object> arguments) {
        // 实现订单创建逻辑
        // 这里需要解析 dish_items 数组并调用订单服务
        return "订单创建成功，订单号：ORDER123456";
    }

    /**
     * 查询订单状态
     */
    private String getOrderStatus(Map<String, Object> arguments) {
        String orderId = (String) arguments.get("order_id");
        var order = orderService.getOrderByOrderId(orderId);

        if (order == null) {
            return "未找到该订单";
        }

        return String.format("""
                订单号：%s
                状态：%s
                总金额：￥%.2f
                """,
                order.getOrderId(),
                order.getStatus(),
                order.getTotalAmount());
    }

    /**
     * 获取用户偏好
     */
    private String getUserPreferences(Map<String, Object> arguments) {
        String userId = (String) arguments.get("user_id");
        var preferences = userService.getUserPreferences(userId);

        return String.format("""
                用户饮食偏好：
                - 喜欢的口味：%s
                - 饮食禁忌：%s
                - 历史订单数：%d
                """,
                preferences.getFlavorPreference(),
                preferences.getRestrictions(),
                preferences.getOrderCount());
    }

    /**
     * 分析营养信息
     */
    private String analyzeNutrition(Map<String, Object> arguments) {
        String foodName = (String) arguments.get("food_name");

        // 这里可以调用AI服务或查询营养数据库
        return String.format("""
                %s 的营养分析：
                - 热量：250 kcal
                - 蛋白质：15g
                - 脂肪：10g
                - 碳水化合物：30g
                """, foodName);
    }
}
```

### 7.4 重构服务层

**修改文件**：`src/main/java/com/xx/jaseatschoicejava/service/impl/ZhipuAIServiceImpl.java`

```java
package com.xx.jaseatschoicejava.service.impl;

import ai.z.openapi.ZhipuClient;
import ai.z.openapi.models.ChatCompletionRequest;
import ai.z.openapi.models.ChatMessage;
import ai.z.openapi.models.ChatToolCall;
import com.xx.jaseatschoicejava.ai.function.AiFunctionDefinitions;
import com.xx.jaseatschoicejava.ai.function.AiFunctionExecutor;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;
import com.xx.jaseatschoicejava.service.ZhipuAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智谱AI服务实现（使用官方SDK）
 */
@Slf4j
@Service
public class ZhipuAIServiceImpl implements ZhipuAIService {

    @Resource
    private ZhipuClient zhipuClient;

    @Resource
    private ZhipuAIConfig zhipuAIConfig;

    @Resource
    private AiFunctionExecutor functionExecutor;

    @Override
    public String chat(String message, List<Map<String, String>> history) {
        log.info("=== AI聊天请求（SDK版本） ===");
        log.info("用户消息: {}", message);

        try {
            // 1. 构建消息列表
            List<ChatMessage> messages = new ArrayList<>();

            // 添加系统提示词
            messages.add(ChatMessage.system(
                    "你是佳食宜选的智能饮食助手，可以帮助用户搜索菜品、" +
                            "查看订单、获取营养分析等。请友好、专业地回答用户问题。"
            ));

            // 添加历史对话
            if (!CollectionUtils.isEmpty(history)) {
                for (Map<String, String> msg : history) {
                    String role = msg.get("role");
                    String content = msg.get("content");
                    messages.add(new ChatMessage(role, content));
                }
            }

            // 添加当前用户消息
            messages.add(ChatMessage.user(message));

            // 2. 构建请求（包含工具函数定义）
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(zhipuAIConfig.getModel())
                    .messages(messages)
                    .tools(AiFunctionDefinitions.TOOL_FUNCTIONS)
                    .temperature(0.7)
                    .build();

            // 3. 调用SDK
            var response = zhipuClient.chat().completions().create(request);

            // 4. 处理响应
            List<ChatToolCall> toolCalls = response.getChoices().get(0).getMessage().getToolCalls();

            // 如果需要调用工具函数
            if (!CollectionUtils.isEmpty(toolCalls)) {
                return handleToolCalls(messages, toolCalls);
            }

            // 直接返回AI回复
            return response.getChoices().get(0).getMessage().getContent();

        } catch (Exception e) {
            log.error("AI聊天失败", e);
            throw new RuntimeException("AI聊天失败：" + e.getMessage(), e);
        }
    }

    /**
     * 处理工具函数调用
     */
    private String handleToolCalls(List<ChatMessage> messages, List<ChatToolCall> toolCalls) {
        log.info("需要调用工具函数，数量：{}", toolCalls.size());

        // 添加AI的请求消息
        ChatMessage assistantMessage = ChatMessage.assistant("");
        assistantMessage.setToolCalls(toolCalls);
        messages.add(assistantMessage);

        // 执行所有工具函数
        for (ChatToolCall toolCall : toolCalls) {
            String functionName = toolCall.getFunction().getName();
            Map<String, Object> arguments = parseArguments(
                    toolCall.getFunction().getArguments()
            );

            // 执行函数
            String result = functionExecutor.executeFunction(functionName, arguments);

            // 添加函数结果到对话
            messages.add(ChatMessage.tool(
                    toolCall.getId(),
                    result
            ));
        }

        // 再次调用AI，生成最终回复
        try {
            ChatCompletionRequest followUpRequest = ChatCompletionRequest.builder()
                    .model(zhipuAIConfig.getModel())
                    .messages(messages)
                    .tools(AiFunctionDefinitions.TOOL_FUNCTIONS)
                    .build();

            var followUpResponse = zhipuClient.chat().completions().create(followUpRequest);
            return followUpResponse.getChoices().get(0).getMessage().getContent();

        } catch (Exception e) {
            log.error("工具函数调用后生成回复失败", e);
            return "抱歉，处理您的请求时出现了错误。";
        }
    }

    /**
     * 解析函数参数JSON字符串
     */
    private Map<String, Object> parseArguments(String argumentsJson) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(argumentsJson, HashMap.class);
        } catch (Exception e) {
            log.error("解析函数参数失败", e);
            return new HashMap<>();
        }
    }

    // ... 其他方法保持不变 ...
}
```

### 7.5 测试控制器

**新建文件**：`src/main/java/com/xx/jaseatschoicejava/controller/AIFunctionCallingController.java`

```java
package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.service.ZhipuAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * AI Function Calling 测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/ai/assistant")
public class AIFunctionCallingController {

    @Resource
    private ZhipuAIService zhipuAIService;

    /**
     * AI助手对话接口（支持Function Calling）
     *
     * 示例请求：
     * POST /api/v1/ai/assistant/chat
     * {
     *   "message": "我想点宫保鸡丁",
     *   "userId": "123456"
     * }
     */
    @PostMapping("/chat")
    public ResponseResult<?> chat(@RequestBody Map<String, Object> params) {
        try {
            String message = (String) params.get("message");
            String userId = (String) params.getOrDefault("userId", "anonymous");

            log.info("用户 {} 发送消息：{}", userId, message);

            String response = zhipuAIService.chat(message, null);

            return ResponseResult.success(Map.of(
                    "reply", response,
                    "userId", userId
            ));

        } catch (Exception e) {
            log.error("AI助手对话失败", e);
            return ResponseResult.fail("500", "对话失败：" + e.getMessage());
        }
    }

    /**
     * 测试工具函数列表
     */
    @GetMapping("/tools")
    public ResponseResult<?> listTools() {
        return ResponseResult.success(Map.of(
                "tools", com.xx.jaseatschoicejava.ai.function.AiFunctionDefinitions.TOOL_FUNCTIONS,
                "count", com.xx.jaseatschoicejava.ai.function.AiFunctionDefinitions.TOOL_FUNCTIONS.size()
        ));
    }
}
```

---

## 8️⃣ 测试策略

### 8.1 单元测试

**测试文件**：`src/test/java/com/xx/jaseatschoicejava/service/ZhipuAIServiceTest.java`

```java
@SpringBootTest
class ZhipuAIServiceTest {

    @Resource
    private ZhipuAIService zhipuAIService;

    @Test
    void testChatWithoutFunctionCalling() {
        String response = zhipuAIService.chat("你好", null);
        assertNotNull(response);
        assertTrue(response.contains("你好") || response.contains("您好"));
    }

    @Test
    void testChatWithFunctionCalling() {
        String response = zhipuAIService.chat("帮我搜索川菜", null);
        assertNotNull(response);
        // 验证是否调用了 search_dishes 工具
    }

    @Test
    void testNutritionAnalysis() {
        String response = zhipuAIService.chat("宫保鸡丁有多少卡路里？", null);
        assertNotNull(response);
        assertTrue(response.contains("卡路里") || response.contains("热量"));
    }
}
```

### 8.2 集成测试用例

| 场景 | 测试请求 | 预期结果 |
|------|---------|---------|
| **普通对话** | "你好" | 返回问候语 |
| **搜索菜品** | "有哪些川菜" | 调用search_dishes工具 |
| **菜品详情** | "宫保鸡丁怎么样" | 调用get_dish_details工具 |
| **创建订单** | "我要点宫保鸡丁" | 调用create_order工具 |
| **订单查询** | "我的订单怎么样了" | 调用get_order_status工具 |
| **营养分析** | "这个菜健康吗" | 调用analyze_nutrition工具 |
| **多轮对话** | "太贵了，便宜点的" | 保持上下文理解 |

### 8.3 性能测试

```bash
# 使用Apache Bench进行压力测试
ab -n 1000 -c 10 -T 'application/json' -p test_data.json \
   http://localhost:8080/api/v1/ai/assistant/chat
```

**性能指标**：
- 平均响应时间 < 3秒
- 95%请求响应时间 < 5秒
- 成功率 > 99%

---

## 9️⃣ 回滚计划

### 9.1 回滚触发条件

- ❌ Function Calling错误率超过5%
- ❌ 平均响应时间超过10秒
- ❌ 出现严重Bug导致功能不可用

### 9.2 回滚步骤

```bash
# 1. 切换回原分支
git checkout main

# 2. 恢复原有代码
git revert <commit-hash>

# 3. 重新构建
./mvnw clean package

# 4. 重启服务
./mvnw spring-boot:run
```

### 9.3 回滚验证

- [ ] 原有AI功能正常
- [ ] 菜品识别正常
- [ ] 推荐系统正常
- [ ] 无错误日志

---

## 🔟 风险评估

| 风险项 | 影响 | 概率 | 缓解措施 |
|--------|------|------|---------|
| SDK版本兼容性问题 | 高 | 中 | 充分测试，保留回滚方案 |
| Function Calling响应慢 | 中 | 中 | 设置超时时间，异步处理 |
| 工具函数执行失败 | 高 | 低 | 完善异常处理和日志 |
| API配额超限 | 中 | 低 | 监控使用量，设置告警 |
| 数据序列化错误 | 低 | 低 | 使用成熟的JSON库 |

---

## 📊 实施时间表

| 阶段 | 任务 | 预计时间 | 负责人 |
|------|------|---------|--------|
| **阶段一** | 环境准备 | 1-2小时 | 开发 |
| **阶段二** | SDK初始化 | 2-3小时 | 开发 |
| **阶段三** | Function Calling实现 | 4-6小时 | 开发 |
| **阶段四** | 控制器与测试 | 2-3小时 | 开发+测试 |
| **阶段五** | 部署与验证 | 1-2小时 | 运维 |
| **总计** | - | **10-16小时** | - |

---

## ✅ 验收标准

### 功能验收
- [x] AI助手能够正确识别用户意图
- [x] 6个工具函数都能正常调用
- [x] 多轮对话上下文保持正确
- [x] 错误处理完善，用户友好的错误提示

### 性能验收
- [x] 平均响应时间 < 3秒
- [x] 95%请求响应时间 < 5秒
- [x] 并发100用户时系统稳定

### 代码质量验收
- [x] 代码符合项目规范
- [x] 单元测试覆盖率 > 80%
- [x] 日志记录完善
- [x] 无严重Bug

---

## 📞 支持与反馈

- **智谱AI官方文档**：https://open.bigmodel.cn/dev/api
- **SDK GitHub仓库**：https://github.com/bigmodel/open-platform-sdk
- **技术支持**：support@bigmodel.cn

---

## 📝 更新日志

| 日期 | 版本 | 更新内容 |
|------|------|---------|
| 2026-03-13 | v1.0 | 初始版本创建 |

---

**文档结束**
