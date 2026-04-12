package com.xx.jaseatschoicejava.agent.service;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import dev.langchain4j.model.chat.ChatModel;

/**
 * 两级意图分类器
 *
 * 第1级：规则匹配（0ms）— 基于关键词快速判断
 * 第2级：LLM分类（~500ms）— 规则未命中时用轻量LLM调用判断
 *
 * 安全兜底：无法确定时默认为 BUSINESS，走 SupervisorAgent 全流程
 *
 * @since 2026-04-12
 */
@Component
public class IntentClassifier {

    private static final Logger log = LoggerFactory.getLogger(IntentClassifier.class);

    /**
     * 简单对话关键词（正则模式）
     * 匹配打招呼、自我介绍请求、感谢道别、功能询问等
     */
    private static final List<Pattern> SIMPLE_CHAT_PATTERNS = List.of(
            // 打招呼
            Pattern.compile("^(你好|嗨|hi|hello|hey|早安|早上好|中午好|下午好|晚上好|晚安|hey)\\s*[！!。.？?]*$", Pattern.CASE_INSENSITIVE),
            // 自我介绍
            Pattern.compile("(你是谁|介绍下?你自己|你叫什么|你的名字|自我介绍|你是什么|你是个?什么)"),
            // 感谢
            Pattern.compile("^(谢谢|感谢|多谢|thanks|thank you|thx)\\s*[！!。.]*$", Pattern.CASE_INSENSITIVE),
            // 道别
            Pattern.compile("^(再见|拜拜|bye|goodbye|see you|回头见|下次见)\\s*[！!。.]*$", Pattern.CASE_INSENSITIVE),
            // 平台功能询问
            Pattern.compile("(你能做什么|你有什么功能|你会什么|你能帮我什么|你的功能|你能提供什么|你有哪些能力)"),
            // 确认/测试
            Pattern.compile("^(在吗|在不在|有人吗|测试|test)\\s*[？?！!。.]*$", Pattern.CASE_INSENSITIVE)
    );

    /**
     * 业务关键词集合
     * 命中任一关键词即判定为业务意图
     */
    private static final Set<String> BUSINESS_KEYWORDS = Set.of(
            // 菜品相关
            "菜品", "菜单", "食谱", "菜式",
            // 推荐相关
            "推荐", "建议", "选什么", "吃什么", "点什么",
            // 订单相关
            "订单", "下单", "点餐", "外卖", "催单", "配送", "送餐",
            // 商家相关
            "商家", "餐厅", "店铺", "饭店", "餐馆",
            // 营养相关
            "营养", "热量", "卡路里", "蛋白质", "脂肪", "碳水", "维生素",
            // 用户偏好
            "偏好", "忌口", "过敏", "口味", "辣度",
            // 位置相关
            "附近", "位置", "距离", "地址", "哪里有",
            // 时间相关
            "早餐", "午餐", "晚餐", "夜宵", "三餐", "时段",
            // 健康目标
            "减肥", "增肌", "健康目标", "饮食记录",
            // 评价相关
            "评价", "评分", "好评", "差评"
    );

    /**
     * 短消息阈值（字符数）
     * 低于此阈值且不含业务关键词的消息视为简单对话
     */
    private static final int SHORT_MESSAGE_THRESHOLD = 6;

    private final ChatModel aiModel;

    public IntentClassifier(@Qualifier("aiModel") ChatModel aiModel) {
        this.aiModel = aiModel;
    }

    /**
     * 分类用户消息的意图
     *
     * @param message 用户消息
     * @return 意图类型
     */
    public IntentType classify(String message) {
        if (message == null || message.isBlank()) {
            return IntentType.BUSINESS;
        }

        String trimmed = message.trim();

        // 第1级：规则匹配
        IntentType ruleResult = classifyByRules(trimmed);
        if (ruleResult != null) {
            log.info("[意图分类] 规则匹配结果: {} (消息: \"{}\")", ruleResult, truncatedMsg(trimmed));
            return ruleResult;
        }

        // 第2级：LLM 快速分类
        IntentType llmResult = classifyByLLM(trimmed);
        log.info("[意图分类] LLM分类结果: {} (消息: \"{}\")", llmResult, truncatedMsg(trimmed));
        return llmResult;
    }

    /**
     * 第1级：基于规则的意图分类
     *
     * @return null 表示规则未命中，需要走 LLM 分类
     */
    private IntentType classifyByRules(String message) {
        // 优先检查业务关键词（安全优先）
        if (containsBusinessKeyword(message)) {
            return IntentType.BUSINESS;
        }

        // 检查简单对话模式
        for (Pattern pattern : SIMPLE_CHAT_PATTERNS) {
            if (pattern.matcher(message).find()) {
                return IntentType.SIMPLE_CHAT;
            }
        }

        // 短消息兜底：很长的消息不太可能是简单对话
        if (message.length() <= SHORT_MESSAGE_THRESHOLD) {
            return IntentType.SIMPLE_CHAT;
        }

        // 规则未命中
        return null;
    }

    /**
     * 检查消息是否包含业务关键词
     */
    private boolean containsBusinessKeyword(String message) {
        String lower = message.toLowerCase();
        for (String keyword : BUSINESS_KEYWORDS) {
            if (lower.contains(keyword)) {
                log.debug("[意图分类] 命中业务关键词: '{}' (消息中位置: {})", keyword, lower.indexOf(keyword));
                return true;
            }
        }
        return false;
    }

    /**
     * 第2级：LLM 快速分类
     *
     * 使用极简 prompt，temperature=0，maxToken 极小，确保快速响应
     * 默认回退为 BUSINESS（安全兜底）
     */
    private IntentType classifyByLLM(String message) {
        try {
            String prompt = "判断以下用户消息是「简单对话」（闲聊/打招呼/问候/自我介绍/感谢/道别）" +
                    "还是「业务请求」（涉及菜品/推荐/订单/商家/营养/饮食/健康/位置等）。\n\n" +
                    "只回答一个词：CONVERSATION 或 BUSINESS\n\n" +
                    "用户消息：" + message;

            String response = aiModel.chat(prompt).aiMessage().text();
            log.debug("[意图分类] LLM原始响应: {}", response);

            if (response != null) {
                String cleaned = response.trim().toUpperCase();
                if (cleaned.contains("CONVERSATION")) {
                    return IntentType.SIMPLE_CHAT;
                }
                if (cleaned.contains("BUSINESS")) {
                    return IntentType.BUSINESS;
                }
            }

            // 无法解析时安全兜底
            return IntentType.BUSINESS;
        } catch (Exception e) {
            log.warn("[意图分类] LLM分类失败，安全兜底为BUSINESS: {}", e.getMessage());
            return IntentType.BUSINESS;
        }
    }

    /**
     * 截断消息用于日志输出
     */
    private String truncatedMsg(String message) {
        if (message.length() <= 30) {
            return message;
        }
        return message.substring(0, 30) + "...";
    }

    /**
     * 意图类型枚举
     */
    public enum IntentType {
        /** 简单对话（闲聊/问候等） */
        SIMPLE_CHAT,
        /** 业务意图（需要走 SupervisorAgent） */
        BUSINESS
    }
}
