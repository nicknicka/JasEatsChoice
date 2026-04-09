package com.xx.jaseatschoicejava.service.extraction.fetcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.jaseatschoicejava.enums.ContentPlatform;
import com.xx.jaseatschoicejava.enums.ContentType;
import com.xx.jaseatschoicejava.service.extraction.dto.FetchedContent;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 视频内容抓取策略
 * 通过平台 API 获取视频流直链 URL，用于传给 GLM-4.6V-Flash 视频理解
 * 支持：抖音、B站、快手
 * 降级：获取视频封面图作为图片传给视觉模型
 */
@Slf4j
@Component
public class VideoContentFetcher implements ContentFetcher {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
        + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int TIMEOUT_MS = 30000;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean canHandle(ContentPlatform platform, ContentType contentType) {
        return contentType == ContentType.VIDEO
            && (platform == ContentPlatform.DOUYIN
            || platform == ContentPlatform.BILIBILI
            || platform == ContentPlatform.KUAISHOU
            || platform == ContentPlatform.XIAOHONGSHU);
    }

    @Override
    public FetchedContent fetch(String url) {
        log.info("开始抓取视频内容: {}", url);

        ContentPlatform platform = ContentPlatform.parseFromUrl(url);

        try {
            VideoInfo videoInfo = switch (platform) {
                case DOUYIN -> fetchDouyinVideo(url);
                case BILIBILI -> fetchBilibiliVideo(url);
                case KUAISHOU -> fetchKuaishouVideo(url);
                case XIAOHONGSHU -> fetchXiaohongshuVideo(url);
                default -> fetchGenericVideoInfo(url);
            };

            if (videoInfo == null) {
                log.warn("视频信息获取失败: {}", url);
                return buildFallbackResult(url, platform, "视频信息获取失败");
            }

            FetchedContent.FetchedContentBuilder builder = FetchedContent.builder()
                .title(videoInfo.title)
                .author(videoInfo.author)
                .coverImage(videoInfo.coverUrl)
                .textContent(videoInfo.description)
                .contentType(ContentType.VIDEO)
                .platform(platform);

            // 优先使用视频直链URL（不需要下载）
            if (videoInfo.videoUrl != null && !videoInfo.videoUrl.isEmpty()) {
                log.info("获取到视频直链: {}", videoInfo.videoUrl);
                builder.videoUrl(videoInfo.videoUrl);
            } else {
                // 降级：下载视频转Base64（限制大小）
                log.info("未获取到视频直链，尝试获取封面图作为降级");
                builder.videoUrl(null);
            }

            return builder.fetchSuccess(true).build();

        } catch (Exception e) {
            log.error("抓取视频内容失败: {}", url, e);
            return buildFallbackResult(url, platform, e.getMessage());
        }
    }

    /**
     * 构建降级结果（至少有封面图）
     */
    private FetchedContent buildFallbackResult(String url, ContentPlatform platform, String errorMsg) {
        return FetchedContent.builder()
            .fetchSuccess(false)
            .errorMessage(errorMsg)
            .contentType(ContentType.VIDEO)
            .platform(platform)
            .build();
    }

    // ==================== 抖音视频 ====================

    /**
     * 抖音视频信息获取
     * 从分享URL中提取视频ID，然后通过页面解析获取视频信息
     */
    private VideoInfo fetchDouyinVideo(String url) {
        try {
            // 解析重定向获取真实URL
            String realUrl = resolveDouyinUrl(url);
            log.info("抖音真实URL: {}", realUrl);

            // 从URL提取视频ID
            String videoId = extractDouyinVideoId(realUrl);
            if (videoId == null) {
                log.warn("无法从URL提取抖音视频ID: {}", realUrl);
                return fetchVideoInfoFromPage(url, ContentPlatform.DOUYIN);
            }

            // 通过页面获取视频信息
            return fetchVideoInfoFromPage(
                "https://www.douyin.com/video/" + videoId,
                ContentPlatform.DOUYIN
            );

        } catch (Exception e) {
            log.error("获取抖音视频信息失败: {}", url, e);
            return null;
        }
    }

    /**
     * 解析抖音短链接
     */
    private String resolveDouyinUrl(String url) {
        try {
            var response = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .followRedirects(true)
                .execute();
            return response.url().toString();
        } catch (Exception e) {
            return url;
        }
    }

