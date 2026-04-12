package com.xx.jaseatschoicejava.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xx.jaseatschoicejava.entity.ContentExtraction;
import com.xx.jaseatschoicejava.entity.ContentSource;
import com.xx.jaseatschoicejava.entity.ExtractionTask;
import com.xx.jaseatschoicejava.enums.ContentPlatform;
import com.xx.jaseatschoicejava.enums.ContentType;
import com.xx.jaseatschoicejava.enums.ExtractionStatus;
import com.xx.jaseatschoicejava.mapper.ContentExtractionMapper;
import com.xx.jaseatschoicejava.mapper.ContentSourceMapper;
import com.xx.jaseatschoicejava.mapper.ExtractionTaskMapper;
import com.xx.jaseatschoicejava.service.extraction.dto.FetchedContent;
import com.xx.jaseatschoicejava.service.extraction.fetcher.ContentFetcher;
import com.xx.jaseatschoicejava.service.extraction.fetcher.ContentFetcherFactory;
import com.xx.jaseatschoicejava.service.extraction.recognizer.ContentRecognizer;

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
    void processPendingTasks_whenAiResponseInvalid_shouldMarkParseFailed() {
        ExtractionTask task = new ExtractionTask();
        task.setId("task-1");
        task.setSourceId("source-1");
        task.setTaskType("EXTRACT");
        task.setTaskStatus("PENDING");
        task.setRetryCount(0);
        task.setPriority(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        ContentSource source = new ContentSource();
        source.setId("source-1");
        source.setUserId("user-1");
        source.setContentUrl("https://example.com/article/1");
        source.setContentType(ContentType.ARTICLE.getCode());
        source.setPlatform(ContentPlatform.DOUYIN.getCode());
        source.setExtractionStatus(ExtractionStatus.PENDING.getCode());
        source.setIsExtracted(false);
        source.setCreateTime(LocalDateTime.now());
        source.setUpdateTime(LocalDateTime.now());

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
        when(contentFetcher.fetch(anyString())).thenReturn(fetchedContent);
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
}