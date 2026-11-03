package com.xx.jaseatschoicejava.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xx.jaseatschoicejava.dto.ContentSourceCreateDTO;
import com.xx.jaseatschoicejava.entity.ContentExtraction;
import com.xx.jaseatschoicejava.entity.ContentSource;
import com.xx.jaseatschoicejava.entity.ExtractionTask;
import com.xx.jaseatschoicejava.entity.Recipe;
import com.xx.jaseatschoicejava.enums.ContentPlatform;
import com.xx.jaseatschoicejava.enums.ContentType;
import com.xx.jaseatschoicejava.enums.ExtractionStatus;
import com.xx.jaseatschoicejava.mapper.ContentExtractionMapper;
import com.xx.jaseatschoicejava.mapper.ContentSourceMapper;
import com.xx.jaseatschoicejava.mapper.ExtractionTaskMapper;
import com.xx.jaseatschoicejava.service.RecipeService;
import com.xx.jaseatschoicejava.service.extraction.dto.FetchedContent;
import com.xx.jaseatschoicejava.service.extraction.fetcher.ContentFetcher;
import com.xx.jaseatschoicejava.service.extraction.fetcher.ContentFetcherFactory;
import com.xx.jaseatschoicejava.service.extraction.recognizer.ContentRecognizer;
import com.xx.jaseatschoicejava.util.UrlSecurityValidator;

@ExtendWith(MockitoExtension.class)
class ContentExtractionServiceImplTest {

    @InjectMocks
    private ContentExtractionServiceImpl service;

    @Mock
    private ContentSourceMapper contentSourceMapper;

    @Mock
    private ContentExtractionMapper contentExtractionMapper;

    @Mock
    private ExtractionTaskMapper extractionTaskMapper;

    @Mock
    private ContentFetcherFactory fetcherFactory;

    @Mock
    private ContentRecognizer contentRecognizer;

    @Mock
    private ContentFetcher contentFetcher;

    @Mock
    private UrlSecurityValidator urlSecurityValidator;

    @Mock
    private RecipeService recipeService;

    @Test
    void parseExtractionResult_whenResponseBlank_shouldThrowParseException() throws Exception {
        Method method = ContentExtractionServiceImpl.class.getDeclaredMethod("parseExtractionResult", String.class);
        method.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
            () -> method.invoke(service, "   "));

