package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.dto.FeedbackCreateDTO;
import com.xx.jaseatschoicejava.entity.Feedback;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.mapper.FeedbackMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceImplTest {

    @Mock
    private FeedbackMapper feedbackMapper;

    @InjectMocks
    private FeedbackServiceImpl feedbackService;

    @BeforeEach
    void setUpMybatisPlus() {
        // 生产代码用 LambdaQueryWrapper 的方法引用解析列名，依赖 TableInfo 缓存；
        // baseMapper 平时由容器注入，纯单测需手动补齐。
        TableInfoHelper.initTableInfo(
            new MapperBuilderAssistant(new MybatisConfiguration(), ""), Feedback.class);
        ReflectionTestUtils.setField(feedbackService, "baseMapper", feedbackMapper);
    }

    private static FeedbackCreateDTO dto(String type, String content) {
        FeedbackCreateDTO dto = new FeedbackCreateDTO();
        dto.setType(type);
        dto.setContent(content);
        return dto;
    }

    @Test
    void create_whenValid_shouldPersistAsPending() {
        when(feedbackMapper.insert(any(Feedback.class))).thenReturn(1);

        feedbackService.create("user-1", dto("bug", "下单后页面白屏"));

        ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackMapper).insert(captor.capture());
        Feedback saved = captor.getValue();
        assertEquals("user-1", saved.getUserId());
        assertEquals("bug", saved.getType());
        assertEquals("pending", saved.getStatus());
        assertEquals("下单后页面白屏", saved.getContent());
    }

    @Test
    void create_whenTypeUnknown_shouldReject() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> feedbackService.create("user-1", dto("nope", "内容")));
        assertEquals("400", ex.getCode());
    }

    @Test
    void create_whenContentBlank_shouldReject() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> feedbackService.create("user-1", dto("bug", "   ")));
        assertEquals("400", ex.getCode());
    }

    @Test
    void create_whenNoIdentity_shouldReject() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> feedbackService.create("  ", dto("bug", "内容")));
        assertEquals("401", ex.getCode());
    }

    @Test
    void create_shouldDeriveEnvironmentButKeepDeviceInfoInternal() {
        when(feedbackMapper.insert(any(Feedback.class))).thenReturn(1);
        FeedbackCreateDTO input = dto("ui", "按钮错位");
        input.setDeviceInfo(Map.of("platform", "ios", "system", "iOS 17"));

        feedbackService.create("user-1", input);

        ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackMapper).insert(captor.capture());
        Feedback saved = captor.getValue();
        assertTrue(saved.getEnvironment().contains("platform=ios"));
        assertTrue(saved.getDeviceInfo().contains("iOS 17"));
    }

    @Test
    void getOwn_whenFeedbackBelongsToOtherUser_shouldLookMissing() {
        Feedback other = new Feedback();
        other.setFeedbackId("fb-1");
        other.setUserId("someone-else");
        when(feedbackMapper.selectById("fb-1")).thenReturn(other);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> feedbackService.getOwn("user-1", "fb-1"));
        assertEquals("404", ex.getCode());
    }

    @Test
    void getOwn_whenOwner_shouldReturnRecord() {
        Feedback mine = new Feedback();
        mine.setFeedbackId("fb-1");
        mine.setUserId("user-1");
        when(feedbackMapper.selectById("fb-1")).thenReturn(mine);

        assertEquals("fb-1", feedbackService.getOwn("user-1", "fb-1").getFeedbackId());
    }

    @Test
    void listOwn_shouldClampPageSize() {
        when(feedbackMapper.selectPage(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

        IPage<Feedback> result = feedbackService.listOwn("user-1", 0, 500, null, null);

        assertTrue(result.getRecords().isEmpty());
        assertTrue(result.getSize() <= 50);
        assertTrue(result.getCurrent() >= 1);
    }
}
