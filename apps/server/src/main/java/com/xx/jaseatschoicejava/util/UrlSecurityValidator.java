package com.xx.jaseatschoicejava.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * 内容提取链接的安全校验器。
 *
 * <p>只做校验，不负责抓取内容：非空、长度上限、必须是 http/https 域名、解析出的地址
 * 不能落在内网/环回/链路本地/保留网段；对短链会跟随重定向并再次校验最终地址，防止用短链绕过限制。</p>
 *
 * @since 2026-11-03
 */
@Component
public class UrlSecurityValidator {

    private static final Logger log = LoggerFactory.getLogger(UrlSecurityValidator.class);

    /** 与 ext_content_source.content_url 字段长度保持一致。 */
    static final int MAX_URL_LENGTH = 500;
    private static final int MAX_REDIRECTS = 3;
    private static final int PROBE_TIMEOUT_MS = 3000;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
        + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /**
     * 校验链接格式与安全性，并跟随短链重定向再次校验最终地址。
     *
     * @param rawUrl 用户提交的原始链接
     * @return 规范化（去掉首尾空白）后的原始链接，短链不会被改写成最终地址
     * @throws InvalidUrlException 链接为空、过长、格式非法，或指向受限地址
     */
    public String validateAndResolve(String rawUrl) {
        String normalized = validate(rawUrl);
        resolveFinalUrl(normalized);
        return normalized;
    }

    /**
     * 校验链接格式（非空、长度、协议、域名）与目标地址是否可公开访问。
     *
     * @param rawUrl 用户提交的原始链接
     * @return 规范化（去掉首尾空白）后的链接
     * @throws InvalidUrlException 链接为空、过长、格式非法，或指向受限地址
     */
    public String validate(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new InvalidUrlException("内容链接不能为空");
        }

        String trimmed = rawUrl.trim();
        if (trimmed.length() > MAX_URL_LENGTH) {
            throw new InvalidUrlException("内容链接过长，最长 " + MAX_URL_LENGTH + " 个字符");
        }

        URI uri = parseHttpUri(trimmed);
        assertHostAllowed(uri.getHost());
        return trimmed;
    }

    /**
     * 跟随重定向并逐跳校验目标地址，返回最终地址。
     *
     * <p>网络探测失败时返回当前地址：此时协议、域名与首个地址的内网校验已经通过，
     * 是否可抓取由抓取阶段决定，避免因探测失败误伤正常链接。</p>
     *
     * @param url 已经过 {@link #validate(String)} 的链接
     * @return 重定向后的最终地址；未发生重定向时返回入参
     * @throws InvalidUrlException 重定向链路中出现受限地址或重定向次数过多
     */
    public String resolveFinalUrl(String url) {
        String currentUrl = url;

        for (int hop = 0; hop <= MAX_REDIRECTS; hop++) {
            URI current = parseHttpUri(currentUrl);
            assertHostAllowed(current.getHost());

            try {
                HttpURLConnection connection = openConnection(current.toURL());
                int status = connection.getResponseCode();
                if (status >= 300 && status < 400) {
                    String location = connection.getHeaderField("Location");
                    connection.disconnect();
                    if (location == null || location.isBlank()) {
                        throw new InvalidUrlException("内容链接格式不正确：重定向地址缺失");
                    }
                    currentUrl = current.resolve(location).toString();
                    continue;
                }
                connection.disconnect();
                return currentUrl;
            } catch (InvalidUrlException e) {
                throw e;
            } catch (IOException | RuntimeException e) {
                log.warn("短链探测失败，跳过重定向校验: url={}, cause={}", currentUrl, e.getMessage());
                return currentUrl;
            }
        }

        throw new InvalidUrlException("内容链接格式不正确：重定向次数过多");
    }

    /**
     * 打开用于重定向探测的连接；禁止自动跳转，便于逐跳校验。测试可覆写此方法注入替身。
     */
    HttpURLConnection openConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(PROBE_TIMEOUT_MS);
        connection.setReadTimeout(PROBE_TIMEOUT_MS);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        return connection;
    }

    private URI parseHttpUri(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("内容链接格式不正确");
        }

        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new InvalidUrlException("内容链接格式不正确，仅支持 http/https 链接");
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new InvalidUrlException("内容链接格式不正确，缺少有效域名");
        }
        return uri;
    }

    /**
     * 只有域名解析出的全部地址都不是内网/环回/链路本地/保留地址时才放行。
     */
    private void assertHostAllowed(String host) {
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            throw new InvalidUrlException("内容链接域名无法解析");
        }
        if (addresses.length == 0) {
            throw new InvalidUrlException("内容链接域名无法解析");
        }

        for (InetAddress address : addresses) {
            if (isBlockedAddress(address)) {
                throw new InvalidUrlException("不允许访问内网地址");
            }
        }
    }

    /**
     * 判断地址是否属于内网、环回、链路本地、组播或保留网段。
     */
    static boolean isBlockedAddress(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
            || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return true;
        }

        byte[] bytes = address.getAddress();
        if (bytes.length == 4) {
            int first = bytes[0] & 0xFF;
            int second = bytes[1] & 0xFF;
            int third = bytes[2] & 0xFF;
            if (first == 0) {
                return true; // 0.0.0.0/8
            }
            if (first == 100 && second >= 64 && second <= 127) {
                return true; // 100.64.0.0/10 运营商级 NAT
            }
            if (first == 192 && second == 0 && third == 0) {
                return true; // 192.0.0.0/24
            }
            if (first == 192 && second == 0 && third == 2) {
                return true; // 192.0.2.0/24 TEST-NET-1
            }
            if (first == 198 && (second == 18 || second == 19)) {
                return true; // 198.18.0.0/15 基准测试
            }
            if (first == 198 && second == 51 && third == 100) {
                return true; // 198.51.100.0/24 TEST-NET-2
            }
            if (first == 203 && second == 0 && third == 113) {
                return true; // 203.0.113.0/24 TEST-NET-3
            }
            return first >= 240; // 240.0.0.0/4 保留
        }

        if (bytes.length == 16) {
            return (bytes[0] & 0xFE) == 0xFC; // fc00::/7 唯一本地地址
        }
        return false;
    }

    /**
     * 链接不合法：为空、过长、格式错误或指向受限地址。
     */
    public static class InvalidUrlException extends RuntimeException {
        public InvalidUrlException(String message) {
            super(message);
        }
    }
}
