package com.xx.jaseatschoicejava.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.agent.agents.stream.StreamingIntelligentAssistantAgent;
import com.xx.jaseatschoicejava.agent.context.ToolExecutionContext;
import com.xx.jaseatschoicejava.common.ResponseResult;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI流式响应控制器（使用LangChain4j）
 * 提供SSE流式聊天接口 - 流式输出
 *
 * @author Claude
 * @since 2026-03-22
 */
@Api(tags = "AI流式对话（LangChain4j）")
@RestController
@RequestMapping("/v1/ai/stream")
public class AIStreamController {

    private static final Logger log = LoggerFactory.getLogger(AIStreamController.class);

    @Resource
    private StreamingIntelligentAssistantAgent streamingIntelligentAssistantAgent;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private ApplicationContext applicationContext;

    /**
     * SSE流式聊天接口（真正的流式输出）
     *
     * @param params 请求参数
     * @return SseEmitter
     */
    @ApiOperation(value = "SSE流式聊天", notes = "使用LangChain4j的StreamingChatLanguageModel实现真正的流式响应")
    @PostMapping("/chat")
    public SseEmitter streamChat(@RequestBody Map<String, Object> params) {
        // 创建SseEmitter（30秒超时）
        SseEmitter emitter = new SseEmitter(30000L);

        try {
            // 1. 提取参数
            String message = (String) params.get("message");
            String userId = (String) params.getOrDefault("userId", "anonymous");

            // 2. 参数验证
            if (message == null || message.trim().isEmpty()) {
                emitter.send(SseEmitter.event()
                    .name("error")
                    .data("消息内容不能为空"));
                emitter.complete();
                return emitter;
            }

            log.info("📥 收到流式聊天请求");
            log.info("   - 用户ID: {}", userId);
            log.info("   - 消息内容: {}", message);

            // 保存用户消息到聊天历史
            try {
                com.xx.jaseatschoicejava.service.AIChatHistoryService chatHistoryService =
                    applicationContext.getBean(com.xx.jaseatschoicejava.service.AIChatHistoryService.class);
                chatHistoryService.saveMessage(userId, "user", message, null, null);
            } catch (Exception e) {
                log.warn("保存用户消息失败: {}", e.getMessage());
            }

            // 3. 调用真正的流式Agent（传递userId）
            streamingIntelligentAssistantAgent.chat(message, userId)
                .onNext(token -> {
                    // 处理每个token（从LLM流式接收）
                    try {
                        if (token != null && !token.isEmpty()) {
                            // 将token包装为JSON对象，确保换行符等特殊字符被正确传输
                            Map<String, String> charData = Map.of("char", token);
                            emitter.send(SseEmitter.event()
                                .name("message")
                                .data(charData));
                        }
                    } catch (IOException e) {
                        log.error("发送token失败", e);
                    }
                })
                .onComplete(response -> {
                    // 流完成时调用
                    try {
                        log.info("✅ 流式响应完成");

                        AiMessage aiMessage = response.content();
                        log.info("📊 响应内容: {}", aiMessage.text());

                        // 从ToolExecutionContext获取工具执行信息
                        Map<String, ToolExecutionContext.ToolExecutionInfo> cardExecutions =
                            ToolExecutionContext.getCardExecutions();

                        log.info("📊 检测到 {} 个需要生成卡片的工具调用", cardExecutions.size());

                        // 如果有工具执行信息，生成对应的卡片数据
                        if (!cardExecutions.isEmpty()) {
                            // 选择优先级最高的卡片（如果有多个）
                            ToolExecutionContext.ToolExecutionInfo executionInfo =
                                cardExecutions.values().stream()
                                    .findFirst()
                                    .orElse(null);

                            if (executionInfo != null) {
                                String cardType = executionInfo.getCardType();
                                log.info("📊 生成卡片数据: cardType={}", cardType);

                                Map<String, Object> cardData = buildCardData(cardType, userId);

                                if (cardData != null) {
                                    emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(Map.of("card_data", cardData)));
                                    log.info("✅ 卡片数据已发送: {}", cardType);
                                }
                            }
                        }

                        // 发送完成事件
                        emitter.send(SseEmitter.event()
                            .name("end")
                            .data(Map.of("done", true)));
                        emitter.complete();

                        // 清理ThreadLocal
                        ToolExecutionContext.clear();

                    } catch (IOException e) {
                        log.error("发送完成事件失败", e);
                        ToolExecutionContext.clear();
                        emitter.completeWithError(e);
                    }
                })
                .onError(error -> {
                    // 发生错误时调用
                    log.error("❌ 流式响应出错", error);
                    ToolExecutionContext.clear();
                    try {
                        emitter.send(SseEmitter.event()
                            .name("error")
                            .data("处理失败：" + error.getMessage()));
                        emitter.completeWithError(error);
                    } catch (IOException e) {
                        log.error("发送错误事件失败", e);
                    }
                })
                .start(); // 启动流式处理

        } catch (Exception e) {
            log.error("创建SseEmitter失败", e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * 根据卡片类型构建卡片数据
     * @param cardType 卡片类型
     * @param userId 用户ID
     * @return 卡片数据，如果不需要卡片则返回null
     */
    private Map<String, Object> buildCardData(String cardType, String userId) {
        try {
            log.info("📊 开始构建卡片数据: cardType={}, userId={}", cardType, userId);

            switch (cardType) {
                case "order_list_card":
                    return buildOrderListCardData(userId);
                case "user_info_card":
                    return buildUserInfoCardData(userId);
                default:
                    log.warn("⚠️ 未知的卡片类型: {}", cardType);
                    return null;
            }
        } catch (Exception e) {
            log.error("构建卡片数据失败: cardType=" + cardType, e);
            return null;
        }
    }

    /**
     * 从响应中构建卡片数据
     * @param response AI响应
     * @param userId 用户ID
     * @return 卡片数据，如果不需要卡片则返回null
     */
    private Map<String, Object> buildCardDataFromResponse(AiMessage response, String userId) {
        try {
            // 检查响应中是否包含工具执行结果
            if (response.hasToolExecutionRequests()) {
                log.info("📊 检测到工具执行请求");
                // 注意：LangChain4j会自动执行工具，这里我们无法直接获取执行结果
                // 需要通过其他方式获取，比如从ChatMemory或直接查询数据库
                return null;
            }

            // 分析响应内容，判断是否需要生成卡片
            String content = response.text();
            if (content == null || content.isEmpty()) {
                return null;
            }

            // 根据关键词判断是否需要生成特定类型的卡片
            // 这是一个简化的实现，实际应该基于工具执行结果
            if (content.contains("订单") || content.contains("订单列表")) {
                log.info("📊 检测到订单相关内容，生成订单卡片");
                return buildOrderListCardData(userId);
            } else if (content.contains("用户信息") || content.contains("个人信息") || content.contains("我的资料")) {
                log.info("📊 检测到用户信息相关内容，生成用户信息卡片");
                return buildUserInfoCardData(userId);
            }

            return null;
        } catch (Exception e) {
            log.error("构建卡片数据失败", e);
            return null;
        }
    }

    /**
     * 构建订单列表卡片数据
     */
    private Map<String, Object> buildOrderListCardData(String userId) {
        try {
            // 查询订单服务获取实际数据
            com.xx.jaseatschoicejava.service.OrderService orderService =
                applicationContext.getBean(com.xx.jaseatschoicejava.service.OrderService.class);

            // 查询订单列表
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.xx.jaseatschoicejava.entity.Order> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .orderByDesc("create_time")
                    .last("LIMIT 20");

            List<com.xx.jaseatschoicejava.entity.Order> orders = orderService.list(queryWrapper);

            if (orders == null || orders.isEmpty()) {
                log.info("用户暂无订单记录: userId={}", userId);
                return null;
            }

            // 构建前端OrderListCard组件需要的数据结构
            Map<String, Object> data = new HashMap<>();
            data.put("total", orders.size());
            data.put("pendingCount", orders.stream()
                    .filter(o -> o.getStatus() != null && o.getStatus() < 5)
                    .count());
            data.put("summary", String.format("找到 %d 条订单记录", orders.size()));

            // 构建订单列表
            List<Map<String, Object>> orderList = new java.util.ArrayList<>();
            for (com.xx.jaseatschoicejava.entity.Order order : orders) {
                Map<String, Object> orderItem = new HashMap<>();
                orderItem.put("orderId", order.getId());
                orderItem.put("status", order.getStatus());
                orderItem.put("statusText", getOrderStatusText(order.getStatus()));
                orderItem.put("totalAmount", order.getTotalAmount() != null ?
                        String.format("%.2f", order.getTotalAmount()) : "0.00");
                orderItem.put("dishCount", 0); // TODO: 从订单详情表获取菜品数量
                orderItem.put("createTime", order.getCreateTime() != null ?
                        order.getCreateTime().toString() : "");

                // 添加可操作按钮
                List<Map<String, String>> actions = new java.util.ArrayList<>();
                if (order.getStatus() != null && order.getStatus() < 3) {
                    actions.add(Map.of("type", "detail", "text", "查看详情", "icon", "View"));
                    if (order.getStatus() == 0 || order.getStatus() == 1) {
                        actions.add(Map.of("type", "cancel", "text", "取消订单", "icon", "Delete"));
                    }
                    actions.add(Map.of("type", "urge", "text", "催单", "icon", "Bell"));
                }
                orderItem.put("actions", actions);

                orderList.add(orderItem);
            }
            data.put("orders", orderList);

            // 返回完整结构
            Map<String, Object> result = new HashMap<>();
            result.put("messageType", "order_list_card");
            result.put("data", data);
            return result;

        } catch (Exception e) {
            log.error("构建订单卡片失败", e);
            return null;
        }
    }

    /**
     * 构建用户信息卡片数据
     */
    private Map<String, Object> buildUserInfoCardData(String userId) {
        try {
            // 查询用户服务获取实际数据
            com.xx.jaseatschoicejava.service.UserService userService =
                applicationContext.getBean(com.xx.jaseatschoicejava.service.UserService.class);

            com.xx.jaseatschoicejava.entity.User user = userService.getById(userId);
            if (user == null) {
                log.info("未找到用户信息: userId={}", userId);
                return null;
            }

            // 构建基本信息
            Map<String, Object> basicInfo = new HashMap<>();
            basicInfo.put("nickname", user.getNickname() != null ? user.getNickname() : "未设置");
            basicInfo.put("phone", user.getPhone() != null ?
                    user.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2") : "未设置");
            basicInfo.put("email", user.getEmail() != null ? user.getEmail() : "未设置");
            basicInfo.put("location", user.getLocation() != null ? user.getLocation() : "未设置");
            basicInfo.put("gender", user.getGender() != null ? user.getGender() : "未设置");
            basicInfo.put("registerTime", user.getCreateTime() != null ?
                    user.getCreateTime().toLocalDate().toString() : "未设置");

            // 构建身体数据
            Map<String, Object> bodyData = new HashMap<>();
            bodyData.put("height", user.getHeight() != null ? user.getHeight() : "-");
            bodyData.put("weight", user.getWeight() != null ? user.getWeight() : "-");

            if (user.getHeight() != null && user.getHeight() > 0
                    && user.getWeight() != null && user.getWeight() > 0) {
                double height = user.getHeight() / 100.0; // 转换为米
                double bmi = user.getWeight() / (height * height);
                bodyData.put("bmi", String.format("%.1f", bmi));
                bodyData.put("bmiStatus", getBMIStatus(bmi));
                bodyData.put("bmiText", getBMIStatusText(bmi));
            }

            // 构建饮食偏好
            Map<String, Object> preferences = new HashMap<>();
            preferences.put("dietGoal", user.getDietGoal() != null ? user.getDietGoal() : "未设置");

            // 操作按钮
            List<Map<String, String>> actions = new java.util.ArrayList<>();
            actions.add(Map.of("type", "edit_profile", "text", "编辑资料", "icon", "Edit"));
            actions.add(Map.of("type", "view_health", "text", "健康分析", "icon", "TrendCharts"));

            // 按照前端UserInfoCard组件期望的数据结构组装
            Map<String, Object> data = new HashMap<>();
            data.put("summary", "用户基本信息档案");
            data.put("basicInfo", basicInfo);
            data.put("bodyData", bodyData);
            data.put("preferences", preferences);
            data.put("actions", actions);

            Map<String, Object> result = new HashMap<>();
            result.put("messageType", "user_info_card");
            result.put("data", data);
            return result;

        } catch (Exception e) {
            log.error("构建用户信息卡片失败", e);
            return null;
        }
    }

    /**
     * 获取订单状态文本
     */
    private String getOrderStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待支付";
            case 1: return "待商家接单";
            case 2: return "商家已接单";
            case 3: return "配送中";
            case 4: return "已完成";
            case 5: return "已取消";
            case 6: return "退款中";
            case 7: return "已退款";
            default: return "未知";
        }
    }

    /**
     * 计算BMI状态
     */
    private String getBMIStatus(double bmi) {
        if (bmi < 18.5) return "underweight";
        if (bmi < 24) return "normal";
        if (bmi < 28) return "overweight";
        return "obese";
    }

    /**
     * 获取BMI状态文本
     */
    private String getBMIStatusText(double bmi) {
        if (bmi < 18.5) return "偏瘦";
        if (bmi < 24) return "正常";
        if (bmi < 28) return "偏胖";
        return "肥胖";
    }

    /**
     * 健康检查接口
     *
     * @return 服务状态
     */
    @ApiOperation(value = "健康检查", notes = "检查AI流式服务是否正常")
    @GetMapping("/health")
    public ResponseResult<?> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "AI Stream (LangChain4j)");
        result.put("version", "3.0.0");
        result.put("framework", "LangChain4j 0.34.0");
        result.put("streaming", "true");
        result.put("note", "已升级到真正的流式Agent，使用ZhipuAiStreamingChatModel");
        result.put("timestamp", System.currentTimeMillis());

        return ResponseResult.success(result);
    }
}
