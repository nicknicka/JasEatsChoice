package com.xx.jaseatschoicejava.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 媒体格式工具：按文件头魔数识别图片格式，避免仅凭链接后缀猜测。
 * 支持 JPEG、PNG、GIF、WebP。
 */
@Slf4j
public final class MediaFormatUtil {

    private MediaFormatUtil() {
    }

    /**
     * 按文件头魔数识别图片 MIME 类型，识别不出返回 null。
     */
    public static String detectImageMimeType(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (matches(bytes, 0, 0xFF, 0xD8, 0xFF)) {
            return "image/jpeg";
        }
        if (matches(bytes, 0, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
            return "image/png";
        }
        if (matches(bytes, 0, 'G', 'I', 'F', '8', '7', 'a')
            || matches(bytes, 0, 'G', 'I', 'F', '8', '9', 'a')) {
            return "image/gif";
        }
        if (matches(bytes, 0, 'R', 'I', 'F', 'F') && matches(bytes, 8, 'W', 'E', 'B', 'P')) {
            return "image/webp";
        }
        return null;
    }

    /**
     * 综合判断图片 MIME 类型：文件头魔数 → 响应头 Content-Type → 链接后缀 → 兜底 image/jpeg（记录日志）。
     */
    public static String resolveImageMimeType(byte[] bytes, String contentType, String url) {
        String byMagic = detectImageMimeType(bytes);
        if (byMagic != null) {
            return byMagic;
        }

        String byHeader = mimeTypeFromContentType(contentType);
        if (byHeader != null) {
            return byHeader;
        }

        String byUrl = mimeTypeFromUrl(url);
        if (byUrl != null) {
            return byUrl;
        }

        log.warn("图片格式无法识别，回退为 image/jpeg: contentType={}, url={}", contentType, url);
        return "image/jpeg";
    }

    private static String mimeTypeFromContentType(String contentType) {
        if (contentType == null) {
            return null;
        }
        String value = contentType.split(";", 2)[0].trim().toLowerCase();
        return value.startsWith("image/") && value.length() > "image/".length() ? value : null;
    }

    private static String mimeTypeFromUrl(String url) {
        if (url == null) {
            return null;
        }
        String lower = url.toLowerCase();
        int query = lower.indexOf('?');
        if (query >= 0) {
            lower = lower.substring(0, query);
        }
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return null;
    }

    private static boolean matches(byte[] bytes, int offset, int... signature) {
        if (bytes.length < offset + signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((bytes[offset + i] & 0xFF) != (signature[i] & 0xFF)) {
                return false;
            }
        }
        return true;
    }
}
