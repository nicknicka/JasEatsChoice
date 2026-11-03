package com.xx.jaseatschoicejava.service.extraction.fetcher;

import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.xx.jaseatschoicejava.config.ContentExtractionConfig;
import com.xx.jaseatschoicejava.enums.ContentPlatform;
import com.xx.jaseatschoicejava.enums.ContentType;
import com.xx.jaseatschoicejava.service.extraction.dto.FetchedContent;
import com.xx.jaseatschoicejava.util.FileUploadUtil;
import com.xx.jaseatschoicejava.util.MediaFormatUtil;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 图片内容抓取策略
 * 直接下载图片并转为Base64
 */
@Slf4j
@Component
public class ImageContentFetcher implements ContentFetcher {

    @Autowired
    private ArticleContentFetcher articleFetcher;

    @Autowired
    private ContentExtractionConfig config;

    @Override
    public boolean canHandle(ContentPlatform platform, ContentType contentType) {
        return contentType == ContentType.IMAGE;
    }

    @Override
    public FetchedContent fetch(String url, String userId) {
        log.info("开始抓取图片内容: {}", url);

        try {
            DownloadedImage downloaded = downloadImage(url);
            if (downloaded == null || downloaded.bytes().length == 0) {
                log.warn("图片下载失败或为空: {}", url);
                return FetchedContent.builder()
                    .fetchSuccess(false)
                    .errorMessage("图片下载失败")
                    .contentType(ContentType.IMAGE)
                    .build();
            }

            byte[] imageBytes = downloaded.bytes();
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = MediaFormatUtil.resolveImageMimeType(imageBytes, downloaded.contentType(), url);
            String localImageUrl = storeImage(url, imageBytes);

            return FetchedContent.builder()
                .textContent("")
                .imageBase64List(List.of("data:" + mimeType + ";base64," + base64))
                .imageUrls(List.of(localImageUrl))
                .coverImage(localImageUrl)
                .contentType(ContentType.IMAGE)
                .fetchSuccess(true)
                .build();

        } catch (RuntimeException e) {
            log.error("抓取图片内容失败: {}", url, e);
            // 降级：尝试用文章抓取器处理
            return articleFetcher.fetch(url, userId);
        }
    }

    /**
     * 下载图片，同时带回响应头 Content-Type 作为格式识别的兜底依据
     */
    private DownloadedImage downloadImage(String url) {
        try (HttpResponse response = HttpRequest.get(url)
            .header("User-Agent", config.getFetch().getUserAgent())
            .setConnectionTimeout(config.getFetch().getConnectTimeout())
            .setReadTimeout(config.getFetch().getReadTimeout())
            .execute()) {
            byte[] bytes = response.bodyBytes();
            if (bytes.length > config.getFetch().getMaxImageSize()) {
                log.warn("图片过大({}字节)，跳过: {}", bytes.length, url);
                return null;
            }
            return new DownloadedImage(bytes, response.header("Content-Type"));
        } catch (RuntimeException e) {
            log.error("下载图片失败: {}", url, e);
            return null;
        }
    }

    private String storeImage(String sourceUrl, byte[] imageBytes) {
        try {
            return FileUploadUtil.uploadImageBytes(imageBytes, sourceUrl, "content-extraction/image", null);
        } catch (java.io.IOException | RuntimeException e) {
            log.warn("图片转存失败，继续使用原始地址: {}", sourceUrl, e);
            return sourceUrl;
        }
    }

    private record DownloadedImage(byte[] bytes, String contentType) {}
}
