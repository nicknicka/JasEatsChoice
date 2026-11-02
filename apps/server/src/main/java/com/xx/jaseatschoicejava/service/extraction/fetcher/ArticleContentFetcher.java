package com.xx.jaseatschoicejava.service.extraction.fetcher;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.xx.jaseatschoicejava.enums.ContentPlatform;
import com.xx.jaseatschoicejava.enums.ContentType;
import com.xx.jaseatschoicejava.service.extraction.dto.FetchedContent;
import com.xx.jaseatschoicejava.util.FileUploadUtil;

import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 文章/图文内容抓取策略
 * 使用 Jsoup 抓取网页 HTML，提取正文文字和图片
 * 支持微信公众号、小红书、今日头条等平台
 */
@Slf4j
@Component
public class ArticleContentFetcher implements ContentFetcher {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
        + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int TIMEOUT_MS = 30000;
    private static final int MAX_IMAGES = 5;
    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024; // 2MB

    /**
     * 小红书登录态 Cookie（来自 deploy/.env 的 XHS_COOKIE）。未配置时为空，小红书链接会明确报错。
     */
    @Value("${XHS_COOKIE:}")
    private String xhsCookie;

    @Override
    public boolean canHandle(ContentPlatform platform, ContentType contentType) {
        // 处理所有 ARTICLE 类型，以及 OTHER 平台的兜底
        return contentType == ContentType.ARTICLE
            || platform == ContentPlatform.OTHER;
    }

    @Override
    public FetchedContent fetch(String url) {
        log.info("开始抓取文章内容: {}", url);

        try {
            // 解析重定向后的真实URL（短链接处理）
            String realUrl = resolveRedirect(url);
            ContentPlatform platform = ContentPlatform.parseFromUrl(realUrl);

            // 抓取网页（小红书正文必须带登录态，否则会被重定向到登录页）
            Document doc = newConnection(realUrl, TIMEOUT_MS).get();

            // 提取标题
            String title = extractTitle(doc);

            // 提取作者
            String author = extractAuthor(doc, platform);

            // 提取封面图
            String coverImage = extractCoverImage(doc);
            String localCoverImage = storeCoverImage(coverImage, "content-extraction/article/cover");

            // 按平台提取正文和图片
            ExtractedArticle article = extractArticleContent(doc, platform);
            String missingContentMessage = describeMissingXiaohongshuContent(
                platform, article.textContent(), article.imageUrls());

            // 下载图片转Base64
            List<String> imageBase64List = downloadImagesAsBase64(article.imageUrls);

            return FetchedContent.builder()
                .title(title)
                .author(author)
                .coverImage(localCoverImage)
                .textContent(article.textContent)
                .imageUrls(article.imageUrls)
                .imageBase64List(imageBase64List)
                .contentType(ContentType.ARTICLE)
                .platform(platform)
                .fetchSuccess(true)
                .authRequired(missingContentMessage != null)
                .errorMessage(missingContentMessage)
                .build();

        } catch (IOException | RuntimeException e) {
            log.error("抓取文章内容失败: {}", url, e);
            return FetchedContent.builder()
                .fetchSuccess(false)
                .errorMessage("抓取失败: " + e.getMessage())
                .contentType(ContentType.ARTICLE)
                .build();
        }
    }

    /**
     * 解析重定向获取真实URL
     */
    private String resolveRedirect(String url) {
        try {
            // 对于短链接，尝试跟随重定向
            String lowerUrl = url.toLowerCase();
            if (lowerUrl.contains("t.cn") || lowerUrl.contains("bit.ly")
                || lowerUrl.contains("dwz.cn") || lowerUrl.contains("xhslink.com")) {
                var response = newConnection(url, 10000).execute();
                return response.url().toString();
            }
        } catch (IOException | RuntimeException e) {
            log.warn("解析重定向失败，使用原始URL: {}", url);
        }
        return url;
    }

    /**
     * 构造抓取连接；只有小红书相关域名才附带登录态 Cookie，避免把凭据发给其他站点。
     */
    private Connection newConnection(String url, int timeoutMs) {
        Connection connection = Jsoup.connect(url)
            .userAgent(USER_AGENT)
            .timeout(timeoutMs)
            .followRedirects(true);

        String cookie = resolveCookieHeader(url);
        if (!cookie.isEmpty()) {
            connection.header("Cookie", cookie);
        }
        return connection;
    }

    private String resolveCookieHeader(String url) {
        if (xhsCookie == null || xhsCookie.isBlank()) {
            return "";
        }

        return isXiaohongshuHost(url) ? xhsCookie.trim() : "";
    }

