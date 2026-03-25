package com.xx.jaseatschoicejava.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;

/**
 * 邮件服务配置
 */
@Configuration
@EnableConfigurationProperties(EmailProperties.class) // 启用配置属性
public class EmailConfiguration {

    @Resource
    private EmailProperties emailProperties;
}
