package com.xx.jaseatschoicejava.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.JasEatsChoiceJavaApplication;
import com.xx.jaseatschoicejava.util.JwtUtil;

/**
 * 视觉能力运行时降级的 HTTP 级验收：模型真的拒绝图片时给稳定能力错误码并立刻关掉入口；
 * 服务端/网络故障则保持泛化失败、不得误判成「不支持视觉」。
 *
 * <p>降级会改动进程内激活配置，所以本类跑完用 {@link DirtiesContext} 丢弃上下文，避免污染其它用例。
 * 两个用例有先后依赖（降级是单向的），用 {@link Order} 固定顺序。
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
        "file.upload.path=target/test-uploads/"
    }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AIControllerVisionDowngradeIntegrationTest {

    private static final String FAKE_BASE = "http://127.0.0.1:4011";
    private static final String UNSUPPORTED_MARKER = "FAKE_VISION_UNSUPPORTED";
    private static final String SERVER_ERROR_MARKER = "FAKE_VISION_ERROR";

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
     * 服务端 5xx 属于上游故障，不能判定为「不支持视觉」：仍是泛化失败，且入口保持开放。
     */
    @Test
    @Order(1)
    void visionServerErrorKeepsGenericFailureAndKeepsVisionEnabled() throws Exception {
        String token = jwtUtil.generateToken("it494-server-error", "13000000000");

        mockMvc.perform(multipart("/v1/ai/dish-recognize")
                .file(pngWithMarker(SERVER_ERROR_MARKER))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("500"));

        mockMvc.perform(get("/v1/ai/capabilities")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.vision").value(true));
    }

    /**
     * 模型 4xx 拒绝图片：返回稳定能力错误码（非泛化 500），capabilities.vision 立刻变 false，
     * 后续识别被入口门控挡住且不再调用模型。
     */
    @Test
    @Order(2)
    void visionRejectedImageReturnsStableCodeAndClosesTheGate() throws Exception {
        String token = jwtUtil.generateToken("it494-downgrade", "13000000000");

        mockMvc.perform(multipart("/v1/ai/dish-recognize")
                .file(pngWithMarker(UNSUPPORTED_MARKER))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("AI_VISION_UNSUPPORTED"))
            .andExpect(jsonPath("$.message").value("当前视觉模型不支持图片输入，菜品识别暂不可用"));

        mockMvc.perform(get("/v1/ai/capabilities")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.vision").value(false));

        int callsAfterDowngrade = introspect().get("imageRequests").asInt();
        mockMvc.perform(multipart("/v1/ai/dish-recognize")
                .file(pngWithMarker("another-image"))
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("AI_VISION_NOT_CONFIGURED"));

        assertEquals(callsAfterDowngrade, introspect().get("imageRequests").asInt(),
            "降级后入口应直接拦截，不再调用模型");
    }

    private static MockMultipartFile pngWithMarker(String marker) {
        byte[] header = pngHeader(320, 200);
        byte[] tail = marker.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        byte[] bytes = new byte[header.length + tail.length];
        System.arraycopy(header, 0, bytes, 0, header.length);
        System.arraycopy(tail, 0, bytes, header.length, tail.length);
        return new MockMultipartFile("image", "dish.png", "image/png", bytes);
    }

    private static byte[] pngHeader(int width, int height) {
        byte[] bytes = new byte[24];
        byte[] signature = { (byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10 };
        System.arraycopy(signature, 0, bytes, 0, signature.length);
        bytes[11] = 13;
        bytes[12] = 'I';
        bytes[13] = 'H';
        bytes[14] = 'D';
        bytes[15] = 'R';
        bytes[16] = (byte) (width >>> 24);
        bytes[17] = (byte) (width >>> 16);
        bytes[18] = (byte) (width >>> 8);
        bytes[19] = (byte) width;
        bytes[20] = (byte) (height >>> 24);
        bytes[21] = (byte) (height >>> 16);
        bytes[22] = (byte) (height >>> 8);
        bytes[23] = (byte) height;
        return bytes;
    }

    private static JsonNode introspect() throws Exception {
        HttpResponse<String> response = HTTP.send(
            HttpRequest.newBuilder(URI.create(FAKE_BASE + "/_introspect")).GET().build(),
            HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "读取 fake-ai 计数失败：" + response.body());
        return JSON.readTree(response.body());
    }
}
