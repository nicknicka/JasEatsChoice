package com.xx.jaseatschoicejava;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Boot应用程序主类
 * 启用Spring Boot自动配置、组件扫描和Mapper接口扫描
 * 启用定时任务功能和缓存功能
 */
@SpringBootApplication          // Spring Boot应用程序的主注解，用于标识这是一个Spring Boot应用
@MapperScan("com.xx.jaseatschoicejava.mapper")  // 扫描Mapper接口所在的包，MyBatis会自动扫描该包下的接口并创建代理对象
@EnableScheduling  // 启用Spring的定时任务功能，允许在应用中使用@Scheduled注解来创建定时任务
@EnableCaching    // 启用Spring的缓存功能，允许在方法上使用@Cacheable等注解来实现缓存
public class JasEatsChoiceJavaApplication implements WebMvcConfigurer {  // 实现WebMvcConfigurer接口，用于自定义Spring MVC的配置

    /**
     * 程序入口方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {  // Java程序的入口方法
        SpringApplication.run(JasEatsChoiceJavaApplication.class, args);
    }

    /**
     * 配置静态资源访问路径
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源访问路径
        registry.addResourceHandler("/uploads/**")
                // 映射到本地上传目录
                .addResourceLocations("file:./uploads/");
    }

}
