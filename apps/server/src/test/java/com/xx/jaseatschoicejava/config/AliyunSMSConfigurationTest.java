package com.xx.jaseatschoicejava.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * 生产短信配置的 fail fast 语义：关闭 mock 后，endpoint / signName / templateCode
 * 任一缺失（含空串）都必须拒绝启动，而不是静默降级成打日志。
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
    void missingEndpointFailsFast() {
        var config = configuration(properties(null, "签名", "SMS_1"));
        assertThrows(IllegalArgumentException.class, config::aliyunSmsClient);
    }

    @Test
    void blankValuesFailFast() {
        var config = configuration(properties("dypnsapi.aliyuncs.com", "   ", "SMS_1"));
        assertThrows(IllegalArgumentException.class, config::aliyunSmsClient);
    }

    @Test
    void completeValuesCreateClient() throws Exception {
        var config = configuration(
                properties("dypnsapi.aliyuncs.com", "速通互联验证码", "SMS_1"));
        assertNotNull(config.aliyunSmsClient());
    }
}
