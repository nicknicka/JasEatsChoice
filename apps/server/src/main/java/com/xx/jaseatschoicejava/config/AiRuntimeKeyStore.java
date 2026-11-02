package com.xx.jaseatschoicejava.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 管理员轮换后的 AI active key 运行时存储。文件内容不进入数据库、Redis 或日志。
 */
@Component
public class AiRuntimeKeyStore {

    private static final String FILE_ENV = "AI_ACTIVE_KEY_FILE";

    private final String filePath;
    private final FileSecretStore store;

    public AiRuntimeKeyStore(@Value("${AI_ACTIVE_KEY_FILE:}") String filePath) {
        this.filePath = filePath;
        this.store = FileSecretStore.file(filePath);
    }

    public String readKey() {
        return store.read("secret");
    }

    public void writeKey(String key) {
        requirePath();
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("AI key 不能为空");
        }
        try {
            store.write("secret", key);
        } catch (IllegalStateException exception) {
            throw new IllegalStateException("AI key 文件写入失败", exception.getCause());
        }
    }

    public String updatedAt() {
        return store.updatedAt("secret");
    }

    public boolean exists() {
        return store.exists("secret");
    }

    public void deleteKey() {
        requirePath();
        try {
            store.delete("secret");
        } catch (IllegalStateException exception) {
            throw new IllegalStateException("AI key 文件删除失败", exception.getCause());
        }
    }

    private void requirePath() {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalStateException(FILE_ENV + " 未配置");
        }
    }
}
