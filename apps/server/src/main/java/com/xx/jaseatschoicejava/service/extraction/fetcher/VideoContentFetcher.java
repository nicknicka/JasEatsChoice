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

            // 验证是否获取到有效内容
            boolean hasContent = (videoInfo.title != null && !videoInfo.title.isEmpty())
                || (videoInfo.description != null && !videoInfo.description.isEmpty())
                || (videoInfo.coverUrl != null && !videoInfo.coverUrl.isEmpty())
                || (videoInfo.videoUrl != null && !videoInfo.videoUrl.isEmpty());

            if (!hasContent) {
                log.warn("视频信息所有字段为空: {}", url);
                return buildFallbackResult(url, platform, "未能从页面提取到任何内容");
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

            // 获取视频流地址（mp4格式，GLM Vision不支持m4s DASH分片）
            long cid = data.path("cid").asLong();
            if (cid > 0) {
                String playUrl = "https://api.bilibili.com/x/player/playurl?bvid=" + bvid + "&cid=" + cid + "&qn=16&fnver=0&fnval=0";
                try {
                    String playJson = Jsoup.connect(playUrl)
                        .userAgent(USER_AGENT)
                        .timeout(TIMEOUT_MS)
                        .ignoreContentType(true)
                        .execute()
                        .body();

                    JsonNode playRoot = objectMapper.readTree(playJson);
                    // fnval=0 返回 durl 数组（mp4格式）
                    JsonNode durlArray = playRoot.at("/data/durl");
                    if (durlArray.isArray() && !durlArray.isEmpty()) {
                        info.videoUrl = durlArray.get(0).path("url").asText("");
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
     * 优先解析 SSR 的 __INITIAL_STATE__ 数据，降级为 meta 标签
     */
    private VideoInfo fetchXiaohongshuVideo(String url) {
        try {
            String noteId = extractXiaohongshuNoteId(url);
            log.info("小红书笔记ID: {}", noteId);

            // 直接使用原始URL（带 xsec_token 等参数），避免被拦截
            var response = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .followRedirects(true)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Cache-Control", "no-cache")
                .header("Sec-Fetch-Dest", "document")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-Site", "none")
                .header("Upgrade-Insecure-Requests", "1")
                .execute();

            var doc = response.parse();
            String finalUrl = response.url().toString();
            log.info("小红书页面获取完成, 最终URL: {}, HTML长度: {}", finalUrl, doc.html().length());

            // 检查是否被重定向到404页面
            if (finalUrl.contains("/404") || finalUrl.contains("error_code=")) {
                log.warn("小红书页面被拦截，重定向到: {}", finalUrl);
                return null;
            }

            // 方法1: 解析 __INITIAL_STATE__ 中的 SSR 数据
            VideoInfo stateInfo = parseXiaohongshuStateFromDoc(doc, noteId);
            if (stateInfo != null && stateInfo.title != null && !stateInfo.title.isEmpty()) {
                log.info("从__INITIAL_STATE__解析成功: title={}, hasVideo={}, hasCover={}",
                    stateInfo.title,
                    stateInfo.videoUrl != null && !stateInfo.videoUrl.isEmpty(),
                    stateInfo.coverUrl != null && !stateInfo.coverUrl.isEmpty());
                return stateInfo;
            }

            // 方法2: 从 meta 标签提取
            VideoInfo metaInfo = new VideoInfo();
            metaInfo.title = getMetaContent(doc, "og:title");
            metaInfo.description = getMetaContent(doc, "og:description");
            metaInfo.coverUrl = getMetaContent(doc, "og:image");
            String videoUrl = getMetaContent(doc, "og:video");
            if (videoUrl.isEmpty()) {
                videoUrl = getMetaContent(doc, "og:video:url");
            }
            metaInfo.videoUrl = videoUrl;

            // 从 title 标签获取兜底
            if ((metaInfo.title == null || metaInfo.title.isEmpty()) && doc.title() != null) {
                metaInfo.title = doc.title().replace(" - 小红书", "").trim();
            }

            log.info("小红书meta标签解析: title={}, hasCover={}, hasVideo={}",
                metaInfo.title,
                metaInfo.coverUrl != null && !metaInfo.coverUrl.isEmpty(),
                metaInfo.videoUrl != null && !metaInfo.videoUrl.isEmpty());

            return metaInfo;

        } catch (Exception e) {
            log.error("获取小红书视频信息失败: {}", url, e);
            return null;
        }
    }

    /**
     * 从URL提取小红书笔记ID
     */
    private String extractXiaohongshuNoteId(String url) {
        // 匹配 /discovery/item/{id} 或 /explore/{id}
        Pattern pattern = Pattern.compile("/(?:discovery/item|explore)/([a-f0-9]{24})");
        Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * 从页面文档中解析 __INITIAL_STATE__ 的 SSR 数据
     */
    private VideoInfo parseXiaohongshuStateFromDoc(org.jsoup.nodes.Document doc, String noteId) {
        for (var script : doc.select("script")) {
            String content = script.html();
            if (content.contains("__INITIAL_STATE__")) {
                try {
                    // 提取 JSON 部分: window.__INITIAL_STATE__=xxx
                    int eqIdx = content.indexOf("=");
                    if (eqIdx < 0) continue;

                    String jsonStr = content.substring(eqIdx + 1);
                    // 去除尾部分号
                    if (jsonStr.endsWith(";")) {
                        jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
                    }
                    jsonStr = jsonStr.trim();
                    if (jsonStr.equals("undefined")) continue;

                    // 小红书 SSR 的 __INITIAL_STATE__ 含 JavaScript undefined 字面量，需替换为 null
                    jsonStr = jsonStr.replace(":undefined", ":null")
                                     .replace(",undefined,", ",null,")
                                     .replace("[undefined,", "[null,")
                                     .replace(",undefined]", ",null]");

                    JsonNode root = objectMapper.readTree(jsonStr);

                    // 导航到笔记详情: note.noteDetailMap.<noteId>.note
                    JsonNode noteDetailMap = root.at("/note/noteDetailMap");
                    if (noteDetailMap.isMissingNode()) continue;

                    JsonNode noteData = null;
                    if (noteId != null) {
                        noteData = noteDetailMap.at("/" + noteId + "/note");
                    }
                    if (noteData == null || noteData.isMissingNode()) {
                        // 取第一个条目
                        var fields = noteDetailMap.fields();
                        if (fields.hasNext()) {
                            noteData = fields.next().getValue().path("note");
                        }
                    }
                    if (noteData == null || noteData.isMissingNode()) continue;

                    VideoInfo info = new VideoInfo();
                    info.title = noteData.path("title").asText("");
                    info.description = noteData.path("desc").asText("");

                    // 封面图 - 从 imageList 获取
                    JsonNode imageList = noteData.path("imageList");
                    if (imageList.isArray() && !imageList.isEmpty()) {
                        info.coverUrl = imageList.get(0).path("urlDefault").asText("");
                        if (info.coverUrl.isEmpty()) {
                            info.coverUrl = imageList.get(0).path("url").asText("");
                        }
                        if (info.coverUrl.isEmpty()) {
                            info.coverUrl = imageList.get(0).path("infoList").path(0).path("url").asText("");
                        }
                    }

                    // 视频URL
                    JsonNode video = noteData.path("video");
                    if (!video.isMissingNode()) {
                        JsonNode media = video.path("media");
                        if (!media.isMissingNode()) {
                            JsonNode stream = media.path("stream");
                            if (stream.isArray() && !stream.isEmpty()) {
                                info.videoUrl = stream.get(0).path("masterUrl").asText("");
                                if (info.videoUrl.isEmpty()) {
                                    JsonNode backupUrls = stream.get(0).path("backupUrls");
                                    if (backupUrls.isArray() && !backupUrls.isEmpty()) {
                                        info.videoUrl = backupUrls.get(0).asText("");
                                    }
                                }
                            }
                        }
                        // 视频封面兜底
                        if (info.coverUrl == null || info.coverUrl.isEmpty()) {
                            info.coverUrl = video.path("cover").asText("");
                        }
                    }

                    // 作者
                    JsonNode user = noteData.path("user");
                    if (!user.isMissingNode()) {
                        info.author = user.path("nickname").asText("");
                    }

                    return info;

                } catch (Exception e) {
                    log.warn("解析小红书__INITIAL_STATE__失败: {}", e.getMessage());
                }
            }
        }
        return null;
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
