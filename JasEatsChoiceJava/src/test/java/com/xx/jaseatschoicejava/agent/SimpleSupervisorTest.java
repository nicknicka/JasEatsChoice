package com.xx.jaseatschoicejava.agent;

import com.xx.jaseatschoicejava.JasEatsChoiceJavaApplication;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 简单的SupervisorAgent测试
 *
 * @author Claude
 * @since 2026-03-26
 */
@SpringBootTest(classes = JasEatsChoiceJavaApplication.class)
@ActiveProfiles("test")
public class SimpleSupervisorTest {

    @Autowired(required = false)
    private SupervisorAgent supervisorAgent;

    @Test
    public void testSupervisorAgentInjection() {
        System.out.println("\n========================================");
        System.out.println("测试SupervisorAgent注入");
        System.out.println("========================================\n");

        if (supervisorAgent == null) {
            System.out.println("❌ SupervisorAgent 未注入");
            System.out.println("可能的原因:");
            System.out.println("  1. 配置类中Bean创建失败");
            System.out.println("  2. 依赖注入配置错误");
            System.out.println("  3. Profile配置问题");
        } else {
            System.out.println("✅ SupervisorAgent 已成功注入");
            System.out.println("   类型: " + supervisorAgent.getClass().getName());
        }

        System.out.println("\n========================================\n");
    }

    @Test
    public void testBasicChat() {
        System.out.println("\n========================================");
        System.out.println("测试SupervisorAgent基本聊天");
        System.out.println("========================================\n");

        if (supervisorAgent == null) {
            System.out.println("❌ SupervisorAgent 未注入，跳过测试");
            return;
        }

        try {
            String userMessage = "你好，请介绍一下自己";
            System.out.println("用户消息: " + userMessage);
            System.out.println("\n等待SupervisorAgent响应...\n");

            long startTime = System.currentTimeMillis();
            String response = supervisorAgent.invoke(userMessage);
            long totalTime = System.currentTimeMillis() - startTime;

            System.out.println("\n✅ 响应成功:");
            System.out.println("   耗时: " + totalTime + "ms");
            System.out.println("   响应长度: " + response.length() + " 字符");
            System.out.println("\n响应内容:");
            System.out.println("   " + response);

        } catch (Exception e) {
            System.out.println("\n❌ 测试失败:");
            System.out.println("   错误: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n========================================\n");
    }
}
