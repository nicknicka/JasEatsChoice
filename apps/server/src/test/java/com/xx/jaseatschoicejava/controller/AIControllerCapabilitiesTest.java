package com.xx.jaseatschoicejava.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.xx.jaseatschoicejava.agent.config.ChatModelProvider;
import com.xx.jaseatschoicejava.agent.dto.AiCapabilitiesDTO;
import com.xx.jaseatschoicejava.common.ResponseResult;
import com.xx.jaseatschoicejava.service.AiAssistantService;
import com.xx.jaseatschoicejava.service.AiQuotaService;
import com.xx.jaseatschoicejava.util.UserIdentityUtil;

@ExtendWith(MockitoExtension.class)
class AIControllerCapabilitiesTest {

    @Mock
    private ChatModelProvider chatModelProvider;

    @Mock
    private AiAssistantService aiAssistantService;

    @Mock
    private AiQuotaService aiQuotaService;

    private AIController controller;

    @BeforeEach
    void setUp() {
        controller = new AIController();
        ReflectionTestUtils.setField(controller, "chatModelProvider", chatModelProvider);
        ReflectionTestUtils.setField(controller, "aiAssistantService", aiAssistantService);
        ReflectionTestUtils.setField(controller, "aiQuotaService", aiQuotaService);
    }

    private static MockHttpServletRequest verifiedRequest(String userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(UserIdentityUtil.VERIFIED_USER_ID_ATTRIBUTE, userId);
        return request;
    }

    @Test
    void getCapabilities_withoutKeyAndVisionModel_capabilitiesAreDisabled() {
        when(chatModelProvider.getCapabilities()).thenReturn(AiCapabilitiesDTO.builder()
                .available(false)
                .vision(false)
                .textChat(false)
                .streaming(false)
                .build());

        ResponseResult<AiCapabilitiesDTO> result = controller.getCapabilities();

        assertTrue(result.getSuccess());
        assertFalse(result.getData().isAvailable());
        assertFalse(result.getData().isVision());
    }

    @Test
    void dishRecognize_withoutVision_returnsBusinessErrorInsteadOfNullPointer() {
        when(chatModelProvider.getCapabilities()).thenReturn(AiCapabilitiesDTO.builder()
                .vision(false)
                .build());

        ResponseResult<?> result = controller.dishRecognize(
                new MockMultipartFile("image", "dish.jpg", "image/jpeg",
                        new byte[] { (byte) 0xFF, (byte) 0xD8, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }),
                verifiedRequest("42"));

        assertFalse(result.getSuccess());
        assertEquals("AI_VISION_NOT_CONFIGURED", result.getCode());
        assertTrue(result.getMessage().contains("暂未开放"));
    }

    @Test
    void recipe_withoutTextCapability_returnsBusinessErrorInsteadOfEmptyResult() {
        when(chatModelProvider.getCapabilities()).thenReturn(AiCapabilitiesDTO.builder()
                .textChat(false)
                .build());

        ResponseResult<?> result = controller.recipeOptimize(
            Map.of("recipe", "番茄炒蛋"), verifiedRequest("42"));

        assertFalse(result.getSuccess());
        assertEquals("AI_NOT_CONFIGURED", result.getCode());
        assertTrue(result.getMessage().contains("暂未开放"));
    }
}
