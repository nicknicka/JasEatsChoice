package com.xx.jaseatschoicejava.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 智谱AI配置类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "zhipuai")
public class ZhipuAIConfig {

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 使用的模型（默认：glm-4.7-flash）
     * glm-4.7-flash: 最新免费模型，支持深度思考
     * glm-4-plus: 增强版，能力更强
     * glm-4: 标准版
     */
    private String model = "glm-4.7-flash";

    /**
     * API基础URL
     */
    private String baseUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 30000;
}