        ExtractionParseException cause = (ExtractionParseException) exception.getCause();
        assertEquals("AI识别结果解析失败: AI返回内容为空", cause.getMessage());
    }

    @Test
    void authRequiredStatus_shouldBeFinalButNotFailed() {
        assertTrue(ExtractionStatus.AUTH_REQUIRED.isFinalStatus());
        assertFalse(ExtractionStatus.AUTH_REQUIRED.isFailed());
    }

    @Test
    void processPendingTasks_whenAuthRequired_shouldMarkAuthRequiredWithoutCallingAi() {
        ExtractionTask task = pendingTask();
        ContentSource source = pendingSource();
        source.setPlatform(ContentPlatform.XIAOHONGSHU.getCode());
        String message = "小红书正文需要登录态：请绑定小红书账号后重试";
        FetchedContent authRequiredContent = FetchedContent.builder()
            .fetchSuccess(true)
            .authRequired(true)
            .errorMessage(message)
            .contentType(ContentType.ARTICLE)
            .platform(ContentPlatform.XIAOHONGSHU)
            .build();

        when(extractionTaskMapper.selectPendingTasks()).thenReturn(List.of(task));
        when(contentSourceMapper.selectById("source-1")).thenReturn(source);
        when(fetcherFactory.getFetcher(any(ContentPlatform.class), any(ContentType.class))).thenReturn(contentFetcher);
        when(contentFetcher.fetch(anyString(), anyString())).thenReturn(authRequiredContent);
        when(extractionTaskMapper.updateById(any(ExtractionTask.class))).thenReturn(1);
        when(contentSourceMapper.updateById(any(ContentSource.class))).thenReturn(1);

        int processedCount = service.processPendingTasks();

        assertEquals(1, processedCount);
        assertEquals("FAILED", task.getTaskStatus());
        assertEquals(message, task.getErrorMessage());
        assertNotNull(task.getEndTime());
        assertEquals(ExtractionStatus.AUTH_REQUIRED.getCode(), source.getExtractionStatus());
        assertEquals(message, source.getErrorMessage());
        assertFalse(source.getIsExtracted());
        verify(contentRecognizer, never()).recognize(any(FetchedContent.class));
        verify(contentExtractionMapper, never()).insert(any(ContentExtraction.class));
    }

    @Test
    void processPendingTasks_whenFetchedNothing_shouldMarkParseFailedWithoutCallingAi() {
        ExtractionTask task = pendingTask();
        ContentSource source = pendingSource();
        FetchedContent emptyContent = FetchedContent.builder()
            .fetchSuccess(true)
            .contentType(ContentType.ARTICLE)
            .platform(ContentPlatform.XIAOHONGSHU)
            .build();

        when(extractionTaskMapper.selectPendingTasks()).thenReturn(List.of(task));
        when(contentSourceMapper.selectById("source-1")).thenReturn(source);
        when(fetcherFactory.getFetcher(any(ContentPlatform.class), any(ContentType.class))).thenReturn(contentFetcher);
        when(contentFetcher.fetch(anyString(), anyString())).thenReturn(emptyContent);
        when(extractionTaskMapper.updateById(any(ExtractionTask.class))).thenReturn(1);
        when(contentSourceMapper.updateById(any(ContentSource.class))).thenReturn(1);

        int processedCount = service.processPendingTasks();

        assertEquals(0, processedCount);
        assertEquals("FAILED", task.getTaskStatus());
        assertEquals("未能从链接获取到内容，可能需要登录或链接已失效", task.getErrorMessage());
        assertEquals(ExtractionStatus.PARSE_FAILED.getCode(), source.getExtractionStatus());
        assertFalse(source.getIsExtracted());
        verify(contentRecognizer, never()).recognize(any(FetchedContent.class));
        verify(contentExtractionMapper, never()).insert(any(ContentExtraction.class));
    }

    @Test
    void processPendingTasks_whenAiSaysNotRecipe_shouldMarkParseFailedWithoutInsert() {
        ExtractionTask task = pendingTask();
        ContentSource source = pendingSource();
        FetchedContent fetchedContent = FetchedContent.builder()
            .textContent("正文内容")
            .fetchSuccess(true)
            .contentType(ContentType.ARTICLE)
            .platform(ContentPlatform.XIAOHONGSHU)
            .build();
        String notRecipe = "{\"isRecipe\": false, \"dishName\": \"\", \"ingredients\": [], \"steps\": []}";

        when(extractionTaskMapper.selectPendingTasks()).thenReturn(List.of(task));
        when(contentSourceMapper.selectById("source-1")).thenReturn(source);
        when(fetcherFactory.getFetcher(any(ContentPlatform.class), any(ContentType.class))).thenReturn(contentFetcher);
        when(contentFetcher.fetch(anyString(), anyString())).thenReturn(fetchedContent);
        when(contentRecognizer.recognize(fetchedContent)).thenReturn(notRecipe);
        when(extractionTaskMapper.updateById(any(ExtractionTask.class))).thenReturn(1);
        when(contentSourceMapper.updateById(any(ContentSource.class))).thenReturn(1);

        int processedCount = service.processPendingTasks();

        assertEquals(0, processedCount);
        assertEquals("FAILED", task.getTaskStatus());
        assertEquals("该内容不是菜谱，无法提取", task.getErrorMessage());
        assertEquals(ExtractionStatus.PARSE_FAILED.getCode(), source.getExtractionStatus());
        verify(contentExtractionMapper, never()).insert(any(ContentExtraction.class));
    }

    private ExtractionTask pendingTask() {
        ExtractionTask task = new ExtractionTask();
        task.setId("task-1");
        task.setSourceId("source-1");
        task.setTaskType("EXTRACT");
        task.setTaskStatus("PENDING");
        task.setRetryCount(0);
        task.setPriority(0);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }

    private ContentSource pendingSource() {
        ContentSource source = new ContentSource();
        source.setId("source-1");
        source.setUserId("user-1");
        source.setContentUrl("https://example.com/article/1");
        source.setContentType(ContentType.ARTICLE.getCode());
        source.setPlatform(ContentPlatform.DOUYIN.getCode());
        source.setExtractionStatus(ExtractionStatus.PENDING.getCode());
        source.setIsExtracted(false);
        source.setCreatedAt(LocalDateTime.now());
        source.setUpdatedAt(LocalDateTime.now());
        return source;
    }

    @Test
    void processPendingTasks_whenAiResponseInvalid_shouldMarkParseFailed() {
        ExtractionTask task = new ExtractionTask();
        task.setId("task-1");
        task.setSourceId("source-1");
        task.setTaskType("EXTRACT");
        task.setTaskStatus("PENDING");
        task.setRetryCount(0);
        task.setPriority(0);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        ContentSource source = new ContentSource();
        source.setId("source-1");
        source.setUserId("user-1");
        source.setContentUrl("https://example.com/article/1");
        source.setContentType(ContentType.ARTICLE.getCode());
        source.setPlatform(ContentPlatform.DOUYIN.getCode());
        source.setExtractionStatus(ExtractionStatus.PENDING.getCode());
        source.setIsExtracted(false);
        source.setCreatedAt(LocalDateTime.now());
        source.setUpdatedAt(LocalDateTime.now());

        FetchedContent fetchedContent = FetchedContent.builder()
            .title("测试标题")
            .author("测试作者")
            .coverImage("https://example.com/cover.jpg")
            .textContent("正文内容")
            .fetchSuccess(true)
            .contentType(ContentType.ARTICLE)
            .platform(ContentPlatform.DOUYIN)
            .build();

        when(extractionTaskMapper.selectPendingTasks()).thenReturn(List.of(task));
        when(contentSourceMapper.selectById("source-1")).thenReturn(source);
        when(fetcherFactory.getFetcher(any(ContentPlatform.class), any(ContentType.class))).thenReturn(contentFetcher);
        when(contentFetcher.fetch(anyString(), anyString())).thenReturn(fetchedContent);
        when(contentRecognizer.recognize(fetchedContent)).thenReturn("not json");
        when(extractionTaskMapper.updateById(any(ExtractionTask.class))).thenReturn(1);
        when(contentSourceMapper.updateById(any(ContentSource.class))).thenReturn(1);

        int processedCount = service.processPendingTasks();

        assertEquals(0, processedCount);
        assertEquals("FAILED", task.getTaskStatus());
        assertEquals(0, task.getRetryCount());
        assertEquals("not json", task.getResultData());
        assertNotNull(task.getEndTime());
        assertEquals(ExtractionStatus.PARSE_FAILED.getCode(), source.getExtractionStatus());
        assertFalse(source.getIsExtracted());
        assertNotNull(source.getErrorMessage());
        verify(contentExtractionMapper, never()).insert(any(ContentExtraction.class));
    }

    @Test
    void addContentSource_whenUrlRejected_shouldThrowAndNotInsert() {
        when(urlSecurityValidator.validateAndResolve(anyString()))
            .thenThrow(new UrlSecurityValidator.InvalidUrlException("不允许访问内网地址"));

        ContentSourceCreateDTO dto = new ContentSourceCreateDTO();
        dto.setContentUrl("http://127.0.0.1/secret");

        assertThrows(UrlSecurityValidator.InvalidUrlException.class,
            () -> service.addContentSource(dto, "user-1"));
        verify(contentSourceMapper, never()).insert(any(ContentSource.class));
        verify(extractionTaskMapper, never()).insert(any(ExtractionTask.class));
    }

    @Test
    void addContentSource_shouldPersistValidatedUrlForUser() {
        when(urlSecurityValidator.validateAndResolve(anyString())).thenReturn("https://example.com/a");

        ContentSourceCreateDTO dto = new ContentSourceCreateDTO();
        dto.setContentUrl("https://example.com/a");

        String sourceId = service.addContentSource(dto, "user-1");

        assertNotNull(sourceId);
        ArgumentCaptor<ContentSource> captor = ArgumentCaptor.forClass(ContentSource.class);
        verify(contentSourceMapper).insert(captor.capture());
        assertEquals("user-1", captor.getValue().getUserId());
        assertEquals("https://example.com/a", captor.getValue().getContentUrl());
        verify(extractionTaskMapper).insert(any(ExtractionTask.class));
    }

    @Test
    void addContentSource_withoutType_shouldAutoDetectAndFallbackToArticle() {
        when(urlSecurityValidator.validateAndResolve(anyString())).thenReturn("https://example.com/unknown");

        ContentSourceCreateDTO dto = new ContentSourceCreateDTO();
        dto.setContentUrl("https://example.com/unknown");

        service.addContentSource(dto, "user-1");

        ArgumentCaptor<ContentSource> captor = ArgumentCaptor.forClass(ContentSource.class);
        verify(contentSourceMapper).insert(captor.capture());
        assertEquals(ContentType.ARTICLE.getCode(), captor.getValue().getContentType());
    }

    @Test
    void addContentSource_withExplicitType_shouldRespectUserChoice() {
        when(urlSecurityValidator.validateAndResolve(anyString())).thenReturn("https://example.com/video/1");

        ContentSourceCreateDTO dto = new ContentSourceCreateDTO();
        dto.setContentUrl("https://example.com/video/1");
        dto.setContentType(ContentType.IMAGE.getCode());

        service.addContentSource(dto, "user-1");

        ArgumentCaptor<ContentSource> captor = ArgumentCaptor.forClass(ContentSource.class);
        verify(contentSourceMapper).insert(captor.capture());
        assertEquals(ContentType.IMAGE.getCode(), captor.getValue().getContentType());
    }

    @Test
    void addContentSource_withInvalidType_shouldRejectWithoutPersisting() {
        when(urlSecurityValidator.validateAndResolve(anyString())).thenReturn("https://example.com/a");

        ContentSourceCreateDTO dto = new ContentSourceCreateDTO();
        dto.setContentUrl("https://example.com/a");
        dto.setContentType("AUDIO");

        assertThrows(IllegalArgumentException.class, () -> service.addContentSource(dto, "user-1"));
        verify(contentSourceMapper, never()).insert(any(ContentSource.class));
        verify(extractionTaskMapper, never()).insert(any(ExtractionTask.class));
    }

    @Test
    void reExtract_withoutType_shouldKeepSavedType() {
        ContentSource source = pendingSource();
        source.setContentType(ContentType.IMAGE.getCode());
        when(contentSourceMapper.selectById("source-1")).thenReturn(source);

        assertTrue(service.reExtract("source-1", null, "user-1"));

        assertEquals(ContentType.IMAGE.getCode(), source.getContentType());
        verify(extractionTaskMapper).insert(any(ExtractionTask.class));
    }

    @Test
    void reExtract_withExplicitType_shouldOverrideSavedType() {
        ContentSource source = pendingSource();
        source.setContentType(ContentType.VIDEO.getCode());
        when(contentSourceMapper.selectById("source-1")).thenReturn(source);

        assertTrue(service.reExtract("source-1", ContentType.ARTICLE.getCode(), "user-1"));

        assertEquals(ContentType.ARTICLE.getCode(), source.getContentType());
    }

    @Test
    void getExtractionDetail_shouldExposeContentType() {
        ContentExtraction extraction = extraction();
        ContentSource source = pendingSource();
        source.setContentType(ContentType.IMAGE.getCode());
        when(contentExtractionMapper.selectById("ext-1")).thenReturn(extraction);
        when(contentSourceMapper.selectById("source-1")).thenReturn(source);

        var detail = service.getExtractionDetail("ext-1", "user-1");

        assertEquals(ContentType.IMAGE.getCode(), detail.getContentType());
        assertEquals(ContentType.IMAGE.getDescription(), detail.getContentTypeName());
    }

    @Test
    void publishAsRecipe_whenSourceOwnedByAnotherUser_shouldThrowAndNotUpdate() {
        ContentExtraction extraction = extraction();
        ContentSource source = pendingSource();
        when(contentExtractionMapper.selectByIdForUpdate("ext-1")).thenReturn(extraction);
        when(contentSourceMapper.selectById("source-1")).thenReturn(source);

        assertThrows(RuntimeException.class, () -> service.publishAsRecipe("ext-1", "lunch", "user-2"));
        verify(contentExtractionMapper, never()).updateById(any(ContentExtraction.class));
    }

    @Test
    void publishAsRecipe_whenOwner_shouldPublish() {
        ContentExtraction extraction = extraction();
        ContentSource source = pendingSource();
        when(contentExtractionMapper.selectByIdForUpdate("ext-1")).thenReturn(extraction);
        when(contentSourceMapper.selectById("source-1")).thenReturn(source);

        Recipe recipe = new Recipe();
        recipe.setId("recipe-1");
        when(recipeService.createFromExtraction(extraction, source, "lunch")).thenReturn(recipe);
        when(contentExtractionMapper.updateById(any(ContentExtraction.class))).thenReturn(1);

        String recipeId = service.publishAsRecipe("ext-1", "lunch", "user-1");

        assertNotNull(recipeId);
        assertEquals("recipe-1", recipeId);
        assertEquals("recipe-1", extraction.getRecipeId());
        assertTrue(extraction.getIsPublished());
        verify(contentExtractionMapper).updateById(extraction);
        verify(recipeService).createFromExtraction(extraction, source, "lunch");
    }

    @Test
    void publishAsRecipe_withoutMealType_shouldDelegateDefaultToRecipeService() {
        ContentExtraction extraction = extraction();
        ContentSource source = pendingSource();
        Recipe recipe = new Recipe();
        recipe.setId("recipe-1");
        when(contentExtractionMapper.selectByIdForUpdate("ext-1")).thenReturn(extraction);
        when(contentSourceMapper.selectById("source-1")).thenReturn(source);
        when(recipeService.createFromExtraction(extraction, source, null)).thenReturn(recipe);
        when(contentExtractionMapper.updateById(any(ContentExtraction.class))).thenReturn(1);

        assertEquals("recipe-1", service.publishAsRecipe("ext-1", null, "user-1"));
    }

    @Test
    void publishAsRecipe_whenAlreadyPublishedAndRecipeExists_shouldBeIdempotent() {
        ContentExtraction extraction = extraction();
        extraction.setIsPublished(true);
        extraction.setRecipeId("recipe-1");
        ContentSource source = pendingSource();
        Recipe recipe = new Recipe();
        recipe.setId("recipe-1");
        when(contentExtractionMapper.selectByIdForUpdate("ext-1")).thenReturn(extraction);
        when(contentSourceMapper.selectById("source-1")).thenReturn(source);
        when(recipeService.getById("recipe-1")).thenReturn(recipe);

        assertEquals("recipe-1", service.publishAsRecipe("ext-1", "dinner", "user-1"));
        verify(recipeService, never()).createFromExtraction(any(), any(), anyString());
        verify(contentExtractionMapper, never()).updateById(any(ContentExtraction.class));
    }

    @Test
    void publishAsRecipe_whenRecipeCreationFails_shouldNotMarkPublished() {
        ContentExtraction extraction = extraction();
        ContentSource source = pendingSource();
        when(contentExtractionMapper.selectByIdForUpdate("ext-1")).thenReturn(extraction);
        when(contentSourceMapper.selectById("source-1")).thenReturn(source);
        when(recipeService.createFromExtraction(extraction, source, "lunch"))
            .thenThrow(new RuntimeException("落库失败"));

        assertThrows(RuntimeException.class, () -> service.publishAsRecipe("ext-1", "lunch", "user-1"));
        assertFalse(extraction.getIsPublished());
        verify(contentExtractionMapper, never()).updateById(any(ContentExtraction.class));
    }

    @Test
    void verifyExtraction_whenSourceOwnedByAnotherUser_shouldThrowAndNotUpdate() {
        ContentExtraction extraction = extraction();
        ContentSource source = pendingSource();
        when(contentExtractionMapper.selectById("ext-1")).thenReturn(extraction);
        when(contentSourceMapper.selectById("source-1")).thenReturn(source);

        assertThrows(RuntimeException.class,
            () -> service.verifyExtraction("ext-1", true, 5, "user-2"));
        verify(contentExtractionMapper, never()).updateById(any(ContentExtraction.class));
    }

    @Test
    void verifyExtraction_whenOwner_shouldUpdateVerification() {
        ContentExtraction extraction = extraction();
        ContentSource source = pendingSource();
        when(contentExtractionMapper.selectById("ext-1")).thenReturn(extraction);
        when(contentSourceMapper.selectById("source-1")).thenReturn(source);
        when(contentExtractionMapper.updateById(any(ContentExtraction.class))).thenReturn(1);

        assertTrue(service.verifyExtraction("ext-1", true, 5, "user-1"));
        assertTrue(extraction.getIsVerified());
        assertEquals(5, extraction.getManualScore());
    }

    private ContentExtraction extraction() {
        ContentExtraction extraction = new ContentExtraction();
        extraction.setId("ext-1");
        extraction.setSourceId("source-1");
        extraction.setIsPublished(false);
        extraction.setIsVerified(false);
        return extraction;
    }
}
