package com.xx.jaseatschoicejava.service;

import com.xx.jaseatschoicejava.config.FileSecretStore;
import com.xx.jaseatschoicejava.config.GitHubBridgeProperties;
import com.xx.jaseatschoicejava.dto.AgentRuntimeConfigDTO;
import com.xx.jaseatschoicejava.dto.AgentRuntimeHealthVO;
import com.xx.jaseatschoicejava.entity.AgentRuntimeConfig;
import com.xx.jaseatschoicejava.entity.AgentRuntimeConfigHistory;
import com.xx.jaseatschoicejava.exception.BusinessException;
import com.xx.jaseatschoicejava.mapper.AgentRuntimeConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Agent 运行时配置与密钥管理。
 *
 * <p>密钥只写不可读：写进文件型密钥存储，接口只回布尔与更新时间，任何日志/响应都不含明文。
 */
@Service
public class AgentRuntimeConfigService {

    private static final String CONFIG_ID = "default";
    private static final String KEY_SLOT = "model";
    /** 允许的供应商白名单，页面无法写入白名单外的值。 */
    private static final Set<String> PROVIDER_ALLOWLIST =
            Set.of("openai-compatible", "openai", "anthropic", "azure-openai");

    @Autowired
    private AgentRuntimeConfigMapper configMapper;

    @Autowired
    private GitHubBridgeProperties gitHubBridgeProperties;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${agent.runtime.key-dir:}")
    private String keyDir;

    @Value("${agent.runtime.dsh-profile-path:}")
    private String dshProfilePath;

    @Value("${agent.runtime.isolated-home:}")
    private String isolatedHome;

    /**
     * 配置页数据：配置 + 健康 + 硬边界 + 密钥元数据（不含明文）。
     */
    public AgentRuntimeHealthVO getHealth() {
        AgentRuntimeConfig config = current();
        AgentRuntimeHealthVO vo = new AgentRuntimeHealthVO();
        vo.setProvider(config.getProvider());
        vo.setModel(config.getModel());
        vo.setEndpoint(config.getEndpoint());
        vo.setDataPolicyAck(Boolean.TRUE.equals(config.getDataPolicyAck()));
        vo.setVersion(config.getVersion());
        vo.setUpdatedBy(config.getUpdatedBy());
        vo.setModelKeyConfigured(keyStore().exists(KEY_SLOT));
        vo.setModelKeyUpdatedAt(keyStore().updatedAt(KEY_SLOT));

        boolean endpointReady = hasText(config.getEndpoint()) && keyStore().exists(KEY_SLOT);
        vo.setGithub(new AgentRuntimeHealthVO.Health("GitHub", gitHubBridgeProperties.isConfigured(),
                gitHubBridgeProperties.isConfigured() ? "token 与仓库已配置" : "未配置 token 与仓库"));
        vo.setDshProfile(new AgentRuntimeHealthVO.Health("DSH profile", pathExists(dshProfilePath),
                pathExists(dshProfilePath) ? "profile 目录存在" : "未找到 DSH profile"));
        vo.setModelHealth(new AgentRuntimeHealthVO.Health("模型", endpointReady,
                endpointReady ? "endpoint 与 key 已就绪" : "缺少 endpoint 或模型 key"));
        vo.setIsolatedAccount(new AgentRuntimeHealthVO.Health("隔离账号", pathExists(isolatedHome),
                pathExists(isolatedHome) ? "专用账号 home 存在" : "未找到专用账号 home"));

        // 硬边界固定为开启（enabled=true 表示限制生效），页面不能关闭。
        vo.setBoundaries(List.of(
                new AgentRuntimeHealthVO.Boundary("no-merge", "禁止代理合并 PR", true),
                new AgentRuntimeHealthVO.Boundary("no-deploy", "禁止代理部署", true),
                new AgentRuntimeHealthVO.Boundary("no-production-data", "禁止访问生产数据", true)));
        return vo;
    }

