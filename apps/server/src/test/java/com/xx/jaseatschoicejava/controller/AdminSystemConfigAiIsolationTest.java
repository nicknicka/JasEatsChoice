package com.xx.jaseatschoicejava.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.jaseatschoicejava.entity.SystemConfig;
import com.xx.jaseatschoicejava.service.AiSystemConfigGuard;
import com.xx.jaseatschoicejava.service.SystemConfigService;

@ExtendWith(MockitoExtension.class)
class AdminSystemConfigAiIsolationTest {

    @Mock
    private SystemConfigService systemConfigService;

    private AdminSystemConfigController controller;
    private AiSystemConfigGuard guard;

    @BeforeEach
    void setUp() {
        controller = new AdminSystemConfigController();
        guard = new AiSystemConfigGuard();
        ReflectionTestUtils.setField(controller, "systemConfigService", systemConfigService);
        ReflectionTestUtils.setField(controller, "aiSystemConfigGuard", guard);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthority(String authority) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
            "admin", "token", List.of(new SimpleGrantedAuthority(authority))));
        SecurityContextHolder.setContext(context);
    }

    @Test
    void adminWithoutAiPermissionCannotListAiRuntimeConfig() {
        setAuthority("admin:setting:config");
        SystemConfig aiConfig = new SystemConfig();
        aiConfig.setConfigKey("ai.runtime-config");
        aiConfig.setConfigGroup("ai");
        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setConfigKey("system.name");
        systemConfig.setConfigGroup("system");
        Page<SystemConfig> page = new Page<>(1, 10);
        page.setRecords(new java.util.ArrayList<>(List.of(aiConfig, systemConfig)));
        when(systemConfigService.page(any(Page.class), any())).thenReturn(page);

        var response = controller.getConfigList(1, 10, null, null, null);

        assertEquals(1, response.getBody().getRecords().size());
        assertEquals("system.name", response.getBody().getRecords().get(0).getConfigKey());
    }

    @Test
    void adminWithoutAiPermissionCannotReadAiRuntimeGroup() {
        setAuthority("admin:setting:config");

        var response = controller.getConfigsByGroup("ai");

        assertEquals(false, response.getBody().get("success"));
        verify(systemConfigService, never()).getConfigsByGroup("ai");
    }

    @Test
    void adminWithoutAiPermissionCannotCreateAiRuntimeConfig() {
        setAuthority("admin:setting:config");
        Map<String, Object> request = new HashMap<>();
        request.put("configKey", "ai.runtime-config");
        request.put("configGroup", "ai");
        request.put("configValue", "{}");

        var response = controller.createConfig(request);

        assertEquals(403, response.getStatusCode().value());
        verify(systemConfigService, never()).save(any(SystemConfig.class));
    }

    @Test
    void adminWithoutAiPermissionCannotUpdateAiRuntimeConfig() {
        setAuthority("admin:setting:config");
        SystemConfig config = new SystemConfig();
        config.setId("ai-config-id");
        config.setConfigKey("ai.runtime-config");
        config.setConfigGroup("ai");
        when(systemConfigService.getById("ai-config-id")).thenReturn(config);

        var response = controller.updateConfig("ai-config-id", Map.of("configValue", "{}"));

        assertEquals(403, response.getStatusCode().value());
        verify(systemConfigService, never()).updateById(any(SystemConfig.class));
    }

    @Test
    void superAdminCanUpdateAiRuntimeConfig() {
        setAuthority("admin:super");
        SystemConfig config = new SystemConfig();
        config.setId("ai-config-id");
        config.setConfigKey("ai.runtime-config");
        config.setConfigGroup("ai");
        when(systemConfigService.getById("ai-config-id")).thenReturn(config);
        when(systemConfigService.updateById(any(SystemConfig.class))).thenReturn(true);

        var response = controller.updateConfig("ai-config-id", Map.of("configValue", "{}"));

        assertEquals(200, response.getStatusCode().value());
        verify(systemConfigService).updateById(any(SystemConfig.class));
    }
}
