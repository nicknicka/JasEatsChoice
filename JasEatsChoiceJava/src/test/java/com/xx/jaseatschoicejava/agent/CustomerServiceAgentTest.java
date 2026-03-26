package com.xx.jaseatschoicejava.agent;

import com.xx.jaseatschoicejava.JasEatsChoiceJavaApplication;
import com.xx.jaseatschoicejava.agent.agents.CustomerServiceAgent;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 客服助手Agent测试
 *
 * 测试场景：
 * 1. 无userId时的基础对话
 * 2. 饮食健康咨询
 * 3. 个性化服务引导
 *
 * @author Claude
 * @since 2026-03-26
 */
@SpringBootTest(classes = JasEatsChoiceJavaApplication.class)
@ActiveProfiles("test")
public class CustomerServiceAgentTest {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceAgentTest.class);

    @Autowired
    private CustomerServiceAgent customerServiceAgent;

    @Test
    public void testBasicConversation() {
        log.info("=== 测试基础对话功能 ===");

        String message = "你好，请问佳食宜选是什么平台？";
        String response = customerServiceAgent.chat(message);

        log.info("用户问题: {}", message);
        log.info("客服回复: {}", response);

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertTrue(response.contains("佳食宜选") || response.contains("平台"));
    }

    @Test
    public void testNutritionConsultation() {
        log.info("=== 测试营养咨询功能 ===");

        String message = "减肥期间应该怎么吃？";
        String response = customerServiceAgent.chat(message);

        log.info("用户问题: {}", message);
        log.info("客服回复: {}", response);

        assertNotNull(response);
        assertFalse(response.isEmpty());
        // 验证回复包含减肥或饮食相关内容
        assertTrue(response.length() > 20);
    }

    @Test
    public void testPersonalizationGuidance() {
        log.info("=== 测试个性化服务引导 ===");

        String message = "我想推荐一些适合我的菜品";
        String response = customerServiceAgent.chat(message);

        log.info("用户问题: {}", message);
        log.info("客服回复: {}", response);

        assertNotNull(response);
        assertFalse(response.isEmpty());
        // 验证客服会引导用户开启个性化服务
        assertTrue(response.contains("个性化") ||
                  response.contains("服务") ||
                  response.contains("开启") ||
                  response.contains("推荐"));
    }

    @Test
    public void testOrderQueryGuidance() {
        log.info("=== 测试订单查询引导 ===");

        String message = "我的订单在哪里？";
        String response = customerServiceAgent.chat(message);

        log.info("用户问题: {}", message);
        log.info("客服回复: {}", response);

        assertNotNull(response);
        assertFalse(response.isEmpty());
        // 验证客服会引导开启个性化服务
        assertTrue(response.contains("个性化") ||
                  response.contains("服务") ||
                  response.contains("开启") ||
                  response.contains("订单"));
    }

    @Test
    public void testPlatformIntroduction() {
        log.info("=== 测试平台功能介绍 ===");

        String message = "你们平台有什么功能？";
        String response = customerServiceAgent.chat(message);

        log.info("用户问题: {}", message);
        log.info("客服回复: {}", response);

        assertNotNull(response);
        assertFalse(response.isEmpty());
        // 验证回复包含平台功能相关内容
        assertTrue(response.length() > 50);
    }

    @Test
    public void testHealthAdvice() {
        log.info("=== 测试健康建议功能 ===");

        String message = "有什么健康的早餐推荐吗？";
        String response = customerServiceAgent.chat(message);

        log.info("用户问题: {}", message);
        log.info("客服回复: {}", response);

        assertNotNull(response);
        assertFalse(response.isEmpty());
        // 验证回复包含健康或早餐相关内容
        assertTrue(response.length() > 30);
    }

    @Test
    public void testMultipleConversationRounds() {
        log.info("=== 测试多轮对话 ===");

        String[] messages = {
            "你好",
            "卡路里是什么？",
            "怎么计算每日卡路里摄入？",
            "我想开启个性化服务"
        };

        for (String message : messages) {
            String response = customerServiceAgent.chat(message);
            log.info("问题: {} => 回复: {}", message, response);
            assertNotNull(response);
            assertFalse(response.isEmpty());

            // 添加延迟，避免请求过快
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
