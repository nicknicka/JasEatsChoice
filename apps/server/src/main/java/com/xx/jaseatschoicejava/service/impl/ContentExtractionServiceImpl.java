package com.xx.jaseatschoicejava.service.impl;

import com.xx.jaseatschoicejava.dto.ContentExtractionUpdateDTO;
import com.xx.jaseatschoicejava.dto.ContentSourceCreateDTO;
import com.xx.jaseatschoicejava.entity.ContentExtraction;
import com.xx.jaseatschoicejava.entity.ContentSource;
import com.xx.jaseatschoicejava.entity.ExtractionTask;
import com.xx.jaseatschoicejava.enums.ContentPlatform;
import com.xx.jaseatschoicejava.enums.ContentType;
import com.xx.jaseatschoicejava.enums.ExtractionStatus;
import com.xx.jaseatschoicejava.mapper.ContentExtractionMapper;
import com.xx.jaseatschoicejava.mapper.ContentSourceMapper;
import com.xx.jaseatschoicejava.mapper.ExtractionTaskMapper;
import com.xx.jaseatschoicejava.service.ContentExtractionService;
import com.xx.jaseatschoicejava.service.RecipeService;
import com.xx.jaseatschoicejava.service.extraction.dto.FetchedContent;
import com.xx.jaseatschoicejava.service.extraction.fetcher.ContentFetcher;
import com.xx.jaseatschoicejava.service.extraction.fetcher.ContentFetcherFactory;
import com.xx.jaseatschoicejava.service.extraction.recognizer.ContentRecognitionResult;
import com.xx.jaseatschoicejava.service.extraction.recognizer.ContentRecognizer;
import com.xx.jaseatschoicejava.util.UrlSecurityValidator;
import com.xx.jaseatschoicejava.vo.ContentExtractionDetailVO;
import com.xx.jaseatschoicejava.vo.ContentSourceVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
        } catch (ExtractionParseException e) {
            log.error("提取任务解析失败: taskId={}", task.getId(), e);
            handleParseFailure(task, e);
 * - 文章/图文：抓取正文和图片后交给通用视觉模型识别
 * - 视频：抓取标题、描述和封面，当前不调用厂商视频理解接口
 * - 图片：下载后交给通用视觉模型识别
 *

 * @since 2025-01-31
 * @updated 2026-04-08 实现真实的内容抓取和AI识别
 */
@Service
public class ContentExtractionServiceImpl implements ContentExtractionService {

    private static final Logger log = LoggerFactory.getLogger(ContentExtractionServiceImpl.class);

    private static final int MAX_RETRY_COUNT = 3;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ContentSourceMapper contentSourceMapper;

    @Autowired
    private ContentExtractionMapper contentExtractionMapper;

    @Autowired
    private ExtractionTaskMapper extractionTaskMapper;

    @Autowired
    private ContentFetcherFactory fetcherFactory;

    @Autowired
    private ContentRecognizer contentRecognizer;

    @Autowired
    private UrlSecurityValidator urlSecurityValidator;

    @Autowired
    private RecipeService recipeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addContentSource(ContentSourceCreateDTO dto, String userId) {
        String contentUrl = urlSecurityValidator.validateAndResolve(normalizeContentUrl(dto.getContentUrl()));

        // 自动识别平台和内容类型
        ContentPlatform platform = ContentPlatform.parseFromUrl(contentUrl);
        ContentType contentType = resolveContentType(dto.getContentType(), contentUrl);

        // 创建内容源
        ContentSource source = new ContentSource();
        source.setId(UUID.randomUUID().toString().replace("-", ""));
        source.setUserId(userId);
        source.setContentUrl(contentUrl);
        source.setContentType(contentType.getCode());
        source.setPlatform(platform.getCode());
        source.setExtractionStatus(ExtractionStatus.PENDING.getCode());
        source.setIsExtracted(false);
        source.setCreatedAt(LocalDateTime.now());
        source.setUpdatedAt(LocalDateTime.now());

        contentSourceMapper.insert(source);

        // 创建提取任务
        ExtractionTask task = new ExtractionTask();
        task.setId(UUID.randomUUID().toString().replace("-", ""));
        task.setSourceId(source.getId());
        task.setTaskType("EXTRACT"); // 简化处理，统一使用EXTRACT
        task.setTaskStatus("PENDING");
        task.setRetryCount(0);
        task.setPriority(0);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        extractionTaskMapper.insert(task);

        log.info("创建内容源和提取任务: sourceId={}, taskId={}, url={}",
            source.getId(), task.getId(), contentUrl);

        return source.getId();
    }

    @Override
    public List<ContentSourceVO> getUserContentSources(String userId) {
        List<ContentSource> sources = contentSourceMapper.selectByUserId(userId);
        return sources.stream()
            .map(this::convertToContentSourceVO)
            .collect(Collectors.toList());
    }

    @Override
    public ContentSourceVO getContentSourceDetail(String sourceId, String userId) {
        ContentSource source = contentSourceMapper.selectById(sourceId);
        if (source == null) {
            throw new RuntimeException("内容源不存在");
        }

        if (!source.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该内容源");
        }

        return convertToContentSourceVO(source);
    }

    @Override
    public ContentExtractionDetailVO getExtractionDetail(String extractionId, String userId) {
        ContentExtraction extraction = contentExtractionMapper.selectById(extractionId);
        if (extraction == null) {
            throw new RuntimeException("提取内容不存在");
        }

        ContentSource source = contentSourceMapper.selectById(extraction.getSourceId());
        if (source == null || !source.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该提取内容");
        }

        return convertToExtractionDetailVO(extraction, source);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateExtraction(ContentExtractionUpdateDTO dto, String userId) {
        ContentExtraction extraction = contentExtractionMapper.selectById(dto.getExtractionId());
        if (extraction == null) {
            throw new RuntimeException("提取内容不存在");
        }

        ContentSource source = contentSourceMapper.selectById(extraction.getSourceId());
        if (source == null || !source.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改该提取内容");
        }

        // 更新提取内容
        extraction.setDishName(dto.getDishName());
        extraction.setDishImage(dto.getDishImage());
        extraction.setDescription(dto.getDescription());

        // 转换食材列表为JSON
        if (dto.getIngredients() != null) {
            extraction.setIngredients(serializeToJson(dto.getIngredients(), "食材列表"));
        }

        // 转换步骤列表为JSON
        if (dto.getSteps() != null) {
            extraction.setSteps(serializeToJson(dto.getSteps(), "步骤列表"));
        }

        extraction.setCookingTime(dto.getCookingTime());
        extraction.setDifficulty(dto.getDifficulty());

        // 转换标签为逗号分隔字符串
        if (dto.getTags() != null) {
            extraction.setTags(String.join(",", dto.getTags()));
        }

        extraction.setCalories(dto.getCalories());
        extraction.setUpdatedAt(LocalDateTime.now());

        int updated = contentExtractionMapper.updateById(extraction);
        return updated > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String publishAsRecipe(String extractionId, String mealType, String userId) {
        // 锁住提取记录，避免并发发布各自创建一条食谱。
        ContentExtraction extraction = contentExtractionMapper.selectByIdForUpdate(extractionId);
        if (extraction == null) {
            throw new RuntimeException("提取内容不存在");
        }

        ContentSource source = contentSourceMapper.selectById(extraction.getSourceId());
        if (source == null || !source.getUserId().equals(userId)) {
            throw new RuntimeException("无权发布该提取内容");
        }

        if (Boolean.TRUE.equals(extraction.getIsPublished())
            && extraction.getRecipeId() != null
            && recipeService.getById(extraction.getRecipeId()) != null) {
            return extraction.getRecipeId();
        }

        String recipeId = recipeService.createFromExtraction(extraction, source, mealType).getId();

        // 更新提取记录
        extraction.setIsPublished(true);
        extraction.setRecipeId(recipeId);
        extraction.setUpdatedAt(LocalDateTime.now());

        if (contentExtractionMapper.updateById(extraction) <= 0) {
            throw new RuntimeException("发布状态更新失败");
        }

        log.info("发布提取内容为食谱: extractionId={}, recipeId={}", extractionId, recipeId);

        return recipeId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyExtraction(String extractionId, Boolean verified, Integer score, String userId) {
        ContentExtraction extraction = contentExtractionMapper.selectById(extractionId);
        if (extraction == null) {
            throw new RuntimeException("提取内容不存在");
        }

        ContentSource source = contentSourceMapper.selectById(extraction.getSourceId());
        if (source == null || !source.getUserId().equals(userId)) {
            throw new RuntimeException("无权验证该提取内容");
        }

        extraction.setIsVerified(verified);
        extraction.setManualScore(score);
        extraction.setUpdatedAt(LocalDateTime.now());

        int updated = contentExtractionMapper.updateById(extraction);
        return updated > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reExtract(String sourceId, String contentType, String userId) {
        ContentSource source = contentSourceMapper.selectById(sourceId);
        if (source == null) {
            throw new RuntimeException("内容源不存在");
        }

        if (!source.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该内容源");
        }

        String contentUrl = normalizeContentUrl(source.getContentUrl());
        ContentPlatform platform = ContentPlatform.parseFromUrl(contentUrl);
        ContentType resolvedContentType = resolveContentTypeForReExtract(contentType, source, contentUrl);

        // 重置提取状态；未显式指定类型时沿用已保存的类型
        source.setContentUrl(contentUrl);
        source.setPlatform(platform.getCode());
        source.setContentType(resolvedContentType.getCode());
        source.setExtractionStatus(ExtractionStatus.PENDING.getCode());
        source.setIsExtracted(false);
        source.setErrorMessage(null);
        source.setUpdatedAt(LocalDateTime.now());

        contentSourceMapper.updateById(source);

        // 重新创建提取任务
        ExtractionTask task = new ExtractionTask();
        task.setId(UUID.randomUUID().toString().replace("-", ""));
        task.setSourceId(sourceId);
        task.setTaskType("EXTRACT");
        task.setTaskStatus("PENDING");
        task.setRetryCount(0);
        task.setPriority(1); // 提高优先级
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        extractionTaskMapper.insert(task);

        log.info("重新提取: sourceId={}", sourceId);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteContentSource(String sourceId, String userId) {
        ContentSource source = contentSourceMapper.selectById(sourceId);
        if (source == null) {
            throw new RuntimeException("内容源不存在");
        }

        if (!source.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该内容源");
        }

        // 删除内容源
        contentSourceMapper.deleteById(sourceId);

        // 删除关联的提取内容
        ContentExtraction extraction = contentExtractionMapper.selectBySourceId(sourceId);
        if (extraction != null) {
            contentExtractionMapper.deleteById(extraction.getId());
        }

        log.info("删除内容源: sourceId={}", sourceId);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processPendingTasks() {
        List<ExtractionTask> tasks = extractionTaskMapper.selectPendingTasks();
        int processedCount = 0;

        for (ExtractionTask task : tasks) {
            try {
                doExtract(task);
                processedCount++;
            } catch (ExtractionParseException e) {
                log.error("处理提取任务解析失败: taskId={}", task.getId(), e);
                handleParseFailure(task, e);
            } catch (RuntimeException e) {
                log.error("处理提取任务失败: taskId={}", task.getId(), e);
                handleTaskFailure(task, e);
            }
        }

        return processedCount;
    }

    /**
     * 真实内容提取
     * 抓取内容 → AI识别 → 解析结果 → 写入数据库
     */
    private void doExtract(ExtractionTask task) {
        log.info("开始处理提取任务: taskId={}, sourceId={}", task.getId(), task.getSourceId());

        // 更新任务状态为处理中
        task.setTaskStatus("PROCESSING");
        task.setStartTime(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        extractionTaskMapper.updateById(task);

        ContentSource source = contentSourceMapper.selectById(task.getSourceId());
        if (source == null) {
            throw new RuntimeException("内容源不存在");
        }

        try {
            // 1. 确定平台和内容类型
            ContentPlatform platform = ContentPlatform.getByCode(source.getPlatform());
            ContentType contentType = ContentType.getByCode(source.getContentType());

            // 2. 抓取内容（Layer 1）
            log.info("抓取内容: platform={}, type={}, url={}", platform, contentType, source.getContentUrl());
            ContentFetcher fetcher = fetcherFactory.getFetcher(platform, contentType);
            FetchedContent fetchedContent = fetcher.fetch(source.getContentUrl(), source.getUserId());

            // 缺登录态是可自愈的最终状态，不进入解析失败，也不继续调用 AI。
            if (fetchedContent.isAuthRequired()) {
                String detail = fetchedContent.getErrorMessage() != null
                    && !fetchedContent.getErrorMessage().isBlank()
                    ? fetchedContent.getErrorMessage()
                    : "需要登录小红书后重试";

                task.setTaskStatus("FAILED");
                task.setErrorMessage(detail);
                task.setEndTime(LocalDateTime.now());
                task.setUpdatedAt(LocalDateTime.now());
                extractionTaskMapper.updateById(task);

                source.setExtractionStatus(ExtractionStatus.AUTH_REQUIRED.getCode());
                source.setIsExtracted(false);
                source.setErrorMessage(detail);
                source.setUpdatedAt(LocalDateTime.now());
                contentSourceMapper.updateById(source);

                log.info("提取任务需要登录态: taskId={}, sourceId={}", task.getId(), source.getId());
                return;
            }

            // 抓不到任何可用内容时直接失败：视频有封面图时仍可进入封面降级路径。
            if (!fetchedContent.hasText() && !fetchedContent.hasImages()
                    && !fetchedContent.hasVideo() && !hasText(fetchedContent.getCoverImage())) {
                String detail = fetchedContent.getErrorMessage();
                throw new ExtractionParseException(
                    detail != null && !detail.isBlank()
                        ? detail
                        : "未能从链接获取到内容，可能需要登录或链接已失效",
                    null, null);
            }

            // 3. AI识别（Layer 2）
            log.info("AI识别: fetchSuccess={}, hasImages={}, hasVideo={}, hasText={}",
                fetchedContent.isFetchSuccess(),
                fetchedContent.hasImages(),
                fetchedContent.hasVideo(),
                fetchedContent.hasText());
            ContentRecognitionResult recognition = contentRecognizer.recognize(fetchedContent);
            String aiResponse = recognition.response();

            log.info("AI识别结果: {}", aiResponse.length() > 200 ? aiResponse.substring(0, 200) + "..." : aiResponse);

            // 4. 解析结果（Layer 3）
            Map<String, Object> extractionResult = parseExtractionResult(aiResponse);

            // AI 判定不是菜谱时不应落库：否则会生成一条“未命名菜品”的空记录
            if (Boolean.FALSE.equals(extractionResult.get("isRecipe"))) {
                throw new ExtractionParseException("该内容不是菜谱，无法提取", aiResponse, null);
            }

            // 5. 补充抓取到的元数据
            if (fetchedContent.getTitle() != null && !fetchedContent.getTitle().isEmpty()) {
                source.setTitle(fetchedContent.getTitle());
            }
            if (fetchedContent.getAuthor() != null && !fetchedContent.getAuthor().isEmpty()) {
                source.setAuthor(fetchedContent.getAuthor());
            }
            if (fetchedContent.getCoverImage() != null && !fetchedContent.getCoverImage().isEmpty()) {
                source.setCoverImage(fetchedContent.getCoverImage());
            }
            source.setUpdatedAt(LocalDateTime.now());
            contentSourceMapper.updateById(source);

            // 6. 创建提取记录
            ContentExtraction extraction = new ContentExtraction();
            extraction.setId(UUID.randomUUID().toString().replace("-", ""));
            extraction.setSourceId(source.getId());
            extraction.setDishName((String) extractionResult.getOrDefault("dishName", "未知菜品"));
            extraction.setDescription((String) extractionResult.getOrDefault("description", ""));
            extraction.setIngredients(serializeToJson(extractionResult.get("ingredients"), "食材列表"));
            extraction.setSteps(serializeToJson(extractionResult.get("steps"), "步骤列表"));
            extraction.setCookingTime(extractIntValue(extractionResult.get("cookingTime"), 30));
            extraction.setDifficulty((String) extractionResult.getOrDefault("difficulty", "中等"));

            extraction.setTags(joinStringList(extractionResult.get("tags")));

            extraction.setCalories(extractIntValue(extractionResult.get("calories"), 0));

            // 如果有封面图，设置为菜品图片
            if (fetchedContent.getCoverImage() != null && !fetchedContent.getCoverImage().isEmpty()) {
                extraction.setDishImage(fetchedContent.getCoverImage());
            }

            extraction.setIsPublished(false);
            extraction.setIsVerified(false);
            extraction.setCreatedAt(LocalDateTime.now());
            extraction.setUpdatedAt(LocalDateTime.now());

            contentExtractionMapper.insert(extraction);

            // 7. 更新内容源状态
            source.setExtractionStatus(ExtractionStatus.SUCCESS.getCode());
            source.setIsExtracted(true);
            source.setErrorMessage(recognition.degradationReason());
            source.setExtractionTime(LocalDateTime.now());
            source.setUpdatedAt(LocalDateTime.now());
            contentSourceMapper.updateById(source);

            // 8. 更新任务状态为成功
            task.setTaskStatus("SUCCESS");
            task.setEndTime(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
            extractionTaskMapper.updateById(task);

            log.info("提取任务完成: taskId={}, extractionId={}, dishName={}",
                task.getId(), extraction.getId(), extraction.getDishName());

        } catch (ExtractionParseException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("提取任务失败: taskId={}", task.getId(), e);

            // 更新任务状态为失败
            task.setTaskStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            task.setEndTime(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
            extractionTaskMapper.updateById(task);

            // 更新内容源状态为失败
            source.setExtractionStatus(ExtractionStatus.FAILED.getCode());
            source.setErrorMessage(e.getMessage());
            source.setUpdatedAt(LocalDateTime.now());
            contentSourceMapper.updateById(source);

            throw new RuntimeException("提取任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理任务失败（含重试逻辑）
     */
    private void handleTaskFailure(ExtractionTask task, RuntimeException e) {
        if (task.getRetryCount() < MAX_RETRY_COUNT) {
            task.setRetryCount(task.getRetryCount() + 1);
            task.setTaskStatus("PENDING"); // 重新排队
            task.setUpdatedAt(LocalDateTime.now());
            extractionTaskMapper.updateById(task);
            log.info("任务将重试: taskId={}, retryCount={}", task.getId(), task.getRetryCount());
        } else {
            task.setTaskStatus("FAILED");
            task.setErrorMessage("重试" + MAX_RETRY_COUNT + "次后仍失败: " + e.getMessage());
            task.setEndTime(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
            extractionTaskMapper.updateById(task);
        }
    }

    /**
     * 处理解析失败：不重试，保留原始AI响应供人工排查或后续重新解析。
     */
    private void handleParseFailure(ExtractionTask task, ExtractionParseException e) {
        task.setTaskStatus("FAILED");
        task.setResultData(e.getRawResponse());
        task.setErrorMessage(e.getMessage());
        task.setEndTime(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        extractionTaskMapper.updateById(task);

        ContentSource source = contentSourceMapper.selectById(task.getSourceId());
        if (source != null) {
            source.setExtractionStatus(ExtractionStatus.PARSE_FAILED.getCode());
            source.setIsExtracted(false);
            source.setErrorMessage(e.getMessage());
            source.setUpdatedAt(LocalDateTime.now());
            contentSourceMapper.updateById(source);
        }
    }

    /**
     * 安全提取整数值
     */
    private int extractIntValue(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number number) return number.intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * 解析AI提取结果
     */
    private Map<String, Object> parseExtractionResult(String responseText) {
        try {
            // 提取JSON
            String json = extractJson(responseText);

            // 解析为Map
            Map<String, Object> result = objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});

            // 确保必要字段存在
            result.putIfAbsent("dishName", "未知菜品");
            result.putIfAbsent("description", "");
            result.putIfAbsent("ingredients", new ArrayList<>());
            result.putIfAbsent("steps", new ArrayList<>());
            result.putIfAbsent("cookingTime", 30);
            result.putIfAbsent("difficulty", "中等");
            result.putIfAbsent("tags", new ArrayList<>());
            result.putIfAbsent("calories", 0);

            return result;

        } catch (IllegalArgumentException | JsonProcessingException e) {
            log.error("解析提取结果失败: {}", responseText, e);
            throw new ExtractionParseException("AI识别结果解析失败: " + e.getMessage(), responseText, e);
        }
    }

    /**
     * 从响应文本中提取JSON
     */
    private String extractJson(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("AI返回内容为空");
        }

        String trimmed = text.trim();

        // 处理 ```json ... ``` 格式
        if (trimmed.contains("```json")) {
            int start = trimmed.indexOf("```json") + 7;
            int end = trimmed.indexOf("```", start);
            if (end > start) {
                return trimmed.substring(start, end).trim();
            }
        }

        // 处理 ``` ... ``` 格式
        if (trimmed.contains("```")) {
            int start = trimmed.indexOf("```") + 3;
            while (start < trimmed.length() && !Character.isWhitespace(trimmed.charAt(start)) && trimmed.charAt(start) != '{') {
                start++;
            }
            int end = trimmed.indexOf("```", start);
            if (end > start) {
                return trimmed.substring(start, end).trim();
            }
        }

        // 尝试找到JSON对象的起始和结束
        int jsonStart = trimmed.indexOf('{');
        int jsonEnd = trimmed.lastIndexOf('}');
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return trimmed.substring(jsonStart, jsonEnd + 1);
        }

        throw new IllegalArgumentException("AI返回内容中未找到JSON对象");
    }

    /**
     * 序列化为JSON。失败时直接抛出异常，避免静默丢字段。
     */
    private String serializeToJson(Object value, String fieldName) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("转换{}为JSON失败", fieldName, e);
            throw new RuntimeException("转换" + fieldName + "为JSON失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将 tags 安全拼接为逗号分隔字符串。
     */
    private String joinStringList(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof List<?> items) {
            return items.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
        }

        return value.toString();
    }

    /**
     * 自动检测内容类型
     */
    private ContentType detectContentType(String url) {
        String lowerUrl = url.toLowerCase();
        ContentPlatform platform = ContentPlatform.parseFromUrl(url);
        if (platform == ContentPlatform.XIAOHONGSHU && !lowerUrl.contains("video")) {
            return ContentType.ARTICLE;
        }

        if (lowerUrl.contains("video") || lowerUrl.contains(".mp4")) {
            return ContentType.VIDEO;
        } else if (lowerUrl.contains("article") || lowerUrl.contains("post")) {
            return ContentType.ARTICLE;
        } else if (lowerUrl.contains("image") || lowerUrl.contains(".jpg") || lowerUrl.contains(".png")) {
            return ContentType.IMAGE;
        }
        return ContentType.ARTICLE;
    }

    private ContentType resolveContentType(String requestedType, String contentUrl) {
        if (requestedType == null) {
            return detectContentType(contentUrl);
        }
        ContentType contentType = ContentType.getByCode(requestedType.trim());
        if (contentType == null) {
            throw new IllegalArgumentException("内容类型不合法");
        }
        return contentType;
    }

    private ContentType resolveContentTypeForReExtract(String requestedType, ContentSource source, String contentUrl) {
        if (requestedType != null) {
            return resolveContentType(requestedType, contentUrl);
        }
        ContentType savedType = ContentType.getByCode(source.getContentType());
        return savedType != null ? savedType : detectContentType(contentUrl);
    }

    private String normalizeContentUrl(String rawContentUrl) {
        if (rawContentUrl == null) {
            throw new IllegalArgumentException("内容URL不能为空");
        }

        String trimmed = rawContentUrl.trim();
        Matcher matcher = Pattern.compile(
                "https?://[^\\s，,。；;]+|(?:www\\.)?(?:xiaohongshu\\.com|xhslink\\.com|douyin\\.com|bilibili\\.com|mp\\.weixin\\.qq\\.com|weixin\\.qq\\.com|toutiao\\.com|kuaishou\\.com)[^\\s，,。；;]*",
                Pattern.CASE_INSENSITIVE)
            .matcher(trimmed);
        String url = matcher.find() ? matcher.group() : trimmed;
        url = url.replaceAll("[)\\]}>，,。；;、]+$", "");

        if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
            url = "https://" + url;
        }

        return url;
    }

    /**
     * 转换为内容源VO
     */
    @SuppressWarnings("null")
    private ContentSourceVO convertToContentSourceVO(ContentSource source) {
        ContentSourceVO vo = new ContentSourceVO();
        BeanUtils.copyProperties(source, vo);

        // 设置枚举名称
        ContentType contentType = ContentType.getByCode(source.getContentType());
        if (contentType != null) {
            vo.setContentTypeName(contentType.getDescription());
        }

        ContentPlatform platform = ContentPlatform.getByCode(source.getPlatform());
        if (platform != null) {
            vo.setPlatformName(platform.getName());
        }

        ExtractionStatus status = ExtractionStatus.getByCode(source.getExtractionStatus());
        if (status != null) {
            vo.setExtractionStatusName(status.getDescription());
        }

        // 格式化视频时长
        if (source.getVideoDuration() != null) {
            int minutes = source.getVideoDuration() / 60;
            int seconds = source.getVideoDuration() % 60;
            vo.setVideoDurationFormatted(String.format("%d:%02d", minutes, seconds));
        }

        // 查询提取的菜品信息
        ContentExtraction extraction = contentExtractionMapper.selectBySourceId(source.getId());
        if (extraction != null) {
            vo.setExtractionId(extraction.getId());
            vo.setExtractedDishName(extraction.getDishName());
            vo.setExtractedDishImage(extraction.getDishImage());
            vo.setIsPublished(extraction.getIsPublished());
            vo.setRecipeId(extraction.getRecipeId());
        }

        return vo;
    }

    /**
     * 转换为提取详情VO
     */
    @SuppressWarnings("null")
    private ContentExtractionDetailVO convertToExtractionDetailVO(ContentExtraction extraction, ContentSource source) {
        ContentExtractionDetailVO vo = new ContentExtractionDetailVO();
        BeanUtils.copyProperties(extraction, vo);

        // 设置内容源信息
        vo.setContentUrl(source.getContentUrl());
        vo.setPlatform(source.getPlatform());
        vo.setOriginalTitle(source.getTitle());

        ContentType contentType = ContentType.getByCode(source.getContentType());
        if (contentType != null) {
            vo.setContentType(contentType.getCode());
            vo.setContentTypeName(contentType.getDescription());
        }

        ContentPlatform platform = ContentPlatform.getByCode(source.getPlatform());
        if (platform != null) {
            vo.setPlatformName(platform.getName());
        }

        // 转换JSON数据为对象
        try {
            if (extraction.getIngredients() != null) {
                List<ContentExtractionDetailVO.IngredientItem> ingredients =
                    objectMapper.readValue(extraction.getIngredients(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<ContentExtractionDetailVO.IngredientItem>>() {});
                vo.setIngredients(ingredients);
            }

            if (extraction.getSteps() != null) {
                List<ContentExtractionDetailVO.StepItem> steps =
                    objectMapper.readValue(extraction.getSteps(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<ContentExtractionDetailVO.StepItem>>() {});
                vo.setSteps(steps);
            }

            if (extraction.getTags() != null) {
                vo.setTags(List.of(extraction.getTags().split(",")));
            }
        } catch (JsonProcessingException e) {
            log.error("解析JSON数据失败: extractionId={}", extraction.getId(), e);
            vo.setIngredients(new ArrayList<>());
            vo.setSteps(new ArrayList<>());
            vo.setTags(new ArrayList<>());
        }

        return vo;
    }
}
