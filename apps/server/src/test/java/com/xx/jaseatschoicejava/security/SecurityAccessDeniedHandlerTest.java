package com.xx.jaseatschoicejava.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class SecurityAccessDeniedHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecurityAccessDeniedHandler handler = new SecurityAccessDeniedHandler(objectMapper);

    @Test
    void returnsUnifiedJsonWithForbiddenStatus() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(new MockHttpServletRequest(), response,
            new AccessDeniedException("forbidden"));

        assertEquals(403, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertFalse(body.path("success").asBoolean());
        assertEquals("403", body.path("code").asText());
        assertEquals("无权限访问该资源", body.path("message").asText());
    }
}
