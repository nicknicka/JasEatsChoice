package com.xx.jaseatschoicejava.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.Set;

/**
 * 文件型密钥存储。密钥内容不进入数据库、Redis 或日志。
 */
public final class FileSecretStore {

    private static final Set<PosixFilePermission> DIRECTORY_PERMISSIONS = Set.of(
        PosixFilePermission.OWNER_READ,
        PosixFilePermission.OWNER_WRITE,
        PosixFilePermission.OWNER_EXECUTE
    );
    private static final Set<PosixFilePermission> FILE_PERMISSIONS = Set.of(
        PosixFilePermission.OWNER_READ,
        PosixFilePermission.OWNER_WRITE
    );
    private static final String SINGLE_KEY = "secret";

    private final String basePath;
    private final boolean directoryMode;

    private FileSecretStore(String basePath, boolean directoryMode) {
        this.basePath = basePath;
        this.directoryMode = directoryMode;
    }

    /**
     * 单文件密钥槽位，key 固定为 {@value SINGLE_KEY}。
     */
    public static FileSecretStore file(String filePath) {
        return new FileSecretStore(filePath, false);
    }

    /**
     * 目录密钥槽位，每个 key 使用独立的受限文件。
     */
    public static FileSecretStore directory(String directoryPath) {
        return new FileSecretStore(directoryPath, true);
    }

    public String read(String key) {
        if (!hasPath()) {
            return "";
        }
        Path path = resolve(key);
        if (!Files.isRegularFile(path)) {
            return "";
        }
        try {
            return Files.readString(path, StandardCharsets.UTF_8).trim();
        } catch (IOException exception) {
            return "";
        }
    }

    public void write(String key, String value) {
        requirePath();
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("密钥不能为空");
        }
        Path path = resolve(key);
        Path directory = path.getParent();
        try {
            Files.createDirectories(directory);
            Files.setPosixFilePermissions(directory, DIRECTORY_PERMISSIONS);

            Path temporaryFile = Files.createTempFile(
                directory, ".secret-", ".tmp",
                PosixFilePermissions.asFileAttribute(FILE_PERMISSIONS));
            try {
                Files.writeString(temporaryFile, value.trim(), StandardCharsets.UTF_8);
                Files.move(temporaryFile, path,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException exception) {
                Files.deleteIfExists(temporaryFile);
                throw exception;
            }
        } catch (IOException exception) {
            throw new IllegalStateException("密钥文件写入失败", exception);
        }
    }

    public String updatedAt(String key) {
        if (!exists(key)) {
            return null;
        }
        try {
            FileTime fileTime = Files.getLastModifiedTime(resolve(key));
            return LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault())
                .withNano(0)
                .toString();
        } catch (IOException exception) {
            return null;
        }
    }

    public boolean exists(String key) {
        return hasPath() && Files.isRegularFile(resolve(key));
    }

    public void delete(String key) {
        requirePath();
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException exception) {
            throw new IllegalStateException("密钥文件删除失败", exception);
        }
    }

    private Path resolve(String key) {
        if (!hasPath()) {
            throw new IllegalStateException("密钥路径未配置");
        }
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("密钥标识不能为空");
        }
        Path base = Path.of(basePath).toAbsolutePath().normalize();
        return directoryMode ? base.resolve(fileName(key)) : base;
    }

    private String fileName(String key) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(key.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 不可用", exception);
        }
    }

    private void requirePath() {
        if (!hasPath()) {
            throw new IllegalStateException("密钥路径未配置");
        }
    }

    private boolean hasPath() {
        return basePath != null && !basePath.isBlank();
    }
}
