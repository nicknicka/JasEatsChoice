package com.xx.jaseatschoicejava.agent.tools.announcement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.xx.jaseatschoicejava.agent.context.AgentChatContext;
import com.xx.jaseatschoicejava.agent.listener.AgentChatContextListener;
import com.xx.jaseatschoicejava.rag.RagAudience;
import com.xx.jaseatschoicejava.rag.RagKnowledgeQuery;
import com.xx.jaseatschoicejava.rag.RagKnowledgeResult;
import com.xx.jaseatschoicejava.rag.RagKnowledgeService;

import dev.langchain4j.agentic.scope.AgentInvocation;
import dev.langchain4j.agentic.scope.AgenticScope;

class AnnouncementKnowledgeToolsTest {

    private final RagKnowledgeService knowledgeService = mock(RagKnowledgeService.class);
    private final AnnouncementKnowledgeTools tools = new AnnouncementKnowledgeTools(knowledgeService);

    @Test
    void customerMerchantNameCannotExpandTrustedScope() {
        AgenticScope scope = scopeFor(AgentChatContext.customer("1001"));
        when(knowledgeService.search(any())).thenReturn(List.of());

        tools.searchAnnouncements(scope, "有什么满减活动", 3);

        ArgumentCaptor<RagKnowledgeQuery> captor = ArgumentCaptor.forClass(RagKnowledgeQuery.class);
        verify(knowledgeService).search(captor.capture());
        assertThat(captor.getValue().audience()).isEqualTo(RagAudience.CUSTOMER);
        assertThat(captor.getValue().merchantId()).isNull();
        assertThat(captor.getValue().limit()).isEqualTo(3);
    }

    @Test
    void merchantScopeUsesTrustedBindingOnly() {
        AgenticScope scope = scopeFor(AgentChatContext.merchant("2002", "merchant-2"));
        when(knowledgeService.search(any())).thenReturn(List.of());

        tools.searchAnnouncements(scope, "平台对商家的上架规则", 2);

        ArgumentCaptor<RagKnowledgeQuery> captor = ArgumentCaptor.forClass(RagKnowledgeQuery.class);
        verify(knowledgeService).search(captor.capture());
        assertThat(captor.getValue().audience()).isEqualTo(RagAudience.MERCHANT);
        assertThat(captor.getValue().merchantId()).isEqualTo("merchant-2");
        assertThat(captor.getValue().limit()).isEqualTo(2);
    }

    @Test
    void customerWithoutMerchantSearchesPlatformScopeOnly() {
        AgenticScope scope = scopeFor(AgentChatContext.customer("1001"));
        when(knowledgeService.search(any())).thenReturn(List.of());

        tools.searchAnnouncements(scope, "平台活动", null);

        ArgumentCaptor<RagKnowledgeQuery> captor = ArgumentCaptor.forClass(RagKnowledgeQuery.class);
        verify(knowledgeService).search(captor.capture());
        assertThat(captor.getValue().audience()).isEqualTo(RagAudience.CUSTOMER);
        assertThat(captor.getValue().merchantId()).isNull();
        assertThat(captor.getValue().limit()).isEqualTo(3);
    }

    @Test
    void resultIncludesTitleAndEffectiveRangeButNotInternalIdentifiers() {
        AgenticScope scope = scopeFor(AgentChatContext.customer("1001"));
        when(knowledgeService.search(any())).thenReturn(List.of(result()));

        String answer = tools.searchAnnouncements(scope, "周末活动", null);

        assertThat(answer).contains("周末满减");
        assertThat(answer).contains("2026-10-01 00:00");
        assertThat(answer).contains("2026-11-30 23:59");
        assertThat(answer).doesNotContain("announcement-1");
        assertThat(answer).doesNotContain("merchant-2");
        assertThat(answer).doesNotContain("分数");
        assertThat(answer).doesNotContain("filter");
    }

    @Test
    void emptySearchExplicitlySaysNoResults() {
        AgenticScope scope = scopeFor(AgentChatContext.customer("1001"));
        when(knowledgeService.search(any())).thenReturn(List.of());

        String answer = tools.searchAnnouncements(scope, "不存在的话题", null);

        assertThat(answer).isEqualTo("未查询到相关有效公告。");
    }

    @Test
    void missingScopeFailsClosed() {
        String answer = tools.searchAnnouncements(null, "平台活动", null);

        assertThat(answer).isEqualTo("公告检索暂不可用，请重新登录后再试。");
        verifyNoInteractions(knowledgeService);
    }

    @Test
    void retrievalFailureReturnsControlledUnavailableMessage() {
        AgenticScope scope = scopeFor(AgentChatContext.customer("1001"));
        when(knowledgeService.search(any())).thenThrow(new IllegalStateException("qdrant unavailable"));

        String answer = tools.searchAnnouncements(scope, "平台活动", null);

        assertThat(answer).isEqualTo("公告检索暂不可用，请稍后再试。");
    }

    private AgenticScope scopeFor(AgentChatContext context) {
        dev.langchain4j.agentic.scope.AgenticScope recordingScope = new RecordingScope();
        new AgentChatContextListener(context).afterAgenticScopeCreated(recordingScope);
        return recordingScope;
    }

    private RagKnowledgeResult result() {
        return new RagKnowledgeResult(
            "announcement-1",
            "周末满减",
            "满30减5，周末可用。",
            0,
            1,
            "merchant-2",
            "customer",
            LocalDateTime.parse("2026-10-01T00:00:00"),
            LocalDateTime.parse("2026-11-30T23:59:59"));
    }

    private static class RecordingScope implements AgenticScope {
        private final java.util.Map<String, Object> states = new java.util.HashMap<>();

        @Override
        public Object memoryId() {
            return "test-scope";
        }

        @Override
        public void writeState(String key, Object value) {
            if (value == null) {
                states.remove(key);
                return;
            }
            states.put(key, value);
        }

        @Override
        public <T> void writeState(Class<? extends dev.langchain4j.agentic.declarative.TypedKey<T>> key, T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void writeStates(java.util.Map<String, Object> values) {
            states.putAll(values);
        }

        @Override
        public boolean hasState(String key) {
            return states.containsKey(key);
        }

        @Override
        public boolean hasState(Class<? extends dev.langchain4j.agentic.declarative.TypedKey<?>> key) {
            return false;
        }

        @Override
        public Object readState(String key) {
            return states.get(key);
        }

        @Override
        public <T> T readState(String key, T defaultValue) {
            @SuppressWarnings("unchecked")
            T value = (T) states.get(key);
            return value == null ? defaultValue : value;
        }

        @Override
        public <T> T readState(Class<? extends dev.langchain4j.agentic.declarative.TypedKey<T>> key) {
            return null;
        }

        @Override
        public java.util.Map<String, Object> state() {
            return new java.util.HashMap<>(states);
        }

        @Override
        public String contextAsConversation(String... keys) {
            return "";
        }

        @Override
        public String contextAsConversation(Object... values) {
            return "";
        }

        @Override
        public java.util.List<AgentInvocation> agentInvocations() {
            return List.of();
        }

        @Override
        public java.util.List<AgentInvocation> agentInvocations(String agentName) {
            return List.of();
        }

        @Override
        public java.util.List<AgentInvocation> agentInvocations(Class<?> agentClass) {
            return List.of();
        }
    }
}
