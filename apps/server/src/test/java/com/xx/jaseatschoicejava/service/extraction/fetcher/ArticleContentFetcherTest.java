package com.xx.jaseatschoicejava.service.extraction.fetcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.util.ReflectionTestUtils;

import com.xx.jaseatschoicejava.config.ContentExtractionConfig;
import com.xx.jaseatschoicejava.config.XhsSessionStore;
import com.xx.jaseatschoicejava.service.extraction.dto.FetchedContent;

/**
 * 小红书登录态 Cookie 的作用域与优先级：用户级优先，且只能发给小红书自己的域名。
 */
class ArticleContentFetcherTest {

    private static final String GLOBAL_COOKIE = "a1=global; web_session=global";
    private static final String USER_COOKIE = "a1=user; web_session=user";
    private static final String USER_ID = "user-1";

    @Test
    void resolveCookieHeader_whenXiaohongshuHost_shouldReturnCookie() throws Exception {
        assertEquals(GLOBAL_COOKIE, resolveFor("https://www.xiaohongshu.com/explore/123"));
        assertEquals(GLOBAL_COOKIE, resolveFor("https://www.xiaohongshu.com/discovery/item/123"));
    }

    @Test
    void resolveCookieHeader_whenShortLinkHost_shouldReturnCookie() throws Exception {
        assertEquals(GLOBAL_COOKIE, resolveFor("https://xhslink.com/a/abcd"));
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
    void resolveCookieHeader_whenUserSessionPresent_shouldPreferUserOverGlobal() throws Exception {
        XhsSessionStore store = mock(XhsSessionStore.class);
        when(store.read(USER_ID)).thenReturn(USER_COOKIE);

        assertEquals(USER_COOKIE,
            resolveFor("https://www.xiaohongshu.com/explore/123", GLOBAL_COOKIE, USER_ID, store));
    }

    @Test
    void resolveCookieHeader_whenUserSessionAbsent_shouldFallBackToGlobal() throws Exception {
        XhsSessionStore store = mock(XhsSessionStore.class);
        when(store.read(USER_ID)).thenReturn("");

        assertEquals(GLOBAL_COOKIE,
            resolveFor("https://www.xiaohongshu.com/explore/123", GLOBAL_COOKIE, USER_ID, store));
    }

    @Test
    void resolveCookieHeader_whenUserSessionOnOtherHost_shouldNotLeak() throws Exception {
        XhsSessionStore store = mock(XhsSessionStore.class);
        when(store.read(USER_ID)).thenReturn(USER_COOKIE);

        assertEquals("", resolveFor("https://example.com/xiaohongshu.com.png", GLOBAL_COOKIE, USER_ID, store));
    }

    @Test
    void fetch_whenNoLoginState_shouldAskToBind() throws Exception {
        FetchedContent result = fetchMissingXiaohongshuContent("");

        assertTrue(result.isAuthRequired());
        assertEquals("小红书正文需要登录态：请绑定小红书账号后重试", result.getErrorMessage());
    }

    @Test
    void fetch_whenGlobalCookiePresentButNoContent_shouldAskToRebind() throws Exception {
        FetchedContent result = fetchMissingXiaohongshuContent(GLOBAL_COOKIE);

        assertTrue(result.isAuthRequired());
        assertEquals("小红书未返回正文，登录态可能已失效，请重新绑定后重试", result.getErrorMessage());
    }

    @Test
    void fetch_whenUserSessionPresentButNoContent_shouldAskToRebind() throws Exception {
        XhsSessionStore store = mock(XhsSessionStore.class);
        when(store.read(USER_ID)).thenReturn(USER_COOKIE);

        FetchedContent result = fetchMissingXiaohongshuContent("", store);

        assertTrue(result.isAuthRequired());
        assertEquals("小红书未返回正文，登录态可能已失效，请重新绑定后重试", result.getErrorMessage());
    }

    @Test
    void fetch_whenLoginWallWithoutUserSession_shouldAskToBind() throws Exception {
        FetchedContent result = fetchOnLoginWall("", null);

        assertTrue(result.isAuthRequired());
        assertEquals("小红书正文需要登录态：请绑定小红书账号后重试", result.getErrorMessage());
    }

    @Test
    void fetch_whenLoginWallWithUserSession_shouldMarkExpiredAndAskToRebind() throws Exception {
        XhsSessionStore store = mock(XhsSessionStore.class);
        when(store.read(USER_ID)).thenReturn(USER_COOKIE);

        FetchedContent result = fetchOnLoginWall("", store);

        assertTrue(result.isAuthRequired());
        assertEquals("小红书登录态已失效，请重新绑定后重试", result.getErrorMessage());
        verify(store).markExpired(USER_ID);
    }

    @Test
    void fetch_whenLoginWallWithGlobalCookieOnly_shouldNotMarkExpired() throws Exception {
        XhsSessionStore store = mock(XhsSessionStore.class);
        when(store.read(USER_ID)).thenReturn("");

        FetchedContent result = fetchOnLoginWall(GLOBAL_COOKIE, store);

        assertTrue(result.isAuthRequired());
        assertEquals("小红书正文需要登录态：请绑定小红书账号后重试", result.getErrorMessage());
        verify(store, never()).markExpired(anyString());
    }

    /**
     * 模拟未登录/登录态失效时小红书 302 到 /login 的场景：
     * 登录页的最终 URL 命中 /login，必须显式判定为登录墙（authRequired=true），而不是“内容有问题”。
     */
    private FetchedContent fetchOnLoginWall(String cookie, XhsSessionStore store) throws Exception {
        ArticleContentFetcher fetcher = newFetcher();
        ReflectionTestUtils.setField(fetcher, "xhsCookie", cookie);
        ReflectionTestUtils.setField(fetcher, "xhsSessionStore", store);

        String url = "https://www.xiaohongshu.com/explore/123";
        Document document = mock(Document.class);
        when(document.location())
            .thenReturn("https://www.xiaohongshu.com/login?redirectPath=%2Fexplore%2F123");

        Connection connection = mock(Connection.class);
        when(connection.userAgent(anyString())).thenReturn(connection);
        when(connection.timeout(anyInt())).thenReturn(connection);
        when(connection.followRedirects(true)).thenReturn(connection);
        when(connection.get()).thenReturn(document);

        try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class)) {
            jsoup.when(() -> Jsoup.connect(url)).thenReturn(connection);
            return fetcher.fetch(url, USER_ID);
        }
    }

    @Test
    void fetch_whenXiaohongshuFetchFailsWithUserSession_shouldMarkAuthRequired() throws Exception {
        XhsSessionStore store = mock(XhsSessionStore.class);
        when(store.read(USER_ID)).thenReturn(USER_COOKIE);

        FetchedContent result = fetchThrowing(store);

        assertTrue(result.isAuthRequired());
        assertEquals("小红书抓取失败，登录态可能已失效，请重新绑定后重试", result.getErrorMessage());
        // 5xx 可能是平台瞬时故障，不据此标记失效
        verify(store, never()).markExpired(anyString());
    }

    @Test
    void fetch_whenXiaohongshuFetchFailsWithoutUserSession_shouldKeepFetchFailure() throws Exception {
        XhsSessionStore store = mock(XhsSessionStore.class);
        when(store.read(USER_ID)).thenReturn("");

        FetchedContent result = fetchThrowing(store);

        assertFalse(result.isAuthRequired());
        assertTrue(result.getErrorMessage().startsWith("抓取失败:"));
        verify(store, never()).markExpired(anyString());
    }

    private FetchedContent fetchThrowing(XhsSessionStore store) throws Exception {
        ArticleContentFetcher fetcher = newFetcher();
        ReflectionTestUtils.setField(fetcher, "xhsCookie", "");
        ReflectionTestUtils.setField(fetcher, "xhsSessionStore", store);

        String url = "https://www.xiaohongshu.com/explore/123";
        Connection connection = mock(Connection.class);
        when(connection.userAgent(anyString())).thenReturn(connection);
        when(connection.timeout(anyInt())).thenReturn(connection);
        when(connection.followRedirects(true)).thenReturn(connection);
        when(connection.get()).thenThrow(new RuntimeException("HTTP error fetching URL. Status=500"));

        try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class)) {
            jsoup.when(() -> Jsoup.connect(url)).thenReturn(connection);
            return fetcher.fetch(url, USER_ID);
        }
    }

    private FetchedContent fetchMissingXiaohongshuContent(String cookie) throws Exception {
        return fetchMissingXiaohongshuContent(cookie, null);
    }

    private FetchedContent fetchMissingXiaohongshuContent(String cookie, XhsSessionStore store) throws Exception {
        ArticleContentFetcher fetcher = newFetcher();
        ReflectionTestUtils.setField(fetcher, "xhsCookie", cookie);
        ReflectionTestUtils.setField(fetcher, "xhsSessionStore", store);

        String url = "https://www.xiaohongshu.com/explore/123";
        Document document = mock(Document.class);
        Connection connection = mock(Connection.class);
        when(connection.userAgent(anyString())).thenReturn(connection);
        when(connection.timeout(anyInt())).thenReturn(connection);
        when(connection.followRedirects(true)).thenReturn(connection);
        when(connection.get()).thenReturn(document);

        try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class)) {
            jsoup.when(() -> Jsoup.connect(url)).thenReturn(connection);
            return fetcher.fetch(url, USER_ID);
        }
    }

    private static ArticleContentFetcher newFetcher() {
        ArticleContentFetcher fetcher = new ArticleContentFetcher();
        ReflectionTestUtils.setField(fetcher, "config", new ContentExtractionConfig());
        return fetcher;
    }

    private String resolveFor(String url) throws Exception {
        return resolveFor(url, GLOBAL_COOKIE);
    }

    private String resolveFor(String url, String cookie) throws Exception {
        return resolveFor(url, cookie, USER_ID, null);
    }

    private String resolveFor(String url, String cookie, String userId, XhsSessionStore store) throws Exception {
        ArticleContentFetcher fetcher = newFetcher();
        ReflectionTestUtils.setField(fetcher, "xhsCookie", cookie);
        ReflectionTestUtils.setField(fetcher, "xhsSessionStore", store);

        Method method = ArticleContentFetcher.class.getDeclaredMethod(
            "resolveCookieHeader", String.class, String.class);
        method.setAccessible(true);
        try {
            return (String) method.invoke(fetcher, url, userId);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }
}
