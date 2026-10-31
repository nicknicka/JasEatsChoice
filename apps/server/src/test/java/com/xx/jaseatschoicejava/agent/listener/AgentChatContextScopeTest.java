package com.xx.jaseatschoicejava.agent.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.xx.jaseatschoicejava.agent.context.AgentChatContext;

import dev.langchain4j.agentic.declarative.TypedKey;
import dev.langchain4j.agentic.scope.AgentInvocation;
import dev.langchain4j.agentic.scope.AgenticScope;

class AgentChatContextScopeTest {

    @Test
    void customerScopeContainsOnlyUserIdAndAudience() {
        RecordingScope scope = new RecordingScope();
        new AgentChatContextListener(AgentChatContext.customer("1001"))
                .afterAgenticScopeCreated(scope);

        assertEquals("1001", scope.states.get("userId"));
        assertEquals("customer", scope.states.get("audience"));
        assertFalse(scope.states.containsKey("merchantId"));
    }

    @Test
    void merchantScopeContainsTrustedMerchantBinding() {
        RecordingScope scope = new RecordingScope();
        new AgentChatContextListener(AgentChatContext.merchant("2002", "m-2002"))
                .afterAgenticScopeCreated(scope);

        assertEquals("2002", scope.states.get("userId"));
        assertEquals("merchant", scope.states.get("audience"));
        assertEquals("m-2002", scope.states.get("merchantId"));
    }

    @Test
    void missingContextFailsClosedWithoutAnyTrustedScope() {
        RecordingScope scope = new RecordingScope();
        new AgentChatContextListener(null).afterAgenticScopeCreated(scope);

        assertTrue(scope.states.isEmpty());
    }

    @Test
    void sseListenerDoesNotDuplicateContextListenerWrites() {
        RecordingScope customerScope = new RecordingScope();
        var composite = new CompositeAgentListener(
            new AgentChatContextListener(AgentChatContext.customer("1001")),
            new SSEAgentListener(null, AgentChatContext.customer("1001"), "推荐菜品"));
        composite.afterAgenticScopeCreated(customerScope);
        assertEquals("1001", customerScope.states.get("userId"));
        assertEquals("customer", customerScope.states.get("audience"));
        assertFalse(customerScope.states.containsKey("merchantId"));

        RecordingScope merchantScope = new RecordingScope();
        var merchantComposite = new CompositeAgentListener(
            new AgentChatContextListener(AgentChatContext.merchant("2002", "m-2002")),
            new SSEAgentListener(null, AgentChatContext.merchant("2002", "m-2002"), "查看店铺公告"));
        merchantComposite.afterAgenticScopeCreated(merchantScope);
        assertEquals("2002", merchantScope.states.get("userId"));
        assertEquals("merchant", merchantScope.states.get("audience"));
        assertEquals("m-2002", merchantScope.states.get("merchantId"));
    }

    @Test
    void sseAloneDoesNotInitializeContextBecauseFactoryOwnsIt() {
        RecordingScope scope = new RecordingScope();
        new SSEAgentListener(null, AgentChatContext.customer("1001"), "推荐菜品")
                .afterAgenticScopeCreated(scope);
        assertFalse(scope.states.containsKey("userId"));
        assertFalse(scope.states.containsKey("audience"));
        assertFalse(scope.states.containsKey("merchantId"));
    }

    private static class RecordingScope implements AgenticScope {
        private final Map<String, Object> states = new HashMap<>();

        @Override
        public Object memoryId() {
            return "test-scope";
        }

        @Override
        public void writeState(String key, Object value) {
            states.put(key, value);
        }

        @Override
        public <T> void writeState(Class<? extends TypedKey<T>> key, T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void writeStates(Map<String, Object> values) {
            states.putAll(values);
        }

        @Override
        public boolean hasState(String key) {
            return states.containsKey(key);
        }

        @Override
        public boolean hasState(Class<? extends TypedKey<?>> key) {
            return false;
        }

        @Override
        public Object readState(String key) {
            return states.get(key);
        }

        @Override
        public <T> T readState(String key, T defaultValue) {
            return states.containsKey(key) ? (T) states.get(key) : defaultValue;
        }

        @Override
        public <T> T readState(Class<? extends TypedKey<T>> key) {
            return null;
        }

        @Override
        public Map<String, Object> state() {
            return new HashMap<>(states);
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
        public List<AgentInvocation> agentInvocations() {
            return new ArrayList<>();
        }

        @Override
        public List<AgentInvocation> agentInvocations(String agentName) {
            return new ArrayList<>();
        }

        @Override
        public List<AgentInvocation> agentInvocations(Class<?> agentClass) {
            return new ArrayList<>();
        }
    }
}
