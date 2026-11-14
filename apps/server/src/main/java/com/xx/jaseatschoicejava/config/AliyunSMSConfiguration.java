package com.xx.jaseatschoicejava.config;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

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
        // 仅真实发送模式（mock-mode=false）校验必填参数。mock 模式（开发/local/集成测试）
        // 下验证码只打日志、根本不调用 client，因此空值不应阻塞应用启动——
        // 否则 compose 注入的 `ALIYUN_SMS_ENDPOINT=` 空串（`:-}` 兜底）会覆盖
        // application.yml 的默认值，让整个应用起不来（见 ADR-0017）。
        if (!Boolean.TRUE.equals(smsProperties.getMockMode())) {
            if (!StringUtils.hasText(smsProperties.getEndpoint())
                    || !StringUtils.hasText(smsProperties.getSignName())
                    || !StringUtils.hasText(smsProperties.getTemplateCode())) {
                throw new IllegalArgumentException(
                        "阿里云短信认证：ALIYUN_SMS_ENDPOINT / ALIYUN_SMS_SIGN_NAME / "
                                + "ALIYUN_SMS_TEMPLATE_CODE 不能为空（mock-mode=false 时必须提供真实值）");
            }
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
