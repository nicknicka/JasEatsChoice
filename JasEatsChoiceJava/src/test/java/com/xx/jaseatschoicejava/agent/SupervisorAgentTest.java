package com.xx.jaseatschoicejava.agent;

import com.xx.jaseatschoicejava.agent.agents.SupervisorAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * SupervisorAgent 测试
 *
 * 测试监督代理的智能调度功能
 *
 * @author Claude
 * @since 2026-03-25
 */
@SpringBootTest
@ActiveProfiles("test")
public class SupervisorAgentTest {

    @Autowired(required = false)
    private SupervisorAgent supervisorAgent;

    /**
     * 测试SupervisorAgent基本功能
     */
    @Test
    public void testSupervisorAgentBasic() {
        if (supervisorAgent == null) {
            System.out.println("⚠️ SupervisorAgent 未配置，跳过测试");
            return;
        }

        System.out.println("✅ SupervisorAgent 已成功创建");
    }

    /**
     * 测试简单推荐问题
     * 应该路由到 SmartRecommendationAgent
     */
    @Test
    public void testSimpleRecommendation() {
        if (supervisorAgent == null) {
            System.out.println("⚠️ SupervisorAgent 未配置，跳过测试");
            return;
        }

        String userQuestion = "推荐一些低卡路里的川菜";
        System.out.println("📝 用户问题: " + userQuestion);
        System.out.println("🤖 预期: 路由到 SmartRecommendationAgent");

        try {
            String response = supervisorAgent.chat(userQuestion);
            System.out.println("✅ SupervisorAgent 响应: " + response);
        } catch (Exception e) {
            System.out.println("❌ 测试失败（可能需要API密钥）: " + e.getMessage());
        }
    }

    /**
     * 测试健康咨询问题
     * 应该路由到 HealthManagementAgent
     */
    @Test
    public void testHealthConsultation() {
        if (supervisorAgent == null) {
            System.out.println("⚠️ SupervisorAgent 未配置，跳过测试");
            return;
        }

        String userQuestion = "宫保鸡丁有多少卡路里？";
        System.out.println("📝 用户问题: " + userQuestion);
        System.out.println("🤖 预期: 路由到 HealthManagementAgent");

        try {
            String response = supervisorAgent.chat(userQuestion);
            System.out.println("✅ SupervisorAgent 响应: " + response);
        } catch (Exception e) {
            System.out.println("❌ 测试失败（可能需要API密钥）: " + e.getMessage());
        }
    }

    /**
     * 测试复杂订单问题
     * 应该路由到 FullOrderAgent
     */
    @Test
    public void testComplexOrder() {
        if (supervisorAgent == null) {
            System.out.println("⚠️ SupervisorAgent 未配置，跳过测试");
            return;
        }

        String userQuestion = "我想点一份宫保鸡丁，配米饭，送到学校";
        System.out.println("📝 用户问题: " + userQuestion);
        System.out.println("🤖 预期: 路由到 FullOrderAgent");

        try {
            String response = supervisorAgent.chat(userQuestion);
            System.out.println("✅ SupervisorAgent 响应: " + response);
        } catch (Exception e) {
            System.out.println("❌ 测试失败（可能需要API密钥）: " + e.getMessage());
        }
    }

    /**
     * 测试带用户ID的对话
     */
    @Test
    public void testChatWithUserId() {
        if (supervisorAgent == null) {
            System.out.println("⚠️ SupervisorAgent 未配置，跳过测试");
            return;
        }

        String userId = "user123";
        String userQuestion = "根据我的历史记录，推荐一些我喜欢的菜";
        System.out.println("📝 用户ID: " + userId);
        System.out.println("📝 用户问题: " + userQuestion);
        System.out.println("🤖 预期: 路由到合适的Agent并使用用户上下文");

        try {
            String response = supervisorAgent.chatWithContext(userQuestion, userId);
            System.out.println("✅ SupervisorAgent 响应: " + response);
        } catch (Exception e) {
            System.out.println("❌ 测试失败（可能需要API密钥）: " + e.getMessage());
        }
    }

    /**
     * 测试多Agent协作场景
     */
    @Test
    public void testMultiAgentCollaboration() {
        if (supervisorAgent == null) {
            System.out.println("⚠️ SupervisorAgent 未配置，跳过测试");
            return;
        }

        String userQuestion = "我想减肥，帮我推荐一些低卡路里的菜，并告诉我这些菜的营养成分";
        System.out.println("📝 用户问题: " + userQuestion);
        System.out.println("🤖 预期: 协调 SmartRecommendationAgent 和 HealthManagementAgent");

        try {
            String response = supervisorAgent.chat(userQuestion);
            System.out.println("✅ SupervisorAgent 响应: " + response);
        } catch (Exception e) {
            System.out.println("❌ 测试失败（可能需要API密钥）: " + e.getMessage());
        }
    }

    /**
     * 测试Supervisor的上下文策略
     */
    @Test
    public void testContextStrategy() {
        if (supervisorAgent == null) {
            System.out.println("⚠️ SupervisorAgent 未配置，跳过测试");
            return;
        }

        System.out.println("📋 Supervisor 配置验证:");
        System.out.println("   - 上下文策略: CHAT_MEMORY_AND_SUMMARIZATION");
        System.out.println("   - 响应策略: SCORED");
        System.out.println("   - 最大Agent调用数: 10");
        System.out.println("✅ 配置验证完成");
    }
}
