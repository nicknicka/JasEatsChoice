package com.xx.jaseatschoicejava.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 管理员轮换后的 AI active key 运行时存储。文件内容不进入数据库、Redis 或日志。
 */
@Component
public class AiRuntimeKeyStore {

    private static final Set<PosixFilePermission> DIRECTORY_PERMISSIONS = Set.of(
        PosixFilePermission.OWNER_READ,
        PosixFilePermission.OWNER_WRITE,
        PosixFilePermission.OWNER_EXECUTE
    );
    private static final Set<PosixFilePermission> FILE_PERMISSIONS = Set.of(
        PosixFilePermission.OWNER_READ,
        PosixFilePermission.OWNER_WRITE
    );

    private final String filePath;

    public AiRuntimeKeyStore(@Value("${AI_ACTIVE_KEY_FILE:}") String filePath) {
        this.filePath = filePath;
    }

    public String readKey() {
        if (!hasPath()) {
            return "";
        }
        try {
            Path path = Path.of(filePath);
            if (!Files.isRegularFile(path)) {
                return "";
            }
            return Files.readString(path, StandardCharsets.UTF_8).trim();
        } catch (IOException exception) {
            return "";
        }
    }

    public void writeKey(String key) {
        requirePath();
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("AI key 不能为空");
        }
        try {
            Path path = Path.of(filePath);
            Path directory = path.toAbsolutePath().getParent();
            Files.createDirectories(directory);
            Files.setPosixFilePermissions(directory, DIRECTORY_PERMISSIONS);

            Path temporaryFile = Files.createTempFile(
                directory, ".ai-active-key-", ".tmp",
                java.nio.file.attribute.PosixFilePermissions.asFileAttribute(FILE_PERMISSIONS));
            try {
                Files.writeString(temporaryFile, key.trim(), StandardCharsets.UTF_8);
                Files.move(temporaryFile, path,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException exception) {
                Files.deleteIfExists(temporaryFile);
                throw exception;
            }
        } catch (IOException exception) {
            throw new IllegalStateException("AI key 文件写入失败", exception);
        }
    }

    public String updatedAt() {
        if (!exists()) {
            return null;
        }
        try {
            FileTime fileTime = Files.getLastModifiedTime(Path.of(filePath));
            return LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault())
                .withNano(0)
                .toString();
        } catch (IOException exception) {
            return null;
        }
    }

    public boolean exists() {
        return hasPath() && Files.isRegularFile(Path.of(filePath));
    }

    public void deleteKey() {
        requirePath();
        try {
            Files.deleteIfExists(Path.of(filePath));
        } catch (IOException exception) {
            throw new IllegalStateException("AI key 文件删除失败", exception);
        }
    }

    private void requirePath() {
        if (!hasPath()) {
            throw new IllegalStateException("AI_ACTIVE_KEY_FILE 未配置");
        }
    }

    private boolean hasPath() {
        return filePath != null && !filePath.isBlank();
    }
}
