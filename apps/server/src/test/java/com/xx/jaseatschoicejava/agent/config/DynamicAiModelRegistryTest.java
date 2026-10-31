package com.xx.jaseatschoicejava.agent.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.xx.jaseatschoicejava.config.AiModelConfig;

import dev.langchain4j.model.chat.ChatModel;

class DynamicAiModelRegistryTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void newRequestUsesActivatedClientWhilePinnedRequestKeepsOldClient() {
        DynamicAiModelRegistry registry = new DynamicAiModelRegistry();
        AiModelConfig oldConfig = AiModelConfig.builder()
            .provider("zhipu").model("old-chat").supervisorModel("old-supervisor")
            .visionModel("old-vision").baseUrl("").timeoutMs(1000).temperature(0.1).build();
        AiModelConfig newConfig = AiModelConfig.builder()
            .provider("zhipu").model("new-chat").supervisorModel("new-supervisor")
            .visionModel("new-vision").baseUrl("").timeoutMs(2000).temperature(0.2).build();

        ChatModel oldModel = mock(ChatModel.class);
        ChatModel newModel = mock(ChatModel.class);
        when(oldModel.chat("ping")).thenReturn("old");
        when(newModel.chat("ping")).thenReturn("new");
        registry.activate(oldConfig, oldModel, oldModel, oldModel, 1);

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        ChatModel proxy = registry.chatModel();
        assertEquals("old", proxy.chat("ping"));

        registry.activate(newConfig, newModel, newModel, newModel, 2);
        assertEquals("old", proxy.chat("ping"));

        RequestContextHolder.resetRequestAttributes();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        assertEquals("new", registry.chatModel().chat("ping"));
    }
}
