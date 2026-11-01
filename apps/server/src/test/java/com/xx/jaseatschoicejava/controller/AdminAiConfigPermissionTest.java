package com.xx.jaseatschoicejava.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import com.xx.jaseatschoicejava.config.MethodSecurityConfig;
import com.xx.jaseatschoicejava.service.AiRuntimeConfigService;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    MethodSecurityConfig.class,
    AdminAiConfigPermissionTest.TestConfig.class
})
class AdminAiConfigPermissionTest {

    @Autowired
    private AiRuntimeConfigService service;

    @Configuration
    static class TestConfig {
        @Bean
        AiRuntimeConfigService aiRuntimeConfigService() {
            return mock(AiRuntimeConfigService.class);
        }

        @Bean
        AdminAiConfigController adminAiConfigController(AiRuntimeConfigService service) {
            return new AdminAiConfigController(service);
        }
    }

    @Autowired
    private AdminAiConfigController controller;

    @BeforeEach
    void setUp() {
        reset(service);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
            "admin", "token", List.of(new SimpleGrantedAuthority("admin:setting:config"))));
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminWithoutAiPermissionCannotReadAiConfig() {
        assertThrows(AccessDeniedException.class, controller::getCurrentConfig);
        verifyNoInteractions(service);
    }

    @Test
    void adminWithAiPermissionCanReadAiConfig() {
        setAuthority("admin:setting:ai");
        assertDoesNotThrow(controller::getCurrentConfig);
        verify(service).getCurrentSnapshot();
    }

    @Test
    void adminWithoutAiPermissionCannotTestAiConnectivity() {
        assertThrows(AccessDeniedException.class, controller::testConnectivity);
        verifyNoInteractions(service);
    }

    @Test
    void adminWithoutAiPermissionCannotReadPreviousSummaryOrRollback() {
        assertThrows(AccessDeniedException.class, controller::getPreviousConfigSummary);
        assertThrows(AccessDeniedException.class, controller::rollbackToPrevious);
        verifyNoInteractions(service);
    }

    @Test
    void adminWithAiPermissionCanReadPreviousSummaryAndRollback() {
        setAuthority("admin:setting:ai");
        assertDoesNotThrow(controller::getPreviousConfigSummary);
        assertDoesNotThrow(controller::rollbackToPrevious);
        verify(service).getPreviousConfigSummary();
        verify(service).rollbackToPrevious("admin");
    }

    @Test
    void superAdminCanReadAiConfig() {
        setAuthority("admin:super");
        assertDoesNotThrow(controller::getCurrentConfig);
        verify(service).getCurrentSnapshot();
    }

    private void setAuthority(String authority) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
            "admin", "token", List.of(new SimpleGrantedAuthority(authority))));
        SecurityContextHolder.setContext(context);
    }
}
