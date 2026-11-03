package com.xx.jaseatschoicejava.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


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
    }

}
