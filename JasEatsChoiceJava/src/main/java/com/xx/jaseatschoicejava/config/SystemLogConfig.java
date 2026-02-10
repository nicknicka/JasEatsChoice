package com.xx.jaseatschoicejava.config;

import com.xx.jaseatschoicejava.service.SystemLogService;
import com.xx.jaseatschoicejava.util.SystemLogHelper;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 系统日志配置类
 * 用于初始化日志相关的组件
 */
@Configuration
public class SystemLogConfig {

    private final SystemLogService systemLogService;

    public SystemLogConfig(SystemLogService systemLogService) {
        this.systemLogService = systemLogService;
    }

    /**
     * 初始化 SystemLogHelper
     */
    @PostConstruct
    public void initSystemLogHelper() {
        SystemLogHelper.setSystemLogService(systemLogService);
    }
}
