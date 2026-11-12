package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.config.GitHubBridgeProperties;
import com.xx.jaseatschoicejava.dto.AgentRuntimeConfigDTO;
import com.xx.jaseatschoicejava.dto.AgentRuntimeHealthVO;
import com.xx.jaseatschoicejava.entity.AgentRuntimeConfig;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.mapper.AgentRuntimeConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentRuntimeConfigServiceTest {

    @Mock
    private AgentRuntimeConfigMapper configMapper;

    @Mock
    private GitHubBridgeProperties gitHubBridgeProperties;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AgentRuntimeConfigService service;

    @TempDir
    Path keyDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "keyDir", keyDir.toString());
        ReflectionTestUtils.setField(service, "dshProfilePath", "");
        ReflectionTestUtils.setField(service, "isolatedHome", "");
    }

    private static AgentRuntimeConfigDTO dto(String provider) {
        AgentRuntimeConfigDTO dto = new AgentRuntimeConfigDTO();
        dto.setProvider(provider);
        dto.setModel("gpt-4o-mini");
        dto.setEndpoint("https://api.example.com/v1/models");
        dto.setDataPolicyAck(true);
        return dto;
    }

    @Test
    void update_whenProviderNotAllowlisted_shouldReject() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.update(dto("some-random-provider"), "admin"));
        assertEquals("400", ex.getCode());
        verify(configMapper, never()).insert(any(AgentRuntimeConfig.class));
    }

    @Test
    void update_shouldIncrementVersionAndWriteHistory() {
        AgentRuntimeConfig existing = new AgentRuntimeConfig();
        existing.setConfigId("default");
        existing.setVersion(0);
        when(configMapper.selectById("default")).thenReturn(existing);
        when(configMapper.updateById(any(AgentRuntimeConfig.class))).thenReturn(1);

        AgentRuntimeHealthVO vo = service.update(dto("openai-compatible"), "admin");

        assertEquals("openai-compatible", vo.getProvider());
        assertEquals(1, vo.getVersion());
        verify(configMapper).updateById(any(AgentRuntimeConfig.class));
        verify(configMapper).insertHistory(anyString(), eq("default"), eq(1),
                eq("openai-compatible"), eq("gpt-4o-mini"), anyString(), eq(true),
                eq("manual"), isNull(), eq("admin"));
    }

    @Test
    void update_withSourceVersion_shouldMarkHistoryAsRollback() {
        AgentRuntimeConfig existing = new AgentRuntimeConfig();
        existing.setConfigId("default");
        existing.setVersion(5);
        when(configMapper.selectById("default")).thenReturn(existing);
        when(configMapper.updateById(any(AgentRuntimeConfig.class))).thenReturn(1);

        AgentRuntimeConfigDTO request = dto("openai-compatible");
        request.setSourceVersion(2);

        service.update(request, "admin");

        // 带来源版本即回滚留痕，而不是一次普通修改
        verify(configMapper).insertHistory(anyString(), eq("default"), eq(6),
                eq("openai-compatible"), eq("gpt-4o-mini"), anyString(), eq(true),
                eq("rollback"), eq(2), eq("admin"));
    }

    @Test
    void update_withNonPositiveSourceVersion_shouldReject() {
        AgentRuntimeConfigDTO request = dto("openai-compatible");
        request.setSourceVersion(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.update(request, "admin"));
        assertEquals("400", ex.getCode());
        verify(configMapper, never()).insertHistory(anyString(), anyString(), anyInt(), anyString(),
                anyString(), anyString(), anyBoolean(), anyString(), any(), anyString());
    }

    @Test
    void rotateKey_shouldStoreWithoutExposingPlaintext() {
        when(configMapper.selectById("default")).thenReturn(null);

        AgentRuntimeHealthVO vo = service.rotateKey("sk-super-secret-value", "admin");

        assertTrue(vo.isModelKeyConfigured());
        // 明文只落文件，不出现在返回的任何可读字段里。
        assertFalse(vo.toString().contains("sk-super-secret-value"));
    }

    @Test
    void revokeKey_shouldClearKey() {
        when(configMapper.selectById("default")).thenReturn(null);
        service.rotateKey("sk-abc", "admin");

        AgentRuntimeHealthVO vo = service.revokeKey();

        assertFalse(vo.isModelKeyConfigured());
    }

    @Test
    void health_shouldAlwaysReportHardBoundariesEnabled() {
        when(configMapper.selectById("default")).thenReturn(null);

        AgentRuntimeHealthVO vo = service.getHealth();

        assertEquals(3, vo.getBoundaries().size());
        assertTrue(vo.getBoundaries().stream().allMatch(AgentRuntimeHealthVO.Boundary::isEnabled));
        assertTrue(vo.getBoundaries().stream().anyMatch(b -> "no-merge".equals(b.getCode())));
        assertTrue(vo.getBoundaries().stream().anyMatch(b -> "no-deploy".equals(b.getCode())));
        assertTrue(vo.getBoundaries().stream().anyMatch(b -> "no-production-data".equals(b.getCode())));
    }
}
