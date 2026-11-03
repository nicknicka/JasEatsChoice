package com.xx.jaseatschoicejava.controller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.config.FileUploadConfig;
import com.xx.jaseatschoicejava.dto.DishDescriptionRequestDTO;
import com.xx.jaseatschoicejava.service.DishDescriptionService;
import com.xx.jaseatschoicejava.agent.config.ChatModelProvider;
import com.xx.jaseatschoicejava.agent.config.DynamicAiModelRegistry;
import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.service.AiAssistantService;
import com.xx.jaseatschoicejava.service.DishRecognitionCacheService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;

/**
 * AI助手控制器（活跃端点）
 *
 * 仅保留菜品识别、食谱优化、健康检查等独立功能。
 * AI对话统一走 SupervisorSSEController（/agent/supervisor-sse/chat）。
 *

 * @since 2026-03-22
 * @updated 2026-04-03 清理废弃端点，保留活跃功能
 */
@Api(tags = "AI助手（独立功能）")
@RestController
@RequestMapping("/v1/ai")
public class AIController {

    private static final Logger log = LoggerFactory.getLogger(AIController.class);
    private static final long SSE_TIMEOUT_MS = 300000L;
    private static final String AI_NOT_CONFIGURED_CODE = "AI_NOT_CONFIGURED";
    private static final String AI_VISION_NOT_CONFIGURED_CODE = "AI_VISION_NOT_CONFIGURED";
    private static final long MAX_REMOTE_IMAGE_BYTES = 10 * 1024 * 1024;
    private static final int MAX_REMOTE_IMAGE_REDIRECTS = 3;
    private static final int REMOTE_IMAGE_TIMEOUT_MS = 15000;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource(name = "chatModelProvider")
    private ChatModelProvider chatModelProvider;

    @Resource
    private DynamicAiModelRegistry aiModelRegistry;

    @Resource
    private AiAssistantService aiAssistantService;

    @Resource
    private FileUploadConfig fileUploadConfig;

    @Resource
    private DishDescriptionService dishDescriptionService;

    @Resource
    private DishRecognitionCacheService dishRecognitionCacheService;

    // ==================== 菜品识别 ====================

    /**
     * AI菜品识别接口
     *
     * 路由: /v1/ai/dish-recognize
     * 返回: ResponseResult (识别结果)
     */
    @ApiOperation(value = "AI菜品识别", notes = "使用通用视觉模型识别菜品图片")
    @PostMapping(value = "/dish-recognize", consumes = "multipart/form-data")
    public ResponseResult<?> dishRecognize(@RequestParam("image") MultipartFile image,
                                          @RequestParam(value = "userId", required = false) String userId) {
        if (image == null || image.isEmpty()) {
            return ResponseResult.fail("400", "请上传菜品图片");
        }
        if (image.getSize() > MAX_REMOTE_IMAGE_BYTES) {
            return ResponseResult.fail("413", "图片超过 10MB 限制");
        }

        byte[] imageBytes;
        try {
            imageBytes = image.getBytes();
        } catch (IOException e) {
            log.error("读取上传图片失败", e);
            return ResponseResult.fail("400", "读取上传图片失败，请重新上传");
        }

        String contentType = detectSupportedImageContentType(imageBytes);
        if (contentType == null) {
            return ResponseResult.fail("415", "仅支持 JPEG、PNG 或 WebP 图片");
        }

        AiCapabilitiesDTO capabilities = chatModelProvider.getCapabilities();
        if (!capabilities.isVision()) {
            return ResponseResult.fail(AI_VISION_NOT_CONFIGURED_CODE, "菜品识别暂未开放：请配置可用的视觉模型");
        }

        long runtimeVersion = aiModelRegistry.current().version();
        java.util.Optional<Map<String, Object>> cachedResult = dishRecognitionCacheService.find(
            imageBytes, capabilities.getProvider(), capabilities.getVisionModel(), runtimeVersion);
        if (cachedResult.isPresent()) {
            return ResponseResult.success(cachedResult.get());
        }

        String suffix = imageSuffix(contentType);
        String uploadDir = fileUploadConfig.getUploadPath() + "dish-recognition/";
        java.io.File directory = new java.io.File(uploadDir);
        if (!directory.exists() && !directory.mkdirs()) {
            log.error("创建菜品图片目录失败：{}", uploadDir);
            return ResponseResult.fail("500", "保存上传图片失败，请稍后重试");
        }

        String fileName = java.util.UUID.randomUUID() + suffix;
        java.nio.file.Path filePath = java.nio.file.Paths.get(uploadDir, fileName);
        try {
            java.nio.file.Files.write(filePath, imageBytes);
        } catch (IOException e) {
            log.error("保存上传图片失败", e);
            deleteQuietly(filePath);
            return ResponseResult.fail("500", "保存上传图片失败，请稍后重试");
        }

        String imageUrl = fileUploadConfig.getServerUrl() + "/" + fileUploadConfig.getUrlPrefix()
            + "dish-recognition/" + fileName;
        try {
            String base64Data = java.util.Base64.getEncoder().encodeToString(imageBytes);
            Map<String, Object> result = aiAssistantService.recognizeDishWithBase64(base64Data, contentType);

            if (result == null) {
                deleteQuietly(filePath);
                return ResponseResult.fail("500", "菜品识别失败：服务返回空结果");
            }
            if (Boolean.TRUE.equals(result.get("error"))) {
                deleteQuietly(filePath);
                if (Boolean.TRUE.equals(result.get("notDish"))) {
                    String message = result.get("message") instanceof String value && !value.isBlank()
                        ? value : "请上传食物图片";
                    return ResponseResult.fail("4001", message);
                }
                String message = result.get("message") instanceof String value && !value.isBlank()
                    ? value : "菜品识别失败，请稍后重试";
                return ResponseResult.fail("500", message);
            }

            result.put("imageUrl", imageUrl);
            if (Boolean.TRUE.equals(result.get("isDish"))) {
                dishRecognitionCacheService.put(
                    imageBytes, capabilities.getProvider(), capabilities.getVisionModel(), runtimeVersion, result);
            }
            return ResponseResult.success(result);
        } catch (RuntimeException e) {
            deleteQuietly(filePath);
            log.error("菜品识别失败", e);
            return ResponseResult.fail("500", "菜品识别失败，请稍后重试");
        }
    }

