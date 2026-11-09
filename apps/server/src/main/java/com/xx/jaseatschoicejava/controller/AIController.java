package com.xx.jaseatschoicejava.controller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.FileUploadConfig;
import com.xx.jaseatschoicejava.dto.DishDescriptionRequestDTO;
import com.xx.jaseatschoicejava.service.DishDescriptionService;
import com.xx.jaseatschoicejava.agent.config.ChatModelProvider;
import com.xx.jaseatschoicejava.agent.config.DynamicAiModelRegistry;
import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.service.AiAssistantService;
import com.xx.jaseatschoicejava.service.AiQuotaScope;
import com.xx.jaseatschoicejava.service.AiQuotaService;
import com.xx.jaseatschoicejava.service.AiRuntimeConfigService;
import com.xx.jaseatschoicejava.service.DishRecognitionCacheService;
import com.xx.jaseatschoicejava.util.UserIdentityUtil;

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
    private static final String AI_NOT_CONFIGURED_CODE = "AI_NOT_CONFIGURED";
    private static final String AI_VISION_NOT_CONFIGURED_CODE = "AI_VISION_NOT_CONFIGURED";
    /** 模型实际拒绝图片输入时的稳定能力错误码（区别于泛化 500）。 */
    private static final String AI_VISION_UNSUPPORTED_CODE = "AI_VISION_UNSUPPORTED";
    private static final String INVALID_REQUEST_CODE = "INVALID_REQUEST";
    private static final String AI_CALL_FAILED_CODE = "AI_CALL_FAILED";
    private static final String AI_RESULT_PARSE_FAILED_CODE = "AI_RESULT_PARSE_FAILED";
    private static final String AI_TIMEOUT_CODE = "AI_TIMEOUT";
    private static final String IMAGE_DIMENSIONS_INVALID_CODE = "IMAGE_DIMENSIONS_INVALID";
    private static final String IMAGE_DIMENSIONS_EXCEEDED_CODE = "IMAGE_DIMENSIONS_EXCEEDED";
    /**
     * SSE Emitter 超时相对统一超时配置的宽限；必须严格大于看门狗触发的超时，
     * 保证先由看门狗发出 error(AI_TIMEOUT)+done，Emitter 超时仅作最后兜底。
     */
    private static final long RECIPE_STREAM_EMITTER_GRACE_MS = 5000L;
    private static final long MAX_REMOTE_IMAGE_BYTES = 10 * 1024 * 1024;
    private static final int MAX_IMAGE_DIMENSION = 8192;
    private static final long MAX_IMAGE_PIXELS = 25_000_000L;
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

    @Resource
    private AiQuotaService aiQuotaService;

    @Resource
    private AiRuntimeConfigService aiRuntimeConfigService;

    @Resource
    private AIConfig aiConfig;

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
                                          HttpServletRequest request) {
        String userId = requireVerifiedUserId(request);
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

        ImageDimensions dimensions = readImageDimensions(imageBytes, contentType);
        if (dimensions == null || dimensions.width() <= 0 || dimensions.height() <= 0) {
            return ResponseResult.fail(IMAGE_DIMENSIONS_INVALID_CODE, "图片尺寸读取失败，请重新选择有效的 JPEG、PNG 或 WebP 图片");
        }
        if (dimensions.width() > MAX_IMAGE_DIMENSION || dimensions.height() > MAX_IMAGE_DIMENSION
            || (long) dimensions.width() * dimensions.height() > MAX_IMAGE_PIXELS) {
            return ResponseResult.fail(IMAGE_DIMENSIONS_EXCEEDED_CODE,
                "图片尺寸或像素过大，请压缩后重试（单边不超过 " + MAX_IMAGE_DIMENSION
                    + " 像素，总像素不超过 " + (MAX_IMAGE_PIXELS / 10_000) + " 万）");
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

        aiQuotaService.consume(userId, AiQuotaScope.DISH_RECOGNITION);

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
                if (Boolean.TRUE.equals(result.get("visionUnsupported"))) {
                    String message = result.get("message") instanceof String value && !value.isBlank()
                        ? value : "当前视觉模型不支持图片输入，菜品识别暂不可用";
                    // 运行时降级：capabilities.vision 立即变 false，入口不再宣称已开放，需重新验证。
                    aiRuntimeConfigService.markVisionUnverified(message);
                    return ResponseResult.fail(AI_VISION_UNSUPPORTED_CODE, message);
                }
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

    private ImageDimensions readImageDimensions(byte[] bytes, String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> readJpegDimensions(bytes);
            case "image/png" -> readPngDimensions(bytes);
            case "image/webp" -> readWebpDimensions(bytes);
            default -> null;
        };
    }

    private ImageDimensions readPngDimensions(byte[] bytes) {
        if (bytes.length < 24 || !matchesAscii(bytes, 12, "IHDR")) {
            return null;
        }
        return new ImageDimensions(readIntBigEndian(bytes, 16), readIntBigEndian(bytes, 20));
    }

    private ImageDimensions readJpegDimensions(byte[] bytes) {
        int offset = 2;
        while (offset + 3 < bytes.length) {
            if ((bytes[offset] & 0xFF) != 0xFF) {
                return null;
            }
            while (offset < bytes.length && (bytes[offset] & 0xFF) == 0xFF) {
                offset++;
            }
            if (offset >= bytes.length) {
                return null;
            }

            int marker = bytes[offset++] & 0xFF;
            if (marker == 0xD8 || marker == 0xD9 || marker == 0x01
                || (marker >= 0xD0 && marker <= 0xD7)) {
                continue;
            }
            if (marker == 0xDA || offset + 2 > bytes.length) {
                return null;
            }

            int segmentLength = readUnsignedShortBigEndian(bytes, offset);
            if (segmentLength < 2 || offset + segmentLength > bytes.length) {
                return null;
            }
            if (isJpegStartOfFrame(marker)) {
                if (segmentLength < 7) {
                    return null;
                }
                return new ImageDimensions(
                    readUnsignedShortBigEndian(bytes, offset + 5),
                    readUnsignedShortBigEndian(bytes, offset + 3));
            }
            offset += segmentLength;
        }
        return null;
    }

    private boolean isJpegStartOfFrame(int marker) {
        return (marker >= 0xC0 && marker <= 0xC3)
            || (marker >= 0xC5 && marker <= 0xC7)
            || (marker >= 0xC9 && marker <= 0xCB)
            || (marker >= 0xCD && marker <= 0xCF);
    }

    private ImageDimensions readWebpDimensions(byte[] bytes) {
        if (bytes.length < 25) {
            return null;
        }
        if (matchesAscii(bytes, 12, "VP8X")) {
            if (bytes.length < 30) {
                return null;
            }
            return new ImageDimensions(readInt24LittleEndian(bytes, 24) + 1,
                readInt24LittleEndian(bytes, 27) + 1);
        }
        if (matchesAscii(bytes, 12, "VP8 ")) {
            if (bytes.length < 30 || (bytes[23] & 0xFF) != 0x9D
                || (bytes[24] & 0xFF) != 0x01 || (bytes[25] & 0xFF) != 0x2A) {
                return null;
            }
            return new ImageDimensions(readUnsignedShortLittleEndian(bytes, 26) & 0x3FFF,
                readUnsignedShortLittleEndian(bytes, 28) & 0x3FFF);
        }
        if (matchesAscii(bytes, 12, "VP8L")) {
            if ((bytes[20] & 0xFF) != 0x2F) {
                return null;
            }
            int bits = readIntLittleEndian(bytes, 21);
            return new ImageDimensions((bits & 0x3FFF) + 1, ((bits >>> 14) & 0x3FFF) + 1);
        }
        return null;
    }

    private boolean matchesAscii(byte[] bytes, int offset, String value) {
        if (offset + value.length() > bytes.length) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if ((bytes[offset + i] & 0xFF) != value.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private int readIntBigEndian(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24) | ((bytes[offset + 1] & 0xFF) << 16)
            | ((bytes[offset + 2] & 0xFF) << 8) | (bytes[offset + 3] & 0xFF);
    }

    private int readIntLittleEndian(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8)
            | ((bytes[offset + 2] & 0xFF) << 16) | ((bytes[offset + 3] & 0xFF) << 24);
    }

    private int readInt24LittleEndian(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8)
            | ((bytes[offset + 2] & 0xFF) << 16);
    }

    private int readUnsignedShortBigEndian(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 8) | (bytes[offset + 1] & 0xFF);
    }

    private int readUnsignedShortLittleEndian(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8);
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
    public ResponseResult<?> dishRecognizeByUrl(@RequestBody Map<String, Object> params,
                                                HttpServletRequest request) {
        String userId = requireVerifiedUserId(request);
        String imageUrl = params.get("imageUrl") instanceof String value ? value.trim() : "";
        if (imageUrl.isEmpty()) {
            return ResponseResult.fail("400", "图片地址不能为空");
        }
        aiQuotaService.consume(userId, AiQuotaScope.DISH_RECOGNITION);
        try {
            Map<String, Object> result = aiAssistantService.recognizeDish(imageUrl);
            return ResponseResult.success(result);
        } catch (RuntimeException e) {
            log.error("菜品识别失败", e);
            return ResponseResult.fail("500", "菜品识别失败，请稍后重试");
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

    private record ImageDimensions(int width, int height) {
    }

    // ==================== 食谱优化 ====================

    /**
     * AI食谱优化接口
     *
     * 路由: /v1/ai/recipe-upload
     */
    @ApiOperation(value = "AI食谱优化", notes = "上传食谱进行优化建议")
    @PostMapping("/recipe-upload")
    public ResponseResult<?> recipeUpload(@RequestBody Map<String, Object> params,
                                          HttpServletRequest request) {
        String userId = requireVerifiedUserId(request);
        Object recipeObj = params.get("recipe");
        if (!(recipeObj instanceof Map<?, ?> recipe)) {
            return ResponseResult.fail("400", "食谱内容格式不正确");
        }

        Object recipeTextObj = recipe.get("text");
        String recipeText = recipeTextObj != null ? recipeTextObj.toString() : null;
        if (recipeText == null || recipeText.trim().isEmpty()) {
            return ResponseResult.fail("400", "食谱内容不能为空");
        }

        aiQuotaService.consume(userId, AiQuotaScope.RECIPE);
        try {
            Map<String, Object> result = aiAssistantService.optimizeRecipe(
                recipeText, AiAssistantService.DEFAULT_RECIPE_GOAL);
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
     * 接收: { "recipe": "食谱内容", "goal": "优化目标（可选，默认 balanced）" }
     * 返回: 单方案统一结构
     */
    @ApiOperation(value = "AI食谱优化（统一入口）", notes = "根据食谱内容与优化目标生成单方案建议")
    @PostMapping("/recipe")
    public ResponseResult<?> recipeOptimize(@RequestBody Map<String, Object> params,
                                            HttpServletRequest request) {
        String userId = requireVerifiedUserId(request);
        Object recipeValue = params.get("recipe");
        String recipeText = recipeValue == null ? null : recipeValue.toString();
        if (recipeText == null || recipeText.trim().isEmpty()) {
            return ResponseResult.fail("400", "食谱内容不能为空");
        }

        String goal = resolveRecipeGoal(params.get("goal"));
        if (goal == null) {
            // 非法目标用真实 HTTP 400 表达（不是 200 + body code），且发生在配额与模型调用之前。
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "不支持的优化目标：" + params.get("goal"));
        }

        if (!chatModelProvider.getCapabilities().isTextChat()) {
            return ResponseResult.fail(AI_NOT_CONFIGURED_CODE, "食谱优化暂未开放：请先配置 AI 模型");
        }

        aiQuotaService.consume(userId, AiQuotaScope.RECIPE);

        try {
            log.info("食谱优化请求，目标：{}，输入长度：{}", goal, recipeText.length());
            Map<String, Object> result = aiAssistantService.optimizeRecipe(recipeText, goal);

            if (Boolean.TRUE.equals(result.get("error"))) {
                String errorMsg = (String) result.get("message");
                log.warn("食谱优化服务返回错误：{}", errorMsg);
                String errorCode = errorMsg != null && errorMsg.contains("未配置")
                        ? AI_NOT_CONFIGURED_CODE : "500";
                return ResponseResult.fail(errorCode, errorMsg != null ? errorMsg : "食谱优化失败");
            }

            result.remove("error");
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
     * 事件名：
     * - progress: {"message":"正在分析食谱内容..."}
     * - token:    {"content":"token文本"}
     * - result:   {"result":{...单方案统一结构...}}
     * - error:    {"code":"AI_TIMEOUT","message":"..."}
     * - done:     {"elapsedMs":12345}
     */
    @ApiOperation(value = "AI食谱优化（SSE流式进度）", notes = "实时推送优化进度、流式token与真实耗时，桌面端专用")
    @PostMapping(value = "/recipe/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter recipeOptimizeStream(@RequestBody Map<String, Object> params,
                                           HttpServletRequest request) {
        String userId = requireVerifiedUserId(request);
        long startedAt = System.currentTimeMillis();
        long timeoutMs = recipeStreamTimeoutMs();
        // Emitter 超时必须严格大于模型超时/看门狗，仅作最后兜底，避免抢在 error+done 之前关闭响应。
        SseEmitter emitter = new SseEmitter(timeoutMs + RECIPE_STREAM_EMITTER_GRACE_MS);

        Object recipeValue = params.get("recipe");
        String recipeText = recipeValue == null ? null : recipeValue.toString();
        if (recipeText == null || recipeText.trim().isEmpty()) {
            finishRecipeStreamWithError(emitter, INVALID_REQUEST_CODE, "食谱内容不能为空",
                startedAt, new AtomicBoolean(false));
            return emitter;
        }

        String goal = resolveRecipeGoal(params.get("goal"));
        if (goal == null) {
            finishRecipeStreamWithError(emitter, INVALID_REQUEST_CODE,
                "不支持的优化目标：" + params.get("goal"), startedAt, new AtomicBoolean(false));
            return emitter;
        }

        if (!chatModelProvider.getCapabilities().isStreaming()) {
            finishRecipeStreamWithError(emitter, AI_NOT_CONFIGURED_CODE,
                "食谱优化暂未开放：请先配置支持流式输出的 AI 模型", startedAt, new AtomicBoolean(false));
            return emitter;
        }

        aiQuotaService.consume(userId, AiQuotaScope.RECIPE);
        mapRecipeResultStream(emitter, recipeText, goal, startedAt, timeoutMs);
        return emitter;
    }

    private void mapRecipeResultStream(SseEmitter emitter, String recipeText, String goal, long startedAt,
                                       long timeoutMs) {
        AtomicBoolean done = new AtomicBoolean(false);
        AtomicBoolean resultSent = new AtomicBoolean(false);

        // langchain4j 默认的 JDK HttpClient 在流式读取时没有可用的读超时：上游发送响应头后挂起时
        // 模型超时不会触发，而 Emitter 超时时响应已关闭、无法再补发事件。因此这里按统一配置主动兜底，
        // 保证客户端一定收到 error(AI_TIMEOUT) + done，并保持与前端取消定时器一致的超时语义。
        CompletableFuture.delayedExecutor(timeoutMs, TimeUnit.MILLISECONDS).execute(() -> {
            if (done.get()) {
                return;
            }
            log.warn("食谱优化生成超时，主动结束 SSE（{}ms）", timeoutMs);
            sendRecipeErrorAndDone(emitter, resultSent, done, AI_TIMEOUT_CODE, "生成超时，请稍后重试", startedAt);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE食谱优化连接超时");
            finishRecipeStreamWithError(emitter, AI_TIMEOUT_CODE, "生成超时，请稍后重试", startedAt, done);
        });
        emitter.onError(e -> log.error("SSE食谱优化连接错误", e));

        aiAssistantService.optimizeRecipeStreaming(
            recipeText,
            goal,
            message -> {
                if (done.get()) {
                    return;
                }
                if (message.startsWith("RESULT:")) {
                    if (!resultSent.compareAndSet(false, true)) {
                        return;
                    }
                    try {
                        Object resultObj = objectMapper.readValue(message.substring(7), Object.class);
                        sendRecipeSseEvent(emitter, "result", Map.of("result", resultObj));
                    } catch (Exception e) {
                        log.error("SSE食谱优化结果解析失败", e);
                        sendRecipeErrorAndDone(emitter, resultSent, done,
                            AI_RESULT_PARSE_FAILED_CODE, "结果解析失败", startedAt);
                    }
                } else if (message.startsWith("ERROR:")) {
                    sendRecipeErrorAndDone(emitter, resultSent, done,
                        parseRecipeErrorCode(message), parseRecipeErrorMessage(message), startedAt);
                } else {
                    sendRecipeSseEvent(emitter, "progress", Map.of("message", message));
                }
            },
            token -> {
                if (!done.get() && !resultSent.get()) {
                    sendRecipeSseEvent(emitter, "token", Map.of("content", token));
                }
            },
            // done.elapsedMs 统一取本次请求的端到端耗时（startedAt），与错误/超时路径保持一致，
            // 因此这里忽略服务层回传的模型调用耗时（serviceElapsedMs）。
            serviceElapsedMs -> {
                if (!resultSent.get()) {
                    sendRecipeErrorAndDone(emitter, resultSent, done,
                        AI_RESULT_PARSE_FAILED_CODE, "没有找到合适的优化食谱", startedAt);
                    return;
                }
                finishRecipeStream(emitter, startedAt, done);
            }
        );
    }

    /**
     * 发送 error + done，并结束 SSE；同一连接只允许一次终态。
     */
    private void sendRecipeErrorAndDone(SseEmitter emitter,
                                        AtomicBoolean resultSent,
                                        AtomicBoolean done,
                                        String code,
                                        String message,
                                        long startedAt) {
        if (!done.compareAndSet(false, true)) {
            return;
        }
        resultSent.set(true);
        sendRecipeSseEvent(emitter, "error", Map.of("code", code, "message", message));
        sendRecipeSseEvent(emitter, "done",
            Map.of("elapsedMs", Math.max(0, System.currentTimeMillis() - startedAt)));
        completeRecipeEmitter(emitter);
    }

    private void finishRecipeStreamWithError(SseEmitter emitter,
                                             String code,
                                             String message,
                                             long startedAt,
                                             AtomicBoolean done) {
        sendRecipeErrorAndDone(emitter, new AtomicBoolean(false), done, code, message, startedAt);
    }

    private void finishRecipeStream(SseEmitter emitter, long startedAt, AtomicBoolean done) {
        if (!done.compareAndSet(false, true)) {
            return;
        }
        sendRecipeSseEvent(emitter, "done",
            Map.of("elapsedMs", Math.max(0, System.currentTimeMillis() - startedAt)));
        completeRecipeEmitter(emitter);
    }

    private String parseRecipeErrorCode(String message) {
        // 约定格式为 ERROR:<CODE>:<MESSAGE>，错误码是 "ERROR:" 之后、首个冒号之前的那段
        String[] parts = message.substring(6).split(":", 2);
        return !parts[0].isBlank() ? parts[0] : AI_CALL_FAILED_CODE;
    }

    private String parseRecipeErrorMessage(String message) {
        // 约定格式为 ERROR:<CODE>:<MESSAGE>，消息取冒号后的剩余全文，避免含冒号时被截断
        String[] parts = message.substring(6).split(":", 2);
        if (parts.length == 2 && !parts[1].isBlank()) {
            return parts[1];
        }
        return "AI调用失败，请稍后重试";
    }

    private void completeRecipeEmitter(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception e) {
            log.debug("SSE emitter关闭异常: {}", e.getMessage());
        }
    }

    /**
     * 校验并归一化优化目标；未知目标返回 null。
     */
    private String resolveRecipeGoal(Object rawGoal) {
        if (rawGoal == null) {
            return AiAssistantService.DEFAULT_RECIPE_GOAL;
        }
        String goal = rawGoal.toString().trim();
        if (goal.isEmpty()) {
            return AiAssistantService.DEFAULT_RECIPE_GOAL;
        }
        return AiAssistantService.RECIPE_GOALS.contains(goal) ? goal : null;
    }

    private void sendRecipeSseEvent(SseEmitter emitter, String eventName, Object data) {
        try {
            final String jsonData = Objects.requireNonNull(objectMapper.writeValueAsString(data));
            emitter.send(SseEmitter.event().name(eventName).data(jsonData));
        } catch (IOException | IllegalStateException e) {
            log.debug("SSE {}事件发送失败: {}", eventName, e.getMessage());
        }
    }

    /**
     * 只从 JwtAuthenticationFilter 写入的可信属性取用户 ID，忽略客户端传入的任何身份。
     */
    private String requireVerifiedUserId(HttpServletRequest request) {
        String userId = UserIdentityUtil.extractVerifiedUserId(request);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "当前账号缺少可用身份，请重新登录");
        }
        return userId;
    }

    private long recipeStreamTimeoutMs() {
        Integer configured = aiConfig == null ? null : aiConfig.getRecipeStreamTimeoutMs();
        return configured != null && configured > 0 ? configured : 120000L;
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
