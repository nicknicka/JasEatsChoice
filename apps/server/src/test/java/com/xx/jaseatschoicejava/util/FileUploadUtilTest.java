package com.xx.jaseatschoicejava.util;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.xx.jaseatschoicejava.config.FileUploadConfig;

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