    private String detectSupportedImageContentType(byte[] bytes) {
        String contentType = detectImageContentType(bytes);
        return contentType != null && contentType.startsWith("image/") && contentType.matches("image/(jpeg|png|webp)")
            ? contentType : null;
    }

    private String imageSuffix(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private void deleteQuietly(java.nio.file.Path filePath) {
        try {
            java.nio.file.Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("清理菜品图片失败：{}", filePath, e);
        }
    }

    /**
     * 兼容旧接口：通过JSON传图片URL识别
     */
    @PostMapping("/dish-recognize-url")
    public ResponseResult<?> dishRecognizeByUrl(@RequestBody Map<String, Object> params) {
        try {
            String imageUrl = (String) params.get("imageUrl");
            Map<String, Object> result = aiAssistantService.recognizeDish(imageUrl);
            return ResponseResult.success(result);
        } catch (RuntimeException e) {
            log.error("菜品识别失败", e);
            return ResponseResult.fail("500", "菜品识别失败：" + e.getMessage());
        }
    }

    /**
     * 代理下载远端图片。
     *
     * 路由: /v1/ai/fetch-remote-image
     * 用途: 用户从网页拖入图片时，浏览器直连外站 CDN 会被 CORS 拦住，统一由后端取回字节。
     */
    @ApiOperation(value = "代理下载远端图片", notes = "供从网页拖入图片时使用，规避浏览器 CORS 限制")
    @PostMapping("/fetch-remote-image")
    public ResponseEntity<Object> fetchRemoteImage(@RequestBody Map<String, Object> params) {
        String rawUrl = params.get("url") instanceof String value ? value.trim() : "";
        if (rawUrl.isEmpty()) {
            return ResponseEntity.badRequest().body(ResponseResult.fail("400", "图片地址不能为空"));
        }

        String url = normalizeRemoteImageUrl(rawUrl);
        if (url == null) {
            return ResponseEntity.badRequest().body(ResponseResult.fail("400", "仅支持 http/https 图片地址"));
        }

        byte[] imageBytes;
        try {
            imageBytes = downloadRemoteImage(url);
        } catch (IOException e) {
            log.warn("代理下载图片失败: {} - {}", url, e.getMessage());
            return ResponseEntity.badRequest().body(ResponseResult.fail("400", e.getMessage()));
        }

        if (imageBytes == null || imageBytes.length == 0) {
            return ResponseEntity.badRequest().body(ResponseResult.fail("400", "图片内容为空"));
        }
        if (imageBytes.length > MAX_REMOTE_IMAGE_BYTES) {
            return ResponseEntity.badRequest().body(ResponseResult.fail("400", "图片超过 10MB 限制"));
        }

        String contentType = detectImageContentType(imageBytes);
        if (contentType == null) {
            return ResponseEntity.badRequest().body(ResponseResult.fail("400", "拖入的地址不是图片"));
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(imageBytes);
    }

    /**
     * 下载远端图片字节。逐跳校验目标地址，避免被重定向到内网（SSRF）。
     */
    private byte[] downloadRemoteImage(String url) throws IOException {
        String currentUrl = url;

        for (int hop = 0; hop <= MAX_REMOTE_IMAGE_REDIRECTS; hop++) {
            URI uri = parseHttpUri(currentUrl);
            if (!isPublicHost(uri.getHost())) {
                throw new IOException("不允许访问内网地址");
            }

            HttpResponse response;
            try {
                response = HttpRequest.get(currentUrl)
                    .setFollowRedirects(false)
                    .timeout(REMOTE_IMAGE_TIMEOUT_MS)
                    .execute();
            } catch (HttpException e) {
                throw new IOException("图片下载失败：" + e.getMessage());
            }
            int status = response.getStatus();

            if (status >= 300 && status < 400) {
                String location = response.header("Location");
                if (location == null || location.isBlank()) {
                    throw new IOException("图片下载失败：重定向地址缺失");
                }
                currentUrl = uri.resolve(location).toString();
                continue;
            }

            if (status != 200) {
                throw new IOException("图片下载失败：HTTP " + status);
            }

            return response.bodyBytes();
        }

        throw new IOException("图片地址重定向次数过多");
    }

    private URI parseHttpUri(String url) throws IOException {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IOException("图片地址格式不正确");
        }

        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new IOException("仅支持 http/https 图片地址");
        }
        return uri;
    }

