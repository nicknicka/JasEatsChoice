package com.xx.jaseatschoicejava.service.extraction.fetcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import com.xx.jaseatschoicejava.config.ContentExtractionConfig;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

/**
 * 抓取阶段必须按真实文件头标注图片格式（产出带真实格式的 data URI），
 * 且图片数量/大小上限来自注入的 ContentExtractionConfig，而不是硬编码常量。
 */
class ArticleContentFetcherFormatTest {

    private static final byte[] PNG_BYTES =
        {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] UNKNOWN_BYTES = {0x00, 0x01, 0x02, 0x03};

    @Test
    void downloadImagesAsBase64_marksMimeTypeByFileMagicNotUrlSuffix() throws Exception {
        // 链接没有后缀，只有文件头能说明它是 PNG
        List<String> result = download("https://cdn.example.com/photo", PNG_BYTES, null, 1024);

        assertEquals(1, result.size());
        assertTrue(result.get(0).startsWith("data:image/png;base64,"), result.get(0));
    }

    @Test
    void downloadImagesAsBase64_fallsBackToResponseContentType() throws Exception {
        List<String> result = download("https://cdn.example.com/photo", UNKNOWN_BYTES, "image/webp", 1024);

        assertEquals(1, result.size());
        assertTrue(result.get(0).startsWith("data:image/webp;base64,"), result.get(0));
    }

    @Test
    void downloadImagesAsBase64_defaultsToJpegWhenFormatUnrecognizable() throws Exception {
        List<String> result = download("https://cdn.example.com/no-extension", UNKNOWN_BYTES, null, 1024);

        assertEquals(1, result.size());
        assertTrue(result.get(0).startsWith("data:image/jpeg;base64,"), result.get(0));
    }

    @Test
    void downloadImagesAsBase64_skipsImageOverInjectedMaxImageSize() throws Exception {
        // 注入的上限小于图片字节数，图片必须被跳过（证明用的是配置值，而不是 2MB 硬编码）
        List<String> result = download("https://cdn.example.com/photo", PNG_BYTES, null, PNG_BYTES.length - 1);

        assertTrue(result.isEmpty());
    }

    @Test
    void extractGenericArticle_limitsImagesByInjectedMaxImages() throws Exception {
        ContentExtractionConfig config = new ContentExtractionConfig();
        config.getFetch().setMaxImages(2);

        ArticleContentFetcher fetcher = new ArticleContentFetcher();
        ReflectionTestUtils.setField(fetcher, "config", config);

        Document doc = Jsoup.parse("<html><body><article>"
            + "<img src=\"https://cdn.example.com/1.jpg\">"
            + "<img src=\"https://cdn.example.com/2.jpg\">"
            + "<img src=\"https://cdn.example.com/3.jpg\">"
            + "</article></body></html>");

        Method method = ArticleContentFetcher.class.getDeclaredMethod("extractGenericArticle", Document.class);
        method.setAccessible(true);
        Object article = method.invoke(fetcher, doc);
        Method imageUrls = article.getClass().getDeclaredMethod("imageUrls");
        imageUrls.setAccessible(true);

        assertEquals(2, ((List<?>) imageUrls.invoke(article)).size());
    }

    private List<String> download(String url, byte[] bytes, String contentType, long maxImageSize) throws Exception {
        ContentExtractionConfig config = new ContentExtractionConfig();
        config.getFetch().setConnectTimeout(1000);
        config.getFetch().setReadTimeout(1000);
        config.getFetch().setMaxImageSize(maxImageSize);

        ArticleContentFetcher fetcher = new ArticleContentFetcher();
        ReflectionTestUtils.setField(fetcher, "config", config);

        HttpResponse response = mock(HttpResponse.class);
        when(response.bodyBytes()).thenReturn(bytes);
        when(response.header("Content-Type")).thenReturn(contentType);

        HttpRequest request = mock(HttpRequest.class);
        when(request.header(anyString(), anyString())).thenReturn(request);
        when(request.setConnectionTimeout(anyInt())).thenReturn(request);
        when(request.setReadTimeout(anyInt())).thenReturn(request);
        when(request.execute()).thenReturn(response);

        try (MockedStatic<HttpRequest> mocked = mockStatic(HttpRequest.class)) {
            mocked.when(() -> HttpRequest.get(url)).thenReturn(request);
            Method method = ArticleContentFetcher.class.getDeclaredMethod("downloadImagesAsBase64", List.class);
            method.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> result = (List<String>) method.invoke(fetcher, List.of(url));
            return result;
        }
    }
}
