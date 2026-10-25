package com.xx.jaseatschoicejava.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云短信认证配置参数
 */
@Data
@ConfigurationProperties(prefix = "aliyun.sms") // 绑定yml前缀
public class AliyunSMSProperties {
    /**
     * 接入点地址（必填，从阿里云文档获取，如：dypnsapi.aliyuncs.com）
     */
    private String endpoint;

    /**
     * 短信签名（必填，系统赠送的签名，从阿里云控制台获取）
     */
    private String signName;

    /**
     * 模板CODE（必填，系统赠送的验证码模板，从控制台获取）
     */
    private String templateCode;

    /**
     * 验证码有效期（默认5分钟，可选）
     */
    private Integer codeExpireMinutes = 5;

    /**
     * 连接超时时间（默认3000毫秒，可选）
     */
    private Integer connectTimeout = 3000;

    /**
     * 读取超时时间（默认3000毫秒，可选）
     */
    private Integer readTimeout = 3000;

    /**
     * 模拟模式（默认true，开发环境使用，验证码打印到控制台不真实发送）
     */
    private Boolean mockMode = true;
}
