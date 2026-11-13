package com.xx.jaseatschoicejava.util;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.xx.jaseatschoicejava.config.FileUploadConfig;
import com.xx.jaseatschoicejava.exception.BusinessException;

class FileUploadUtilTest {

    @Test
    void uploadImageBytes_shouldStoreFileAndReturnPublicUrl() throws Exception {
        Path tempDir = Files.createTempDirectory("file-upload-util-test");

        FileUploadConfig config = new FileUploadConfig();
        config.setUploadPath(tempDir.toString() + "/");
        config.setUrlPrefix("/api/uploads/");
        config.setServerUrl("http://localhost:8080");
        setStaticConfig(config);

        byte[] imageBytes = new byte[] {1, 2, 3, 4, 5};
        String sourceName = "https://sns-webpic-qc.xhscdn.com/202604092337/demo.webp_3";

        String publicUrl = FileUploadUtil.uploadImageBytes(imageBytes, sourceName, "content-extraction/article/cover", null);

        assertTrue(publicUrl.startsWith("http://localhost:8080/api/uploads/content-extraction/article/cover/"));
        assertTrue(publicUrl.endsWith(".webp"));

        String fileName = publicUrl.substring(publicUrl.lastIndexOf('/') + 1);
        Path storedFile = tempDir.resolve("content-extraction/article/cover").resolve(fileName);
        assertTrue(Files.exists(storedFile));
        assertEquals(5L, Files.size(storedFile));

        setStaticConfig(null);
        deleteRecursively(tempDir);
    }

    @Test
    void resolveSafeExtension_shouldRejectHtmlAndDisguisedMultiSuffix() {
        for (String name : new String[] {"a.html", "a.HTML", "a.html.txt", "a.htm", "a.svg", "a.xml", "a.js", "payload.HtM.png"}) {
            assertThrows(BusinessException.class,
                () -> FileUploadUtil.resolveSafeExtension(name, FileUploadUtil.FILE_EXTENSIONS),
                "应拒绝：" + name);
            assertThrows(BusinessException.class,
                () -> FileUploadUtil.resolveSafeExtension(name, FileUploadUtil.IMAGE_EXTENSIONS),
                "应拒绝：" + name);
        }
    }

    @Test
    void resolveSafeExtension_shouldRejectMissingOrEmptyExtension() {
        assertThrows(BusinessException.class, () -> FileUploadUtil.resolveSafeExtension(null, FileUploadUtil.FILE_EXTENSIONS));
        assertThrows(BusinessException.class, () -> FileUploadUtil.resolveSafeExtension("noext", FileUploadUtil.FILE_EXTENSIONS));
        assertThrows(BusinessException.class, () -> FileUploadUtil.resolveSafeExtension("trailing.", FileUploadUtil.FILE_EXTENSIONS));
        assertThrows(BusinessException.class, () -> FileUploadUtil.resolveSafeExtension("   ", FileUploadUtil.FILE_EXTENSIONS));
    }

    @Test
    void resolveSafeExtension_shouldNormalizeCaseAndStripPath() {
        assertEquals(".png", FileUploadUtil.resolveSafeExtension("a.PNG", FileUploadUtil.IMAGE_EXTENSIONS));
        assertEquals(".jpeg", FileUploadUtil.resolveSafeExtension("../../etc/a.JpEg", FileUploadUtil.IMAGE_EXTENSIONS));
        assertEquals(".pdf", FileUploadUtil.resolveSafeExtension("C:\\docs\\a.pdf", FileUploadUtil.FILE_EXTENSIONS));
    }

    @Test
    void resolveSafeExtension_shouldKeepPerCategoryWhitelist() {
        assertThrows(BusinessException.class,
            () -> FileUploadUtil.resolveSafeExtension("a.pdf", FileUploadUtil.IMAGE_EXTENSIONS));
        assertThrows(BusinessException.class,
            () -> FileUploadUtil.resolveSafeExtension("a.exe", FileUploadUtil.FILE_EXTENSIONS));
        assertEquals(".mp4", FileUploadUtil.resolveSafeExtension("a.mp4", FileUploadUtil.FILE_EXTENSIONS));
    }

    @Test
    void uploadFile_shouldRejectHtmlWithoutWriting() throws Exception {
        Path tempDir = Files.createTempDirectory("file-upload-util-reject");
        FileUploadConfig config = new FileUploadConfig();
        config.setUploadPath(tempDir.toString() + "/");
        config.setUrlPrefix("/api/uploads/");
        setStaticConfig(config);

        try {
            MockMultipartFile file = new MockMultipartFile("file", "exploit.html", "text/html", "<script>alert(1)</script>".getBytes());
            assertThrows(BusinessException.class, () -> FileUploadUtil.uploadFile(file, "chat"));

            Path chatDir = tempDir.resolve("chat");
            assertTrue(!Files.exists(chatDir) || Files.list(chatDir).findAny().isEmpty(), "不允许落盘 .html");
        } finally {
            setStaticConfig(null);
            deleteRecursively(tempDir);
        }
    }

    @Test
    void uploadFile_shouldStoreWhitelistedDocument() throws Exception {
        Path tempDir = Files.createTempDirectory("file-upload-util-accept");
        FileUploadConfig config = new FileUploadConfig();
        config.setUploadPath(tempDir.toString() + "/");
        config.setUrlPrefix("/api/uploads/");
        setStaticConfig(config);

        try {
            MockMultipartFile file = new MockMultipartFile("file", "合同.PDF", "application/pdf", new byte[] {1, 2, 3});
            String relativePath = FileUploadUtil.uploadFile(file, "chat");

            assertTrue(relativePath.startsWith("chat/"));
            assertTrue(relativePath.endsWith(".pdf"));
            assertTrue(Files.exists(tempDir.resolve(relativePath)));
        } finally {
            setStaticConfig(null);
            deleteRecursively(tempDir);
        }
    }

    private void setStaticConfig(FileUploadConfig config) throws Exception {
        Field field = FileUploadUtil.class.getDeclaredField("fileUploadConfig");
        field.setAccessible(true);
        field.set(null, config);
    }

    private void deleteRecursively(Path root) throws Exception {
        if (root == null || !Files.exists(root)) {
            return;
        }

        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (java.io.IOException ignored) {
                    }
                });
        }
    }
}