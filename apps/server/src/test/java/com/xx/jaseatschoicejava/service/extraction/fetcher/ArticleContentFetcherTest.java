package com.xx.jaseatschoicejava.service.extraction.fetcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
