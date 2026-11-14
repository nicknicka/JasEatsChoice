package com.xx.jaseatschoicejava.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * 生产短信配置的 fail fast 语义：关闭 mock 后，endpoint / signName / templateCode
 * 任一缺失（含空串）都必须拒绝启动，而不是静默降级成打日志。
 *
 * 反向约束同样重要：mock 模式（开发 / local / 集成测试）下验证码只打日志、不调用
 * client，因此空值不应阻塞启动。否则 compose 注入的 `ALIYUN_SMS_ENDPOINT=` 空串
 * 会覆盖 application.yml 默认值，让整个应用起不来（见 ADR-0017）。
 */
class AliyunSMSConfigurationTest {

    private AliyunSMSConfiguration configuration(AliyunSMSProperties properties) {
        AliyunSMSConfiguration configuration = new AliyunSMSConfiguration();
        try {
            var field = AliyunSMSConfiguration.class.getDeclaredField("smsProperties");
            field.setAccessible(true);
            field.set(configuration, properties);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return configuration;
    }

    private AliyunSMSProperties properties(String endpoint, String signName, String templateCode) {
        AliyunSMSProperties properties = new AliyunSMSProperties();
        properties.setEndpoint(endpoint);
        properties.setSignName(signName);
        properties.setTemplateCode(templateCode);
        return properties;
    }

    @Test
    void missingEndpointFailsFastWhenMockDisabled() {
        var props = properties(null, "签名", "SMS_1");
        props.setMockMode(false);
        var config = configuration(props);
        assertThrows(IllegalArgumentException.class, config::aliyunSmsClient);
    }

    @Test
    void blankValuesFailFastWhenMockDisabled() {
        var props = properties("dypnsapi.aliyuncs.com", "   ", "SMS_1");
        props.setMockMode(false);
        var config = configuration(props);
        assertThrows(IllegalArgumentException.class, config::aliyunSmsClient);
    }

    @Test
    void completeValuesCreateClientWhenMockDisabled() throws Exception {
        var props = properties("dypnsapi.aliyuncs.com", "速通互联验证码", "SMS_1");
        props.setMockMode(false);
        var config = configuration(props);
        assertNotNull(config.aliyunSmsClient());
    }

    @Test
    void mockModeToleratesBlankValuesSoLocalStackStarts() throws Exception {
        // 复现 E2E / local 栈的真实输入：compose 注入的空串覆盖了 application.yml 默认值。
        var props = properties("", "", "");
        props.setMockMode(true);
        var config = configuration(props);
        assertNotNull(config.aliyunSmsClient());
    }

    @Test
    void mockModeIsDefaultWhenUnset() throws Exception {
        var props = properties("", "", "");
        var config = configuration(props);
        assertNotNull(config.aliyunSmsClient());
    }
}
