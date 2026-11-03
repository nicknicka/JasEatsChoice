package com.xx.jaseatschoicejava.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.xx.jaseatschoicejava.agent.config.ChatModelProvider;
import com.xx.jaseatschoicejava.agent.config.DynamicAiModelRegistry;
import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.config.AiModelConfig;
import com.xx.jaseatschoicejava.config.FileUploadConfig;
import com.xx.jaseatschoicejava.service.AiAssistantService;
import com.xx.jaseatschoicejava.service.DishRecognitionCacheService;

@ExtendWith(MockitoExtension.class)
class AIControllerDishRecognitionTest {

    private static final byte[] JPEG = { (byte) 0xFF, (byte) 0xD8, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 0, 0, 0, 13 };
    private static final byte[] WEBP = { 'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P' };

    @Mock
    private ChatModelProvider chatModelProvider;

    @Mock
    private AiAssistantService aiAssistantService;

    @Mock
    private DynamicAiModelRegistry aiModelRegistry;

    @Mock
    private DishRecognitionCacheService dishRecognitionCacheService;

    @TempDir
    private Path tempDir;

    private AIController controller;
    private FileUploadConfig fileUploadConfig;

    @BeforeEach
    void setUp() {
        fileUploadConfig = new FileUploadConfig();
        fileUploadConfig.setUploadPath(tempDir + "/");
        fileUploadConfig.setServerUrl("http://localhost:8080");
        fileUploadConfig.setUrlPrefix("/api/uploads/");

        controller = new AIController();
        ReflectionTestUtils.setField(controller, "chatModelProvider", chatModelProvider);
        ReflectionTestUtils.setField(controller, "aiAssistantService", aiAssistantService);
        ReflectionTestUtils.setField(controller, "fileUploadConfig", fileUploadConfig);
        ReflectionTestUtils.setField(controller, "aiModelRegistry", aiModelRegistry);
        ReflectionTestUtils.setField(controller, "dishRecognitionCacheService", dishRecognitionCacheService);
    }

    @AfterEach
    void cleanUploads() throws IOException {
        Path uploadDir = tempDir.resolve("dish-recognition");
        if (Files.exists(uploadDir)) {
            try (Stream<Path> paths = Files.walk(uploadDir)) {
                paths.sorted((left, right) -> right.getNameCount() - left.getNameCount())
                    .forEach(path -> path.toFile().delete());
            }
        }
    }

    static Stream<Arguments> supportedImages() {
        return Stream.of(
            Arguments.of("dish.jpg", "image/jpeg", JPEG, "image/jpeg", ".jpg"),
            Arguments.of("dish.txt", "text/plain", PNG, "image/png", ".png"),
            Arguments.of("dish.jpg", "image/jpeg", WEBP, "image/webp", ".webp")
        );
    }

