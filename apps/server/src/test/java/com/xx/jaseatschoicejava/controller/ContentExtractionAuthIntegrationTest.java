package com.xx.jaseatschoicejava.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.xx.jaseatschoicejava.JasEatsChoiceJavaApplication;
import com.xx.jaseatschoicejava.entity.ContentExtraction;
import com.xx.jaseatschoicejava.entity.ContentSource;
import com.xx.jaseatschoicejava.mapper.ContentExtractionMapper;
import com.xx.jaseatschoicejava.mapper.ContentSourceMapper;
import com.xx.jaseatschoicejava.mapper.RecipeMapper;
import com.xx.jaseatschoicejava.service.ContentExtractionService;
import com.xx.jaseatschoicejava.util.JwtUtil;

/**
 * 内容提取接口 × 真实 Spring Security 过滤器链：未登录 401、身份伪造无效、非法链接 400、
 * 跨用户越权被拒。需要 MySQL/Redis/Qdrant 才能启动上下文（由 scripts/ci-backend.sh 提供）。
 */
@SpringBootTest(
    classes = JasEatsChoiceJavaApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContentExtractionAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ContentSourceMapper contentSourceMapper;

    @Autowired
    private ContentExtractionMapper contentExtractionMapper;

    @Autowired
    private RecipeMapper recipeMapper;

    @Autowired
    private ContentExtractionService contentExtractionService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<String> createdSourceIds = new ArrayList<>();
    private final List<String> createdExtractionIds = new ArrayList<>();
    private final List<String> createdRecipeIds = new ArrayList<>();

    @AfterEach
    void cleanup() {
        createdRecipeIds.forEach(recipeMapper::deleteById);
        createdExtractionIds.forEach(contentExtractionMapper::deleteById);
        createdSourceIds.forEach(contentSourceMapper::deleteById);
        createdRecipeIds.clear();
        createdExtractionIds.clear();
        createdSourceIds.clear();
    }

    @Test
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/v1/content-extraction/sources"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void forgedHeaderWithoutTokenIsStillUnauthenticated() throws Exception {
        mockMvc.perform(get("/v1/content-extraction/sources").header("X-User-Id", "forged-user"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void forgedHeaderCannotChangeVerifiedIdentity() throws Exception {
        String userA = "it209-a-" + randomSuffix();
        String userB = "it209-b-" + randomSuffix();
        String sourceB = seedSource(userB);

        // 携带 A 的合法令牌 + 伪造的 B 身份请求头：服务端身份只能来自令牌
        mockMvc.perform(get("/v1/content-extraction/sources")
                .header("Authorization", bearer(userA))
                .header("X-User-Id", userB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isEmpty())
            .andExpect(jsonPath("$.data[?(@.id == '" + sourceB + "')]").isEmpty());
    }

    @Test
    void blankUrlReturns400AndDoesNotPersist() throws Exception {
        String user = "it209-blank-" + randomSuffix();

        mockMvc.perform(createSource(user, "{\"contentUrl\":\"\"}"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(createSource(user, "{\"contentUrl\":\"   \"}"))
            .andExpect(status().isBadRequest());

        assertTrue(contentSourceMapper.selectByUserId(user).isEmpty());
    }

    @Test
    void illegalAndTooLongUrlReturn400AndDoNotPersist() throws Exception {
        String user = "it209-format-" + randomSuffix();
        String tooLong = "https://8.8.8.8/" + "a".repeat(600);

        mockMvc.perform(createSource(user, "{\"contentUrl\":\"not a link\"}"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(createSource(user, "{\"contentUrl\":\"" + tooLong + "\"}"))
            .andExpect(status().isBadRequest());

        assertTrue(contentSourceMapper.selectByUserId(user).isEmpty());
    }

    @Test
    void internalUrlReturns400AndDoesNotPersist() throws Exception {
        String user = "it209-internal-" + randomSuffix();

        mockMvc.perform(createSource(user, "{\"contentUrl\":\"http://127.0.0.1/secret\"}"))
            .andExpect(status().isBadRequest());

        assertTrue(contentSourceMapper.selectByUserId(user).isEmpty());
    }

    @Test
    void invalidContentTypeReturns400AndDoesNotPersist() throws Exception {
        String user = "it211-type-" + randomSuffix();

        mockMvc.perform(createSource(user,
                "{\"contentUrl\":\"https://example.com/a\",\"contentType\":\"AUDIO\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("内容类型不合法"));

        assertTrue(contentSourceMapper.selectByUserId(user).isEmpty());
    }

    @Test
    void invalidReExtractContentTypeReturns400() throws Exception {
        String user = "it211-retype-" + randomSuffix();
        String sourceId = seedSource(user);

        mockMvc.perform(post("/v1/content-extraction/source/" + sourceId + "/re-extract")
                .header("Authorization", bearer(user))
                .param("contentType", "AUDIO"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("内容类型不合法"));
    }

    @Test
    void otherUsersCannotReadModifyDeleteReExtractPublishOrVerify() throws Exception {
        String userA = "it209-owner-" + randomSuffix();
        String userB = "it209-other-" + randomSuffix();
        String sourceB = seedSource(userB);
        String extractionB = seedExtraction(sourceB);
        String tokenA = bearer(userA);

        // 读
        mockMvc.perform(get("/v1/content-extraction/source/" + sourceB).header("Authorization", tokenA))
            .andExpect(jsonPath("$.success").value(false));

        // 删
        mockMvc.perform(delete("/v1/content-extraction/source/" + sourceB).header("Authorization", tokenA))
            .andExpect(jsonPath("$.success").value(false));
        assertNotNull(contentSourceMapper.selectById(sourceB), "越权删除不得生效");

        // 重新提取
        mockMvc.perform(post("/v1/content-extraction/source/" + sourceB + "/re-extract")
                .header("Authorization", tokenA))
            .andExpect(jsonPath("$.success").value(false));

        // 修改提取内容
        mockMvc.perform(put("/v1/content-extraction/extraction")
                .header("Authorization", tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"extractionId\":\"" + extractionB + "\",\"dishName\":\"越权改名\"}"))
            .andExpect(jsonPath("$.success").value(false));
        assertEquals(null, contentExtractionMapper.selectById(extractionB).getDishName(), "越权修改不得生效");

        // 发布
        mockMvc.perform(post("/v1/content-extraction/extraction/" + extractionB + "/publish")
                .header("Authorization", tokenA))
            .andExpect(jsonPath("$.success").value(false));
        assertEquals(Boolean.FALSE, contentExtractionMapper.selectById(extractionB).getIsPublished());

        // 验证
        mockMvc.perform(post("/v1/content-extraction/extraction/" + extractionB + "/verify")
                .header("Authorization", tokenA)
                .param("verified", "true")
                .param("score", "5"))
            .andExpect(jsonPath("$.success").value(false));
        assertEquals(Boolean.FALSE, contentExtractionMapper.selectById(extractionB).getIsVerified());
    }

    @Test
    void publishCreatesRealRecipeWithSourceAndMappedFields() throws Exception {
        String userId = numericUserId();
        String sourceId = seedSource(userId);
        String extractionId = seedExtraction(sourceId);

        ContentSource source = contentSourceMapper.selectById(sourceId);
        source.setPlatform("XIAOHONGSHU");
        source.setContentUrl("https://www.xiaohongshu.com/explore/it212-publish");
        contentSourceMapper.updateById(source);

        ContentExtraction extraction = contentExtractionMapper.selectById(extractionId);
        extraction.setDishName("番茄炒蛋");
        extraction.setDescription("酸甜开胃");
        extraction.setIngredients("[{\"name\":\"番茄\",\"amount\":\"2个\"},{\"name\":\"鸡蛋\",\"amount\":\"3个\"}]");
        extraction.setSteps("[{\"title\":\"切配\",\"description\":\"番茄切块，鸡蛋打散\"},{\"description\":\"热锅炒熟\"}]");
        extraction.setCookingTime(15);
        extraction.setCalories(180);
        contentExtractionMapper.updateById(extraction);

        String body = mockMvc.perform(post("/v1/content-extraction/extraction/" + extractionId + "/publish")
                .header("Authorization", bearer(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mealType\":\"dinner\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isNotEmpty())
            .andReturn().getResponse().getContentAsString();
        String recipeId = objectMapper.readTree(body).path("data").asText();
        createdRecipeIds.add(recipeId);

        mockMvc.perform(get("/v1/recipe/" + recipeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("番茄炒蛋"))
            .andExpect(jsonPath("$.data.type").value("dinner"))
            .andExpect(jsonPath("$.data.cookTime").value("15分钟"))
            .andExpect(jsonPath("$.data.calories").value(180))
            .andExpect(jsonPath("$.data.protein").value(0))
            .andExpect(jsonPath("$.data.carbs").value(0))
            .andExpect(jsonPath("$.data.fat").value(0))
            .andExpect(jsonPath("$.data.sourceUrl").value(source.getContentUrl()))
            .andExpect(jsonPath("$.data.sourcePlatform").value("XIAOHONGSHU"))
            .andExpect(jsonPath("$.data.items").isNotEmpty());

        mockMvc.perform(get("/v1/content-extraction/sources").header("Authorization", bearer(userId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].isPublished").value(true))
            .andExpect(jsonPath("$.data[0].recipeId").value(recipeId));

        ContentExtraction published = contentExtractionMapper.selectById(extractionId);
        assertEquals(Boolean.TRUE, published.getIsPublished());
        assertEquals(recipeId, published.getRecipeId());
    }

    @Test
    void repeatedPublishReturnsSameRecipeWithoutCreatingAnother() throws Exception {
        String userId = numericUserId();
        String sourceId = seedSource(userId);
        String extractionId = seedExtraction(sourceId);

        ContentExtraction extraction = contentExtractionMapper.selectById(extractionId);
        extraction.setDishName("测试菜");
        contentExtractionMapper.updateById(extraction);

        String firstBody = mockMvc.perform(post("/v1/content-extraction/extraction/" + extractionId + "/publish")
                .header("Authorization", bearer(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mealType\":\"lunch\"}"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        String firstRecipeId = objectMapper.readTree(firstBody).path("data").asText();
        createdRecipeIds.add(firstRecipeId);

        String secondBody = mockMvc.perform(post("/v1/content-extraction/extraction/" + extractionId + "/publish")
                .header("Authorization", bearer(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mealType\":\"dinner\"}"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertEquals(firstRecipeId, objectMapper.readTree(secondBody).path("data").asText());
        assertEquals(1, recipeMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.xx.jaseatschoicejava.entity.Recipe>()
                .eq("user_id", userId)
                .eq("source_url", contentSourceMapper.selectById(sourceId).getContentUrl())));
    }

    @Test
    void concurrentPublishCreatesOnlyOneRecipe() throws Exception {
        String userId = numericUserId();
        String sourceId = seedSource(userId);
        String extractionId = seedExtraction(sourceId);

        ContentExtraction extraction = contentExtractionMapper.selectById(extractionId);
        extraction.setDishName("并发测试菜");
        contentExtractionMapper.updateById(extraction);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<String> first = executor.submit(() -> publishAfterSignal(
                extractionId, userId, ready, start));
            Future<String> second = executor.submit(() -> publishAfterSignal(
                extractionId, userId, ready, start));

            assertTrue(ready.await(5, TimeUnit.SECONDS), "发布线程未能就绪");
            start.countDown();

            String firstRecipeId = first.get(10, TimeUnit.SECONDS);
            String secondRecipeId = second.get(10, TimeUnit.SECONDS);
            createdRecipeIds.add(firstRecipeId);
            createdRecipeIds.add(secondRecipeId);

            assertEquals(firstRecipeId, secondRecipeId);
            assertEquals(1, recipeMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.xx.jaseatschoicejava.entity.Recipe>()
                    .eq("user_id", userId)
                    .eq("source_url", contentSourceMapper.selectById(sourceId).getContentUrl())));

            ContentExtraction published = contentExtractionMapper.selectById(extractionId);
            assertTrue(Boolean.TRUE.equals(published.getIsPublished()));
            assertEquals(firstRecipeId, published.getRecipeId());
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void invalidMealTypeReturns400WithoutCreatingRecipe() throws Exception {
        String userId = numericUserId();
        String sourceId = seedSource(userId);
        String extractionId = seedExtraction(sourceId);

        mockMvc.perform(post("/v1/content-extraction/extraction/" + extractionId + "/publish")
                .header("Authorization", bearer(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mealType\":\"夜宵\"}"))
            .andExpect(status().isBadRequest());

        ContentExtraction extraction = contentExtractionMapper.selectById(extractionId);
        assertFalse(Boolean.TRUE.equals(extraction.getIsPublished()));
        assertEquals(0, recipeMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.xx.jaseatschoicejava.entity.Recipe>()
                .eq("user_id", userId)));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder createSource(
            String userId, String body) {
        return post("/v1/content-extraction/source")
            .header("Authorization", bearer(userId))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body);
    }

    private String bearer(String userId) {
        return "Bearer " + jwtUtil.generateToken(userId, "13000000000");
    }

    private String publishAfterSignal(String extractionId, String userId,
            CountDownLatch ready, CountDownLatch start) throws InterruptedException {
        ready.countDown();
        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("等待并发发布信号超时");
        }
        return contentExtractionService.publishAsRecipe(extractionId, "lunch", userId);
    }

    private String seedSource(String userId) {
        ContentSource source = new ContentSource();
        source.setId(("src" + UUID.randomUUID().toString().replace("-", "")).substring(0, 32));
        source.setUserId(userId);
        source.setContentUrl("https://example.com/" + source.getId());
        source.setContentType("ARTICLE");
        source.setPlatform("OTHER");
        source.setExtractionStatus("SUCCESS");
        source.setIsExtracted(true);
        source.setCreatedAt(LocalDateTime.now());
        source.setUpdatedAt(LocalDateTime.now());
        contentSourceMapper.insert(source);
        createdSourceIds.add(source.getId());
        return source.getId();
    }

    private String seedExtraction(String sourceId) {
        ContentExtraction extraction = new ContentExtraction();
        extraction.setId(("ext" + UUID.randomUUID().toString().replace("-", "")).substring(0, 32));
        extraction.setSourceId(sourceId);
        extraction.setIsPublished(false);
        extraction.setIsVerified(false);
        extraction.setCreatedAt(LocalDateTime.now());
        extraction.setUpdatedAt(LocalDateTime.now());
        contentExtractionMapper.insert(extraction);
        createdExtractionIds.add(extraction.getId());
        return extraction.getId();
    }

    private String randomSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private String numericUserId() {
        return String.valueOf(2000000000000000000L + Math.abs(UUID.randomUUID().getLeastSignificantBits() % 100000000L));
    }
}
