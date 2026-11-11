package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.dto.FeedbackCreateDTO;
import com.xx.jaseatschoicejava.dto.FeedbackTriageDTO;
import com.xx.jaseatschoicejava.dto.FeedbackIssueDraftDTO;
import com.xx.jaseatschoicejava.entity.Feedback;
import com.xx.jaseatschoicejava.entity.FeedbackAttachment;
import com.xx.jaseatschoicejava.entity.FeedbackAudit;
import com.xx.jaseatschoicejava.entity.FeedbackIssueLink;
import com.xx.jaseatschoicejava.entity.FeedbackMessage;
import com.xx.jaseatschoicejava.entity.Order;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.mapper.FeedbackAttachmentMapper;
import com.xx.jaseatschoicejava.mapper.FeedbackAuditMapper;
import com.xx.jaseatschoicejava.mapper.FeedbackMapper;
import com.xx.jaseatschoicejava.mapper.FeedbackMessageMapper;
import com.xx.jaseatschoicejava.service.OrderService;
import com.xx.jaseatschoicejava.service.bridge.FeedbackIssueBridge;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceImplTest {

    @Mock
    private FeedbackMapper feedbackMapper;

    @Mock
    private FeedbackMessageMapper feedbackMessageMapper;

    @Mock
    private FeedbackAttachmentMapper feedbackAttachmentMapper;

    @Mock
    private FeedbackAuditMapper feedbackAuditMapper;

    @Mock
    private OrderService orderService;

    @Mock
    private FeedbackIssueBridge feedbackIssueBridge;

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

    private static FeedbackTriageDTO triageDto(String status) {
        FeedbackTriageDTO dto = new FeedbackTriageDTO();
        dto.setStatus(status);
        return dto;
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

    @Test
    void create_whenOrderBelongsToOtherUser_shouldReject() {
        Order order = new Order();
        order.setId("o-1");
        order.setUserId("someone-else");
        order.setMerchantId("m-1");
        when(orderService.getById("o-1")).thenReturn(order);

        FeedbackCreateDTO input = dto("order", "订单出问题");
        input.setOrderId("o-1");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> feedbackService.create("user-1", input));
        assertEquals("400", ex.getCode());
    }

    @Test
    void create_whenOwnOrderProvided_shouldDeriveMerchant() {
        when(feedbackMapper.insert(any(Feedback.class))).thenReturn(1);
        Order order = new Order();
        order.setId("o-1");
        order.setUserId("user-1");
        order.setMerchantId("m-1");
        when(orderService.getById("o-1")).thenReturn(order);

        FeedbackCreateDTO input = dto("payment", "支付异常");
        input.setOrderId("o-1");

        feedbackService.create("user-1", input);

        ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackMapper).insert(captor.capture());
        assertEquals("o-1", captor.getValue().getOrderId());
        assertEquals("m-1", captor.getValue().getMerchantId());
    }

    @Test
    void appendUserMessage_shouldForceUserVisibility() {
        Feedback mine = new Feedback();
        mine.setFeedbackId("fb-1");
        mine.setUserId("user-1");
        when(feedbackMapper.selectById("fb-1")).thenReturn(mine);
        when(feedbackMessageMapper.insert(any(FeedbackMessage.class))).thenReturn(1);

        feedbackService.appendUserMessage("user-1", "fb-1", "  补充信息  ");

        ArgumentCaptor<FeedbackMessage> captor = ArgumentCaptor.forClass(FeedbackMessage.class);
        verify(feedbackMessageMapper).insert(captor.capture());
        assertEquals("USER", captor.getValue().getVisibility());
        assertEquals("补充信息", captor.getValue().getContent());
    }

    @Test
    void appendUserMessage_whenNotOwner_shouldReject() {
        Feedback other = new Feedback();
        other.setFeedbackId("fb-1");
        other.setUserId("someone-else");
        when(feedbackMapper.selectById("fb-1")).thenReturn(other);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> feedbackService.appendUserMessage("user-1", "fb-1", "hi"));
        assertEquals("404", ex.getCode());
    }

    @Test
    void addAttachment_whenNotImage_shouldReject() {
        Feedback mine = new Feedback();
        mine.setFeedbackId("fb-1");
        mine.setUserId("user-1");
        when(feedbackMapper.selectById("fb-1")).thenReturn(mine);
        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "x".getBytes());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> feedbackService.addAttachment("user-1", "fb-1", file));
        assertEquals("400", ex.getCode());
    }

    @Test
    void addAttachment_whenLimitReached_shouldReject() {
        Feedback mine = new Feedback();
        mine.setFeedbackId("fb-1");
        mine.setUserId("user-1");
        when(feedbackMapper.selectById("fb-1")).thenReturn(mine);
        when(feedbackAttachmentMapper.selectCount(any())).thenReturn(3L);
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", "x".getBytes());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> feedbackService.addAttachment("user-1", "fb-1", file));
        assertEquals("400", ex.getCode());
    }

    @Test
    void updateAssociation_shouldWriteAudit() {
        Feedback feedback = new Feedback();
        feedback.setFeedbackId("fb-1");
        feedback.setUserId("user-1");
        when(feedbackMapper.selectById("fb-1")).thenReturn(feedback);
        when(feedbackAuditMapper.insert(any(FeedbackAudit.class))).thenReturn(1);

        com.xx.jaseatschoicejava.dto.FeedbackAssociationUpdateDTO dto =
                new com.xx.jaseatschoicejava.dto.FeedbackAssociationUpdateDTO();
        dto.setMerchantId("m-9");
        feedbackService.updateAssociation(7L, "fb-1", dto);

        ArgumentCaptor<FeedbackAudit> captor = ArgumentCaptor.forClass(FeedbackAudit.class);
        verify(feedbackAuditMapper).insert(captor.capture());
        assertEquals("ASSOCIATION", captor.getValue().getAction());
        assertEquals("7", captor.getValue().getActorId());
    }

    @Test
    void triage_whenIllegalTransition_shouldReject() {
        Feedback resolved = new Feedback();
        resolved.setFeedbackId("fb-1");
        resolved.setStatus("resolved");
        when(feedbackMapper.selectById("fb-1")).thenReturn(resolved);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> feedbackService.triage(1L, "fb-1", triageDto("accepted")));
        assertEquals("409", ex.getCode());
    }

    @Test
    void triage_whenAcceptedFromTriaging_shouldWriteAudit() {
        Feedback fb = new Feedback();
        fb.setFeedbackId("fb-1");
        fb.setStatus("triaging");
        when(feedbackMapper.selectById("fb-1")).thenReturn(fb);
        when(feedbackAuditMapper.insert(any(FeedbackAudit.class))).thenReturn(1);

        feedbackService.triage(1L, "fb-1", triageDto("accepted"));

        ArgumentCaptor<FeedbackAudit> captor = ArgumentCaptor.forClass(FeedbackAudit.class);
        verify(feedbackAuditMapper).insert(captor.capture());
        assertEquals("TRIAGE", captor.getValue().getAction());
    }

    @Test
    void resolve_whenNotAccepted_shouldReject() {
        Feedback fb = new Feedback();
        fb.setFeedbackId("fb-1");
        fb.setStatus("triaging");
        when(feedbackMapper.selectById("fb-1")).thenReturn(fb);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> feedbackService.resolve(1L, "fb-1", "fixed"));
        assertEquals("409", ex.getCode());
    }

    @Test
    void resolve_whenAccepted_shouldStoreResolutionCode() {
        Feedback fb = new Feedback();
        fb.setFeedbackId("fb-1");
        fb.setStatus("accepted");
        when(feedbackMapper.selectById("fb-1")).thenReturn(fb);
        when(feedbackAuditMapper.insert(any(FeedbackAudit.class))).thenReturn(1);

        feedbackService.resolve(1L, "fb-1", "fixed");

        ArgumentCaptor<FeedbackAudit> captor = ArgumentCaptor.forClass(FeedbackAudit.class);
        verify(feedbackAuditMapper).insert(captor.capture());
        assertEquals("RESOLVE", captor.getValue().getAction());
    }

    @Test
    void addAdminMessage_defaultVisibilityIsUser() {
        Feedback fb = new Feedback();
        fb.setFeedbackId("fb-1");
        when(feedbackMapper.selectById("fb-1")).thenReturn(fb);
        when(feedbackMessageMapper.insert(any(FeedbackMessage.class))).thenReturn(1);

        feedbackService.addAdminMessage(1L, "fb-1", "已处理", null);

        ArgumentCaptor<FeedbackMessage> captor = ArgumentCaptor.forClass(FeedbackMessage.class);
        verify(feedbackMessageMapper).insert(captor.capture());
        assertEquals("USER", captor.getValue().getVisibility());
        assertEquals("ADMIN", captor.getValue().getAuthorType());
    }

    @Test
    void addAdminMessage_internalVisibilityHonored() {
        Feedback fb = new Feedback();
        fb.setFeedbackId("fb-1");
        when(feedbackMapper.selectById("fb-1")).thenReturn(fb);
        when(feedbackMessageMapper.insert(any(FeedbackMessage.class))).thenReturn(1);

        feedbackService.addAdminMessage(1L, "fb-1", "内部排查", "INTERNAL");

        ArgumentCaptor<FeedbackMessage> captor = ArgumentCaptor.forClass(FeedbackMessage.class);
        verify(feedbackMessageMapper).insert(captor.capture());
        assertEquals("INTERNAL", captor.getValue().getVisibility());
    }

    @Test
    void createIssue_whenNotAccepted_shouldReject() {
        Feedback fb = new Feedback();
        fb.setFeedbackId("fb-1");
        fb.setStatus("triaging");
        when(feedbackMapper.selectById("fb-1")).thenReturn(fb);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> feedbackService.createIssue(1L, "fb-1", new FeedbackIssueDraftDTO()));
        assertEquals("409", ex.getCode());
    }

    @Test
    void createIssue_whenNotPlatformSystem_shouldReject() {
        Feedback fb = new Feedback();
        fb.setFeedbackId("fb-1");
        fb.setStatus("accepted");
        fb.setPlatformSystem(false);
        when(feedbackMapper.selectById("fb-1")).thenReturn(fb);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> feedbackService.createIssue(1L, "fb-1", new FeedbackIssueDraftDTO()));
        assertEquals("409", ex.getCode());
    }

    @Test
    void createIssue_whenEligible_shouldDelegateToBridge() {
        Feedback fb = new Feedback();
        fb.setFeedbackId("fb-1");
        fb.setStatus("accepted");
        fb.setPlatformSystem(true);
        when(feedbackMapper.selectById("fb-1")).thenReturn(fb);
        FeedbackIssueLink link = new FeedbackIssueLink();
        link.setFeedbackId("fb-1");
        link.setRepo("o/r");
        link.setIssueNumber("42");
        when(feedbackIssueBridge.createOrGet(any(Feedback.class), any(FeedbackIssueDraftDTO.class))).thenReturn(link);
        when(feedbackAuditMapper.insert(any(FeedbackAudit.class))).thenReturn(1);

        FeedbackIssueLink result = feedbackService.createIssue(1L, "fb-1", new FeedbackIssueDraftDTO());

        assertEquals("42", result.getIssueNumber());
        verify(feedbackIssueBridge).createOrGet(any(Feedback.class), any(FeedbackIssueDraftDTO.class));
    }
}
