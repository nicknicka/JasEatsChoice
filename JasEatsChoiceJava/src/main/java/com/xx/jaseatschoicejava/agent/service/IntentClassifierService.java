package com.xx.jaseatschoicejava.agent.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI驱动的意图分类服务
 *
 * 使用大语言模型进行意图识别，替代基于关键词的规则引擎
 *
 * @author Claude
 * @since 2026-03-22
 */
@Slf4j
@Service
public class IntentClassifierService {

    @Resource
    private ChatLanguageModel chatLanguageModel;

    /**
     * 意图分类缓存（提升性能）
     */
    private final ConcurrentHashMap<String, String> intentCache = new ConcurrentHashMap<>();

    /**
     * 意图分类提示词模板
     */
    private static final String INTENT_CLASSIFICATION_PROMPT = """
            你是一个专业的意图分类助手。请分析用户消息，判断其意图类别。

            **支持的意图类型：**

            1. **NUTRITION** - 营养咨询
               - 营养成分分析（卡路里、蛋白质、脂肪、碳水等）
               - 食物热量计算
               - 饮食健康评估
               - 每日营养需求
               - 示例："苹果有多少卡路里？"、"这道菜营养如何"

            2. **RECOMMENDATION** - 美食推荐
               - 个性化菜品推荐
               - 菜品搜索
               - 热门菜品查询
               - 饮食偏好匹配
               - 示例："今天推荐什么菜？"、"有什么低卡路里的菜"

            3. **ORDER** - 订餐服务
               - 创建订单
               - 查询订单状态
               - 取消订单
               - 配送相关
               - 示例："我要下单"、"查询我的订单"、"取消订单123"

            4. **GREETING** - 问候语
               - 打招呼、感谢等社交表达
               - 示例："你好"、"谢谢"、"在吗"

            5. **GENERAL** - 一般咨询
               - 其他未明确分类的咨询
               - 示例："你们支持哪些支付方式？"、"营业时间"

            **分类规则：**
            - 理解用户的核心诉求，而不是简单匹配关键词
            - 考虑上下文和语义
            - 当用户意图不明确时，选择最可能的类别
            - 否定表达要正确处理（如"不想知道卡路里"不是营养咨询）

            **输出格式：**
            只返回意图类型代码（如：NUTRITION），不要添加任何其他内容。

            **用户消息：**
            %s
            """;

    /**
     * 使用AI进行意图分类
     *
     * @param userMessage 用户消息
     * @return 意图类型
     */
    public String classifyIntent(String userMessage) {
        // 检查缓存
        String cached = intentCache.get(userMessage);
        if (cached != null) {
            log.debug("意图分类命中缓存：{} -> {}", userMessage, cached);
            return cached;
        }

        try {
            // 构建提示词
            String prompt = String.format(INTENT_CLASSIFICATION_PROMPT, userMessage);

            // 调用AI模型
            String aiResponse = chatLanguageModel.generate(prompt);

            // 解析AI响应
            String intent = parseIntent(aiResponse);

            // 缓存结果（最多缓存1000条）
            if (intentCache.size() < 1000) {
                intentCache.put(userMessage, intent);
            }

            log.info("AI意图分类：{} -> {}", userMessage, intent);
            return intent;

        } catch (Exception e) {
            log.error("AI意图分类失败，降级到规则引擎", e);
            // 降级：使用规则引擎
            return classifyIntentByRules(userMessage);
        }
    }

    /**
     * 解析AI响应，提取意图类型
     */
    private String parseIntent(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return "GENERAL";
        }

        String response = aiResponse.trim().toUpperCase();

        // 移除可能的markdown格式
        response = response.replaceAll("`", "").trim();

        // 验证是否为有效的意图类型
        if (response.matches("NUTRITION|RECOMMENDATION|ORDER|GREETING|GENERAL")) {
            return response;
        }

        // 如果AI返回了无效的意图，尝试从文本中提取
        if (response.contains("NUTRITION") || response.contains("营养")) {
            return "NUTRITION";
        } else if (response.contains("RECOMMENDATION") || response.contains("推荐")) {
            return "RECOMMENDATION";
        } else if (response.contains("ORDER") || response.contains("订单")) {
            return "ORDER";
        } else if (response.contains("GREETING") || response.contains("问候")) {
            return "GREETING";
        }

        // 默认返回一般咨询
        return "GENERAL";
    }

    /**
     * 规则引擎降级方案（基于关键词）
     * 当AI服务不可用时使用
     */
    private String classifyIntentByRules(String message) {
        String lowerMessage = message.toLowerCase();

        // 问候语
        if (lowerMessage.matches(".*(你好|嗨|hello|hi|您好|在吗|帮忙|协助|谢谢|感谢).*")) {
            return "GREETING";
        }

        // 订单相关关键词（优先级高，避免误判）
        if (lowerMessage.contains("下单") || lowerMessage.contains("订餐") ||
            lowerMessage.matches(".*(我要|我想|我要买|我要点).*") ||
            lowerMessage.contains("配送") || lowerMessage.contains("送餐") ||
            lowerMessage.matches(".*(查询|查看|取消).*订单")) {
            return "ORDER";
        }

        // 营养相关关键词
        if (lowerMessage.matches(".*(多少|怎么样|如何).*营养") ||
            lowerMessage.matches(".*(多少|含有|包含).*(卡路里|热量|蛋白质|脂肪|碳水|成分)") ||
            lowerMessage.contains("营养成分") || lowerMessage.contains("健康饮食")) {
            return "NUTRITION";
        }

        // 推荐相关关键词
        if (lowerMessage.contains("推荐") || lowerMessage.contains("吃什么") ||
            lowerMessage.contains("搜索") || lowerMessage.contains("热门") ||
            lowerMessage.matches(".*(有什么|看看|找).*(菜|美食|推荐)")) {
            return "RECOMMENDATION";
        }

        return "GENERAL";
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        intentCache.clear();
        log.info("意图分类缓存已清除");
    }

    /**
     * 获取缓存统计
     */
    public int getCacheSize() {
        return intentCache.size();
    }
}