    /**
     * 只有域名解析出的全部地址都是公网地址时才放行。
     */
    private boolean isPublicHost(String host) {
        if (host == null || host.isBlank()) {
            return false;
        }

        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            return false;
        }

        if (addresses.length == 0) {
            return false;
        }

        for (InetAddress address : addresses) {
            if (isBlockedAddress(address)) {
                return false;
            }
        }
        return true;
    }

    private boolean isBlockedAddress(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
            || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return true;
        }

        byte[] bytes = address.getAddress();
        if (bytes.length == 4) {
            int first = bytes[0] & 0xFF;
            int second = bytes[1] & 0xFF;
            // 0.0.0.0/8 与 100.64.0.0/10（运营商级 NAT）
            return first == 0 || (first == 100 && second >= 64 && second <= 127);
        }

        // IPv6 唯一本地地址 fc00::/7
        return (bytes[0] & 0xFE) == 0xFC;
    }

    /**
     * 补全协议相对（//host/x.png）与缺省协议的图片地址。
     */
    private String normalizeRemoteImageUrl(String rawUrl) {
        if (rawUrl.startsWith("//")) {
            return "https:" + rawUrl;
        }
        if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            return rawUrl;
        }
        return null;
    }

    /**
     * 按魔数判定图片类型，只接受前端校验白名单内的格式。
     */
    private String detectImageContentType(byte[] bytes) {
        if (bytes.length < 12) {
            return null;
        }
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8) {
            return "image/jpeg";
        }
        if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') {
            return "image/png";
        }
        if (bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F') {
            return "image/gif";
        }
        if (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
            && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    // ==================== 食谱优化 ====================

    /**
     * AI食谱优化接口
     *
     * 路由: /v1/ai/recipe-upload
     */
    @ApiOperation(value = "AI食谱优化", notes = "上传食谱进行优化建议")
    @PostMapping("/recipe-upload")
    public ResponseResult<?> recipeUpload(@RequestBody Map<String, Object> params) {
        try {
            Object recipeObj = params.get("recipe");
            if (!(recipeObj instanceof Map<?, ?> recipe)) {
                return ResponseResult.fail("400", "食谱内容格式不正确");
            }

            Object recipeTextObj = recipe.get("text");
            String recipeText = recipeTextObj != null ? recipeTextObj.toString() : null;
            if (recipeText == null || recipeText.trim().isEmpty()) {
                return ResponseResult.fail("400", "食谱内容不能为空");
            }

            Map<String, Object> result = aiAssistantService.optimizeRecipe(recipeText);
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("食谱优化失败", e);
            return ResponseResult.fail("500", "食谱优化失败：" + e.getMessage());
        }
    }

    // ==================== 食谱优化（前端统一入口） ====================

    /**
     * AI食谱优化接口（前端统一调用）
     *
     * 路由: POST /v1/ai/recipe
     * 接收: { "recipe": "食谱内容", "goal": "优化目标" }
     * 返回: 优化后的食谱结果
     */
    @ApiOperation(value = "AI食谱优化（统一入口）", notes = "根据食材或食谱生成优化建议")
    @PostMapping("/recipe")
    public ResponseResult<?> recipeOptimize(@RequestBody Map<String, Object> params) {
        try {
            // 兼容桌面端 {foodName: "..."} 和 UniApp {recipe: "...", goal: "..."}
            String foodName = (String) params.get("foodName");
            String recipeText = (String) params.get("recipe");
            String goal = (String) params.get("goal");

            log.info("食谱优化请求，参数：{}, foodName：{}, goal：{}", params.keySet(), foodName, goal);

            String inputText = (foodName != null && !foodName.trim().isEmpty()) ? foodName : recipeText;

            if (inputText == null || inputText.trim().isEmpty()) {
                return ResponseResult.fail("400", "食谱内容不能为空");
            }

            if (!chatModelProvider.getCapabilities().isTextChat()) {
                return ResponseResult.fail(AI_NOT_CONFIGURED_CODE, "食谱优化暂未开放：请先配置 AI 模型");
            }

            log.info("食谱优化请求，目标：{}，输入：{}", goal, inputText.length() > 100 ? inputText.substring(0, 100) + "..." : inputText);
            Map<String, Object> result = aiAssistantService.optimizeRecipe(inputText);

            // 检查 service 层是否返回了错误
            if (Boolean.TRUE.equals(result.get("error"))) {
                String errorMsg = (String) result.get("message");
                log.warn("食谱优化服务返回错误：{}", errorMsg);
                String errorCode = errorMsg != null && errorMsg.contains("未配置")
                        ? AI_NOT_CONFIGURED_CODE : "500";
                return ResponseResult.fail(errorCode, errorMsg != null ? errorMsg : "食谱优化失败");
            }

            log.info("食谱优化成功，返回字段：{}", result.keySet());

            // 兼容桌面端：前端期望 data 直接是数组
            Object recipes = result.get("recipes");
            if (recipes != null) {
                return ResponseResult.success(recipes);
            }
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("食谱优化失败", e);
            return ResponseResult.fail("500", "食谱优化失败：" + e.getMessage());
        }
    }

    // ==================== 食谱优化（SSE流式进度，桌面端专用） ====================

    /**
     * AI食谱优化 SSE 流式端点（真正流式输出）
     *
     * 路由: POST /v1/ai/recipe/stream
     * 事件格式：
     * - 进度: {"message":"正在分析食谱内容...","progress":true}
     * - 流式token: {"content":"token文本","streaming":true}
     * - 结果: {"recipes":[...],"done":true}
     * - 错误: {"error":true,"message":"...","done":true}
     */
    @ApiOperation(value = "AI食谱优化（SSE流式进度）", notes = "实时推送优化进度和流式token，桌面端专用")
    @PostMapping(value = "/recipe/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter recipeOptimizeStream(@RequestBody Map<String, Object> params) {
        String foodName = (String) params.get("foodName");
        String recipeText = (String) params.get("recipe");
        String inputText = (foodName != null && !foodName.trim().isEmpty()) ? foodName : recipeText;

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        if (inputText == null || inputText.trim().isEmpty()) {
            sendRecipeSseData(emitter, Map.of("error", true, "message", "食谱内容不能为空", "done", true));
            emitter.complete();
            return emitter;
        }

        if (!chatModelProvider.getCapabilities().isStreaming()) {
            sendRecipeSseData(emitter, Map.of("error", true,
                    "message", "食谱优化暂未开放：请先配置支持流式输出的 AI 模型", "done", true));
            emitter.complete();
            return emitter;
        }

        // 使用流式调用
        aiAssistantService.optimizeRecipeStreaming(
                inputText,
                // progressCallback：处理进度和最终结果
                message -> {
                    if (message.startsWith("RECIPES:")) {
                        // 最终食谱结果：将JSON字符串解析为对象后发送
                        try {
                            Object recipesObj = objectMapper.readValue(
                                    message.substring(8), Object.class);
                            sendRecipeSseData(emitter, Map.of("recipes", recipesObj, "done", true));
                        } catch (Exception e) {
                            sendRecipeSseData(emitter, Map.of("error", true,
                                    "message", "结果解析失败", "done", true));
                        }
                    } else if (message.startsWith("ERROR:")) {
                        sendRecipeSseData(emitter, Map.of("error", true,
                                "message", message.substring(6), "done", true));
                    } else {
                        sendRecipeSseEvent(emitter, message);
                    }
                },
                // tokenCallback：逐token推送
                token -> {
                    try {
                        final String jsonData = Objects.requireNonNull(objectMapper.writeValueAsString(
                                Map.of("content", token, "streaming", true)));
                        emitter.send(SseEmitter.event().name("message").data(jsonData));
                    } catch (java.io.IOException e) {
                        log.debug("SSE token发送失败: {}", e.getMessage());
                    }
                },
                // onComplete
                () -> {
                    try {
                        emitter.complete();
                    } catch (Exception e) {
                        log.debug("SSE emitter关闭异常: {}", e.getMessage());
                    }
                }
        );

        emitter.onTimeout(() -> {
            log.warn("SSE食谱优化连接超时");
            emitter.complete();
        });
        emitter.onError(e -> {
            log.error("SSE食谱优化连接错误", e);
        });

        return emitter;
    }

    private void sendRecipeSseEvent(SseEmitter emitter, String message) {
        try {
            final String jsonData = Objects.requireNonNull(objectMapper.writeValueAsString(
                    Map.of("message", message, "progress", true)));
            emitter.send(SseEmitter.event().name("message").data(jsonData));
        } catch (java.io.IOException e) {
            log.debug("SSE进度事件发送失败: {}", e.getMessage());
        }
    }

    private void sendRecipeSseData(SseEmitter emitter, Object data) {
        try {
            final String jsonData = Objects.requireNonNull(objectMapper.writeValueAsString(data));
            emitter.send(SseEmitter.event().name("message").data(jsonData));
        } catch (java.io.IOException e) {
            log.debug("SSE数据发送失败: {}", e.getMessage());
        }
    }

    // ==================== 菜品描述生成 ====================

    /**
     * AI菜品描述生成接口
     *
     * 路由: /v1/ai/dish-description
     */
    @ApiOperation(value = "AI菜品描述生成", notes = "使用AI生成吸引人的菜品描述")
    @PostMapping("/dish-description")
    public ResponseResult<?> generateDishDescription(@RequestBody DishDescriptionRequestDTO request) {
        try {
            if (request.getName() == null || request.getName().isEmpty()) {
                return ResponseResult.fail("400", "菜品名称不能为空");
            }
            String description = dishDescriptionService.generateDescription(request);
            return ResponseResult.success(description);
        } catch (Exception e) {
            log.error("菜品描述生成失败", e);
            return ResponseResult.fail("500", "菜品描述生成失败：" + e.getMessage());
        }
    }

    // ==================== 健康检查 ====================

    /**
     * 健康检查接口
     *
     * 路由: /v1/ai/health
     */
    @ApiOperation(value = "AI 能力清单", notes = "返回当前模型支持的能力，供前端动态调整UI")
    @GetMapping("/capabilities")
    public ResponseResult<AiCapabilitiesDTO> getCapabilities() {
        AiCapabilitiesDTO capabilities = chatModelProvider.getCapabilities();
        return ResponseResult.success(capabilities);
    }

    @ApiOperation(value = "健康检查", notes = "检查AI服务状态")
    @GetMapping("/health")
    public ResponseResult<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "AI Assistant");
        health.put("version", "3.0.0");
        health.put("timestamp", System.currentTimeMillis());
        health.put("features", new String[]{
            "菜品识别",
            "食谱优化"
        });
        health.put("architecture", "L2→L1两层架构");
        health.put("chatEndpoint", "/agent/supervisor-sse/chat");

        return ResponseResult.success(health);
    }
}
