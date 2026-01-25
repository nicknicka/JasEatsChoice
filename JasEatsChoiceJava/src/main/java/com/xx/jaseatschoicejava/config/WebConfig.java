package com.xx.jaseatschoicejava.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setWriteAcceptCharset(false); // 避免在响应头中添加charset
        converters.add(0, stringConverter);
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

        // 确保路径以/结尾
        if (!absolutePath.endsWith(File.separator)) {
            absolutePath += File.separator;
        }

        // 配置上传文件访问路径
        // 注意：由于 context-path 是 /api，所以这里配置 /uploads/** 后，实际访问路径是 /api/uploads/**
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath)
                .setCachePeriod(0); // 禁用缓存，便于调试
    }

    /**
     * 配置CORS
     */
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:5174")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
