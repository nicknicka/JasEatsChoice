package com.xx.jaseatschoicejava.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.xx.jaseatschoicejava.agent.agents.CustomerServiceAgent;
import com.xx.jaseatschoicejava.agent.agents.SimpleChatAgent;
import com.xx.jaseatschoicejava.agent.context.AgentChatContext;
import com.xx.jaseatschoicejava.agent.context.AgentChatContextResolver;
import com.xx.jaseatschoicejava.agent.service.IntentClassifier;
import com.xx.jaseatschoicejava.agent.service.SupervisorAgentFactory;

import dev.langchain4j.agentic.supervisor.SupervisorAgent;

class AgentChatContextEndpointTest {

    private static final AgentChatContext CUSTOMER = AgentChatContext.customer("1001");
    private static final AgentChatContext MERCHANT = AgentChatContext.merchant("2002", "m-2002");

    private final AgentChatContextResolver resolver = mock(AgentChatContextResolver.class);
    private final SupervisorAgentFactory supervisorAgentFactory = mock(SupervisorAgentFactory.class);
    private final CustomerServiceAgent customerServiceAgent = mock(CustomerServiceAgent.class);
    private final SimpleChatAgent simpleChatAgent = mock(SimpleChatAgent.class);
    private final IntentClassifier intentClassifier = mock(IntentClassifier.class);
    private final SupervisorAgent supervisorAgent = mock(SupervisorAgent.class);

    private MockMvc syncMockMvc;
    private SupervisorSSEController sseController;

    @BeforeEach
    void setUp() {
        SupervisorAgentController syncController = new SupervisorAgentController();
        ReflectionTestUtils.setField(syncController, "supervisorAgentFactory", supervisorAgentFactory);
        ReflectionTestUtils.setField(syncController, "agentChatContextResolver", resolver);
        syncMockMvc = MockMvcBuilders.standaloneSetup(syncController).build();

        sseController = new SupervisorSSEController(
                supervisorAgentFactory, customerServiceAgent, simpleChatAgent, intentClassifier, resolver);
        when(supervisorAgentFactory.createWithListener(any(), any())).thenReturn(supervisorAgent);
        when(supervisorAgent.invoke(any())).thenReturn("处理完成");
        when(supervisorAgentFactory.cleanDebugInfo("处理完成")).thenReturn("处理完成");
    }

    @Test
    void syncChatUsesSignedCustomerContextAndIgnoresRequestBodyUserId() throws Exception {
        when(resolver.resolve(any())).thenReturn(Optional.of(CUSTOMER));

        syncMockMvc.perform(post("/agent/supervisor/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"推荐菜品","userId":"999"}
                                """))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));

        ArgumentCaptor<AgentChatContext> captor = ArgumentCaptor.forClass(AgentChatContext.class);
        verify(supervisorAgentFactory).createWithListener(eq(null), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(CUSTOMER, captor.getValue());
    }

    @Test
    void syncChatUsesSignedMerchantContextFromResolver() throws Exception {
        when(resolver.resolve(any())).thenReturn(Optional.of(MERCHANT));

        syncMockMvc.perform(post("/agent/supervisor/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"查看店铺公告","userId":"999"}
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<AgentChatContext> captor = ArgumentCaptor.forClass(AgentChatContext.class);
        verify(supervisorAgentFactory).createWithListener(eq(null), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(MERCHANT, captor.getValue());
    }

    @Test
    void syncChatFailsClosedWithoutSignedIdentity() throws Exception {
        when(resolver.resolve(any())).thenReturn(Optional.empty());

        syncMockMvc.perform(post("/agent/supervisor/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"推荐菜品","userId":"999"}
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<AgentChatContext> captor = ArgumentCaptor.forClass(AgentChatContext.class);
        verify(supervisorAgentFactory).createWithListener(eq(null), captor.capture());
        org.junit.jupiter.api.Assertions.assertNull(captor.getValue());
    }

    @Test
    void sseChatUsesSameSignedCustomerContext() {
        when(resolver.resolve(any())).thenReturn(Optional.of(CUSTOMER));
        when(intentClassifier.classify(any())).thenReturn(IntentClassifier.IntentType.BUSINESS);

        sseController.chatStream("推荐菜品", "signed-token", new MockHttpServletRequest());

        ArgumentCaptor<AgentChatContext> captor = ArgumentCaptor.forClass(AgentChatContext.class);
        verify(supervisorAgentFactory, timeout(TimeUnit.SECONDS.toMillis(1)))
                .createWithListener(any(), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(CUSTOMER, captor.getValue());
    }

    @Test
    void sseChatUsesSameSignedMerchantContext() {
        when(resolver.resolve(any())).thenReturn(Optional.of(MERCHANT));
        when(intentClassifier.classify(any())).thenReturn(IntentClassifier.IntentType.BUSINESS);

        sseController.chatStream("查看店铺公告", "signed-token", new MockHttpServletRequest());

        ArgumentCaptor<AgentChatContext> captor = ArgumentCaptor.forClass(AgentChatContext.class);
        verify(supervisorAgentFactory, timeout(TimeUnit.SECONDS.toMillis(1)))
                .createWithListener(any(), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(MERCHANT, captor.getValue());
    }

    @Test
    void sseChatFailsClosedWithoutSignedIdentity() throws Exception {
        when(resolver.resolve(any())).thenReturn(Optional.empty());
        when(customerServiceAgent.chat(any())).thenReturn("未登录，无法访问个性化能力");

        sseController.chatStream("推荐菜品", null, new MockHttpServletRequest());

        Thread.sleep(100);
        verify(supervisorAgentFactory, org.mockito.Mockito.never())
                .createWithListener(any(), any());
    }
}
