package com.xx.jaseatschoicejava.service.extraction.fetcher;

import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.xx.jaseatschoicejava.enums.ContentPlatform;
import com.xx.jaseatschoicejava.enums.ContentType;
import com.xx.jaseatschoicejava.service.extraction.dto.FetchedContent;
import com.xx.jaseatschoicejava.util.FileUploadUtil;

import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 图片内容抓取策略
 * 直接下载图片并转为Base64
 */
@Slf4j
@Component
public class ImageContentFetcher implements ContentFetcher {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    @Autowired
    private ArticleContentFetcher articleFetcher;

    @Override
    public boolean canHandle(ContentPlatform platform, ContentType contentType) {
        return contentType == ContentType.IMAGE;
    }

    @Override
    public FetchedContent fetch(String url) {
        log.info("开始抓取图片内容: {}", url);

        try {
            byte[] imageBytes = downloadImage(url);
            if (imageBytes == null || imageBytes.length == 0) {
                log.warn("图片下载失败或为空: {}", url);
                return FetchedContent.builder()
                    .fetchSuccess(false)
                    .errorMessage("图片下载失败")
                    .contentType(ContentType.IMAGE)
                    .build();
            }

            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = guessMimeType(url);
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
            return articleFetcher.fetch(url);
        }
    }

    /**
     * 下载图片
     */
    private byte[] downloadImage(String url) {
        try {
            byte[] bytes = HttpUtil.downloadBytes(url);
            if (bytes.length > MAX_IMAGE_SIZE) {
                log.warn("图片过大({}字节)，跳过: {}", bytes.length, url);
                return null;
            }
            return bytes;
        } catch (RuntimeException e) {
            log.error("下载图片失败: {}", url, e);
            return null;
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
        return "image/jpeg"; // 默认jpeg
    }

    private String storeImage(String sourceUrl, byte[] imageBytes) {
        try {
            return FileUploadUtil.uploadImageBytes(imageBytes, sourceUrl, "content-extraction/image", null);
        } catch (java.io.IOException | RuntimeException e) {
            log.warn("图片转存失败，继续使用原始地址: {}", sourceUrl, e);
            return sourceUrl;
        }
    }
}
