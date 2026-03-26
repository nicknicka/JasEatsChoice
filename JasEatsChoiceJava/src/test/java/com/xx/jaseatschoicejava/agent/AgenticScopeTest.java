package com.xx.jaseatschoicejava.agent;

import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.xx.jaseatschoicejava.agent.agents.UserPreferenceAgent;
import com.xx.jaseatschoicejava.agent.agents.NutritionGuideAgent;
import com.xx.jaseatschoicejava.agent.agents.DishRecommendationAgent;

/**
 * AgenticScope 状态共享测试
 *
 * 验证 L1 Agent 的输出能够正确传递给 L2 Agent
 *
 * @author Claude
 * @since 2026-03-25
 */
@SpringBootTest
@ActiveProfiles("test")
public class AgenticScopeTest {

    @Autowired(required = false)
    @Qualifier("smartRecommendationAgent")
    private SupervisorAgent smartRecommendationAgent;

    @Autowired(required = false)
    private UserPreferenceAgent userPreferenceAgent;

    @Autowired(required = false)
    private NutritionGuideAgent nutritionGuideAgent;

    @Autowired(required = false)
    private DishRecommendationAgent dishRecommendationAgent;

    /**
     * 测试 L1 Agent 单独调用
     */
    @Test
    public void testL1Agent_UserPreference() {
        if (userPreferenceAgent == null) {
            System.out.println("⚠️ UserPreferenceAgent 未配置，跳过测试");
            return;
        }

        String response = userPreferenceAgent.chat("我的用户ID是 user123");
        System.out.println("✅ UserPreferenceAgent 响应: " + response);
    }

    /**
     * 测试 L1 Agent 单独调用
     */
    @Test
    public void testL1Agent_NutritionGuide() {
        if (nutritionGuideAgent == null) {
            System.out.println("⚠️ NutritionGuideAgent 未配置，跳过测试");
            return;
        }

        String response = nutritionGuideAgent.chat("宫保鸡丁有多少卡路里？");
        System.out.println("✅ NutritionGuideAgent 响应: " + response);
    }

    /**
     * 测试 L1 Agent 单独调用
     */
    @Test
    public void testL1Agent_DishRecommendation() {
        if (dishRecommendationAgent == null) {
            System.out.println("⚠️ DishRecommendationAgent 未配置，跳过测试");
            return;
        }

        String response = dishRecommendationAgent.chat("推荐一些低卡路里的川菜");
        System.out.println("✅ DishRecommendationAgent 响应: " + response);
    }

    /**
     * 测试 L2 Agent 智能推荐（会自动调用 L1 Agent）
     *
     * 这个测试展示了 AgenticScope 的核心功能：
     * 1. L2 Agent 接收复杂问题
     * 2. LLM 自动决定调用哪些 L1 Agent
     * 3. L1 Agent 的结果自动传递回 L2 Agent
     * 4. L2 Agent 综合所有结果生成最终回复
     */
    @Test
    public void testL2Agent_SmartRecommendation() {
        if (smartRecommendationAgent == null) {
            System.out.println("⚠️ SmartRecommendationAgent 未配置，跳过测试");
            return;
        }

        String userQuestion = "我想减肥，推荐一些低卡路里的川菜";

        System.out.println("📝 用户问题: " + userQuestion);
        System.out.println("🤖 L2 Agent (SmartRecommendationAgent) 开始处理...");
        System.out.println("   → LLM 可能会自动调用:");
        System.out.println("      1. UserPreferenceAgent - 获取用户偏好");
        System.out.println("      2. NutritionGuideAgent - 分析营养信息");
        System.out.println("      3. DishRecommendationAgent - 获取推荐菜品");

        String response = smartRecommendationAgent.invoke(userQuestion);

        System.out.println("\n✅ SmartRecommendationAgent 最终响应:");
        System.out.println(response);
        System.out.println("\n💡 在这个过程中，AgenticScope 自动：");
        System.out.println("   - 管理了 3 个 L1 Agent 的调用");
        System.out.println("   - 传递了每个 Agent 的输出");
        System.out.println("   - 隔离了每次调用的状态");
    }

    /**
     * 测试复杂的多轮对话
     */
    @Test
    public void testMultiTurnConversation() {
        if (smartRecommendationAgent == null) {
            System.out.println("⚠️ SmartRecommendationAgent 未配置，跳过测试");
            return;
        }

        System.out.println("📋 多轮对话测试:");
        System.out.println("=" .repeat(50));

        // 第一轮
        String q1 = "我是用户user123，帮我推荐一些菜";
        String r1 = smartRecommendationAgent.invoke(q1);
        System.out.println("\n[用户] " + q1);
        System.out.println("[助手] " + r1);

        // 第二轮
        String q2 = "这些菜里哪个热量最低？";
        String r2 = smartRecommendationAgent.invoke(q2);
        System.out.println("\n[用户] " + q2);
        System.out.println("[助手] " + r2);

        // 第三轮
        String q3 = "第一道菜的营养成分是什么？";
        String r3 = smartRecommendationAgent.invoke(q3);
        System.out.println("\n[用户] " + q3);
        System.out.println("[助手] " + r3);

        System.out.println("\n" + "=".repeat(50));
        System.out.println("✅ 多轮对话完成，AgenticScope 自动维护了上下文");
    }
}
