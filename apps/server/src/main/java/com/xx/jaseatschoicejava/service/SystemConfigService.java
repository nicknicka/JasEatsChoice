package com.xx.jaseatschoicejava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.jaseatschoicejava.entity.SystemConfig;

import java.util.Map;

/**
 * 系统配置Service接口
 */
public interface SystemConfigService extends IService<SystemConfig> {

    /**
     * 根据配置键获取配置值
     */
    String getConfigValue(String configKey);

    /**
     * 根据配置键获取配置值，如果不存在返回默认值
     */
    String getConfigValue(String configKey, String defaultValue);

    /**
     * 根据配置键获取配置对象
     */
    SystemConfig getConfigByKey(String configKey);

    /**
     * 根据配置分组获取所有配置
     */
    Map<String, String> getConfigsByGroup(String configGroup);

    /**
     * 设置配置值（如果存在则更新，不存在则创建）
     */
    boolean setConfigValue(String configKey, String configValue, String configName, String configGroup);

    /**
     * 更新配置
     */
    boolean updateConfig(String configKey, String configValue);

    /**
     * 删除配置
     */
    boolean deleteConfig(String configKey);

    /**
     * 刷新配置缓存
     */
    void refreshConfigCache();
}
