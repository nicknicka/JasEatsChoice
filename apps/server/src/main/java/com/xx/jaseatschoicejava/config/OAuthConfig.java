package com.xx.jaseatschoicejava.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * OAuth 第三方登录配置
 * 支持模拟模式（毕设演示）和真实平台模式
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "oauth")
public class OAuthConfig {

    /** 是否启用模拟模式（true=模拟授权服务器，false=真实平台） */
    private boolean mockMode = true;

    private WechatConfig wechat = new WechatConfig();
    private QqConfig qq = new QqConfig();

    @Data
    public static class WechatConfig {
        private String appId = "mock_app_id";
        private String appSecret = "mock_secret";
        private String authUrl = "http://localhost:7777/api/mock/oauth/wechat/authorize";
        private String tokenUrl = "http://localhost:7777/api/mock/oauth/wechat/token";
        private String userInfoUrl = "http://localhost:7777/api/mock/oauth/wechat/userinfo";
        private String redirectUri = "http://localhost:7777/api/v1/oauth/mock-callback";
    }

    @Data
    public static class QqConfig {
        private String appId = "mock_app_id";
        private String appSecret = "mock_secret";
        private String authUrl = "http://localhost:7777/api/mock/oauth/qq/authorize";
        private String tokenUrl = "http://localhost:7777/api/mock/oauth/qq/token";
        private String userInfoUrl = "http://localhost:7777/api/mock/oauth/qq/userinfo";
        private String redirectUri = "http://localhost:7777/api/v1/oauth/mock-callback";
    }
}