    @ParameterizedTest
    @MethodSource("supportedImages")
    void dishRecognize_usesMagicNumberAndReturnsStableFields(String filename, String clientType,
            byte[] bytes, String expectedMime, String expectedSuffix) {
        enableVision();
        when(aiAssistantService.recognizeDishWithBase64(anyString(), eq(expectedMime)))
            .thenReturn(successfulResult());

        ResponseResult<?> response = controller.dishRecognize(
            new MockMultipartFile("image", filename, clientType, bytes), null);

        assertTrue(response.getSuccess());
        assertEquals("200", response.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getData();
        assertTrue((Boolean) data.get("isDish"));
        assertTrue((Boolean) data.get("nutritionEstimated"));
        assertTrue(((String) data.get("imageUrl")).endsWith(expectedSuffix));
        verify(aiAssistantService).recognizeDishWithBase64(anyString(), eq(expectedMime));
        verify(dishRecognitionCacheService).put(bytes, null, null, 7L, data);
    }

    @Test
    void dishRecognize_cacheHitReturnsResultWithoutWritingFileOrCallingModel() {
        enableVision("openai-compatible", "vision-a");
        Map<String, Object> cached = successfulResult();
        cached.put("imageUrl", "http://localhost:8080/api/uploads/dish-recognition/cached.jpg");
        when(dishRecognitionCacheService.find(eq(JPEG), eq("openai-compatible"), eq("vision-a"), eq(7L)))
            .thenReturn(Optional.of(cached));

        ResponseResult<?> response = controller.dishRecognize(
            new MockMultipartFile("image", "dish.jpg", "image/jpeg", JPEG), null);

        assertTrue(response.getSuccess());
        assertEquals(cached, response.getData());
        assertTrue(uploadDirIsEmpty());
        verifyNoInteractions(aiAssistantService);
    }

    @Test
    void dishRecognize_cacheMissUsesCurrentModelIdentity() {
        enableVision("openai-compatible", "vision-a");
        when(aiAssistantService.recognizeDishWithBase64(anyString(), eq("image/jpeg")))
            .thenReturn(successfulResult());

        controller.dishRecognize(new MockMultipartFile("image", "dish.jpg", "image/jpeg", JPEG), null);

        verify(dishRecognitionCacheService).find(JPEG, "openai-compatible", "vision-a", 7L);
    }

    @Test
    void dishRecognize_nonImage_returnsClearErrorWithoutCallingModel() {
        ResponseResult<?> response = controller.dishRecognize(
            new MockMultipartFile("image", "notes.txt", "image/jpeg", new byte[] { 'n', 'o', 't', 'i', 'm', 'a', 'g', 'e' }),
            null);

        assertFalse(response.getSuccess());
        assertEquals("415", response.getCode());
        assertTrue(response.getMessage().contains("JPEG、PNG 或 WebP"));
        verifyNoInteractions(aiAssistantService);
    }

    @Test
    void dishRecognize_unsupportedGif_returnsClearError() {
        ResponseResult<?> response = controller.dishRecognize(
            new MockMultipartFile("image", "dish.gif", "image/gif", "GIF89a123456".getBytes()),
            null);

        assertFalse(response.getSuccess());
        assertEquals("415", response.getCode());
        verifyNoInteractions(aiAssistantService);
    }

    @Test
    void dishRecognize_nonImageWithVisionDisabledStillReturnsFormatError() {
        ResponseResult<?> response = controller.dishRecognize(
            new MockMultipartFile("image", "notes.txt", "image/jpeg", new byte[] { 'n', 'o', 't', 'i', 'm', 'a', 'g', 'e' }),
            null);

        assertFalse(response.getSuccess());
        assertEquals("415", response.getCode());
        verifyNoInteractions(aiAssistantService);
    }

    @Test
    void dishRecognize_oversizedFile_returnsClearError() throws IOException {
        MultipartFile image = mock(MultipartFile.class);
        when(image.getSize()).thenReturn(11L * 1024 * 1024);

        ResponseResult<?> response = controller.dishRecognize(image, null);

        assertFalse(response.getSuccess());
        assertEquals("413", response.getCode());
        assertTrue(response.getMessage().contains("10MB"));
        verify(image, never()).getBytes();
    }

    @Test
    void dishRecognize_readFailure_returnsClearError() throws IOException {
        MultipartFile image = mock(MultipartFile.class);
        when(image.getSize()).thenReturn(12L);
        when(image.isEmpty()).thenReturn(false);
        when(image.getBytes()).thenThrow(new IOException("broken stream"));

        ResponseResult<?> response = controller.dishRecognize(image, null);

        assertFalse(response.getSuccess());
        assertEquals("400", response.getCode());
        assertTrue(response.getMessage().contains("读取上传图片失败"));
    }

    @Test
    void dishRecognize_modelFailure_removesSavedFile() {
        enableVision();
        when(aiAssistantService.recognizeDishWithBase64(anyString(), eq("image/jpeg")))
            .thenReturn(Map.of("error", true, "message", "视觉模型调用失败"));

        ResponseResult<?> response = controller.dishRecognize(
            new MockMultipartFile("image", "dish.jpg", "image/jpeg", JPEG), null);

        assertFalse(response.getSuccess());
        assertEquals("500", response.getCode());
        assertTrue(uploadDirIsEmpty());
        verify(dishRecognitionCacheService, never()).put(any(byte[].class), anyString(), anyString(), anyLong(),
            any(Map.class));
    }

    @Test
    void dishRecognize_nonDish_returnsBusinessCode4001AndRemovesSavedFile() {
        enableVision();
        when(aiAssistantService.recognizeDishWithBase64(anyString(), eq("image/jpeg")))
            .thenReturn(Map.of("error", true, "notDish", true, "message", "请上传食物图片"));

        ResponseResult<?> response = controller.dishRecognize(
            new MockMultipartFile("image", "dish.jpg", "image/jpeg", JPEG), null);

        assertFalse(response.getSuccess());
        assertEquals("4001", response.getCode());
        assertEquals("请上传食物图片", response.getMessage());
        assertTrue(uploadDirIsEmpty());
        verify(dishRecognitionCacheService, never()).put(any(byte[].class), anyString(), anyString(), anyLong(),
            any(Map.class));
    }

    @Test
    void dishRecognize_modelException_removesSavedFileAndDoesNotCache() {
        enableVision();
        when(aiAssistantService.recognizeDishWithBase64(anyString(), eq("image/jpeg")))
            .thenThrow(new IllegalStateException("vision unavailable"));

        ResponseResult<?> response = controller.dishRecognize(
            new MockMultipartFile("image", "dish.jpg", "image/jpeg", JPEG), null);

        assertFalse(response.getSuccess());
        assertEquals("500", response.getCode());
        assertTrue(uploadDirIsEmpty());
        verify(dishRecognitionCacheService, never()).put(any(byte[].class), anyString(), anyString(), anyLong(),
            any(Map.class));
    }

    @Test
    void dishRecognize_saveFailure_returnsClearError() throws IOException {
        enableVision();
        Path occupiedPath = tempDir.resolve("blocked");
        Files.writeString(occupiedPath, "not a directory");
        fileUploadConfig.setUploadPath(occupiedPath + "/");

        ResponseResult<?> response = controller.dishRecognize(
            new MockMultipartFile("image", "dish.jpg", "image/jpeg", JPEG), null);

        assertFalse(response.getSuccess());
        assertEquals("500", response.getCode());
        assertTrue(response.getMessage().contains("保存上传图片失败"));
        verifyNoInteractions(aiAssistantService);
    }

    private void enableVision() {
        enableVision(null, null);
    }

    private void enableVision(String provider, String visionModel) {
        when(aiModelRegistry.current()).thenReturn(new DynamicAiModelRegistry.ActiveModels(
            AiModelConfig.builder().provider(provider).visionModel(visionModel).build(),
            null, null, null, null, 7L));
        when(chatModelProvider.getCapabilities()).thenReturn(AiCapabilitiesDTO.builder()
            .provider(provider)
            .visionModel(visionModel)
            .vision(true)
            .build());
    }

    private Map<String, Object> successfulResult() {
        return new java.util.HashMap<>(Map.ofEntries(
            Map.entry("isDish", true),
            Map.entry("name", "番茄炒蛋"),
            Map.entry("calories", 180),
            Map.entry("protein", 10),
            Map.entry("fat", 12),
            Map.entry("carbs", 8),
            Map.entry("difficulty", "简单"),
            Map.entry("preparationTime", "15分钟"),
            Map.entry("ingredients", List.of("番茄", "鸡蛋")),
            Map.entry("tags", List.of("家常菜")),
            Map.entry("confidence", 0.92),
            Map.entry("nutritionScore", 8),
            Map.entry("nutritionEstimated", true)
        ));
    }

    private boolean uploadDirIsEmpty() {
        Path uploadDir = tempDir.resolve("dish-recognition");
        if (!Files.exists(uploadDir)) {
            return true;
        }
        try (Stream<Path> paths = Files.list(uploadDir)) {
            return paths.findAny().isEmpty();
        } catch (IOException e) {
            return false;
        }
    }
}
