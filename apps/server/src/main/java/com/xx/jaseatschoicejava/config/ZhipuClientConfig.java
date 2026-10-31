package com.xx.jaseatschoicejava.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ai.z.openapi.ZhipuAiClient;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 智谱AI SDK客户端配置类
 * 负责初始化ZhipuAiClient实例
 *

 * @since 2026-03-14
 */
@Slf4j
@Configuration
public class ZhipuClientConfig {

    /**
     * 缺通用 AI_API_KEY 时的占位 key，只用于让 Bean 能构造出来。
     * ZaiConfig 的构造器要求 apiKey.split(".") 恰好得到 2 段，所以必须带一个点，
     * 否则 SDK 会在这里抛 "invalid api Key"，整个上下文又起不来。
     */
    private static final String PLACEHOLDER_API_KEY = "0.DISABLED";

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
        // 缺 key 时不能让 Bean 构造失败：zhipuClient 是饿汉单例，一旦这里抛异常整个
        // Spring 上下文就起不来，新克隆的仓库和 CI 将无法启动后端。改用占位 key，
        // 客户端只在真正发起请求时被智谱拒绝，其余功能照常可用。
        String apiKey = zhipuAIConfig.getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("智谱AI API Key未配置，将使用占位 key 初始化客户端；"
                + "请在 deploy/.env 设置 AI_API_KEY后重启，"
                + "否则所有 AI 接口调用都会返回鉴权失败");
            apiKey = PLACEHOLDER_API_KEY;
        }

        // 创建客户端（使用Builder模式）
        this.zhipuClient = ZhipuAiClient.builder()
                .apiKey(apiKey)
                .build();

        // 记录初始化日志（脱敏API Key）
        String maskedApiKey = maskApiKey(apiKey);
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
                        } catch (ReflectiveOperationException ex) {
                            log.warn("ZhipuAiClient - 无法关闭OkHttp ExecutorService: {}", ex.getMessage());
                        }
                    }
                }
            } catch (ReflectiveOperationException e) {
                log.warn("ZhipuAiClient - 反射关闭OkHttpClient失败: {}", e.getMessage());
            }
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
