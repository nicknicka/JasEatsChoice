package com.xx.jaseatschoicejava.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * 远端图片代理（/v1/ai/fetch-remote-image）的内网地址拦截规则。
 */
class AIControllerImageProxyTest {

    private final AIController controller = new AIController();

    @Test
    void isBlockedAddress_whenPrivateOrReserved_shouldBeTrue() throws Exception {
        assertTrue(isBlocked("127.0.0.1"));
        assertTrue(isBlocked("10.0.0.5"));
        assertTrue(isBlocked("172.16.0.1"));
        assertTrue(isBlocked("192.168.1.1"));
        assertTrue(isBlocked("169.254.169.254")); // 云元数据地址
        assertTrue(isBlocked("0.0.0.0"));
        assertTrue(isBlocked("0.1.2.3"));
        assertTrue(isBlocked("100.64.0.1")); // 运营商级 NAT
        assertTrue(isBlocked("224.0.0.1"));
        assertTrue(isBlocked("::1"));
        assertTrue(isBlocked("fc00::1")); // IPv6 唯一本地地址
        assertTrue(isBlocked("fe80::1"));
    }

    @Test
    void isBlockedAddress_whenPublic_shouldBeFalse() throws Exception {
        assertFalse(isBlocked("8.8.8.8"));
        assertFalse(isBlocked("1.1.1.1"));
        assertFalse(isBlocked("2001:4860:4860::8888"));
    }

    @Test
    void parseHttpUri_whenSchemeNotHttp_shouldThrow() throws Exception {
        IOException error = assertThrows(IOException.class, () -> invoke("parseHttpUri", String.class, "file:///etc/passwd"));
        assertEquals("仅支持 http/https 图片地址", error.getMessage());
    }

    @Test
    void parseHttpUri_whenHttps_shouldPass() throws Exception {
        Object uri = invoke("parseHttpUri", String.class, "https://example.com/a.png");
        assertEquals("example.com", ((java.net.URI) uri).getHost());
    }

    @Test
    void normalizeRemoteImageUrl_whenProtocolRelative_shouldPrependHttps() throws Exception {
        Object normalized = invoke("normalizeRemoteImageUrl", String.class, "//cdn.example.com/a.png");
        assertEquals("https://cdn.example.com/a.png", normalized);
    }

    @Test
    void normalizeRemoteImageUrl_whenUnsupportedScheme_shouldReturnNull() throws Exception {
        assertEquals(null, invoke("normalizeRemoteImageUrl", String.class, "ftp://example.com/a.png"));
    }

    private boolean isBlocked(String host) throws Exception {
        return (boolean) invoke("isBlockedAddress", InetAddress.class, InetAddress.getByName(host));
    }

    private Object invoke(String name, Class<?> parameterType, Object argument) throws Exception {
        Method method = AIController.class.getDeclaredMethod(name, parameterType);
        method.setAccessible(true);
        try {
            return method.invoke(controller, argument);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception cause) {
                throw cause;
            }
            throw e;
        }
    }
}
