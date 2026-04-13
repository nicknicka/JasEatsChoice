package com.xx.jaseatschoicejava.config;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private FileUploadConfig fileUploadConfig;

    /**
     * 配置HTTP消息转换器，强制使用UTF-8编码
     */
    @Override
    public void configureMessageConverters(@NonNull List<HttpMessageConverter<?>> converters) {
        // 添加字符串转换器，明确指定UTF-8编码
        final java.nio.charset.Charset utf8Charset = Objects.requireNonNull(StandardCharsets.UTF_8);
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(utf8Charset);
        stringConverter.setWriteAcceptCharset(false); // 避免在响应头中添加charset
        converters.add(0, stringConverter);

        // 配置JSON转换器使用UTF-8
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jsonConverter) {
                jsonConverter.setDefaultCharset(StandardCharsets.UTF_8);
            }
        }
    }

    /**
     * 配置Jackson ObjectMapper，确保JSON使用UTF-8编码
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // 禁止未知属性报错
            builder.failOnUnknownProperties(false);
            // 禁止空Bean报错
            builder.failOnEmptyBeans(false);
            // 禁止将日期写为时间戳
            builder.featuresToDisable(
                    com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
            );
            // 其他配置通过application.yml完成
        };
    }

    /**
     * 配置静态资源访问路径
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 获取上传目录的绝对路径
        String uploadPath = fileUploadConfig.getUploadPath();
        File uploadDir = new File(uploadPath);
        String absolutePath = uploadDir.getAbsolutePath();

        // 确保路径以文件分隔符结尾
        if (!absolutePath.endsWith(File.separator)) {
            absolutePath += File.separator;
        }

        // 打印调试信息
        System.out.println("配置静态资源映射：");
        System.out.println("  - uploadPath: " + uploadPath);
        System.out.println("  - absolutePath: " + absolutePath);
        System.out.println("  - url pattern: /uploads/**");
        System.out.println("  - resource location: file:" + absolutePath);

        // 配置上传文件访问路径
        // 注意：由于 context-path 是 /api，所以这里配置 /uploads/** 后，实际访问路径是 /api/uploads/**
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath)
                .setCachePeriod(0); // 禁用缓存，便于调试

        // 同时配置不带 /api 前缀的映射（用于兼容）
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:" + absolutePath)
                .setCachePeriod(0);

        // 配置通配符文件访问映射（用于兼容旧的直接文件名访问方式）
        // 例如：/api/abc123.png 会映射到 uploads 目录下的 abc123.png
        // 如果文件不存在，会自动尝试在 chat 子目录下查找
        registry.addResourceHandler("/*.png", "/*.jpg", "/*.jpeg", "/*.gif", "/*.webp", "/*.pdf", "/*.doc", "/*.docx")
                .addResourceLocations("file:" + absolutePath)
                .setCachePeriod(0);

        // 配置 /files/** 路径映射（新的推荐访问方式）
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + absolutePath)
                .setCachePeriod(3600); // 缓存1小时
    }

    /**
     * 延长异步请求默认超时，避免 SSE 流式响应被 Spring MVC 默认回收。
     */
    @Override
    public void configureAsyncSupport(@NonNull AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(300000L);
    }
}
