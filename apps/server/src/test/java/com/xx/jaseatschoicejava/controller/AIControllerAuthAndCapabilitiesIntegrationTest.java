package com.xx.jaseatschoicejava.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

import com.xx.jaseatschoicejava.JasEatsChoiceJavaApplication;
import com.xx.jaseatschoicejava.config.JwtConfig;
import com.xx.jaseatschoicejava.util.JwtUtil;

import io.jsonwebtoken.Jwts;

/**
 * 食谱优化端点的真实 Security 过滤器链：匿名与伪造身份 401、缺少 userId claim 的合法令牌 403、
 * 配额超限 429、capabilities 匿名可读。需要 MySQL/Redis/Qdrant 才能启动上下文。
 */
@SpringBootTest(
    classes = JasEatsChoiceJavaApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    // 菜品识别配额置 0：429 用例无需耗尽真实额度，也不触发模型调用。
    properties = "ai-quota.dish-recognition.limit=0"
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AIControllerAuthAndCapabilitiesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtConfig jwtConfig;

    @Test
    void anonymousRecipeRequestReturns401() throws Exception {
        mockMvc.perform(post("/v1/ai/recipe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipe\":\"番茄炒蛋\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void forgedXUserIdWithoutTokenStillReturns401() throws Exception {
        mockMvc.perform(post("/v1/ai/recipe")
                .header("X-User-Id", "forged-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipe\":\"番茄炒蛋\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void validTokenWithoutUserIdClaimReturns403() throws Exception {
        String token = Jwts.builder()
            .subject("13000000000")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 3_600_000L))
            .signWith(jwtConfig.getSigningKey())
            .compact();

        mockMvc.perform(post("/v1/ai/recipe")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipe\":\"番茄炒蛋\"}"))
            .andExpect(status().isForbidden());
    }

    /**
     * 非法 goal 走完 Security -> Controller -> GlobalExceptionHandler 全链路：必须是真实 HTTP 400，
     * 而不是 200 + body code；且在配额与模型调用之前就返回。
     */
    @Test
    void illegalRecipeGoalReturnsHttp400() throws Exception {
        String token = jwtUtil.generateToken("it492-user", "13000000000");

        mockMvc.perform(post("/v1/ai/recipe")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipe\":\"番茄炒蛋\",\"goal\":\"keto\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("400"))
            .andExpect(jsonPath("$.message").value(containsString("不支持的优化目标")));
    }

    /**
     * capabilities 是 AI 页的能力探测，只在登录后的 /user/home/ai 调用（桌面端路由守卫
     * 对 /user/** 强制登录），因此 #576 起与其余 /v1/ai/** 一样要求登录。
     * 这里固定「带令牌读到真实能力字段」这条契约。
     */
    @Test
    void capabilitiesRequireAuthenticationAndExposeRecipeStreamTimeout() throws Exception {
        // 匿名 -> 401（默认拒绝覆盖）
        mockMvc.perform(get("/v1/ai/capabilities"))
            .andExpect(status().isUnauthorized());

        // 带令牌 -> 200 + 能力字段
        String token = jwtUtil.generateToken("it216-user", "13000000000");
        mockMvc.perform(get("/v1/ai/capabilities")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.dishRecognitionTimeoutMs", greaterThan(0)))
            .andExpect(jsonPath("$.data.recipeStreamTimeoutMs", greaterThan(0)));
    }

    /**
     * SSE 端点：SseEmitter 完成时容器会以 ASYNC 派发再走一遍过滤器链。
     * 首次 REQUEST 派发已经鉴权，ASYNC 续派发不得再被拦截；否则响应已提交却抛
     * AccessDenied，chunked 响应收不到终止块，客户端会一直挂到自身超时。
     */
    @Test
    void recipeStreamAsyncDispatchIsNotRejectedBySecurity() throws Exception {
        String token = jwtUtil.generateToken("it216-user", "13000000000");

        MvcResult result = mockMvc.perform(post("/v1/ai/recipe/stream")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipe\":\"   \"}"))
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk());
    }

    /**
     * 配额超限走完 Security -> Controller -> AiQuotaService -> GlobalExceptionHandler 全链路。
     * dish-recognize-url 在调用模型之前先扣配额，因此本用例不依赖可用模型。
     */
    @Test
    void dishRecognizeUrlReturns429WhenQuotaExceeded() throws Exception {
        String token = jwtUtil.generateToken("it216-quota-user", "13000000000");

        mockMvc.perform(post("/v1/ai/dish-recognize-url")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"imageUrl\":\"https://example.com/dish.jpg\"}"))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.code").value("429"))
            .andExpect(jsonPath("$.message").value("请求过于频繁，请稍后再试"));
    }

    /**
     * 保留 JwtUtil 依赖，确保集成测试确实运行在带签名配置的过滤器链上。
     * 合法 userId 令牌的 401/403 分支由上面的无 claim 用例覆盖，避免触发真实模型与配额。
     */
    @Test
    void validTokenWithUserIdIsAuthenticatedPastSecurityFilter() throws Exception {
        String token = jwtUtil.generateToken("it216-user", "13000000000");

        mockMvc.perform(post("/v1/ai/recipe")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipe\":\"   \"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("400"));
    }
}
