package com.xx.jaseatschoicejava.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * 图片格式必须按文件头魔数识别，认不出时依次回退响应头、链接后缀，最后兜底 JPEG。
 */
class MediaFormatUtilTest {

    @Test
    void detectImageMimeType_jpeg() {
        assertEquals("image/jpeg", MediaFormatUtil.detectImageMimeType(
            new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0}));
    }

    @Test
    void detectImageMimeType_png() {
        assertEquals("image/png", MediaFormatUtil.detectImageMimeType(
            new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A}));
    }

    @Test
    void detectImageMimeType_gif() {
        assertEquals("image/gif", MediaFormatUtil.detectImageMimeType(
            "GIF87a".getBytes(StandardCharsets.US_ASCII)));
        assertEquals("image/gif", MediaFormatUtil.detectImageMimeType(
            "GIF89a".getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    void detectImageMimeType_webp() {
        byte[] webp = new byte[] {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};
        assertEquals("image/webp", MediaFormatUtil.detectImageMimeType(webp));
    }

    @Test
    void detectImageMimeType_unknown_returnsNull() {
        assertNull(MediaFormatUtil.detectImageMimeType(unrecognizable()));
        assertNull(MediaFormatUtil.detectImageMimeType(null));
    }

    @Test
    void resolveImageMimeType_magicWinsOverHeaderAndSuffix() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        assertEquals("image/png",
            MediaFormatUtil.resolveImageMimeType(png, "image/jpeg", "https://cdn.example.com/a.jpg"));
    }

    @Test
    void resolveImageMimeType_fallsBackToContentType() {
        assertEquals("image/webp",
            MediaFormatUtil.resolveImageMimeType(unrecognizable(), "image/webp; charset=binary", "https://cdn.example.com/a.jpg"));
    }

    @Test
    void resolveImageMimeType_fallsBackToUrlSuffix() {
        assertEquals("image/gif",
            MediaFormatUtil.resolveImageMimeType(unrecognizable(), null, "https://cdn.example.com/a.GIF?token=1"));
    }

    @Test
    void resolveImageMimeType_defaultsToJpegWhenUnrecognizable() {
        assertEquals("image/jpeg",
            MediaFormatUtil.resolveImageMimeType(unrecognizable(), null, "https://cdn.example.com/no-extension"));
    }

    private static byte[] unrecognizable() {
        return new byte[] {0x00, 0x01, 0x02, 0x03, 0x04, 0x05};
    }
}
