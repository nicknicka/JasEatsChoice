package com.xx.jaseatschoicejava.agent.config;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.config.AIConfig;
import com.xx.jaseatschoicejava.config.ZhipuAIConfig;

class ZhipuChatModelFactoryTest {

    @Test
    void getCapabilities_withoutKeyAndVisionModel_reportsAllCapabilitiesUnavailable() {
        ZhipuAIConfig zhipuConfig = new ZhipuAIConfig();
        zhipuConfig.setApiKey("");
        zhipuConfig.setVisionModel("");
        AIConfig aiConfig = new AIConfig();
        aiConfig.setApiKey("");
        aiConfig.setVisionModel("");

        ZhipuChatModelFactory factory = new ZhipuChatModelFactory();
        ReflectionTestUtils.setField(factory, "zhipuAIConfig", zhipuConfig);
        ReflectionTestUtils.setField(factory, "aiConfig", aiConfig);

        AiCapabilitiesDTO capabilities = factory.chatModelProvider().getCapabilities();

        assertFalse(capabilities.isAvailable());
        assertFalse(capabilities.isTextChat());
        assertFalse(capabilities.isStreaming());
        assertFalse(capabilities.isVision());
    }
}
