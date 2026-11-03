package com.xx.jaseatschoicejava.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * 内容提取链接校验：协议、主机、长度、内网/环回/链路本地/保留网段，以及短链最终地址。
 */
class UrlSecurityValidatorTest {

    @Test
    void validate_whenBlank_shouldReject() {
        UrlSecurityValidator validator = new UrlSecurityValidator();
        assertEquals("内容链接不能为空", throwsMessage(() -> validator.validate(null)));
        assertEquals("内容链接不能为空", throwsMessage(() -> validator.validate("   ")));
    }

    @Test
    void validate_whenSchemeNotHttp_shouldReject() {
        UrlSecurityValidator validator = new UrlSecurityValidator();
        assertTrue(throwsMessage(() -> validator.validate("ftp://example.com/a")).contains("http/https"));
        assertTrue(throwsMessage(() -> validator.validate("file:///etc/passwd")).contains("http/https"));
    }

    @Test
    void validate_whenHostMissing_shouldReject() {
        UrlSecurityValidator validator = new UrlSecurityValidator();
        assertTrue(throwsMessage(() -> validator.validate("http:///no-host")).contains("缺少有效域名"));
    }

    @Test
    void validate_whenTooLong_shouldRejectButBoundaryPasses() {
        UrlSecurityValidator validator = new UrlSecurityValidator();
        String prefix = "https://8.8.8.8/";
        String atLimit = prefix + "a".repeat(UrlSecurityValidator.MAX_URL_LENGTH - prefix.length());

        assertEquals(atLimit, validator.validate(atLimit));
        assertTrue(throwsMessage(() -> validator.validate(atLimit + "a")).contains("过长"));
    }

    @Test
    void validate_whenPublicLiteral_shouldAccept() {
        UrlSecurityValidator validator = new UrlSecurityValidator();
        assertEquals("https://8.8.8.8/article/1",
            validator.validate("  https://8.8.8.8/article/1  "));
    }

    @Test
    void validate_whenInternalOrReserved_shouldReject() {
        UrlSecurityValidator validator = new UrlSecurityValidator();
        for (String url : new String[] {
            "http://127.0.0.1/admin",
            "http://10.0.0.5/x",
            "http://172.16.0.1/x",
            "http://192.168.1.1/x",
            "http://169.254.169.254/latest/meta-data",
            "http://0.0.0.0/x",
            "http://100.64.0.1/x",
            "http://224.0.0.1/x",
            "http://240.0.0.1/x"
        }) {
            assertEquals("不允许访问内网地址", throwsMessage(() -> validator.validate(url)),
                "应拒绝受限地址: " + url);
        }
    }

    @Test
    void isBlockedAddress_whenPrivateOrReserved_shouldBeTrue() throws Exception {
        assertTrue(isBlocked("127.0.0.1"));
        assertTrue(isBlocked("10.0.0.5"));
        assertTrue(isBlocked("172.16.0.1"));
        assertTrue(isBlocked("192.168.1.1"));
        assertTrue(isBlocked("169.254.169.254"));
        assertTrue(isBlocked("0.0.0.0"));
        assertTrue(isBlocked("0.1.2.3"));
        assertTrue(isBlocked("100.64.0.1"));
        assertTrue(isBlocked("224.0.0.1"));
        assertTrue(isBlocked("240.0.0.1"));
        assertTrue(isBlocked("::1"));
        assertTrue(isBlocked("fc00::1"));
        assertTrue(isBlocked("fe80::1"));
    }

    @Test
    void isBlockedAddress_whenPublic_shouldBeFalse() throws Exception {
        assertFalse(isBlocked("8.8.8.8"));
        assertFalse(isBlocked("1.1.1.1"));
        assertFalse(isBlocked("2001:4860:4860::8888"));
    }

    @Test
    void validateAndResolve_whenNoRedirect_shouldReturnTrimmedOriginal() throws Exception {
        StubValidator validator = new StubValidator();
        validator.stubs.put("https://8.8.8.8/a", response(200, null));

        assertEquals("https://8.8.8.8/a", validator.validateAndResolve("  https://8.8.8.8/a  "));
    }

    @Test
    void resolveFinalUrl_whenRedirectToInternal_shouldReject() throws Exception {
        StubValidator validator = new StubValidator();
        validator.stubs.put("https://8.8.8.8/short", response(302, "http://127.0.0.1/secret"));

        UrlSecurityValidator.InvalidUrlException error = assertThrows(
            UrlSecurityValidator.InvalidUrlException.class,
            () -> validator.resolveFinalUrl("https://8.8.8.8/short"));
        assertEquals("不允许访问内网地址", error.getMessage());
    }

    @Test
    void resolveFinalUrl_whenRedirectToPublic_shouldReturnFinalUrl() throws Exception {
        StubValidator validator = new StubValidator();
        validator.stubs.put("https://8.8.8.8/short", response(302, "https://1.1.1.1/real"));
        validator.stubs.put("https://1.1.1.1/real", response(200, null));

        assertEquals("https://1.1.1.1/real", validator.resolveFinalUrl("https://8.8.8.8/short"));
    }

    @Test
    void resolveFinalUrl_whenProbeFails_shouldNotBlock() throws Exception {
        StubValidator validator = new StubValidator();
        HttpURLConnection failing = mock(HttpURLConnection.class);
        when(failing.getResponseCode()).thenThrow(new IOException("network down"));
        validator.stubs.put("https://8.8.8.8/x", failing);

        assertEquals("https://8.8.8.8/x", validator.resolveFinalUrl("https://8.8.8.8/x"));
    }

    private HttpURLConnection response(int status, String location) throws Exception {
        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getResponseCode()).thenReturn(status);
        if (location != null) {
            when(connection.getHeaderField("Location")).thenReturn(location);
        }
        return connection;
    }

    private boolean isBlocked(String host) throws Exception {
        Method method = UrlSecurityValidator.class.getDeclaredMethod("isBlockedAddress", InetAddress.class);
        method.setAccessible(true);
        try {
            return (boolean) method.invoke(null, InetAddress.getByName(host));
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception cause) {
                throw cause;
            }
            throw e;
        }
    }

    private String throwsMessage(org.junit.jupiter.api.function.Executable executable) {
        UrlSecurityValidator.InvalidUrlException error = assertThrows(
            UrlSecurityValidator.InvalidUrlException.class, executable);
        return error.getMessage();
    }

    /**
     * 用替身连接替换真实网络请求，避免单测依赖外网。
     */
    private static class StubValidator extends UrlSecurityValidator {
        private final Map<String, HttpURLConnection> stubs = new HashMap<>();

        @Override
        HttpURLConnection openConnection(URL url) {
            HttpURLConnection connection = stubs.get(url.toString());
            if (connection == null) {
                throw new IllegalStateException("未预置连接的地址: " + url);
            }
            return connection;
        }
    }
}
