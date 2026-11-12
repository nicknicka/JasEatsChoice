package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.config.GitHubBridgeProperties;
import com.xx.jaseatschoicejava.dto.AgentRuntimeHealthVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentRuntimeConfigServiceTest {

    @Mock
    private GitHubBridgeProperties gitHubBridgeProperties;

    @InjectMocks
    private AgentRuntimeConfigService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "dshProfilePath", "");
        ReflectionTestUtils.setField(service, "isolatedHome", "");
    }

    @Test
    void health_shouldAlwaysReportHardBoundariesEnabled() {
        when(gitHubBridgeProperties.isConfigured()).thenReturn(false);

        AgentRuntimeHealthVO vo = service.getHealth();

        assertEquals(2, vo.getBoundaries().size());
        assertTrue(vo.getBoundaries().stream().allMatch(AgentRuntimeHealthVO.Boundary::isEnabled));
        assertTrue(vo.getBoundaries().stream().anyMatch(b -> "no-deploy".equals(b.getCode())));
        assertTrue(vo.getBoundaries().stream().anyMatch(b -> "no-production-data".equals(b.getCode())));
        assertFalse(vo.getBoundaries().stream().anyMatch(b -> "no-merge".equals(b.getCode())));
    }

    @Test
    void health_shouldReflectGithubBridgeConfigured() {
        when(gitHubBridgeProperties.isConfigured()).thenReturn(true);

        assertTrue(service.getHealth().getGithub().isOk());
    }

    @Test
    void health_shouldMarkGithubUnconfiguredWhenBridgeMissing() {
        when(gitHubBridgeProperties.isConfigured()).thenReturn(false);

        assertFalse(service.getHealth().getGithub().isOk());
    }

    @Test
    void health_shouldDetectDshProfileAndIsolatedHomeByPath(@TempDir Path tmp) {
        ReflectionTestUtils.setField(service, "dshProfilePath", tmp.toString());
        ReflectionTestUtils.setField(service, "isolatedHome", tmp.resolve("missing").toString());
        when(gitHubBridgeProperties.isConfigured()).thenReturn(false);

        AgentRuntimeHealthVO vo = service.getHealth();

        assertTrue(vo.getDshProfile().isOk());
        assertFalse(vo.getIsolatedAccount().isOk());
    }
}
