package com.xx.jaseatschoicejava.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * dsh Management API 客户端配置。
 *
 * <p>base-url 默认指向本机实际端口 3083，与脚本侧 {@code DSH_PANEL_PORT}
 * （{@code scripts/agent-runtime/dsh-ctl.sh}、{@code probe.sh}、{@code verify-isolation.sh}）
 * 的默认值保持一致：改这一侧必须同步另一侧，避免端口再次漂移。
 * 可用环境变量 {@code DSH_MANAGEMENT_BASE_URL} 覆盖。
 */
@ConfigurationProperties(prefix = "agent.dsh")
public class DshProperties {

    /** dsh Management API 基地址，默认本机回环 3083。 */
    private String baseUrl = "http://127.0.0.1:3083";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
