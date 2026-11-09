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
import org.springframework.mock.web.MockHttpServletRequest;
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
import com.xx.jaseatschoicejava.service.AiQuotaScope;
import com.xx.jaseatschoicejava.service.AiQuotaService;
import com.xx.jaseatschoicejava.service.AiRuntimeConfigService;
import com.xx.jaseatschoicejava.service.DishRecognitionCacheService;
import com.xx.jaseatschoicejava.util.UserIdentityUtil;

@ExtendWith(MockitoExtension.class)
class AIControllerDishRecognitionTest {

    private static final byte[] JPEG = jpegHeader(320, 200);
    private static final byte[] PNG = pngHeader(320, 200);
    private static final byte[] WEBP = webpHeader(320, 200);

    @Mock
    private ChatModelProvider chatModelProvider;

    @Mock
    private AiAssistantService aiAssistantService;

    @Mock
    private DynamicAiModelRegistry aiModelRegistry;

    @Mock
    private DishRecognitionCacheService dishRecognitionCacheService;

    @Mock
    private AiQuotaService aiQuotaService;

    @Mock
    private AiRuntimeConfigService aiRuntimeConfigService;

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
        ReflectionTestUtils.setField(controller, "aiQuotaService", aiQuotaService);
        ReflectionTestUtils.setField(controller, "aiRuntimeConfigService", aiRuntimeConfigService);
    }

    private static MockHttpServletRequest verifiedRequest(String userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(UserIdentityUtil.VERIFIED_USER_ID_ATTRIBUTE, userId);
        return request;
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
            new MockMultipartFile("image", filename, clientType, bytes), verifiedRequest("42"));

        assertTrue(response.getSuccess());
        assertEquals("200", response.getCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getData();
        assertTrue((Boolean) data.get("isDish"));
        assertTrue((Boolean) data.get("nutritionEstimated"));
        assertTrue(((String) data.get("imageUrl")).endsWith(expectedSuffix));
        verify(aiAssistantService).recognizeDishWithBase64(anyString(), eq(expectedMime));
        verify(dishRecognitionCacheService).put(bytes, null, null, 7L, data);
        verify(aiQuotaService).consume("42", AiQuotaScope.DISH_RECOGNITION);
    }

    @Test
    void dishRecognize_cacheHitReturnsResultWithoutWritingFileOrCallingModel() {
        enableVision("openai-compatible", "vision-a");
        Map<String, Object> cached = successfulResult();
        cached.put("imageUrl", "http://localhost:8080/api/uploads/dish-recognition/cached.jpg");
        when(dishRecognitionCacheService.find(eq(JPEG), eq("openai-compatible"), eq("vision-a"), eq(7L)))
            .thenReturn(Optional.of(cached));

        ResponseResult<?> response = controller.dishRecognize(
            new MockMultipartFile("image", "dish.jpg", "image/jpeg", JPEG), verifiedRequest("42"));

        assertTrue(response.getSuccess());
        assertEquals(cached, response.getData());
        assertTrue(uploadDirIsEmpty());
        verifyNoInteractions(aiAssistantService);
        verifyNoInteractions(aiQuotaService);
    }

    @Test
    void dishRecognize_cacheMissUsesCurrentModelIdentity() {
        enableVision("openai-compatible", "vision-a");
        when(aiAssistantService.recognizeDishWithBase64(anyString(), eq("image/jpeg")))
            .thenReturn(successfulResult());

        controller.dishRecognize(
            new MockMultipartFile("image", "dish.jpg", "image/jpeg", JPEG), verifiedRequest("42"));

        verify(dishRecognitionCacheService).find(JPEG, "openai-compatible", "vision-a", 7L);
    }

    @Test
    void dishRecognize_nonImage_returnsClearErrorWithoutCallingModel() {
        ResponseResult<?> response = controller.dishRecognize(
            new MockMultipartFile("image", "notes.txt", "image/jpeg", new byte[] { 'n', 'o', 't', 'i', 'm', 'a', 'g', 'e' }),
            verifiedRequest("42"));

        assertFalse(response.getSuccess());
        assertEquals("415", response.getCode());
        assertTrue(response.getMessage().contains("JPEG、PNG 或 WebP"));
        verifyNoInteractions(aiAssistantService);
    }

    @Test
    void dishRecognize_unsupportedGif_returnsClearError() {
        ResponseResult<?> response = controller.dishRecognize(
            new MockMultipartFile("image", "dish.gif", "image/gif", "GIF89a123456".getBytes()),
            verifiedRequest("42"));

        assertFalse(response.getSuccess());
        assertEquals("415", response.getCode());
        verifyNoInteractions(aiAssistantService);
    }

    @ParameterizedTest
    @MethodSource("oversizedDimensions")
    void dishRecognize_dimensionsExceeded_returnsStableCodeWithoutWritingFile(byte[] bytes, String filename) {
        ResponseResult<?> response = controller.dishRecognize(
            new MockMultipartFile("image", filename, "application/octet-stream", bytes), verifiedRequest("42"));

        assertFalse(response.getSuccess());
        assertEquals("IMAGE_DIMENSIONS_EXCEEDED", response.getCode());
        assertTrue(response.getMessage().contains("8192"));
        assertTrue(uploadDirIsEmpty(), "失败路径不得留下孤儿文件");
        verifyNoInteractions(aiAssistantService, dishRecognitionCacheService, aiQuotaService);
    }

    static Stream<Arguments> oversizedDimensions() {
        return Stream.of(
            Arguments.of(pngHeader(9000, 100), "huge.png"),
            Arguments.of(pngHeader(6000, 6000), "too-many-pixels.png"),
            Arguments.of(jpegHeader(9000, 100), "huge.jpg"),
            Arguments.of(webpHeader(9000, 100), "huge.webp")
        );
    }

    @Test
    void dishRecognize_exactPixelLimit_isAllowed() {
        enableVision();
        when(aiAssistantService.recognizeDishWithBase64(anyString(), eq("image/png")))
            .thenReturn(successfulResult());

        ResponseResult<?> response = controller.dishRecognize(
            new MockMultipartFile("image", "limit.png", "image/png", pngHeader(5000, 5000)),
            verifiedRequest("42"));

        assertTrue(response.getSuccess());
        assertEquals("200", response.getCode());
    }

    @Test
    void dishRecognize_unreadableDimensions_returnsStableCodeWithoutWritingFile() {
        byte[] malformedPng = { (byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 0, 0, 0, 13 };

        ResponseResult<?> response = controller.dishRecognize(
            new MockMultipartFile("image", "broken.png", "image/png", malformedPng), verifiedRequest("42"));

        assertFalse(response.getSuccess());
        assertEquals("IMAGE_DIMENSIONS_INVALID", response.getCode());
        assertTrue(uploadDirIsEmpty(), "失败路径不得留下孤儿文件");
        verifyNoInteractions(aiAssistantService, dishRecognitionCacheService, aiQuotaService);
    }

    @Test
    void dishRecognize_nonImageWithVisionDisabledStillReturnsFormatError() {
        ResponseResult<?> response = controller.dishRecognize(
            new MockMultipartFile("image", "notes.txt", "image/jpeg", new byte[] { 'n', 'o', 't', 'i', 'm', 'a', 'g', 'e' }),
            verifiedRequest("42"));

        assertFalse(response.getSuccess());
        assertEquals("415", response.getCode());
        verifyNoInteractions(aiAssistantService);
    }

    @Test
    void dishRecognize_oversizedFile_returnsClearError() throws IOException {
        MultipartFile image = mock(MultipartFile.class);
        when(image.getSize()).thenReturn(11L * 1024 * 1024);

        ResponseResult<?> response = controller.dishRecognize(image, verifiedRequest("42"));

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

        ResponseResult<?> response = controller.dishRecognize(image, verifiedRequest("42"));

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
            new MockMultipartFile("image", "dish.jpg", "image/jpeg", JPEG), verifiedRequest("42"));

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
            new MockMultipartFile("image", "dish.jpg", "image/jpeg", JPEG), verifiedRequest("42"));

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
            new MockMultipartFile("image", "dish.jpg", "image/jpeg", JPEG), verifiedRequest("42"));

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
            new MockMultipartFile("image", "dish.jpg", "image/jpeg", JPEG), verifiedRequest("42"));

        assertFalse(response.getSuccess());
        assertEquals("500", response.getCode());
        assertTrue(response.getMessage().contains("保存上传图片失败"));
        verifyNoInteractions(aiAssistantService);
    }

    @Test
    void dishRecognize_modelRejectsImageReturnsStableCodeAndDowngradesVision() {
        enableVision();
        Map<String, Object> rejected = new java.util.HashMap<>();
        rejected.put("error", true);
        rejected.put("visionUnsupported", true);
        rejected.put("message", "当前视觉模型不支持图片输入，菜品识别暂不可用");
        when(aiAssistantService.recognizeDishWithBase64(anyString(), eq("image/png")))
            .thenReturn(rejected);

        ResponseResult<?> response = controller.dishRecognize(
            new MockMultipartFile("image", "dish.png", "image/png", PNG), verifiedRequest("42"));

        assertFalse(response.getSuccess());
        assertEquals("AI_VISION_UNSUPPORTED", response.getCode());
        assertEquals("当前视觉模型不支持图片输入，菜品识别暂不可用", response.getMessage());
        verify(aiRuntimeConfigService)
            .markVisionUnverified("当前视觉模型不支持图片输入，菜品识别暂不可用");
        assertTrue(uploadDirIsEmpty(), "失败路径不得留下孤儿文件");
        verify(dishRecognitionCacheService, never()).put(any(), anyString(), anyString(), anyLong(), any());
    }

    @Test
    void dishRecognize_serverErrorKeepsGenericFailureCode() {
        enableVision();
        when(aiAssistantService.recognizeDishWithBase64(anyString(), eq("image/png")))
            .thenReturn(Map.of("error", true, "message", "菜品识别失败，请稍后重试"));

        ResponseResult<?> response = controller.dishRecognize(
            new MockMultipartFile("image", "dish.png", "image/png", PNG), verifiedRequest("42"));

        assertFalse(response.getSuccess());
        assertEquals("500", response.getCode());
        verifyNoInteractions(aiRuntimeConfigService);
    }

    private void enableVision() {
        enableVision(null, null);
    }

    private void enableVision(String provider, String visionModel) {
        when(aiModelRegistry.current()).thenReturn(new DynamicAiModelRegistry.ActiveModels(
            AiModelConfig.builder().provider(provider).visionModel(visionModel).build(),
            null, null, null, 7L));
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

    private static byte[] jpegHeader(int width, int height) {
        return new byte[] {
            (byte) 0xFF, (byte) 0xD8,
            (byte) 0xFF, (byte) 0xC0, 0, 17, 8,
            (byte) (height >>> 8), (byte) height,
            (byte) (width >>> 8), (byte) width,
            3, 1, 17, 0, 2, 17, 1, 3, 17, 1,
            (byte) 0xFF, (byte) 0xD9
        };
    }

    private static byte[] pngHeader(int width, int height) {
        byte[] bytes = new byte[24];
        byte[] signature = { (byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10 };
        System.arraycopy(signature, 0, bytes, 0, signature.length);
        bytes[8] = 0;
        bytes[9] = 0;
        bytes[10] = 0;
        bytes[11] = 13;
        bytes[12] = 'I';
        bytes[13] = 'H';
        bytes[14] = 'D';
        bytes[15] = 'R';
        writeIntBigEndian(bytes, 16, width);
        writeIntBigEndian(bytes, 20, height);
        return bytes;
    }

    private static byte[] webpHeader(int width, int height) {
        byte[] bytes = new byte[30];
        putAscii(bytes, 0, "RIFF");
        writeIntLittleEndian(bytes, 4, bytes.length - 8);
        putAscii(bytes, 8, "WEBP");
        putAscii(bytes, 12, "VP8X");
        writeIntLittleEndian(bytes, 16, 10);
        writeInt24LittleEndian(bytes, 24, width - 1);
        writeInt24LittleEndian(bytes, 27, height - 1);
        return bytes;
    }

    private static void putAscii(byte[] target, int offset, String value) {
        for (int i = 0; i < value.length(); i++) {
            target[offset + i] = (byte) value.charAt(i);
        }
    }

    private static void writeIntBigEndian(byte[] target, int offset, int value) {
        target[offset] = (byte) (value >>> 24);
        target[offset + 1] = (byte) (value >>> 16);
        target[offset + 2] = (byte) (value >>> 8);
        target[offset + 3] = (byte) value;
    }

    private static void writeIntLittleEndian(byte[] target, int offset, int value) {
        target[offset] = (byte) value;
        target[offset + 1] = (byte) (value >>> 8);
        target[offset + 2] = (byte) (value >>> 16);
        target[offset + 3] = (byte) (value >>> 24);
    }

    private static void writeInt24LittleEndian(byte[] target, int offset, int value) {
        target[offset] = (byte) value;
        target[offset + 1] = (byte) (value >>> 8);
        target[offset + 2] = (byte) (value >>> 16);
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
