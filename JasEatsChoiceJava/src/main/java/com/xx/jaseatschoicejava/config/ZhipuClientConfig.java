package com.xx.jaseatschoicejava.config;

import ai.z.openapi.ZhipuAiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

/**
 * 智谱AI SDK客户端配置类
 * 负责初始化ZhipuAiClient实例
 *
 * @author Claude
 * @since 2026-03-14
 */
@Slf4j
@Configuration
public class ZhipuClientConfig {

    @Resource
    private ZhipuAIConfig zhipuAIConfig;

    private ZhipuAiClient zhipuClient;

    /**
     * 创建智谱AI客户端Bean
     *
     * @return ZhipuAiClient实例
     */
    @Bean(destroyMethod = "") // 禁用Spring的默认destroy方法，避免异常
    public ZhipuAiClient zhipuClient() {
        log.info("开始初始化智谱AI客户端...");

        // 验证配置
        if (zhipuAIConfig.getApiKey() == null || zhipuAIConfig.getApiKey().isEmpty()) {
            throw new IllegalStateException("智谱AI API Key未配置，请在application.yml中设置zhipuai.api-key");
        }

        // 创建客户端（使用Builder模式）
        this.zhipuClient = ZhipuAiClient.builder()
                .apiKey(zhipuAIConfig.getApiKey())
                .build();

        // 记录初始化日志（脱敏API Key）
        String maskedApiKey = maskApiKey(zhipuAIConfig.getApiKey());
        log.info("智谱AI客户端初始化成功，API Key: {}****, 模型: {}, 超时: {}ms",
                maskedApiKey,
                zhipuAIConfig.getModel(),
                zhipuAIConfig.getTimeout());

        return this.zhipuClient;
    }

    /**
     * 应用关闭时清理资源
     */
    @PreDestroy
    public void cleanup() {
        log.info("ZhipuAiClient资源清理开始...");

        try {
            if (zhipuClient != null) {
                // 尝试通过反射关闭底层的OkHttpClient
                try {
                    java.lang.reflect.Field clientField = zhipuClient.getClass().getDeclaredField("okHttpClient");
                    clientField.setAccessible(true);
                    Object client = clientField.get(zhipuClient);

                    if (client != null && client.getClass().getName().contains("okhttp3.OkHttpClient")) {
                        // 调用OkHttpClient的shutdown方法
                        try {
                            java.lang.reflect.Method shutdownMethod = client.getClass().getMethod("shutdown");
                            shutdownMethod.invoke(client);
                            log.info("ZhipuAiClient - OkHttpClient已成功关闭");
                        } catch (NoSuchMethodException e) {
                            // 如果没有shutdown方法，尝试使用dispatcher().executorService().shutdown()
                            try {
                                java.lang.reflect.Method dispatcherMethod = client.getClass().getMethod("dispatcher");
                                Object dispatcher = dispatcherMethod.invoke(client);
                                java.lang.reflect.Method executorServiceMethod = dispatcher.getClass().getMethod("executorService");
                                java.util.concurrent.ExecutorService executorService =
                                    (java.util.concurrent.ExecutorService) executorServiceMethod.invoke(dispatcher);
                                executorService.shutdown();
                                log.info("ZhipuAiClient - OkHttp ExecutorService已成功关闭");
                            } catch (Exception ex) {
                                log.warn("ZhipuAiClient - 无法关闭OkHttp ExecutorService: {}", ex.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("ZhipuAiClient - 反射关闭OkHttpClient失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("ZhipuAiClient资源清理失败", e);
        }

        log.info("ZhipuAiClient资源清理完成");
    }

    /**
     * 脱敏API Key（只显示前8位）
     *
     * @param apiKey 完整的API Key
     * @return 脱敏后的API Key
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "***";
        }
        return apiKey.substring(0, 8);
    }
}
