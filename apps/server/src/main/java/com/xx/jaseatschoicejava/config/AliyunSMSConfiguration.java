package com.xx.jaseatschoicejava.config;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;

/**
 * 阿里云短信客户端配置
 */
@Configuration
@EnableConfigurationProperties(AliyunSMSProperties.class) // 启用配置属性
public class AliyunSMSConfiguration {

    @Resource
    private AliyunSMSProperties smsProperties;

    /**
     * 创建短信客户端（单例，避免重复创建）
     */
    @Bean
    @ConditionalOnMissingBean // 防止重复注入
    public Client aliyunSmsClient() throws Exception {
        // 1. 校验必填参数（避免空指针）
        if (smsProperties.getEndpoint() == null
                || smsProperties.getSignName() == null
                || smsProperties.getTemplateCode() == null) {
            throw new IllegalArgumentException("阿里云短信认证：endpoint、signName、templateCode不能为空！");
        }

        // 2. 初始化配置（自动读取阿里云AK/SK等认证信息）
        // 自动从环境变量或配置文件读取AK/SK：
        // 环境变量: ALIBABA_CLOUD_ACCESS_KEY_ID/ALIBABA_CLOUD_ACCESS_KEY_SECRET
        // 或配置文件: ~/.alibabacloud/credentials
        Config config = new Config()
                .setEndpoint(smsProperties.getEndpoint())
                .setConnectTimeout(smsProperties.getConnectTimeout())
                .setReadTimeout(smsProperties.getReadTimeout());

        // 4. 创建并返回客户端
        return new Client(config);
    }
}
