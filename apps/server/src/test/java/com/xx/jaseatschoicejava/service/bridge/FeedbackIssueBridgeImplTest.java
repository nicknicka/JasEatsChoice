package com.xx.jaseatschoicejava.service.bridge;

import com.xx.jaseatschoicejava.config.GitHubBridgeProperties;
import com.xx.jaseatschoicejava.dto.FeedbackIssueDraftDTO;
import com.xx.jaseatschoicejava.entity.Feedback;
import com.xx.jaseatschoicejava.entity.FeedbackIssueLink;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.mapper.FeedbackIssueLinkMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackIssueBridgeImplTest {

    @Mock
    private FeedbackIssueLinkMapper linkMapper;

    @Mock
    private GitHubBridgeProperties properties;

    @InjectMocks
    private FeedbackIssueBridgeImpl bridge;

    /** 假 GitHub 适配器：记录调用，绝不联网。 */
    private static class FakeGitHub implements GitHubIssueClient {
        final List<String> bodies = new ArrayList<>();
        final List<List<String>> labels = new ArrayList<>();
        int calls = 0;

        @Override
        public CreatedIssue createIssue(String repo, String title, String body, List<String> issueLabels) {
            calls++;
            bodies.add(body);
            labels.add(issueLabels);
            return new CreatedIssue("42", "https://github.com/" + repo + "/issues/42");
        }

        @Override
        public String getState(String repo, String issueNumber) {
            return "open";
        }
    }

    private FakeGitHub fake;

    @BeforeEach
    void setUp() {
        fake = new FakeGitHub();
        ReflectionTestUtils.setField(bridge, "gitHubIssueClient", fake);
    }

    private static Feedback feedback() {
        Feedback fb = new Feedback();
        fb.setFeedbackId("fb-1");
        fb.setType("bug");
        fb.setContent("下单后白屏");
        fb.setEnvironment("platform=ios, system=iOS 17");
        fb.setContact("13800000000");
        fb.setOrderId("order-secret");
        return fb;
    }

    private static FeedbackIssueDraftDTO draft() {
        FeedbackIssueDraftDTO dto = new FeedbackIssueDraftDTO();
        dto.setTitle("[bug] 下单后白屏");
        dto.setSummary("iOS 端点单后页面白屏");
        dto.setReproductionSteps("1. 登录\n2. 下单\n3. 观察");
        dto.setExpectedBehavior("进入支付页");
        dto.setActualBehavior("页面白屏");
        dto.setAcceptanceCriteria("下单后正常进入支付页");
        return dto;
    }

    @Test
    void createOrGet_whenDraftIncomplete_shouldReject() {
        FeedbackIssueDraftDTO incomplete = new FeedbackIssueDraftDTO();
        incomplete.setSummary("只有摘要");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> bridge.createOrGet(feedback(), incomplete));
        assertEquals("400", ex.getCode());
        assertEquals(0, fake.calls);
    }

    @Test
    void createOrGet_shouldSanitizeAndCarryHiddenMarker() {
        when(properties.getRepo()).thenReturn("nicknicka/JasEatsChoice");
        when(linkMapper.selectOne(any())).thenReturn(null);
        when(linkMapper.insert(any(FeedbackIssueLink.class))).thenReturn(1);

        bridge.createOrGet(feedback(), draft());

        String body = fake.bodies.get(0);
        assertTrue(body.contains("<!-- feedback:fb-1 -->"));
        assertTrue(body.contains("iOS 端点单后页面白屏"));
        assertTrue(body.contains("platform=ios"));
        assertFalse(body.contains("13800000000"));
        assertFalse(body.contains("order-secret"));
    }

    @Test
    void createOrGet_labelsQueueAndCategory() {
        when(properties.getRepo()).thenReturn("nicknicka/JasEatsChoice");
        when(linkMapper.selectOne(any())).thenReturn(null);
        when(linkMapper.insert(any(FeedbackIssueLink.class))).thenReturn(1);

        bridge.createOrGet(feedback(), draft());

        List<String> labels = fake.labels.get(0);
        assertTrue(labels.contains("ready-for-agent"));
        assertTrue(labels.contains("feedback:bug"));
    }

    @Test
    void createOrGet_whenAlreadyLinked_shouldBeIdempotent() {
        FeedbackIssueLink existing = new FeedbackIssueLink();
        existing.setFeedbackId("fb-1");
        existing.setIssueNumber("42");
        when(linkMapper.selectOne(any())).thenReturn(existing);

        FeedbackIssueLink result = bridge.createOrGet(feedback(), draft());

        assertEquals("42", result.getIssueNumber());
        assertEquals(0, fake.calls);
        verify(linkMapper, never()).insert(any(FeedbackIssueLink.class));
    }

    @Test
    void syncState_shouldUpdateLink() {
        FeedbackIssueLink link = new FeedbackIssueLink();
        link.setFeedbackId("fb-1");
        link.setState("open");
        when(linkMapper.selectOne(any())).thenReturn(link);
        when(linkMapper.updateById(any(FeedbackIssueLink.class))).thenReturn(1);

        FeedbackIssueLink result = bridge.syncState("fb-1", "closed");

        assertEquals("closed", result.getState());
        verify(linkMapper, times(1)).updateById(any(FeedbackIssueLink.class));
    }
}