    /**
     * 按 host 精确判断，避免把 Cookie 发给形如 example.com/xiaohongshu.com.png 的第三方地址。
     */
    private boolean isXiaohongshuHost(String url) {
        String host;
        try {
            host = new URI(url).getHost();
        } catch (URISyntaxException e) {
            return false;
        }

        if (host == null) {
            return false;
        }

        String lowerHost = host.toLowerCase();
        return lowerHost.equals("xiaohongshu.com") || lowerHost.endsWith(".xiaohongshu.com")
            || lowerHost.equals("xhslink.com") || lowerHost.endsWith(".xhslink.com");
    }

    /**
     * 小红书未取到正文时给出可操作的提示（Cookie 未配置 / 已失效）。
     */
    private String describeMissingXiaohongshuContent(ContentPlatform platform, String textContent, List<String> imageUrls) {
        if (platform != ContentPlatform.XIAOHONGSHU) {
            return null;
        }

        boolean hasText = textContent != null && !textContent.isBlank();
        if (hasText || !imageUrls.isEmpty()) {
            return null;
        }

        return xhsCookie == null || xhsCookie.isBlank()
            ? "小红书正文需要登录态：请在 deploy/.env 配置 XHS_COOKIE 后重试"
            : "小红书未返回正文，XHS_COOKIE 可能已过期，请重新复制 Cookie 后重试";
    }

    /**
     * 提取标题
     */
    private String extractTitle(Document doc) {
        // 优先 og:title
        Element ogTitle = doc.selectFirst("meta[property=og:title]");
        if (ogTitle != null && !ogTitle.attr("content").isEmpty()) {
            return ogTitle.attr("content");
        }
        // 其次 title 标签
        Element titleEl = doc.selectFirst("title");
        if (titleEl != null && !titleEl.text().isEmpty()) {
            return titleEl.text();
        }
        // 最后 h1
        Element h1 = doc.selectFirst("h1");
        return h1 != null ? h1.text() : "";
    }

    /**
     * 提取作者
     */
    private String extractAuthor(Document doc, ContentPlatform platform) {
        Element authorEl = switch (platform) {
            case WECHAT -> doc.selectFirst("#js_name");
            case XIAOHONGSHU -> doc.selectFirst(".user-name");
            case TOUTIAO -> doc.selectFirst(".article-meta .source");
            default -> doc.selectFirst("[rel=author], .author-name, .author");
        };
        return authorEl != null ? authorEl.text() : "";
    }

    /**
     * 提取封面图
     */
    private String extractCoverImage(Document doc) {
        Element ogImage = doc.selectFirst("meta[property=og:image]");
        if (ogImage != null && !ogImage.attr("content").isEmpty()) {
            // og:image 常写成协议相对地址（//host/x.png）或站内相对路径，补全后才能下载转存
            String absolute = ogImage.absUrl("content");
            return absolute.isEmpty() ? ogImage.attr("content") : absolute;
        }
        Element firstImg = doc.selectFirst("img[src]");
        return firstImg != null ? firstImg.attr("abs:src") : "";
    }

    /**
     * 按平台提取正文内容和图片
     */
    private ExtractedArticle extractArticleContent(Document doc, ContentPlatform platform) {
        return switch (platform) {
            case WECHAT -> extractWechatArticle(doc);
            case XIAOHONGSHU -> extractXiaohongshuArticle(doc);
            case TOUTIAO -> extractToutiaoArticle(doc);
            default -> extractGenericArticle(doc);
        };
    }

    /**
     * 微信公众号文章提取
     */
    private ExtractedArticle extractWechatArticle(Document doc) {
        Element content = doc.selectFirst("#js_content");
        if (content == null) {
            content = doc.selectFirst(".rich_media_content");
        }
        if (content == null) {
            return extractGenericArticle(doc);
        }

        String text = content.text();
        Elements imgs = content.select("img");
        List<String> imageUrls = imgs.stream()
            .map(img -> {
                // 微信图片优先取 data-src
                String src = img.attr("data-src");
                if (src.isEmpty()) {
                    src = img.attr("src");
                }
                return src;
            })
            .filter(src -> !src.isEmpty() && src.startsWith("http"))
            .limit(MAX_IMAGES)
            .toList();

        return new ExtractedArticle(text, new ArrayList<>(imageUrls));
    }