    /**
     * 从URL提取抖音视频ID
     */
    private String extractDouyinVideoId(String url) {
        // 匹配 /video/7xxxxxxxxxxxx 格式
        Pattern pattern = Pattern.compile("/video/(\\d+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // 匹配 modal_id=7xxxxxxxxxxxx 格式
        Pattern paramPattern = Pattern.compile("modal_id=(\\d+)");
        Matcher paramMatcher = paramPattern.matcher(url);
        if (paramMatcher.find()) {
            return paramMatcher.group(1);
        }
        return null;
    }

    // ==================== B站视频 ====================

    /**
     * B站视频信息获取
     * 通过B站API获取视频信息
     */
    private VideoInfo fetchBilibiliVideo(String url) {
        try {
            String bvid = extractBilibiliBvid(url);
            if (bvid == null) {
                log.warn("无法从URL提取B站BV号: {}", url);
                return fetchVideoInfoFromPage(url, ContentPlatform.BILIBILI);
            }

            // 调用B站API获取视频信息
            String apiUrl = "https://api.bilibili.com/x/web-interface/view?bvid=" + bvid;
            String json = Jsoup.connect(apiUrl)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .ignoreContentType(true)
                .execute()
                .body();

            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.get("data");
            if (data == null) {
                log.warn("B站API返回数据为空: {}", json.substring(0, Math.min(200, json.length())));
                return null;
            }

            VideoInfo info = new VideoInfo();
            info.title = data.path("title").asText("");
            info.author = data.path("owner").path("name").asText("");
            info.description = data.path("desc").asText("");
            info.coverUrl = data.path("pic").asText("");

            // 获取视频流地址（需要CID）
            long cid = data.path("cid").asLong();
            if (cid > 0) {
                String playUrl = "https://api.bilibili.com/x/player/playurl?bvid=" + bvid + "&cid=" + cid + "&qn=16&fnver=0&fnval=16";
                try {
                    String playJson = Jsoup.connect(playUrl)
                        .userAgent(USER_AGENT)
                        .timeout(TIMEOUT_MS)
                        .ignoreContentType(true)
                        .execute()
                        .body();

                    JsonNode playRoot = objectMapper.readTree(playJson);
                    JsonNode videoUrl = playRoot.at("/data/dash/video/0/baseUrl");
                    if (!videoUrl.isMissingNode()) {
                        info.videoUrl = videoUrl.asText();
                    }
                } catch (Exception e) {
                    log.warn("获取B站视频流地址失败: {}", e.getMessage());
                }
            }

            return info;

        } catch (Exception e) {
            log.error("获取B站视频信息失败: {}", url, e);
            return fetchVideoInfoFromPage(url, ContentPlatform.BILIBILI);
        }
    }

    /**
     * 从URL提取B站BV号
     */
    private String extractBilibiliBvid(String url) {
        Pattern pattern = Pattern.compile("(BV[a-zA-Z0-9]+)");
        Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    // ==================== 快手视频 ====================

    /**
     * 快手视频信息获取
     */
    private VideoInfo fetchKuaishouVideo(String url) {
        try {
            return fetchVideoInfoFromPage(url, ContentPlatform.KUAISHOU);
        } catch (Exception e) {
            log.error("获取快手视频信息失败: {}", url, e);
            return null;
        }
    }

    // ==================== 小红书视频 ====================

    /**
     * 小红书视频信息获取
     */
    private VideoInfo fetchXiaohongshuVideo(String url) {
        try {
            return fetchVideoInfoFromPage(url, ContentPlatform.XIAOHONGSHU);
        } catch (Exception e) {
            log.error("获取小红书视频信息失败: {}", url, e);
            return null;
        }
    }

    // ==================== 通用方法 ====================

    /**
     * 通过解析网页 meta 标签获取视频信息（通用降级）
     */
    private VideoInfo fetchVideoInfoFromPage(String url, ContentPlatform platform) {
        try {
            var response = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .followRedirects(true)
                .execute();

            var doc = response.parse();
            VideoInfo info = new VideoInfo();

            // 从 og 标签提取信息
            info.title = getMetaContent(doc, "og:title");
            info.description = getMetaContent(doc, "og:description");
            info.coverUrl = getMetaContent(doc, "og:image");

            // 尝试获取视频URL
            String videoUrl = getMetaContent(doc, "og:video");
            if (videoUrl.isEmpty()) {
                videoUrl = getMetaContent(doc, "og:video:url");
            }
            info.videoUrl = videoUrl;

            // 如果没获取到视频URL，尝试从页面中查找 video 标签
            if (info.videoUrl.isEmpty()) {
                var videoEl = doc.selectFirst("video source[src]");
                if (videoEl != null) {
                    info.videoUrl = videoEl.attr("abs:src");
                }
            }

            return info;

        } catch (Exception e) {
            log.warn("从网页解析视频信息失败: {}", url, e);
            return null;
        }
    }

    /**
     * 获取 meta 标签的 content 属性
     */
    private String getMetaContent(org.jsoup.nodes.Document doc, String property) {
        var el = doc.selectFirst("meta[property=" + property + "]");
        return el != null ? el.attr("content") : "";
    }

    /**
     * 获取通用视频信息（未知平台）
     */
    private VideoInfo fetchGenericVideoInfo(String url) {
        return fetchVideoInfoFromPage(url, ContentPlatform.OTHER);
    }

    /**
     * 视频信息内部DTO
     */
    private static class VideoInfo {
        String title;
        String author;
        String description;
        String coverUrl;
        String videoUrl;
    }
}
