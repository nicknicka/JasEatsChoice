package com.xx.jaseatschoicejava.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 邮件服务配置参数
 */
@Data
@ConfigurationProperties(prefix = "email")
public class EmailProperties {
    /**
     * SMTP服务器地址（必填，如：smtp.qq.com）
     */
    private String host;

    /**
     * SMTP端口（必填，如：465或587）
     */
    private Integer port;

    /**
     * 发件人邮箱账号（必填）
     */
    private String username;

    /**
     * 邮箱密码或授权码（必填）
     */
    private String password;

    /**
     * 是否启用SSL（默认true）
     */
    private Boolean sslEnable = true;

    /**
     * 是否启用STARTTLS（默认false）
     */
    private Boolean starttlsEnable = false;

    /**
     * 获取SSL启用状态（兼容isSslEnable方法名）
     */
    public boolean isSslEnable() {
        return sslEnable != null && sslEnable;
    }

    /**
     * 获取STARTTLS启用状态（兼容isStarttlsEnable方法名）
     */
    public boolean isStarttlsEnable() {
        return starttlsEnable != null && starttlsEnable;
    }
}