    /**
     * 小红书笔记提取
     * 小红书页面数据通常在 SSR 数据中
     */
    private ExtractedArticle extractXiaohongshuArticle(Document doc) {
        List<String> imageUrls = new ArrayList<>();
        StringBuilder text = new StringBuilder();

        // 尝试从 SSR 数据提取
        Element stateScript = doc.selectFirst("script:containsData(__INITIAL_STATE__)");
        if (stateScript != null) {
            String html = stateScript.html();
            // 提取笔记内容
            Pattern notePattern = Pattern.compile("\"desc\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = notePattern.matcher(html);
            if (matcher.find()) {
                text.append(matcher.group(1));
            }

            // 提取图片URL
            Pattern imgPattern = Pattern.compile("\"urlDefault\"\\s*:\\s*\"([^\"]+)\"");
            Matcher imgMatcher = imgPattern.matcher(html);
            int count = 0;
            while (imgMatcher.find() && count < MAX_IMAGES) {
                String imgUrl = imgMatcher.group(1)
                    .replace("\\u002F", "/")
                    .replace("\\/", "/");
                if (imgUrl.startsWith("http")) {
                    imageUrls.add(imgUrl);
                    count++;
                }
            }
        }

        // 如果 SSR 提取失败，降级为通用提取
        if (text.isEmpty()) {
            return extractGenericArticle(doc);
        }

        return new ExtractedArticle(text.toString(), imageUrls);
    }

    /**
     * 今日头条文章提取
     */
    private ExtractedArticle extractToutiaoArticle(Document doc) {
        Element content = doc.selectFirst(".article-content");
        if (content == null) {
            content = doc.selectFirst("article");
        }
        if (content == null) {
            return extractGenericArticle(doc);
        }

        String text = content.text();
        List<String> imageUrls = content.select("img[src]")
            .stream()
            .map(img -> img.attr("abs:src"))
            .filter(src -> !src.isEmpty())
            .limit(MAX_IMAGES)
            .toList();

        return new ExtractedArticle(text, new ArrayList<>(imageUrls));
    }

    /**
     * 通用文章提取（兜底策略）
     */
    private ExtractedArticle extractGenericArticle(Document doc) {
        Element content = doc.selectFirst("article");
        if (content == null) {
            content = doc.selectFirst("main");
        }
        if (content == null) {
            content = doc.selectFirst(".content, .article, .post");
        }
        if (content == null) {
            content = doc.body();
        }

        String text = content != null ? content.text() : "";
        List<String> imageUrls = new ArrayList<>();

        if (content != null) {
            Elements imgs = content.select("img[src]");
            for (Element img : imgs) {
                String src = img.attr("abs:src");
                if (!src.isEmpty() && !src.contains("avatar") && !src.contains("icon")
                    && !src.contains("logo") && !src.contains("banner")) {
                    imageUrls.add(src);
                    if (imageUrls.size() >= MAX_IMAGES) break;
                }
            }
        }

        return new ExtractedArticle(text, imageUrls);
    }

    /**
     * 下载图片并转为Base64
     */
    private List<String> downloadImagesAsBase64(List<String> imageUrls) {
        List<String> base64List = new ArrayList<>();
        for (String url : imageUrls) {
            try {
                byte[] bytes = HttpUtil.downloadBytes(url);
                if (bytes.length > 0 && bytes.length <= MAX_IMAGE_SIZE) {
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    String mimeType = guessMimeType(url);
                    base64List.add("data:" + mimeType + ";base64," + base64);
                } else {
                    log.warn("图片过大或为空({}字节)，跳过: {}", bytes.length, url);
                }
            } catch (RuntimeException e) {
                log.warn("下载图片失败: {}", url, e);
            }
        }
        return base64List;
    }

    private String storeCoverImage(String coverImage, String category) {
        if (coverImage == null || coverImage.isEmpty()) {
            return "";
        }

        try {
            return FileUploadUtil.uploadRemoteImage(coverImage, category, null);
        } catch (IOException | RuntimeException e) {
            log.warn("封面图转存失败，继续使用原始地址: {}", coverImage, e);
            return coverImage;
        }
    }

    /**
     * 根据URL猜测图片MIME类型
     */
    private String guessMimeType(String url) {
        String lower = url.toLowerCase();
        if (lower.contains(".png")) return "image/png";
        if (lower.contains(".gif")) return "image/gif";
        if (lower.contains(".webp")) return "image/webp";
        return "image/jpeg";
    }

    /**
     * 提取的文章内容内部DTO
     */
    private record ExtractedArticle(String textContent, List<String> imageUrls) {}
}
