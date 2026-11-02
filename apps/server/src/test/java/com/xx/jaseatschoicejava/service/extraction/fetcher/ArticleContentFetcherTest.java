package com.xx.jaseatschoicejava.service.extraction.fetcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mockito.MockedStatic;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.springframework.test.util.ReflectionTestUtils;

import com.xx.jaseatschoicejava.service.extraction.dto.FetchedContent;

/**
 * 小红书登录态 Cookie 的作用域：只能发给小红书自己的域名。
 */
class ArticleContentFetcherTest {

    private static final String COOKIE = "a1=abc; web_session=def";

    @Test
    void resolveCookieHeader_whenXiaohongshuHost_shouldReturnCookie() throws Exception {
        assertEquals(COOKIE, resolveFor("https://www.xiaohongshu.com/explore/123"));
        assertEquals(COOKIE, resolveFor("https://www.xiaohongshu.com/discovery/item/123"));
    }

    @Test
    void resolveCookieHeader_whenShortLinkHost_shouldReturnCookie() throws Exception {
        assertEquals(COOKIE, resolveFor("https://xhslink.com/a/abcd"));
    }

    @Test
    void resolveCookieHeader_whenOtherHost_shouldReturnEmpty() throws Exception {
        assertEquals("", resolveFor("https://example.com/xiaohongshu.com.png"));
        assertEquals("", resolveFor("https://evil.com/?u=xiaohongshu.com"));
        assertEquals("", resolveFor("https://weixin.qq.com/s?__biz=abc"));
    }

    @Test
    void resolveCookieHeader_whenCookieNotConfigured_shouldReturnEmpty() throws Exception {
        assertEquals("", resolveFor("https://www.xiaohongshu.com/explore/123", ""));
        assertEquals("", resolveFor("https://www.xiaohongshu.com/explore/123", null));
    }

    @Test
    void fetch_whenXiaohongshuCookieMissing_shouldMarkAuthRequired() throws Exception {
        FetchedContent result = fetchMissingXiaohongshuContent("");

        assertTrue(result.isAuthRequired());
        assertEquals("小红书正文需要登录态：请在 deploy/.env 配置 XHS_COOKIE 后重试", result.getErrorMessage());
    }

    @Test
    void fetch_whenXiaohongshuCookieMayBeExpired_shouldMarkAuthRequired() throws Exception {
        FetchedContent result = fetchMissingXiaohongshuContent(COOKIE);

        assertTrue(result.isAuthRequired());
        assertEquals("小红书未返回正文，XHS_COOKIE 可能已过期，请重新复制 Cookie 后重试", result.getErrorMessage());
    }

    private FetchedContent fetchMissingXiaohongshuContent(String cookie) throws Exception {
        ArticleContentFetcher fetcher = new ArticleContentFetcher();
        ReflectionTestUtils.setField(fetcher, "xhsCookie", cookie);

        String url = "https://www.xiaohongshu.com/explore/123";
        Document document = mock(Document.class);
        Connection connection = mock(Connection.class);
        when(connection.userAgent(anyString())).thenReturn(connection);
        when(connection.timeout(anyInt())).thenReturn(connection);
        when(connection.followRedirects(true)).thenReturn(connection);
        when(connection.get()).thenReturn(document);

        try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class)) {
            jsoup.when(() -> Jsoup.connect(url)).thenReturn(connection);
            return fetcher.fetch(url);
        }
    }

    private String resolveFor(String url) throws Exception {
        return resolveFor(url, COOKIE);
    }

    private String resolveFor(String url, String cookie) throws Exception {
        ArticleContentFetcher fetcher = new ArticleContentFetcher();
        ReflectionTestUtils.setField(fetcher, "xhsCookie", cookie);

        Method method = ArticleContentFetcher.class.getDeclaredMethod("resolveCookieHeader", String.class);
        method.setAccessible(true);
        try {
            return (String) method.invoke(fetcher, url);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }
}
