package com.xx.jaseatschoicejava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.jaseatschoicejava.entity.SystemConfig;
import com.xx.jaseatschoicejava.mapper.SystemConfigMapper;
import com.xx.jaseatschoicejava.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统配置Service实现
 */
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    private static final Logger log = LoggerFactory.getLogger(SystemConfigServiceImpl.class);

    // 本地缓存
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    @Override
    public String getConfigValue(String configKey) {
        // 先从缓存获取
        String value = configCache.get(configKey);
        if (value != null) {
            return value;
        }

        // 从数据库获取
        SystemConfig config = this.getOne(
            new QueryWrapper<SystemConfig>()
                .eq("config_key", configKey)
                .eq("status", "active")
        );

        if (config != null) {
            configCache.put(configKey, config.getConfigValue());
            return config.getConfigValue();
        }

        return null;
    }

    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }

    @Override
    public SystemConfig getConfigByKey(String configKey) {
        return this.getOne(
            new QueryWrapper<SystemConfig>()
                .eq("config_key", configKey)
        );
    }

    @Override
    public Map<String, String> getConfigsByGroup(String configGroup) {
        List<SystemConfig> configs = this.list(
            new QueryWrapper<SystemConfig>()
                .eq("config_group", configGroup)
                .eq("status", "active")
        );

        Map<String, String> result = new HashMap<>();
        for (SystemConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
            // 更新缓存
            configCache.put(config.getConfigKey(), config.getConfigValue());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setConfigValue(String configKey, String configValue, String configName, String configGroup) {
        SystemConfig existingConfig = getConfigByKey(configKey);

        if (existingConfig != null) {
            // 更新现有配置
            existingConfig.setConfigValue(configValue);
            boolean success = this.updateById(existingConfig);
            if (success) {
                configCache.put(configKey, configValue);
                log.info("更新系统配置: {} = {}", configKey, configValue);
            }
            return success;
        } else {
            // 创建新配置
            SystemConfig newConfig = new SystemConfig();
            newConfig.setConfigKey(configKey);
            newConfig.setConfigValue(configValue);
            newConfig.setConfigName(configName);
            newConfig.setConfigGroup(configGroup);
            newConfig.setConfigType("string");
            newConfig.setIsSystem(false);
            newConfig.setStatus("active");

            boolean success = this.save(newConfig);
            if (success) {
                configCache.put(configKey, configValue);
                log.info("创建系统配置: {} = {}", configKey, configValue);
            }
            return success;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfig(String configKey, String configValue) {
        SystemConfig config = getConfigByKey(configKey);
        if (config == null) {
            log.warn("配置不存在，无法更新: {}", configKey);
            return false;
        }

        config.setConfigValue(configValue);
        boolean success = this.updateById(config);
        if (success) {
            configCache.put(configKey, configValue);
            log.info("更新系统配置: {} = {}", configKey, configValue);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConfig(String configKey) {
        SystemConfig config = getConfigByKey(configKey);
        if (config == null) {
            log.warn("配置不存在，无法删除: {}", configKey);
            return false;
        }

        // 系统内置配置不允许删除
        if (Boolean.TRUE.equals(config.getIsSystem())) {
            log.warn("系统内置配置不允许删除: {}", configKey);
            return false;
        }

        boolean success = this.removeById(config.getId());
        if (success) {
            configCache.remove(configKey);
            log.info("删除系统配置: {}", configKey);
        }

        return success;
    }

    @Override
    public void refreshConfigCache() {
        configCache.clear();

        List<SystemConfig> configs = this.list(
            new QueryWrapper<SystemConfig>()
                .eq("status", "active")
        );

        for (SystemConfig config : configs) {
            configCache.put(config.getConfigKey(), config.getConfigValue());
        }

        log.info("刷新系统配置缓存，共加载 {} 个配置", configCache.size());
    }
}
