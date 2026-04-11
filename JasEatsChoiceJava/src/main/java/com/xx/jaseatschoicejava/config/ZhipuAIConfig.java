package com.xx.jaseatschoicejava.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智谱AI配置类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "zhipuai")
public class ZhipuAIConfig {

    /**
     * API密钥
     */
    private String apiKey;

    /**
    * 使用的模型（默认：glm-4.7）
     * glm-4-flash: 标准Flash版本，免费，速度快，并发限制中等（推荐）
     * glm-4-air: 轻量级版本，免费，更低的并发限制
     * glm-4: 标准版，需要付费，能力强
     * glm-4-plus: 增强版，需要付费，能力最强
     * glm-4.7: 新一代推理模型，适合更强的文本推理任务
     */
    private String model = "glm-4.7";

    /**
     * 视觉识别模型（用于菜品识别、图像理解）
     * glm-4.6v-flash: 支持视觉识别的免费模型（推荐用于菜品识别）
     */
    private String visionModel = "glm-4.6v-flash";

    /**
     * API基础URL
     */
    private String baseUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 30000;

    // 显式声明 getter 方法（确保 Lombok 未生效时也能正常工作）
    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public String getVisionModel() {
        return visionModel;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Integer getTimeout() {
        return timeout;
    }

    // setter 方法
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setVisionModel(String visionModel) {
        this.visionModel = visionModel;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
