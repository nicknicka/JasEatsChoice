package com.xx.jaseatschoicejava.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * dsh Management API 客户端配置注册。
 */
@Configuration
@EnableConfigurationProperties(DshProperties.class) // 启用配置属性
public class DshManagementConfig {
}
