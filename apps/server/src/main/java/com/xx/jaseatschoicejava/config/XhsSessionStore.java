package com.xx.jaseatschoicejava.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 小红书登录态存储：按用户隔离，key 就是 userId。内容不进入数据库、Redis 或日志。
 */
@Component
public class XhsSessionStore {

    private final FileSecretStore store;

    public XhsSessionStore(@Value("${XHS_SESSION_DIR:/app/runtime/secrets/xhs}") String directory) {
        this.store = FileSecretStore.directory(directory);
    }

    public String read(String userId) {
        return store.read(userId);
    }

    public void write(String userId, String cookie) {
        store.write(userId, cookie);
    }

    public void delete(String userId) {
        store.delete(userId);
    }

    public boolean exists(String userId) {
        return store.exists(userId);
    }

    public String updatedAt(String userId) {
        return store.updatedAt(userId);
    }
}
