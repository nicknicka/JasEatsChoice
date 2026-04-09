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
import com.xx.jaseatschoicejava.service.extraction.dto.FetchedContent;
import com.xx.jaseatschoicejava.service.extraction.fetcher.ContentFetcher;
import com.xx.jaseatschoicejava.service.extraction.fetcher.ContentFetcherFactory;
import com.xx.jaseatschoicejava.service.extraction.recognizer.ContentRecognizer;
import com.xx.jaseatschoicejava.vo.ContentExtractionDetailVO;
import com.xx.jaseatschoicejava.vo.ContentSourceVO;
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
import java.util.stream.Collectors;

/**
 * 内容提取服务实现类
 *
 * 使用 GLM-4.6V-Flash 视觉模型进行内容识别：
 * - 文章/图文：Jsoup抓取 + GLM-4.6V-Flash OCR识别
 * - 视频：平台API获取视频流 + GLM-4.6V-Flash 视频理解
 * - 图片：下载 + GLM-4.6V-Flash 图片识别
 *
 * @author Claude
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addContentSource(ContentSourceCreateDTO dto, String userId) {
        // 自动识别平台和内容类型
        ContentPlatform platform = ContentPlatform.parseFromUrl(dto.getContentUrl());
        ContentType contentType = dto.getContentType() != null ?
            ContentType.getByCode(dto.getContentType()) : detectContentType(dto.getContentUrl());

        // 创建内容源
        ContentSource source = new ContentSource();
        source.setId(UUID.randomUUID().toString().replace("-", ""));
        source.setUserId(userId);
        source.setContentUrl(dto.getContentUrl());
        source.setContentType(contentType != null ? contentType.getCode() : ContentType.VIDEO.getCode());
        source.setPlatform(platform.getCode());
        source.setExtractionStatus(ExtractionStatus.PENDING.getCode());
        source.setIsExtracted(false);
        source.setCreateTime(LocalDateTime.now());
        source.setUpdateTime(LocalDateTime.now());

        contentSourceMapper.insert(source);

        // 创建提取任务
        ExtractionTask task = new ExtractionTask();
        task.setId(UUID.randomUUID().toString().replace("-", ""));
        task.setSourceId(source.getId());
        task.setTaskType("EXTRACT"); // 简化处理，统一使用EXTRACT
        task.setTaskStatus("PENDING");
        task.setRetryCount(0);
        task.setPriority(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        extractionTaskMapper.insert(task);

        log.info("创建内容源和提取任务: sourceId={}, taskId={}, url={}",
            source.getId(), task.getId(), dto.getContentUrl());

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
            try {
                extraction.setIngredients(objectMapper.writeValueAsString(dto.getIngredients()));
            } catch (Exception e) {
                log.error("转换食材列表为JSON失败", e);
            }
        }

        // 转换步骤列表为JSON
        if (dto.getSteps() != null) {
            try {
                extraction.setSteps(objectMapper.writeValueAsString(dto.getSteps()));
            } catch (Exception e) {
                log.error("转换步骤列表为JSON失败", e);
            }
        }

        extraction.setCookingTime(dto.getCookingTime());
        extraction.setDifficulty(dto.getDifficulty());

        // 转换标签为逗号分隔字符串
        if (dto.getTags() != null) {
            extraction.setTags(String.join(",", dto.getTags()));
        }

        extraction.setCalories(dto.getCalories());
        extraction.setUpdateTime(LocalDateTime.now());

        int updated = contentExtractionMapper.updateById(extraction);
        return updated > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String publishAsRecipe(String extractionId, String userId) {
        ContentExtraction extraction = contentExtractionMapper.selectById(extractionId);
        if (extraction == null) {
            throw new RuntimeException("提取内容不存在");
        }

        if (extraction.getIsPublished()) {
            throw new RuntimeException("该内容已发布为食谱");
        }

        // TODO: 调用RecipeService创建食谱
        String recipeId = UUID.randomUUID().toString().replace("-", "");

        // 更新提取记录
        extraction.setIsPublished(true);
        extraction.setRecipeId(recipeId);
        extraction.setUpdateTime(LocalDateTime.now());

        contentExtractionMapper.updateById(extraction);

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

        extraction.setIsVerified(verified);
        extraction.setManualScore(score);
        extraction.setUpdateTime(LocalDateTime.now());

        int updated = contentExtractionMapper.updateById(extraction);
        return updated > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reExtract(String sourceId, String userId) {
        ContentSource source = contentSourceMapper.selectById(sourceId);
        if (source == null) {
            throw new RuntimeException("内容源不存在");
        }

        if (!source.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该内容源");
        }

        // 重置提取状态
        source.setExtractionStatus(ExtractionStatus.PENDING.getCode());
        source.setIsExtracted(false);
        source.setErrorMessage(null);
        source.setUpdateTime(LocalDateTime.now());

        contentSourceMapper.updateById(source);

        // 重新创建提取任务
        ExtractionTask task = new ExtractionTask();
        task.setId(UUID.randomUUID().toString().replace("-", ""));
        task.setSourceId(sourceId);
        task.setTaskType("EXTRACT");
        task.setTaskStatus("PENDING");
        task.setRetryCount(0);
        task.setPriority(1); // 提高优先级
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

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
            } catch (Exception e) {
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
        task.setUpdateTime(LocalDateTime.now());
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
            FetchedContent fetchedContent = fetcher.fetch(source.getContentUrl());

            // 3. AI识别（Layer 2）
            log.info("AI识别: fetchSuccess={}, hasImages={}, hasVideo={}, hasText={}",
                fetchedContent.isFetchSuccess(),
                fetchedContent.hasImages(),
                fetchedContent.hasVideo(),
                fetchedContent.hasText());
            String aiResponse = contentRecognizer.recognize(fetchedContent);

            log.info("AI识别结果: {}", aiResponse.length() > 200 ? aiResponse.substring(0, 200) + "..." : aiResponse);

            // 4. 解析结果（Layer 3）
            Map<String, Object> extractionResult = parseExtractionResult(aiResponse);

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

            // 6. 创建提取记录
            ContentExtraction extraction = new ContentExtraction();
            extraction.setId(UUID.randomUUID().toString().replace("-", ""));
            extraction.setSourceId(source.getId());
            extraction.setDishName((String) extractionResult.getOrDefault("dishName", "未知菜品"));
            extraction.setDescription((String) extractionResult.getOrDefault("description", ""));
            extraction.setIngredients(objectMapper.writeValueAsString(extractionResult.get("ingredients")));
            extraction.setSteps(objectMapper.writeValueAsString(extractionResult.get("steps")));
            extraction.setCookingTime(extractIntValue(extractionResult.get("cookingTime"), 30));
            extraction.setDifficulty((String) extractionResult.getOrDefault("difficulty", "中等"));

            List<String> tags = (List<String>) extractionResult.get("tags");
            extraction.setTags(tags != null ? String.join(",", tags) : "");

            extraction.setCalories(extractIntValue(extractionResult.get("calories"), 0));

            // 如果有封面图，设置为菜品图片
            if (fetchedContent.getCoverImage() != null && !fetchedContent.getCoverImage().isEmpty()) {
                extraction.setDishImage(fetchedContent.getCoverImage());
            }

            extraction.setIsPublished(false);
            extraction.setIsVerified(false);
            extraction.setCreateTime(LocalDateTime.now());
            extraction.setUpdateTime(LocalDateTime.now());

            contentExtractionMapper.insert(extraction);

            // 7. 更新内容源状态
            source.setExtractionStatus(ExtractionStatus.SUCCESS.getCode());
            source.setIsExtracted(true);
            source.setExtractionTime(LocalDateTime.now());
            source.setUpdateTime(LocalDateTime.now());
            contentSourceMapper.updateById(source);

            // 8. 更新任务状态为成功
            task.setTaskStatus("SUCCESS");
            task.setEndTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            extractionTaskMapper.updateById(task);

            log.info("提取任务完成: taskId={}, extractionId={}, dishName={}",
                task.getId(), extraction.getId(), extraction.getDishName());

        } catch (Exception e) {
            log.error("提取任务失败: taskId={}", task.getId(), e);

            // 更新任务状态为失败
            task.setTaskStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            task.setEndTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            extractionTaskMapper.updateById(task);

            // 更新内容源状态为失败
            source.setExtractionStatus(ExtractionStatus.FAILED.getCode());
            source.setErrorMessage(e.getMessage());
            source.setUpdateTime(LocalDateTime.now());
            contentSourceMapper.updateById(source);

            throw new RuntimeException("提取任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理任务失败（含重试逻辑）
     */
    private void handleTaskFailure(ExtractionTask task, Exception e) {
        if (task.getRetryCount() < MAX_RETRY_COUNT) {
            task.setRetryCount(task.getRetryCount() + 1);
            task.setTaskStatus("PENDING"); // 重新排队
            task.setUpdateTime(LocalDateTime.now());
            extractionTaskMapper.updateById(task);
            log.info("任务将重试: taskId={}, retryCount={}", task.getId(), task.getRetryCount());
        } else {
            task.setTaskStatus("FAILED");
            task.setErrorMessage("重试" + MAX_RETRY_COUNT + "次后仍失败: " + e.getMessage());
            task.setEndTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            extractionTaskMapper.updateById(task);
        }
    }

    /**
     * 安全提取整数值
     */
    private int extractIntValue(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
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

        } catch (Exception e) {
            log.error("解析提取结果失败: {}", responseText, e);
            // 返回默认值
            Map<String, Object> defaultResult = new java.util.HashMap<>();
            defaultResult.put("dishName", "提取失败");
            defaultResult.put("description", "");
            defaultResult.put("ingredients", new ArrayList<>());
            defaultResult.put("steps", new ArrayList<>());
            defaultResult.put("cookingTime", 30);
            defaultResult.put("difficulty", "中等");
            defaultResult.put("tags", new ArrayList<>());
            defaultResult.put("calories", 0);
            return defaultResult;
        }
    }

    /**
     * 从响应文本中提取JSON
     */
    private String extractJson(String text) {
        if (text == null || text.isEmpty()) {
            return "{}";
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

        return trimmed;
    }

    /**
     * 自动检测内容类型
     */
    private ContentType detectContentType(String url) {
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.contains("video") || lowerUrl.contains(".mp4")) {
            return ContentType.VIDEO;
        } else if (lowerUrl.contains("article") || lowerUrl.contains("post")) {
            return ContentType.ARTICLE;
        } else if (lowerUrl.contains("image") || lowerUrl.contains(".jpg") || lowerUrl.contains(".png")) {
            return ContentType.IMAGE;
        }
        return ContentType.VIDEO; // 默认为视频
    }

    /**
     * 转换为内容源VO
     */
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
        }

        return vo;
    }

    /**
     * 转换为提取详情VO
     */
    private ContentExtractionDetailVO convertToExtractionDetailVO(ContentExtraction extraction, ContentSource source) {
        ContentExtractionDetailVO vo = new ContentExtractionDetailVO();
        BeanUtils.copyProperties(extraction, vo);

        // 设置内容源信息
        vo.setContentUrl(source.getContentUrl());
        vo.setPlatform(source.getPlatform());
        vo.setOriginalTitle(source.getTitle());

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
        } catch (Exception e) {
            log.error("解析JSON数据失败: extractionId={}", extraction.getId(), e);
            vo.setIngredients(new ArrayList<>());
            vo.setSteps(new ArrayList<>());
            vo.setTags(new ArrayList<>());
        }

        return vo;
    }
}