    /**
     * 更新供应商/模型/endpoint/数据政策，写版本历史。
     */
    public AgentRuntimeHealthVO update(AgentRuntimeConfigDTO dto, String adminName) {
        if (dto == null || !PROVIDER_ALLOWLIST.contains(dto.getProvider())) {
            throw new BusinessException("400", "供应商不在白名单内");
        }
        if (!hasText(dto.getModel())) {
            throw new BusinessException("400", "模型不能为空");
        }
        Integer sourceVersion = dto.getSourceVersion();
        if (sourceVersion != null && sourceVersion <= 0) {
            throw new BusinessException("400", "回填来源版本非法");
        }
        // 带来源版本即视为回滚留痕；不带则是一次手工修改
        String changeKind = sourceVersion == null ? "manual" : "rollback";

        AgentRuntimeConfig config = current();
        int nextVersion = (config.getVersion() == null ? 0 : config.getVersion()) + 1;
        config.setProvider(dto.getProvider());
        config.setModel(dto.getModel().trim());
        config.setEndpoint(trimToNull(dto.getEndpoint()));
        config.setDataPolicyAck(Boolean.TRUE.equals(dto.getDataPolicyAck()));
        config.setVersion(nextVersion);
        config.setUpdatedBy(adminName);
        save(config);
        configMapper.insertHistory(UUID.randomUUID().toString().replace("-", ""), CONFIG_ID, nextVersion,
                config.getProvider(), config.getModel(), config.getEndpoint(), config.getDataPolicyAck(),
                changeKind, sourceVersion, adminName);
        return getHealth();
    }

    /**
     * 配置版本历史（倒序，最多 20 条）。只返回配置值，不含密钥。
     */
    public List<AgentRuntimeConfigHistory> listHistory() {
        return configMapper.selectHistory(CONFIG_ID);
    }

    /**
     * 轮换模型 key（只写不可读）。
     */
    public AgentRuntimeHealthVO rotateKey(String key, String adminName) {
        if (!hasText(key)) {
            throw new BusinessException("400", "模型 key 不能为空");
        }
        keyStore().write(KEY_SLOT, key);
        return getHealth();
    }

    /**
     * 独立吊销模型 key。
     */
    public AgentRuntimeHealthVO revokeKey() {
        keyStore().delete(KEY_SLOT);
        return getHealth();
    }

    /**
     * 连接测试：只读取配置级就绪度并对 endpoint 发一次探测；失败不抛异常，返回结果。
     */
    public AgentRuntimeHealthVO.Health testConnectivity() {
        AgentRuntimeConfig config = current();
        if (!hasText(config.getEndpoint()) || !keyStore().exists(KEY_SLOT)) {
            return new AgentRuntimeHealthVO.Health("模型", false, "缺少 endpoint 或模型 key");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(keyStore().read(KEY_SLOT));
            ResponseEntity<String> response = restTemplate.exchange(
                    config.getEndpoint(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
            return new AgentRuntimeHealthVO.Health("模型", response.getStatusCode().is2xxSuccessful(),
                    "HTTP " + response.getStatusCode().value());
        } catch (Exception e) {
            return new AgentRuntimeHealthVO.Health("模型", false, "连接失败：" + e.getClass().getSimpleName());
        }
    }

    private AgentRuntimeConfig current() {
        AgentRuntimeConfig config = configMapper.selectById(CONFIG_ID);
        if (config == null) {
            config = new AgentRuntimeConfig();
            config.setConfigId(CONFIG_ID);
            config.setVersion(0);
            config.setDataPolicyAck(false);
        }
        return config;
    }

    private void save(AgentRuntimeConfig config) {
        config.setUpdatedAt(java.time.LocalDateTime.now());
        if (configMapper.selectById(CONFIG_ID) == null) {
            configMapper.insert(config);
        } else {
            configMapper.updateById(config);
        }
    }

    private FileSecretStore keyStore() {
        return FileSecretStore.directory(keyDir);
    }

    private static boolean pathExists(String path) {
        return hasText(path) && Files.isDirectory(Path.of(path));
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
