package com.xx.jaseatschoicejava.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * 内容提取相关配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "content-extraction")
public class ContentExtractionConfig {

    private FetchConfig fetch = new FetchConfig();
    private RetryConfig retry = new RetryConfig();
    private AiConfig ai = new AiConfig();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FetchConfig {
        /** HTTP请求User-Agent */
        private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        /** 连接超时（毫秒） */
        private int connectTimeout = 30000;
        /** 读取超时（毫秒） */
        private int readTimeout = 60000;
        /** 每次最多下载图片数 */
        private int maxImages = 5;
        /** 单张图片最大字节数 */
        private long maxImageSize = 2 * 1024 * 1024; // 2MB
        /** 视频最大字节数 */
        private long maxVideoSize = 50 * 1024 * 1024; // 50MB
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetryConfig {
        /** 最大重试次数 */
        private int maxCount = 3;
        /** 重试间隔（毫秒） */
        private long delayMs = 5000;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiConfig {
        /** 视频理解API超时（毫秒） */
        private int videoApiTimeout = 120000;
    }

    /**
     * 视频API专用RestTemplate（超时更长）
     */
    @Bean("videoApiRestTemplate")
    public RestTemplate videoApiRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(30));
        factory.setReadTimeout(Duration.ofMillis(ai.getVideoApiTimeout()));
        return new RestTemplate(factory);
    }
}
