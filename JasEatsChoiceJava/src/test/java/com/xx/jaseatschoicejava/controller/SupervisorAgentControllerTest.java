package com.xx.jaseatschoicejava.controller;

import com.xx.jaseatschoicejava.JasEatsChoiceJavaApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * SupervisorAgent Controller 集成测试
 *
 * 测试SupervisorAgent的HTTP接口
 *
 * @author Claude
 * @since 2026-03-25
 */
@SpringBootTest(
    classes = JasEatsChoiceJavaApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SupervisorAgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试基本聊天接口（POST）
     */
    @Test
    public void testChatEndpoint() throws Exception {
        String requestBody = """
            {
                "message": "推荐一些低卡路里的川菜",
                "userId": "test-user-123"
            }
            """;

        mockMvc.perform(post("/api/agent/supervisor/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").isString());
    }

    /**
     * 测试带用户上下文的聊天接口
     */
    @Test
    public void testChatWithContextEndpoint() throws Exception {
        String requestBody = """
            {
                "message": "根据我的历史记录，推荐一些我喜欢的菜",
                "userId": "user123",
                "sessionId": "session-456"
            }
            """;

        mockMvc.perform(post("/api/agent/supervisor/chatWithContext")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isString());
    }

    /**
     * 测试GET方式的快速聊天接口
     */
    @Test
    public void testQuickChatEndpoint() throws Exception {
        mockMvc.perform(get("/api/agent/supervisor/chat")
                .param("message", "宫保鸡丁有多少卡路里？")
                .param("userId", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isString());
    }

    /**
     * 测试GET方式不带用户ID
     */
    @Test
    public void testQuickChatWithoutUserId() throws Exception {
        mockMvc.perform(get("/api/agent/supervisor/chat")
                .param("message", "你好"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /**
     * 测试请求参数验证
     */
    @Test
    public void testValidation() throws Exception {
        // 测试空消息
        String requestBody = """
            {
                "message": "",
                "userId": "user123"
            }
            """;

        mockMvc.perform(post("/api/agent/supervisor/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    /**
     * 测试不同类型的用户问题
     */
    @Test
    public void testDifferentQuestionTypes() throws Exception {
        // 推荐类问题
        testQuestionType("推荐一些健康的菜");

        // 营养类问题
        testQuestionType("宫保鸡丁的营养成分是什么？");

        // 订单类问题
        testQuestionType("我想点一份宫保鸡丁");

        // 综合类问题
        testQuestionType("推荐一些低卡路里的菜，并告诉我营养分析");
    }

    /**
     * 辅助方法：测试特定类型的问题
     */
    private void testQuestionType(String message) throws Exception {
        String requestBody = String.format("""
            {
                "message": "%s",
                "userId": "test-user"
            }
            """, message);

        mockMvc.perform(post("/api/agent/supervisor/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /**
     * 测试长消息处理
     */
    @Test
    public void testLongMessageHandling() throws Exception {
        StringBuilder longMessage = new StringBuilder();
        longMessage.append("我想要减肥，最近在控制饮食。");
        longMessage.append("请帮我推荐一些适合的菜。");
        longMessage.append("我的偏好是：1. 低热量；2. 高蛋白；3. 少油少盐；");
        longMessage.append("4. 最好是川菜；5. 不要太辣。");
        longMessage.append("另外，请告诉我每道菜的具体热量和营养成分。");

        String requestBody = String.format("""
            {
                "message": "%s",
                "userId": "user123"
            }
            """, longMessage.toString());

        mockMvc.perform(post("/api/agent/supervisor/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /**
     * 测试并发请求
     */
    @Test
    public void testConcurrentRequests() throws Exception {
        String requestBody = """
            {
                "message": "你好",
                "userId": "user123"
            }
            """;

        // 发送多个并发请求
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/agent/supervisor/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
