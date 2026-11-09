package com.xx.jaseatschoicejava.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.JasEatsChoiceJavaApplication;
import com.xx.jaseatschoicejava.util.JwtUtil;

/**
 * fake-ai 主测试接缝的 HTTP 级集成测试：真实 Spring Security 过滤器链 + 真实模型客户端，
 * 打到仓库内假服务（tests/fake-ai/server.py）。覆盖视觉真实 MIME 与缓存命中、优惠目标进入
 * Prompt、SSE 事件序列、未登录 401。需要 MySQL/Redis/Qdrant 才能启动上下文，且本地 4011
 * 端口跑着假服务。
 *
 * <p>上游调用事实通过假服务的 GET /_introspect 断言，避免只看后端返回值而漏掉“Prompt 未带目标码/
 * 图片 MIME 写死”的回归。每个用例开始前先 POST /_reset 隔离计数。
 */
@SpringBootTest(
    classes = JasEatsChoiceJavaApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "ai.base-url=http://127.0.0.1:4011/v1",
        "ai.api-key=test-key",
        "ai.model=fake-model",
        "ai.vision-model=fake-model",
        "ai.vision-verified=true",
        "ai-quota.dish-recognition.limit=1000",
        "ai-quota.recipe.limit=1000",
        // 共享 .env 把上传目录指到容器内 /data/uploads，本机不可写；覆盖到模块内 target
        "file.upload.path=target/test-uploads/"
    }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AIControllerFakeSeamIntegrationTest {

    private static final String FAKE_BASE = "http://127.0.0.1:4011";

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    void resetFakeService() throws Exception {
        HttpResponse<String> response = HTTP.send(
            HttpRequest.newBuilder(URI.create(FAKE_BASE + "/_reset")).POST(HttpRequest.BodyPublishers.noBody()).build(),
            HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "重置 fake-ai 计数失败：" + response.body());
    }

    /**
     * 真实 PNG 魔数（非客户端声明的 image/jpeg）必须原样传给上游，且同图二次上传走缓存不再调模型。
     */
    @Test
    void visionUsesRealMimeAndCachesRepeatUpload() throws Exception {
        byte[] png = realPngBytes();
        MockMultipartFile image = new MockMultipartFile("image", "dish.png", "image/png", png);
        String token = userToken("it495-vision");

        mockMvc.perform(multipart("/v1/ai/dish-recognize").file(image)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.isDish").value(true))
            .andExpect(jsonPath("$.data.name").exists())
            .andExpect(jsonPath("$.data.calories").exists())
            .andExpect(jsonPath("$.data.protein").exists())
            .andExpect(jsonPath("$.data.fat").exists())
            .andExpect(jsonPath("$.data.carbs").exists())
            .andExpect(jsonPath("$.data.difficulty").exists())
            .andExpect(jsonPath("$.data.preparationTime").exists())
            .andExpect(jsonPath("$.data.ingredients").isArray())
            .andExpect(jsonPath("$.data.tags").isArray())
            .andExpect(jsonPath("$.data.confidence").exists())
            .andExpect(jsonPath("$.data.nutritionScore").exists())
            .andExpect(jsonPath("$.data.nutritionEstimated").exists())
            .andExpect(jsonPath("$.data.imageUrl").exists());

        JsonNode first = introspect();
        assertEquals(1, first.get("imageRequests").asInt(), "首传应触发一次模型视觉调用");
        assertEquals("image/png", first.get("lastImageMime").asText(), "上传 MIME 必须按真实魔数判定");

        mockMvc.perform(multipart("/v1/ai/dish-recognize").file(image)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        JsonNode second = introspect();
        assertEquals(1, second.get("imageRequests").asInt(), "同图二次上传应命中缓存，不再调模型");
    }

    /**
     * 优化目标必须真正进入发给模型的 Prompt，而不是只在后端本地回填。
     */
    @Test
    void recipeGoalReachesUpstreamPrompt() throws Exception {
        String token = userToken("it495-recipe");

        mockMvc.perform(post("/v1/ai/recipe")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipe\":\"番茄炒蛋：鸡蛋2个、番茄2个\",\"goal\":\"fat_loss\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.goal").value("fat_loss"));

        JsonNode state = introspect();
        assertEquals("fat_loss", state.get("lastRecipeGoal").asText(),
            "Prompt 未把 fat_loss 目标码传给上游");
    }

    /**
     * SSE 必须按 progress -> token -> result -> done 的顺序产出，result 带目标码、done 带真实耗时。
     */
    @Test
    void recipeStreamEmitsOrderedEventsWithGoalAndElapsed() throws Exception {
        String token = userToken("it495-stream");

        MvcResult result = mockMvc.perform(post("/v1/ai/recipe/stream")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipe\":\"鸡胸肉沙拉\",\"goal\":\"muscle_gain\"}"))
            .andExpect(request().asyncStarted())
            .andReturn();

        MvcResult dispatched = mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk())
            .andReturn();

        String body = dispatched.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<String> eventNames = parseEventNames(body);
        assertTrue(eventNames.contains("progress"), "SSE 缺少 progress 事件：" + eventNames);
        assertTrue(eventNames.contains("token"), "SSE 缺少 token 事件：" + eventNames);
        assertTrue(eventNames.contains("result"), "SSE 缺少 result 事件：" + eventNames);
        assertTrue(eventNames.contains("done"), "SSE 缺少 done 事件：" + eventNames);
        assertTrue(eventNames.indexOf("progress") < eventNames.indexOf("token"), "progress 应先于 token");
        assertTrue(eventNames.indexOf("token") < eventNames.indexOf("result"), "token 应先于 result");
        assertTrue(eventNames.indexOf("result") < eventNames.indexOf("done"), "result 应先于 done");

        JsonNode resultData = parseEventData(body, "result").path("result");
        assertEquals("muscle_gain", resultData.path("goal").asText(), "result 载荷未回显目标码");

        long elapsedMs = parseEventData(body, "done").path("elapsedMs").asLong();
        assertTrue(elapsedMs > 0, "done 载荷的 elapsedMs 应大于 0，实际=" + elapsedMs);
    }

    /**
     * /v1/ai/* 端点要求 Bearer token；匿名请求必须在 Security 过滤器链被拒。
     */
    @Test
    void anonymousRecipeRequestReturns401() throws Exception {
        mockMvc.perform(post("/v1/ai/recipe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipe\":\"番茄炒蛋\",\"goal\":\"balanced\"}"))
            .andExpect(status().isUnauthorized());
    }

    private String userToken(String userId) {
        return jwtUtil.generateToken(userId, "13000000000");
    }

    /**
     * PNG 魔数 + 足量填充；随机填充让内容哈希每次运行都不同，避免命中上一次运行残留的 Redis 缓存，
     * 从而稳定断言“首传 imageRequests==1、同图二传仍为 1”。
     */
    private static byte[] realPngBytes() {
        byte[] padding = new byte[64];
        long nonce = System.nanoTime();
        for (int i = 0; i < padding.length; i++) {
            padding[i] = (byte) ((nonce >>> (i % 8 * 8)) ^ (i * 31));
        }
        byte[] header = { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 13 };
        byte[] bytes = new byte[header.length + padding.length];
        System.arraycopy(header, 0, bytes, 0, header.length);
        System.arraycopy(padding, 0, bytes, header.length, padding.length);
        return bytes;
    }

    private static JsonNode introspect() throws Exception {
        HttpResponse<String> response = HTTP.send(
            HttpRequest.newBuilder(URI.create(FAKE_BASE + "/_introspect")).GET().build(),
            HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "读取 fake-ai 计数失败：" + response.body());
        return JSON.readTree(response.body());
    }

    private static List<String> parseEventNames(String body) {
        List<String> names = new ArrayList<>();
        for (String block : body.split("\n\n")) {
            for (String line : block.split("\n")) {
                if (line.startsWith("event:")) {
                    names.add(line.substring("event:".length()).trim());
                }
            }
        }
        return names;
    }

    private static JsonNode parseEventData(String body, String eventName) throws Exception {
        for (String block : body.split("\n\n")) {
            List<String> lines = List.of(block.split("\n"));
            boolean matches = lines.stream().anyMatch(line -> line.equals("event:" + eventName));
            if (!matches) {
                continue;
            }
            StringBuilder data = new StringBuilder();
            for (String line : lines) {
                if (line.startsWith("data:")) {
                    data.append(line.substring("data:".length()).trim());
                }
            }
            assertTrue(data.length() > 0, "事件 " + eventName + " 没有 data 载荷");
            return JSON.readTree(data.toString());
        }
        throw new AssertionError("未找到事件：" + eventName + "，响应=" + body);
    }
}
