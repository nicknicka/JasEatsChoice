package com.xx.jaseatschoicejava.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 公告 Embedding 独立的运行时密钥槽位。
 *
 * 与「当前激活对话供应商的 key」分开存放、分别轮换：轮换 Embedding key 不影响对话模型，
 * 轮换对话 key 也不会被 Embedding 隐式借用。文件读写复用 {@link FileSecretStore} 的实现。
 */
@Component
public class AiEmbeddingRuntimeKeyStore {

    private static final String FILE_ENV = "AI_EMBEDDING_ACTIVE_KEY_FILE";

    private final String filePath;
    private final FileSecretStore store;

    public AiEmbeddingRuntimeKeyStore(@Value("${AI_EMBEDDING_ACTIVE_KEY_FILE:}") String filePath) {
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
